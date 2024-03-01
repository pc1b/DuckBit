package dws.duckbit.controlers;

import dws.duckbit.Entities.*;
import dws.duckbit.services.ComboService;
import dws.duckbit.services.LeakService;
import dws.duckbit.services.UserService;
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

	@GetMapping(value = {"/api/user/{id}", "/api/user/{id}/"})
	public ResponseEntity<User> getUser(@PathVariable int id) {
		User u = this.userDB.getByID(id);
		if (u != null) {
			return ResponseEntity.ok(u);
		} else {
			return null;
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
	public ResponseEntity<Object> uploadLeak(@RequestParam String enterprise, @RequestParam String date, @RequestParam MultipartFile leakInfo) throws IOException {
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
	@GetMapping(value = {"/api/combos", "/api/combos/"})
	public ResponseEntity<Collection<Combo>> getComboDB() {
		 Collection<Combo> c = this.comboDB.getAll();
		if (c != null) {
			return ResponseEntity.ok(c);
		} else {
			return ResponseEntity.notFound().build();
		}
	}
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
