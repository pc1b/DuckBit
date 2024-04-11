package dws.duckbit.controllers;

import dws.duckbit.entities.*;
import dws.duckbit.services.ComboService;
import dws.duckbit.services.LeakService;
import dws.duckbit.services.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.hibernate.engine.jdbc.BlobProxy;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Optional;

import static org.springframework.http.ResponseEntity.status;
import static org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentRequest;

@RestController
@RequestMapping("/api")
public class ApiController {

	// ---------- DEFAULT PATHS ---------- //
	private final Path LEAKS_FOLDER = Paths.get("src/main/resources/static/leaks");
	private final Path COMBO_FOLDER = Paths.get("src/main/resources/static/combo");

	// ---------- SERVICES ---------- //
	private final UserService userDB;
	private final ComboService comboDB;
	private final LeakService leaksDB;

	// ---------- BUILDER ---------- //
	public ApiController(UserService userDB, ComboService comboDB, LeakService leaksDB) {
		this.userDB = userDB;
		this.comboDB = comboDB;
		this.leaksDB = leaksDB;
	}

	// ---------- INDEX ---------- //

	@GetMapping({"/", ""})
	public ResponseEntity<Object> home()
	{
		return ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, "text/plain")
				.body("Welcome to Duckbit api");
	}

	// ---------- USER ---------- //

	//USER INFO
	@GetMapping(value = {"/user/{id}", "/user/{id}/"})
	public ResponseEntity<Object> getUser(@PathVariable Long id)
	{
		Optional<UserD> u = this.userDB.findByID(id);
		if (u.isPresent())
		{
			return ResponseEntity.ok(u);
		}
		else
		{
			return status(HttpStatus.BAD_REQUEST).build();
		}
	}

	//NUMBER OF USERS
	@GetMapping(value = {"/users/number", "/users/number/"})
	public ResponseEntity<Object> getNumberUsers()
	{
		return ResponseEntity.ok(this.userDB.getSize());
	}

	//USERS
	@GetMapping(value = {"/user", "/user/"})
	public ResponseEntity<Object> getRegisteredUsers()
	{
		return ResponseEntity.ok(this.userDB.findAll());
	}

	//USER BUY CREDITS
	@GetMapping(value = {"/{id}/credits", "/{id}/credits/"})
	public ResponseEntity<UserD> buyCredits(@PathVariable Long id)
	{
		Optional<UserD> u = this.userDB.findByID(id);
		if (u.isPresent())
		{
			this.userDB.addCreditsToUser(500, id);
			return ResponseEntity.ok(u.get());
		}
		else
		{
			return status(HttpStatus.BAD_REQUEST).build();
		}
	}

	// ---------- LEAKS ---------- //

	//GET ALL LEAKS
	@GetMapping({"/leak/", "/leak"})
	public ResponseEntity<Object> getLeaks()
	{
		return ResponseEntity.ok(this.leaksDB.findAll());
	}

	//GET A LEAK
	@GetMapping({"/leak/{id}/", "/leak/{id}"})
	public ResponseEntity<Object> getLeak(@PathVariable int id)
	{
		Optional<Leak> l = this.leaksDB.findByID(id);
		if (l.isPresent())
		{
			return ResponseEntity.ok(l.get());
		}
		else
		{
			return ResponseEntity.notFound().build();
		}
	}

	//CREATE A NEW LEAK
	@PostMapping({"/leak/", "/leak"})
	public ResponseEntity<Object> uploadLeak(@RequestParam String enterprise, @RequestParam String date,
											 @RequestParam MultipartFile leakInfo) throws IOException
	{
        String REGEX_PATTERN = "^[A-Za-z0-9.]{1,255}$";
        String filename = leakInfo.getOriginalFilename();
        if (filename == null || !(filename.matches(REGEX_PATTERN)))
        {
            filename = String.valueOf(this.leaksDB.getNextId()) + ".txt";
        }
		Leak l = this.leaksDB.createLeak(enterprise, date, filename);
		if (l != null)
		{
			Files.createDirectories(LEAKS_FOLDER);
			Path txtPath = LEAKS_FOLDER.resolve(filename);
			leakInfo.transferTo(txtPath);
			return status(HttpStatus.CREATED).body(l);
		}
		else
		{
			return ResponseEntity.notFound().build();
		}
	}

	//DELETE A LEAK
	@DeleteMapping({"/leak/{id}", "/leak/{id}/"})
	public ResponseEntity<Object> deleteLeak(@PathVariable int id) throws IOException
	{
		Optional<Leak> l = this.leaksDB.findByID(id);
		if (l.isPresent())
		{
			this.leaksDB.delete(l.get());
			this.comboDB.deleteLeak(l.get());
			Files.createDirectories(this.LEAKS_FOLDER);
			String nameFile = l.get().getId() + ".txt";
			Path leakPath = this.LEAKS_FOLDER.resolve(nameFile);
			File leak = leakPath.toFile();
			if (leak.exists())
			{
				try
				{
					if (!leak.delete())
					{
						return ResponseEntity.internalServerError().build();
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
					return ResponseEntity.internalServerError().build();
				}
			}
			return ResponseEntity.ok().build();
		}
		else
		{
			return ResponseEntity.notFound().build();
		}
	}

	// ---------- COMBOS ---------- //

	//DOWNLOAD A COMBO
	@GetMapping({"/combo/{id}/file/", "/combo/{id}/file"})
	public ResponseEntity<String> getComboDownload(@PathVariable int id)
	{
		Optional<Combo> c = this.comboDB.findById(id);
		if (c.isPresent())
		{
			return ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE,
					"text/plain").body(c.get().leakedInfo());
		}
		else
		{
			return ResponseEntity.notFound().build();
		}
	}
	//SOLD COMBOS
	@GetMapping(value = {"/combo/sold/number", "/combo/sold/number/"})
	public ResponseEntity<Object> getSoldCombos()
	{
		return ResponseEntity.ok(this.comboDB.getSoldCombos());
	}
	//GET A COMBO
	@GetMapping({"/combo/{id}/", "/combo/{id}"})
	public ResponseEntity<Object> getCombo(@PathVariable int id)
	{
		Optional<Combo> c = this.comboDB.findById(id);
		if (c.isPresent())
		{
			return ResponseEntity.ok(c.get());
		}
		else
		{
			return ResponseEntity.notFound().build();
		}
	}

	//CREATE NEW COMBO
	@PostMapping({"/combo", "/combo/"})
	public ResponseEntity<Object> createCombo(@RequestParam String name, @RequestParam ArrayList<Long> leaks,
											  @RequestParam int price, @RequestParam String description) throws IOException
	{
		Combo c = this.comboDB.createCombo(name, leaks, price, description);
		if (c == null)
		{
			return status(HttpStatus.BAD_REQUEST).build();
		}
		this.comboDB.save(c);
		return status(HttpStatus.CREATED).body(c);
	}

	//DELETE A COMBO
	@DeleteMapping({"/combo/{id}", "/combo/{id}/"})
	public ResponseEntity<Object> deleteCombo(@PathVariable int id) throws IOException
	{
		Optional<Combo> c = this.comboDB.findById(id);
		if (c.isPresent())
		{
			this.comboDB.delete(c.get().getId());
			Files.createDirectories(this.COMBO_FOLDER);
			String nameFile = c.get().getId() + ".txt";
			Path comboPath = this.COMBO_FOLDER.resolve(nameFile);
			File combo = comboPath.toFile();
			if (combo.exists())
			{
				try
				{
					if (!combo.delete())
					{
						return ResponseEntity.internalServerError().build();
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
					return ResponseEntity.internalServerError().build();
				}
			}
			return ResponseEntity.ok().build();
		}
		else
		{
			return ResponseEntity.notFound().build();
		}
	}

	//EDIT COMBO
	@PutMapping({"/combo/{id}", "/combo/{id}/"})
	public ResponseEntity<Object> EditCombo(@RequestParam String name, @RequestParam String price,
											@PathVariable int id, @RequestParam ArrayList<Integer> leaks, @RequestParam String description) throws IOException
	{
		Optional<Combo> c = comboDB.findById(id);
		ArrayList<Leak> leaksEdit = new ArrayList<>();
		if (c.isPresent())
		{
			if (this.leaksDB.getNextId() > 0)
			{
				for (int i : leaks)
				{
					Optional<Leak> leak = this.leaksDB.findByID(i);
					if (leak.isPresent())
					{
						leaksEdit.add(leak.get());
					}
					else
					{
						return status(HttpStatus.BAD_REQUEST).body("Leak " + i + " not found in the server");
					}
				}
				String nameFile = id + ".txt";
				Path comboPath = this.COMBO_FOLDER.resolve(nameFile);
				Resource comboF = new UrlResource(comboPath.toUri());
				if (comboF.exists())
				{
					Files.delete(comboPath);
				}
				c.get().editCombo(name, Integer.parseInt(price), leaksEdit, description);
			}
			return status(HttpStatus.CREATED).body(c);
		}
		return status(HttpStatus.NOT_FOUND).build();
	}

	// ---------- LOGIN AND REGISTER ---------- //
	//LOGIN
	@PostMapping({"/login/", "/login"})
	public ResponseEntity<Object> login(@RequestParam String username, @RequestParam String password,
										HttpServletResponse response){
		Long userID = this.userDB.getIDUser(username, password);
		Cookie cookie = new Cookie("id", String.valueOf(userID));
		if (userID >= 0)
		{
			response.addCookie(cookie);
			return ResponseEntity.ok().build();
		}
		else
		{
			return status(HttpStatus.BAD_REQUEST).body("Wrong username or password");
		}

	}

	//REGISTER
	@PostMapping({"/register","/register/"})
	public ResponseEntity<Object> Register(@RequestParam String username, @RequestParam String password,
										   @RequestParam String mail)
	{
		if (this.userDB.userExists(username))
		{
			return status(HttpStatus.BAD_REQUEST).body("Username already registered");
		}
		else
		{
			this.userDB.addUser(username, mail, password);
		}
		return status(HttpStatus.CREATED).body(this.userDB.findByID(this.userDB.getIDUser(username, password)));
	}

	// ---------- SHOP ---------- //
	//SHOP INDEX
	@GetMapping({"/combo", "/combo/"})
	public ResponseEntity<Collection<Combo>> getComboDB()
	{
		Collection<Combo> c = this.comboDB.getAvilableCombos();
		if (c != null)
		{
			return ResponseEntity.ok(c);
		}
		else
		{
			return ResponseEntity.notFound().build();
		}
	}

	//BUY COMBO
	@PostMapping({"/{id}/combo", "/{id}/combo/"})
	public ResponseEntity<Object> buyCombo(@RequestParam int combo, @PathVariable Long id)
	{
		Optional<Combo> c = this.comboDB.findById(combo);
		if (c.isEmpty() || this.userDB.findByID(id).isEmpty())
		{
			return ResponseEntity.notFound().build();
		}
		int comboPrice = this.comboDB.getComboPrice(combo);
		if (this.userDB.hasEnoughCredits(comboPrice, id))
		{
			this.userDB.substractCreditsToUser(comboPrice, id);
			Combo comboBought = c.get();
			this.userDB.addComboToUser(comboBought, id);
			comboBought.setUser(this.userDB.findByID(id).get());
			this.comboDB.save(comboBought);
			this.comboDB.updateSoldCombo();
			return ResponseEntity.ok(comboBought);
		}
		else
		{
			return status(HttpStatus.BAD_REQUEST).body("NOT ENOUGH CREDITS");
		}
	}

	// ---------- IMAGES MANIPULATION ---------- //
	//DOWNLOAD IMAGE

	@GetMapping({"/{id}/image", "/{id}/image/"})
	public ResponseEntity<Object> downloadImage(@PathVariable long id) throws SQLException {

		UserD u = this.userDB.findByID(id).orElseThrow();

		if (u.getImageFile() != null) {

			Resource file = new InputStreamResource(u.getImageFile().getBinaryStream());

			return ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, "image/jpeg")
					.contentLength(u.getImageFile().length()).body(file);

		} else {
			return ResponseEntity.notFound().build();
		}
	}

	//POST AN IMAGE
	@PostMapping({"/{id}/image", "/{id}/image/"})
	public ResponseEntity<Object> uploadImage(@PathVariable long id, @RequestParam MultipartFile imageFile)
			throws IOException {

		UserD user = this.userDB.findByID(id).orElseThrow();

		URI location = fromCurrentRequest().build().toUri();

		user.setImageFile(BlobProxy.generateProxy(imageFile.getInputStream(), imageFile.getSize()));
		this.userDB.save(user);

		return ResponseEntity.created(location).build();
	}

	//DELETE AN IMAGE
	@DeleteMapping(value = {"/{id}/image", "/{id}/image/"})
	public ResponseEntity<Object> deleteImage(@PathVariable long id) throws IOException {

		UserD u = this.userDB.findByID(id).orElseThrow();

		u.setImageFile(null);

		this.userDB.save(u);

		return ResponseEntity.noContent().build();
	}
}