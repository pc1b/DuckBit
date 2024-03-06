package dws.duckbit.controlers;

import dws.duckbit.Entities.*;
import dws.duckbit.services.ComboService;
import dws.duckbit.services.LeakService;
import dws.duckbit.services.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.web.servlet.server.Session;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;


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

import static org.springframework.http.ResponseEntity.status;
import static org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentRequest;

@RestController
@RequestMapping("/api")
public class ApiControler {

	private final Path IMAGES_FOLDER = Paths.get("src/main/resources/static/images/profile_images");
	private final Path LEAKS_FOLDER = Paths.get("src/main/resources/static/leaks");
	private final Path COMBO_FOLDER = Paths.get("src/main/resources/static/combo");

	private final UserService userDB;
	private final ComboService comboDB;
	private final LeakService leaksDB;

	public ApiControler(UserService userDB, ComboService comboDB, LeakService leaksDB) {
		this.userDB = userDB;
		this.comboDB = comboDB;
		this.leaksDB = leaksDB;
	}


	@GetMapping({"/", ""})
	public ResponseEntity<Object> home() {

		return ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, "text/plain").body("Welcome to Duckbit api");

	}

	@GetMapping(value = {"/user/{id}", "/user/{id}/"})
	public ResponseEntity<Object> getUser(@PathVariable int id) {
		User u = this.userDB.getByID(id);
		if (u != null) {
			if (id == 0){
				HashMap<String,Object> response = new HashMap<>();
				response.put("User", this.userDB.getByID(id).getUser());
				response.put("Mail", this.userDB.getByID(id).getMail());
				response.put("Registered users", this.userDB.getSize());
				response.put("Uploaded combos", this.comboDB.getComboSize());
				response.put("Sold combos", this.comboDB.getSoldCombos());
				response.put("Leaks", this.leaksDB.getAll());
				response.put("Combos", this.comboDB.getAll());
				return ResponseEntity.ok(response);
			}
			return ResponseEntity.ok(u);
		} else {
			return status(HttpStatus.BAD_REQUEST).build();
		}
	}

	@GetMapping(value = {"/{id}/credits", "/{id}/credits/"})
	public ResponseEntity<User> buyCredits(@PathVariable int id) {
		User u = this.userDB.getByID(id);
		if (u != null) {
			this.userDB.addCreditsToUser(500, id);
			return ResponseEntity.ok(u);
		} else {
			return status(HttpStatus.BAD_REQUEST).build();
		}
	}

	//LEAKS MAPPING
	/*@GetMapping({"/leak/{id}/", "/leak/{id}"})
	public ResponseEntity<Object> getLeak(@PathVariable int id) throws IOException {
		Leak l = this.leaksDB.getByID(id);
		if (l != null) {
			return ResponseEntity.ok(l);
		} else {
			return ResponseEntity.notFound().build();
		}
	}*/

	@GetMapping({"/leaks/", "/leaks"})
	public ResponseEntity<Object> getLeaks() {
		return ResponseEntity.ok(this.leaksDB.getAll());
	}

	@PostMapping({"/leak/", "/leak"})
	public ResponseEntity<Object> uploadLeak(@RequestParam String enterprise, @RequestParam String date, @RequestParam MultipartFile leakInfo) throws IOException {
		Leak l = this.leaksDB.createLeak(enterprise, date);
		if (l != null) {
			this.leaksDB.addLeak(l);
			Files.createDirectories(LEAKS_FOLDER);
			String nameFile = l.getId() + ".txt";
			Path txtPath = LEAKS_FOLDER.resolve(nameFile);
			leakInfo.transferTo(txtPath);
			return status(HttpStatus.CREATED).body(l);
		} else {
			return ResponseEntity.notFound().build();
		}
	}

	@DeleteMapping(value = {"/leak/{id}", "/leak/{id}/"})
	public ResponseEntity<Object> deleteLeak(@PathVariable int id) throws IOException {
		Leak l = this.leaksDB.getByID(id);
		if (l != null) {
			this.leaksDB.deleteLeak(l);
			this.comboDB.deleteLeak(l);
			Files.createDirectories(this.LEAKS_FOLDER);
			String nameFile = l.getId() + ".txt";
			Path leakPath = this.LEAKS_FOLDER.resolve(nameFile);
			File leak = leakPath.toFile();
			if (leak.exists()) {
				try{
					if (!leak.delete()){
						return ResponseEntity.internalServerError().build();
					}
				}catch (Exception e){
					e.printStackTrace();
					return ResponseEntity.internalServerError().build();
				}
			}
			return ResponseEntity.ok().build();
		} else {
			return ResponseEntity.notFound().build();
		}
	}

	//COMBO MAPPING
