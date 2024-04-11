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
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;


import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Blob;
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
			return status(HttpStatus.NOT_FOUND).build();
		}
	}

	//NUMBER OF USERS
	@GetMapping(value = {"/user/number", "/user/number/"})
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
	//USER Combos
	@GetMapping(value = {"/user/{id}/combos", "/user/{id}/combos/"})
	public ResponseEntity<Object> getCombosUser(@PathVariable Long id)
	{
		Optional<UserD> u = this.userDB.findByID(id);
		if (u.isPresent())
		{
			return ResponseEntity.ok(this.comboDB.findByUser(u.get()));
		}
		else
		{
			return status(HttpStatus.NOT_FOUND).build();
		}
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
			return status(HttpStatus.NOT_FOUND).build();
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
        String REGEX_PATTERN = "^[A-Za-z.]{1,255}$";
        String filename = leakInfo.getOriginalFilename();
		String REGEX_DATE_PATTERN = "^\\d{4}-\\d{2}-\\d{2}$";
		if (enterprise.length() > 255 || enterprise.isEmpty() || filename == null || !(filename.matches(REGEX_PATTERN)))
			return status(HttpStatus.BAD_REQUEST).body("Wrong enterprise name");
		if (!(date.matches(REGEX_DATE_PATTERN)) || Integer.parseInt(date.toString().split("-")[0]) > 9990)
			return status(HttpStatus.BAD_REQUEST).body("Wrong filename");
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
			//this.comboDB.deleteLeak(l.get());
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
	public ResponseEntity<String> getComboDownload(@PathVariable Long id)
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
	public ResponseEntity<Object> getCombo(@PathVariable Long id)
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
		if (name.length() > 255)
			return status(HttpStatus.BAD_REQUEST).body("The name of the combo is too large, it must be 255 characters or less :(");
		if (description.length() > 255)
			return status(HttpStatus.BAD_REQUEST).body("The description of the combo is too large, it must be 255 characters or less :(");
		if (price <= 0)
			return status(HttpStatus.BAD_REQUEST).body("The price of the combo is wrong :(");
		Combo c = this.comboDB.createCombo(name, leaks, price, description);
		if (c == null)
		{
			return status(HttpStatus.BAD_REQUEST).body("Some leaks were not found in the server");
		}
		this.comboDB.save(c);

		return getCombo(c.getId());
	}

	//DELETE A COMBO
	@DeleteMapping({"/combo/{id}", "/combo/{id}/"})
	public ResponseEntity<Object> deleteCombo(@PathVariable Long id) throws IOException
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
												@PathVariable Long id, @RequestParam ArrayList<Integer> leaks, @RequestParam String description) throws IOException
	{
		if (name.length() > 255)
			return status(HttpStatus.BAD_REQUEST).body("The name of the combo is too large, it must be 255 characters or less :(");
		if (description.length() > 255)
			return status(HttpStatus.BAD_REQUEST).body("The description of the combo is too large, it must be 255 characters or less :(");
		if (price.length() > 10 || Integer.parseInt(price) <= 0)
			return status(HttpStatus.BAD_REQUEST).body("The price of the combo is wrong :(");
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
				this.comboDB.editCombo(c.get(), name, Integer.parseInt(price), leaksEdit, description);
				this.comboDB.save(c.get());
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
		if (username.length() > 255)
			return status(HttpStatus.BAD_REQUEST).body("Username too long, the maximum is 255 characters");
		if (password.length() > 255)
			return status(HttpStatus.BAD_REQUEST).body("Password too long, the maximum is 255 characters");
		if (mail.length() > 255)
			return status(HttpStatus.BAD_REQUEST).body("Mail too long, the maximum is 255 characters");
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

	//QUERY
	@GetMapping({"/query", "/query/"})
	public ResponseEntity<Object> getMethodName(Model model, @RequestParam(defaultValue = "") String enterprise, @RequestParam(defaultValue = "-1") Integer price)
	{
		Collection<Combo> c = this.comboDB.findAll(enterprise, price);
		if (!c.isEmpty())
		{
			return ResponseEntity.ok(c);
		}
		return ResponseEntity.notFound().build();
	}
	//BUY COMBO
	@PostMapping({"/{id}/combo", "/{id}/combo/"})
	public ResponseEntity<Object> buyCombo(@RequestParam Long combo, @PathVariable Long id)
	{
		Optional<Combo> c = this.comboDB.findById(combo);
		if (c.isEmpty() || this.userDB.findByID(id).isEmpty())
		{
			return ResponseEntity.notFound().build();
		}
		if (!this.comboDB.getAvilableCombos().contains(c.get())){
			return ResponseEntity.badRequest().body("This combo is no more for sell");
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

		Optional<UserD> user = this.userDB.findByID(id);
		if (user.isEmpty()){
			return ResponseEntity.notFound().build();
		}
		UserD u = user.get();

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
	public ResponseEntity<Object> uploadImage(@PathVariable long id, @RequestParam MultipartFile imageFile) {

		Optional<UserD> u = this.userDB.findByID(id);
		if (u.isEmpty()){
			return ResponseEntity.notFound().build();
		}
		UserD user = u.get();
		URI location = fromCurrentRequest().build().toUri();
		Blob image;
		try {
			image = BlobProxy.generateProxy(imageFile.getInputStream(), imageFile.getSize());
		} catch (IOException e) {
			return ResponseEntity.badRequest().body("Not an image");
		}
		user.setImageFile(image);
		this.userDB.save(user);

		return ResponseEntity.created(location).build();
	}

	//DELETE AN IMAGE
	@DeleteMapping(value = {"/{id}/image", "/{id}/image/"})
	public ResponseEntity<Object> deleteImage(@PathVariable long id) throws IOException {

		Optional<UserD> user = this.userDB.findByID(id);
		if (user.isEmpty()){
			return ResponseEntity.notFound().build();
		}
		UserD u = user.get();

		u.setImageFile(null);

		this.userDB.save(u);

		return ResponseEntity.noContent().build();
	}
}