package dws.duckbit;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

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
    private AlmacenUsuarios userDB = new AlmacenUsuarios();

    // INDEX

    @GetMapping("/")
    public String index()
    {
        return "index";
    }

    // USERS TYPES

    @GetMapping("/admin")
    public String Admin()
    {
        return "admin";
    }

    @GetMapping("/user")
    public String User(Model model)
    {
        //model.addAttribute("username",model.getAttribute("username"));
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
    public RedirectView Login(@RequestParam String user, @RequestParam String pass, RedirectAttributes attributes)
    {   
        int userID = this.userDB.getIDUser(user, pass);
        if (userID == 0)
        {
            return new RedirectView("admin");

        }
        else if (userID > 0)
        {
                attributes.addFlashAttribute("username", user);
                return new RedirectView("user");

        }
        else
        {
            return new RedirectView("login");
        }
    }

    @PostMapping("/register")
    public RedirectView Register(@RequestParam String user, @RequestParam String pass, @RequestParam String mail)
    {
        this.userDB.addUser(user, mail, pass);
        return new RedirectView("login");
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
    public RedirectView uploadImage(@RequestParam String username, @RequestParam MultipartFile image, RedirectAttributes attributes) throws IOException {
        Files.createDirectories(IMAGES_FOLDER);
        String nameFile = username + ".jpg";
        Path imagePath = IMAGES_FOLDER.resolve(nameFile);
        image.transferTo(imagePath);
        if (this.userDB.getByID(0).getUser().equals(username))
            return new RedirectView("admin");
        attributes.addFlashAttribute("username", username);
        return new RedirectView("user");
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