/*
	@GetMapping(value = {"/api/combo/{id}", "/api/combo/{id}/"})
	public ResponseEntity<Combo> getComboInfo(@PathVariable int id) {
		Combo c = this.comboDB.getByID(id);
		if (c != null) {
			return ResponseEntity.ok(c);
		} else {
			return ResponseEntity.notFound().build();
		}
	}*/

	@GetMapping(value = {"/combo/{id}", "/combo/{id}/"})
	public ResponseEntity<String> getCombo(@PathVariable int id) {
		Combo c = this.comboDB.getByID(id);
		if (c != null) {
			return ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, "text/plain").body(c.leakedInfo());
		} else {
			return ResponseEntity.notFound().build();
		}
	}

	@PostMapping(value = {"/combo", "/combo/"})
	public ResponseEntity<Object> createCombo(@RequestParam String name, @RequestParam ArrayList<Integer> leaks, @RequestParam int price) throws IOException {
		Combo c = this.comboDB.createCombo(name, leaks, price);
		if (c == null){
			return status(HttpStatus.BAD_REQUEST).build();
		}
		this.comboDB.addCombo(c);
		return status(HttpStatus.CREATED).body(c);
	}

	@DeleteMapping(value = {"/combo/{id}", "/combo/{id}/"})
	public ResponseEntity<Object> deleteCombo(@PathVariable int id) throws IOException {
		Combo c = this.comboDB.getByID(id);
		if (c != null) {
			this.comboDB.deleteCombo(c);
			Files.createDirectories(this.COMBO_FOLDER);
			String nameFile = c.getId() + ".txt";
			Path comboPath = this.COMBO_FOLDER.resolve(nameFile);
			File combo = comboPath.toFile();
			if (combo.exists()) {
				try{
					if (!combo.delete()){
						return ResponseEntity.internalServerError().build();
					}
				}catch (Exception e){
					e.printStackTrace();
					return ResponseEntity.internalServerError().build();
				}
			}
			return ResponseEntity.ok().build();
		} else {
			return ResponseEntity.notFound().build();
		}
	}

	//SESION MAPPING
	@PostMapping(value = {"/login/", "/login"})
	public ResponseEntity<Object> login(@RequestParam String username, @RequestParam String password, HttpServletResponse response){
		int userID = this.userDB.getIDUser(username, password);
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

	@PostMapping(value = {"/register","/register/"})
	public ResponseEntity<Object> Register(@RequestParam String username, @RequestParam String password, @RequestParam String mail)
	{
		if (this.userDB.userExists(username))
		{
			return status(HttpStatus.BAD_REQUEST).body("Username already registered");
		}
		else
		{
			this.userDB.addUser(username, mail, password);
		}
		return status(HttpStatus.CREATED).body(this.userDB.getByID(this.userDB.getIDUser(username, password)));
	}

	//SHOP MAPPING
	@GetMapping(value = {"/shop", "/shop/"})
	public ResponseEntity<Collection<Combo>> getComboDB() {
		Collection<Combo> c = this.comboDB.getAll();
		if (c != null) {
			return ResponseEntity.ok(c);
		} else {
			return ResponseEntity.notFound().build();
		}
	}

	@PostMapping(value = {"/{id}/buy_combo", "/{id}/buy_combo/"})
	public ResponseEntity<Object> BuyCombo(@RequestParam int combo, @PathVariable int id)
	{
		// We must check if a combo exists
		if (this.comboDB.getByID(combo) == null){
			return ResponseEntity.notFound().build();
		}
		int comboPrice = this.comboDB.getComboPrice(combo);
		if (this.userDB.hasEnoughCredits(comboPrice, id))
		{
			this.userDB.substractCreditsToUser(comboPrice, id);
			Combo comboBuyed = this.comboDB.getByID(combo);
			this.comboDB.removeByID(combo);
			this.userDB.addComboToUser(comboBuyed, id);
			this.comboDB.updateSoldCombo();
			return ResponseEntity.ok(comboBuyed);
		}
		else {
			return status(HttpStatus.BAD_REQUEST).body("NOT ENOUGH CREDITS");
		}
	}



	//IMAGE MAPPING
	@GetMapping(value = {"/{id}/image", "/{id}/image/"})
	public ResponseEntity<Object> downloadImage(@PathVariable int id) throws MalformedURLException {
		Path imgPath = IMAGES_FOLDER.resolve(this.userDB.getByID(id).getUser() +".jpg");
		Resource file = new UrlResource(imgPath.toUri());

		if(!Files.exists(imgPath)) {
			return ResponseEntity.notFound().build();
		} else {
			return ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, "image/jpeg").body(file);
		}
	}

	@PostMapping(value = {"/{id}/image", "/{id}/image/"})
	public ResponseEntity<Object> uploadImage(@PathVariable int id, @RequestParam MultipartFile image) throws IOException {

		User user = this.userDB.getByID(id);

		if (user != null) {
			URI location = fromCurrentRequest().build().toUri();
			Files.createDirectories(IMAGES_FOLDER);
			String nameFile = user.getUser() + ".jpg";
			Path imagePath = IMAGES_FOLDER.resolve(nameFile);
			image.transferTo(imagePath);
			return ResponseEntity.created(location).build();

		} else {
			return ResponseEntity.notFound().build();
		}
	}

	@DeleteMapping(value = {"/{id}/image", "/{id}/image/"})
	public ResponseEntity<Object> deleteImage(@PathVariable int id) throws IOException {

		User user = this.userDB.getByID(id);

		if(user != null) {

			Files.createDirectories(IMAGES_FOLDER);
			String nameFile = user.getUser() + ".jpg";
			Path imagePath = IMAGES_FOLDER.resolve(nameFile);
			File img = imagePath.toFile();
			if (img.exists()) {
				try{
					if (!img.delete()){
						status(HttpStatus.INTERNAL_SERVER_ERROR).build();
					}
				}catch (Exception e){
					e.printStackTrace();
					status(HttpStatus.INTERNAL_SERVER_ERROR).build();
				}
			}
			return ResponseEntity.noContent().build();

		} else {
			return ResponseEntity.notFound().build();
		}
	}
}
