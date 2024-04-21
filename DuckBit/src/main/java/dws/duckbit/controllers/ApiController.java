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
import java.util.Optional;

import static org.springframework.http.ResponseEntity.status;
import static org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentRequest;


@RestController
@RequestMapping("/api")
public class ApiController
{

	// ---------- DEFAULT PATHS ---------- //
	private final Path LEAKS_FOLDER = Paths.get("files/leaks");
	private final Path COMBO_FOLDER = Paths.get("files/combo");

	// ---------- SERVICES ---------- //
	private final UserService userService;
	private final ComboService comboService;
	private final LeakService leaksDB;

	// ---------- BUILDER ---------- //
	public ApiController(UserService userService, ComboService comboService, LeakService leaksDB)
	{
		this.userService = userService;
		this.comboService = comboService;
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
		Optional<UserD> u = this.userService.findByID(id);
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
		return ResponseEntity.ok(this.userService.getSize());
	}

	//USERS
	@GetMapping(value = {"/user", "/user/"})
	public ResponseEntity<Object> getRegisteredUsers()
	{
		return ResponseEntity.ok(this.userService.findAll());
	}

	//USER Combos
	@GetMapping(value = {"/user/{id}/combos", "/user/{id}/combos/"})
	public ResponseEntity<Object> getCombosUser(@PathVariable Long id)
	{
		Optional<UserD> u = this.userService.findByID(id);
		if (u.isPresent())
		{
			return ResponseEntity.ok(this.comboService.findByUser(u.get()));
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
		Optional<UserD> u = this.userService.findByID(id);
		if (u.isPresent())
		{
			this.userService.addCreditsToUser(500, id);
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
		int upload = leaksDB.upload(leakInfo, enterprise, this.leaksDB, date);
		String filename = leakInfo.getOriginalFilename();
		if (upload == 1)
			return status(HttpStatus.BAD_REQUEST).body("Wrong enterprise name");
		if(upload == 2)
			return status(HttpStatus.BAD_REQUEST).body("Wrong filename");
		if (upload == 3)
			return status(HttpStatus.BAD_REQUEST).body("Wrong date");
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

	//COMBOS IN LEAKS
	@GetMapping({"/leak/{id}/combos", "/leak/{id}/combos"})
	public ResponseEntity<Object> getCombosInLeak(@PathVariable int id)
	{
		Optional<Leak> l = this.leaksDB.findByID(id);
		if (l.isPresent())
		{
			return ResponseEntity.ok(l.get().getCombos());
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
		Optional<Combo> c = this.comboService.findById(id);
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
		return ResponseEntity.ok(this.comboService.getSoldCombos());
	}

	//GET A COMBO
	@GetMapping({"/combo/{id}/", "/combo/{id}"})
	public ResponseEntity<Object> getCombo(@PathVariable Long id)
	{
		Optional<Combo> c = this.comboService.findById(id);
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
		int check = comboService.checkCreateCombo(name, description, price);
		if (check == 1)
			return status(HttpStatus.BAD_REQUEST).body("The name of the combo is too large, it must be 255 characters or less :(");
		if (check == 2)
			return status(HttpStatus.BAD_REQUEST).body("The description of the combo is too large, it must be 255 characters or less :(");
		if (check == 3)
			return status(HttpStatus.BAD_REQUEST).body("The price of the combo is wrong :(");
		Combo c = this.comboService.createCombo(name, leaks, price, description);
		if (c == null)
		{
			return status(HttpStatus.BAD_REQUEST).body("Some leaks were not found in the server");
		}
		this.comboService.save(c);
		Combo ret = new Combo(name, price, description);
		ArrayList<Leak> ls = new ArrayList<>();
		for (Long l : leaks){
			ls.add(this.leaksDB.findByID(l).get());
		}
		ret.setLeaks(ls);
		return ResponseEntity.ok(ret);
	}

	//DELETE A COMBO
	@DeleteMapping({"/combo/{id}", "/combo/{id}/"})
	public ResponseEntity<Object> deleteCombo(@PathVariable Long id) throws IOException
	{
		Optional<Combo> c = this.comboService.findById(id);
		if (c.isPresent())
		{
			this.comboService.delete(c.get().getId());
			return ResponseEntity.ok().build();
		}
		else
		{
			return ResponseEntity.notFound().build();
		}
	}

	//EDIT COMBO
	@PutMapping({"/combo/{id}", "/combo/{id}/"})
	public ResponseEntity<Object> EditCombo(@RequestParam String name, @RequestParam int price,
												@PathVariable Long id, @RequestParam ArrayList<Integer> leaks, @RequestParam String description) throws IOException
	{
		int check = comboService.checkEditCombo(name, description, price, id, this.comboService, this.leaksDB, leaks);
		if (check == 1)
			return status(HttpStatus.BAD_REQUEST).body("The name of the combo is too large, it must be 255 characters or less :(");
		if (check == 2)
			return status(HttpStatus.BAD_REQUEST).body("The description of the combo is too large, it must be 255 characters or less :(");
		if (check == 3)
			return status(HttpStatus.BAD_REQUEST).body("The price of the combo is wrong :(");
		if (check == 4)
			return status(HttpStatus.BAD_REQUEST).body("One of the leaks has not been found in the server");
		if (check == 5) {
			Optional<Combo> c = comboService.findById(id);
			return status(HttpStatus.CREATED).body(c);
		}
		return status(HttpStatus.NOT_FOUND).build();
	}

	// ---------- LOGIN AND REGISTER ---------- //

	//LOGIN
	@PostMapping({"/login/", "/login"})
	public ResponseEntity<Object> login(@RequestParam String username, @RequestParam String password,
										HttpServletResponse response)
	{
		Long userID = this.userService.getIDUser(username, password);
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
		if (this.userService.userExists(username))
			return status(HttpStatus.BAD_REQUEST).body("Username already registered");
		else
			this.userService.addUser(username, mail, password);
		return status(HttpStatus.CREATED).body(this.userService.findByID(this.userService.getIDUser(username, password)));
	}

	//DELETE USERS
	@DeleteMapping({"/user/{id}", "/user/{id}/"})
	public ResponseEntity<Object> deleteUser(@PathVariable Long id) throws IOException {
		if (this.comboService.deleteUser(id)){
			return ResponseEntity.ok().build();
		}
		return ResponseEntity.notFound().build();
	}

	// ---------- SHOP ---------- //
	//SHOP INDEX
	@GetMapping({"/combo", "/combo/"})
	public ResponseEntity<Collection<Combo>> getcomboService()
	{
		Collection<Combo> c = this.comboService.getAvilableCombos();
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
		Collection<Combo> c = this.comboService.findAll(enterprise, price);
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
		Optional<Combo> c = this.comboService.findById(combo);
		if (c.isEmpty() || this.userService.findByID(id).isEmpty())
		{
			return ResponseEntity.notFound().build();
		}
		if (!this.comboService.getAvilableCombos().contains(c.get()))
		{
			return ResponseEntity.badRequest().body("This combo is no more for sell");
		}
		int comboPrice = this.comboService.getComboPrice(combo);
		if (this.userService.hasEnoughCredits(comboPrice, id))
		{
			this.userService.substractCreditsToUser(comboPrice, id);
			Combo comboBought = c.get();
			this.userService.addComboToUser(comboBought, id);
			comboBought.setUser(this.userService.findByID(id).get());
			this.comboService.save(comboBought);
			this.comboService.updateSoldCombo();
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
	public ResponseEntity<Object> downloadImage(@PathVariable long id) throws SQLException
	{
		Optional<UserD> user = this.userService.findByID(id);
		if (user.isEmpty())
		{
			return ResponseEntity.notFound().build();
		}
		UserD u = user.get();
		if (u.getImageFile() != null)
		{

			Resource file = new InputStreamResource(u.getImageFile().getBinaryStream());
			return ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, "image/jpeg")
					.contentLength(u.getImageFile().length()).body(file);
		}
		else
		{
			return ResponseEntity.notFound().build();
		}
	}

	//POST AN IMAGE
	@PostMapping({"/{id}/image", "/{id}/image/"})
	public ResponseEntity<Object> uploadImage(@PathVariable long id, @RequestParam MultipartFile imageFile)
	{

		Optional<UserD> u = this.userService.findByID(id);
		if (u.isEmpty())
		{
			return ResponseEntity.notFound().build();
		}
		UserD user = u.get();
		URI location = fromCurrentRequest().build().toUri();
		Blob image;
		try
		{
			image = BlobProxy.generateProxy(imageFile.getInputStream(), imageFile.getSize());
		}
		catch (IOException e)
		{
			return ResponseEntity.badRequest().body("Not an image");
		}
		user.setImageFile(image);
		this.userService.save(user);
		return ResponseEntity.created(location).build();
	}

	//DELETE AN IMAGE
	@DeleteMapping(value = {"/{id}/image", "/{id}/image/"})
	public ResponseEntity<Object> deleteImage(@PathVariable long id) throws IOException
	{

		Optional<UserD> user = this.userService.findByID(id);
		if (user.isEmpty())
		{
			return ResponseEntity.notFound().build();
		}
		UserD u = user.get();
		u.setImageFile(null);
		this.userService.save(u);
		return ResponseEntity.noContent().build();
	}
}