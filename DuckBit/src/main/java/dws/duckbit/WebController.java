package dws.duckbit;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.annotation.JsonCreator.Mode;

import dws.duckbit.Entities.AlmacenUsuarios;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.core.io.UrlResource;

import java.net.MalformedURLException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
public class WebController {
    private static final Path IMAGES_FOLDER = Paths.get("src/main/resources/static/images/profile_images");
    private AlmacenUsuarios userDB;

    // INDEX

    @GetMapping("/")
    public String index()
    {
        return "index";
    }

    // USERS TYPES

    @GetMapping("/admin")
    public String aboutUs()
    {
        return "admin";
    }

    @GetMapping("/user")
    public String User(Model model)
    {
        model.addAttribute("Name", "Paco");
        return "user";
    }

    // LOGIN AND REGISTER

    @GetMapping("/login")
    public String Login()
    {
        return "login";
    }

    @GetMapping("/register")
    public String Register()
    {
        return "register";
    }

    @PostMapping("/login")
    public String Login(@RequestParam String user, @RequestParam String pass, Model model)
    {   
        int userID = this.userDB.getIDUser(user, pass);
        if (userID == 0)
        {
            return "admin";
        }
        else if (userID > 0)
        {
            model.addAttribute("username", user);
            return "user";
        }
        else
        {
            return "login";
        }
    }

    @PostMapping("/register")
    public String Register(@RequestParam String user, @RequestParam String pass, @RequestParam String mail)
    {
        this.userDB.addUser(user, pass, mail);
        return "login";
    }

    // THE SHOP

    @GetMapping("/user/shop")
    public String shop_user(Model model)
    {
        model.addAttribute("Name", "Paco");
        return "shop_user";
    }

    @GetMapping("/admin/shop")
    public String shop_admin()
    {
        return "shop_admin";
    }

    @PostMapping("/upload_image")
    public String uploadImage(@RequestParam String username, @RequestParam MultipartFile image, Model model) throws IOException {
        Files.createDirectories(IMAGES_FOLDER);
        String nameFile = username + ".jpg";
        Path imagePath = IMAGES_FOLDER.resolve(nameFile);
        image.transferTo(imagePath);
        return "admin";
    }

    @GetMapping("/download_image")
    public ResponseEntity<Object> downloadImage(@RequestParam String username, Model model)
            throws MalformedURLException {
        String nameFile = username + ".jpg";
        Path imagePath = IMAGES_FOLDER.resolve(nameFile);
        Resource image = new UrlResource(imagePath.toUri());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "image/jpeg")
                .body(image);
    }

}