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

import static org.springframework.http.ResponseEntity.status;
import static org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentRequest;

@RestController
public class ApiControler {

	private Path IMAGES_FOLDER = Paths.get("src/main/resources/static/images/profile_images");
	private final Path LEAKS_FOLDER = Paths.get("src/main/resources/static/leaks");

	private final UserService userDB;
	private final ComboService comboDB;
	private final LeakService leaksDB;

	public ApiControler(UserService userDB, ComboService comboDB, LeakService leaksDB) {
		this.userDB = userDB;
		this.comboDB = comboDB;
		this.leaksDB = leaksDB;
	}


	@GetMapping(value = {"/api", "/api/"})
	public ResponseEntity<Object> home() {

		return ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, "text/plain").body("Wellcome to Duckbit api");

	}

	@GetMapping(value = {"/api/user/", "/api/user"})
	public ResponseEntity<User> getUser(@CookieValue(value = "id", defaultValue = "-1") String id) {
		User u = this.userDB.getByID(Integer.parseInt(id));
		if (u != null) {
			return ResponseEntity.ok(u);
		} else {
			return status(HttpStatus.UNAUTHORIZED).build();
		}
	}

	//LEAKS MAPPING
	@GetMapping(value = {"/api/leak/{id}/", "/api/leak/{id}"})
	public ResponseEntity<Object> getLeak(@PathVariable int id) throws IOException {
		Leak l = this.leaksDB.getByID(id);
		if (l != null) {
			return ResponseEntity.ok(l);
		} else {
			return ResponseEntity.notFound().build();
		}
	}

	@GetMapping(value = {"/api/leaks", "/api/leaks"})
	public ResponseEntity<Object> getLeaks() {
		return ResponseEntity.ok(this.leaksDB.getAll());
	}

	@PostMapping(value = {"/api/upload_leak", "/api/upload_leak/"})
	public ResponseEntity<Object> uploadLeak(@RequestParam String enterprise, @RequestParam String date, @RequestParam MultipartFile leakInfo, @CookieValue(value = "id", defaultValue = "-1") String id) throws IOException {
		if (Integer.parseInt(id) != 0)
			return status(HttpStatus.UNAUTHORIZED).build();
		Leak l = this.leaksDB.createLeak(enterprise, date);
		if (l != null) {
			this.leaksDB.addLeak(l);
			URI location = fromCurrentRequest().build().toUri();
			Files.createDirectories(LEAKS_FOLDER);
			String nameFile = l.getId() + ".txt";
			Path txtPath = LEAKS_FOLDER.resolve(nameFile);
			leakInfo.transferTo(txtPath);
			return status(HttpStatus.CREATED).body(l);
			//return ResponseEntity.ok(l);
		} else {
			return ResponseEntity.notFound().build();
		}
	}

	@DeleteMapping(value = {"/api/delete_leak/{id}", "/api/delete_leak/{id}/"})
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

	@GetMapping(value = {"/api/download_combo/{id}", "/api/download_combo/{id}/"})
	public ResponseEntity<String> getCombo(@PathVariable int id) {
		Combo c = this.comboDB.getByID(id);
		if (c != null) {
			return ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, "text/plain").body(c.leakedInfo());
		} else {
			return ResponseEntity.notFound().build();
		}
	}

	@PostMapping(value = {"/api/create_combo", "/api/create_combo/"})
	public ResponseEntity<Object> createCombo(@RequestParam String name, @RequestParam ArrayList<Integer> leaks, @RequestParam int price) throws IOException {
		Combo c = this.comboDB.createCombo(name, leaks, price);
		if (c == null){
			return status(HttpStatus.BAD_REQUEST).build();
		}
		this.comboDB.addCombo(c);
		//URI location = fromCurrentRequest().build().toUri();
		return status(HttpStatus.CREATED).body(c);
	}

	//SESION MAPPING
	@PostMapping(value = {"/api/login/", "/api/login"})
	public ResponseEntity<Object> login(@RequestParam String username, @RequestParam String password, HttpServletResponse response){
		int userID = this.userDB.getIDUser(username, password);
		Cookie cookie = new Cookie("id", String.valueOf(userID));
		if (userID == 0)
		{
			response.addCookie(cookie);
			return ResponseEntity.ok().build();
		}
		else if (userID > 0)
		{
			response.addCookie(cookie);
			return ResponseEntity.ok().build();
		}
		else
		{
			return status(HttpStatus.BAD_REQUEST).build();
		}

	}

	@PostMapping(value = {"/api/register","/api/register/"})
	public ResponseEntity<Object> Register(@RequestParam String username, @RequestParam String password, @RequestParam String mail)
	{
		if (this.userDB.userExists(username))
		{
			return status(HttpStatus.BAD_REQUEST).body("Username allready registered");
		}
		else
		{
			this.userDB.addUser(username, mail, password);
		}
		return status(HttpStatus.CREATED).build();
	}

	//SHOP MAPPING
	@GetMapping(value = {"/api/shop", "/api/shop/"})
	public ResponseEntity<Collection<Combo>> getComboDB() {
		Collection<Combo> c = this.comboDB.getAll();
		if (c != null) {
			return ResponseEntity.ok(c);
		} else {
			return ResponseEntity.notFound().build();
		}
	}

	@PostMapping(value = {"/buy_combo/{id}", "/buy_combo/{id}/"})
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
			return status(HttpStatus.BAD_REQUEST).body("NOT ENAUGH CREDITS");
		}
	}

	//IMAGE MAPPING
	@GetMapping(value = {"/api/{id}/image", "/api/{id}/image/"})
	public ResponseEntity<Object> downloadImage(@PathVariable int id) throws MalformedURLException {
		Path imgPath = IMAGES_FOLDER.resolve(this.userDB.getByID(id).getUser() +".jpg");
		Resource file = new UrlResource(imgPath.toUri());

		if(!Files.exists(imgPath)) {
			return ResponseEntity.notFound().build();
		} else {
			return ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, "image/jpeg").body(file);
		}
	}

	@PostMapping(value = {"/api/{id}/upload_image", "/api/{id}/upload_image/"})
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

	@DeleteMapping(value = {"api/{id}/delete_image", "api/{id}/delete_image/"})
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


//	@PostMapping("/api/login")
//	public ResponseEntity<Map<>> Login(@RequestBody String user, @RequestBody String pass, RedirectAttributes attributes, HttpServletResponse response)
//	{
//		int userID = userDB.getIDUser(user, pass);
//		OAuth2ResourceServerProperties.Jwt jwt = new OAuth2ResourceServerProperties.Jwt();
//		Cookie cookie = new Cookie("id", String.valueOf(userID));
//		if (userID == 0)
//		{
//			response.addCookie(cookie);
//			return new RedirectView("admin");
//		}
//		else if (userID > 0)
//		{
//			response.addCookie(cookie);
//			return new RedirectView("user");
//		}
//		else
//		{
//			return new RedirectView("login");
//		}
//	}
}
