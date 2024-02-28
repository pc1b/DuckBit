package dws.duckbit.controlers;

import dws.duckbit.Entities.*;
import dws.duckbit.responses.ResponseUser;
import dws.duckbit.services.ComboService;
import dws.duckbit.services.UserService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
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

import static org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentRequest;

@RestController
public class ApiControler {
	//@Autowired
	private static final Path IMAGES_FOLDER = Paths.get("src/main/resources/static/images/profile_images");
	private static final UserService userDB = new UserService();
	private static final ComboService combos = new ComboService();
	@GetMapping("/api/user/{id}")
	public ResponseEntity<ResponseUser> getUser(@PathVariable int id) {
		User u = userDB.getByID(id);
		if (u != null) {
			ResponseUser response = new ResponseUser(u.getID(), u.getUser(), u.getMail(), u.getCombos());
			return ResponseEntity.ok(response);
		} else {
			return ResponseEntity.notFound().build();
		}
	}
	@GetMapping("/api/combo/{id}")
	public ResponseEntity<Combo> getComboInfo(@PathVariable int id) {
		Combo c = combos.getByID(id);
		if (c != null) {
			return ResponseEntity.ok(c);
		} else {
			return ResponseEntity.notFound().build();
		}
	}
	@GetMapping("/api/download_combo/{id}")
	public ResponseEntity<String> getCombo(@PathVariable int id) {
		Combo c = combos.getByID(id);
		if (c != null) {
			return ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, "text/plain").body(c.getLeakedInfo());
		} else {
			return ResponseEntity.notFound().build();
		}
	}

	@GetMapping("/api/{id}/image")
	public ResponseEntity<Object> downloadImage(@PathVariable int id) throws MalformedURLException {
		Path imgPath = IMAGES_FOLDER.resolve(userDB.getByID(id) +".jpg");
		Resource file = new UrlResource(imgPath.toUri());

		if(!Files.exists(imgPath)) {
			return ResponseEntity.notFound().build();
		} else {
			return ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, "image/jpeg").body(file);
		}
	}

	@PostMapping("/api/{id}/upload_image")
	public ResponseEntity<Object> uploadImage(@PathVariable int id, @RequestParam MultipartFile image) throws IOException {

		User user = userDB.getByID(id);

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

	@DeleteMapping("api/{id}/delete_image")
	public ResponseEntity<Object> deleteImage(@PathVariable int id) throws IOException {

		User user = userDB.getByID(id);

		if(user != null) {

			Files.createDirectories(IMAGES_FOLDER);
			String nameFile = user.getUser() + ".jpg";
			Path imagePath = IMAGES_FOLDER.resolve(nameFile);
			File img = imagePath.toFile();
			if (img.exists()) {
				try{
					img.delete();
				}catch (Exception e){
					e.printStackTrace();
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
