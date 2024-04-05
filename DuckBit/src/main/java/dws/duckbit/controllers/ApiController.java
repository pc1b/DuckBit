package dws.duckbit.controllers;

import dws.duckbit.entities.*;
import dws.duckbit.services.ComboService;
import dws.duckbit.services.LeakService;
import dws.duckbit.services.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
	private final Path IMAGES_FOLDER = Paths.get("src/main/resources/static/images/profile_images");
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
		User u = this.userDB.findByID(id);
		if (u != null)
		{
			if (id == 0)
			{
				HashMap<String,Object> response = new HashMap<>();
				response.put("User", this.userDB.findByID(id).getUser());
				response.put("Mail", this.userDB.findByID(id).getMail());
				response.put("Registered users", this.userDB.getSize());
				response.put("Uploaded combos", this.comboDB.getComboSize());
				response.put("Sold combos", this.comboDB.getSoldCombos());
				response.put("Leaks", this.leaksDB.findAll());
				response.put("Combos", this.comboDB.findAll());
				return ResponseEntity.ok(response);
			}
			return ResponseEntity.ok(u);
		}
		else
		{
			return status(HttpStatus.BAD_REQUEST).build();
		}
	}

	//USER BUY CREDITS
	@GetMapping(value = {"/{id}/credits", "/{id}/credits/"})
	public ResponseEntity<User> buyCredits(@PathVariable Long id)
	{
		User u = this.userDB.findByID(id);
		if (u != null)
		{
			this.userDB.addCreditsToUser(500, id);
			return ResponseEntity.ok(u);
		}
		else
		{
			return status(HttpStatus.BAD_REQUEST).build();
		}
	}

	// ---------- LEAKS ---------- //
	//GET ALL LEAKS
	@GetMapping({"/leaks/", "/leaks"})
	public ResponseEntity<Object> getLeaks()
	{
		return ResponseEntity.ok(this.leaksDB.findAll());
	}

	//CREATE A NEW LEAK
	@PostMapping({"/leak/", "/leak"})
	public ResponseEntity<Object> uploadLeak(@RequestParam String enterprise, @RequestParam String date,
											 @RequestParam MultipartFile leakInfo) throws IOException
	{
		Leak l = this.leaksDB.createLeak(enterprise, date);
		if (l != null)
		{
			this.leaksDB.save(l);
			Files.createDirectories(LEAKS_FOLDER);
			String nameFile = l.getId() + ".txt";
			Path txtPath = LEAKS_FOLDER.resolve(nameFile);
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
		Leak l = this.leaksDB.findByID(id);
		if (l != null)
		{
			this.leaksDB.delete(l);
			this.comboDB.deleteLeak(l);
			Files.createDirectories(this.LEAKS_FOLDER);
			String nameFile = l.getId() + ".txt";
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
	@GetMapping({"/combo/{id}", "/combo/{id}/"})
	public ResponseEntity<String> getCombo(@PathVariable int id)
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
					Leak leak = this.leaksDB.findByID(i);
					if (leak != null)
					{
						leaksEdit.add(leak);
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
	@GetMapping({"/shop", "/shop/"})
	public ResponseEntity<Collection<Combo>> getComboDB()
	{
		Collection<Combo> c = this.comboDB.findAll();
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
		if (c.isEmpty())
		{
			return ResponseEntity.notFound().build();
		}
		int comboPrice = this.comboDB.getComboPrice(combo);
		if (this.userDB.hasEnoughCredits(comboPrice, id))
		{
			this.userDB.substractCreditsToUser(comboPrice, id);
			Combo comboBought = c.get();
			this.comboDB.delete(combo);
			this.userDB.addComboToUser(comboBought, id);
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
	public ResponseEntity<Object> downloadImage(@PathVariable Long id) throws MalformedURLException
	{
		User u = this.userDB.findByID(id);
		if (u == null)
		{
			return ResponseEntity.notFound().build();
		}
		Path imgPath = IMAGES_FOLDER.resolve(u.getUser() +".jpg");
		Resource file = new UrlResource(imgPath.toUri());
		if(!Files.exists(imgPath))
		{
			imgPath = IMAGES_FOLDER.resolve( "../admin.jpg");
			file = new UrlResource(imgPath.toUri());
		}
		return ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, "image/jpeg").body(file);

	}

	//POST AN IMAGE
	@PostMapping({"/{id}/image", "/{id}/image/"})
	public ResponseEntity<Object> uploadImage(@PathVariable Long id, @RequestParam MultipartFile image)
			throws IOException
	{
		User user = this.userDB.findByID(id);
		if (user != null)
		{
			URI location = fromCurrentRequest().build().toUri();
			Files.createDirectories(IMAGES_FOLDER);
			String nameFile = user.getUser() + ".jpg";
			Path imagePath = IMAGES_FOLDER.resolve(nameFile);
			image.transferTo(imagePath);
			return ResponseEntity.created(location).build();

		}
		else
		{
			return ResponseEntity.notFound().build();
		}
	}

	//DELETE AN IMAGE
	@DeleteMapping(value = {"/{id}/image", "/{id}/image/"})
	public ResponseEntity<Object> deleteImage(@PathVariable Long id) throws IOException
	{
		User user = this.userDB.findByID(id);
		if(user != null)
		{
			Files.createDirectories(IMAGES_FOLDER);
			String nameFile = user.getUser() + ".jpg";
			Path imagePath = IMAGES_FOLDER.resolve(nameFile);
			File img = imagePath.toFile();
			if (img.exists())
			{
				try
				{
					if (!img.delete())
					{
						status(HttpStatus.INTERNAL_SERVER_ERROR).build();
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
					status(HttpStatus.INTERNAL_SERVER_ERROR).build();
				}
			}
			return ResponseEntity.noContent().build();
		}
		else
		{
			return ResponseEntity.notFound().build();
		}
	}
}