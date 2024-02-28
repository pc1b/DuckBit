package dws.duckbit.controlers;

import dws.duckbit.Entities.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import dws.duckbit.services.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.core.io.UrlResource;

import java.io.File;
import java.net.MalformedURLException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
public class WebController {
    private static final Path IMAGES_FOLDER = Paths.get("src/main/resources/static/images/profile_images");
    private UserService userDB = new UserService();

    // INDEX

    @GetMapping("/")
    public String index()
    {
        return "index";
    }

    // USERS TYPES

    @GetMapping("/admin")
    public String Admin(Model model, @CookieValue(value = "id", defaultValue = "-1") String id)
    {
        int idNum = Integer.parseInt(id);
        if (idNum == 0)
        {
            String name = this.userDB.getByID(idNum).getUser();
            model.addAttribute("username", name);
            return "admin";
        }
        else if (idNum > 0)
        {
            String name = this.userDB.getByID(idNum).getUser();
            model.addAttribute("username", name);
            return "user";
        }
        return "login";
    }

    @GetMapping("/user")
    public String User(Model model, @CookieValue(value = "id", defaultValue = "-1") String id)
    {
        int idNum = Integer.parseInt(id);
        if (idNum == 0)
        {
            String name = this.userDB.getByID(idNum).getUser();
            model.addAttribute("username", name);
            return "admin";
        }
        else if (idNum > 0)
        {
            String name = this.userDB.getByID(idNum).getUser();
            model.addAttribute("username", name);
            return "user";
        }
        return "login";
    }

    // LOGIN AND REGISTER

    @GetMapping("/login")
    public String Login(Model model, @CookieValue(value = "id", defaultValue = "-1") String id)
    {
        int idNum = Integer.parseInt(id);
        if (idNum == 0)
        {
            String name = this.userDB.getByID(idNum).getUser();
            model.addAttribute("username", name);
            return "admin";
        }
        else if (idNum > 0)
        {
            String name = this.userDB.getByID(idNum).getUser();
            model.addAttribute("username", name);
            return "user";
        }
        return "login";
    }

    @GetMapping("/register")
    public String Register(Model model, @CookieValue(value = "id", defaultValue = "-1") String id)
    {
        int idNum = Integer.parseInt(id);
        if (idNum == 0)
        {
            String name = this.userDB.getByID(idNum).getUser();
            model.addAttribute("username", name);
            return "admin";
        }
        else if (idNum > 0)
        {
            String name = this.userDB.getByID(idNum).getUser();
            model.addAttribute("username", name);
            return "user";
        }
        return "register";
    }

    @PostMapping("/login")
    public RedirectView Login(@RequestParam String user, @RequestParam String pass, RedirectAttributes attributes, HttpServletResponse response)
    {
        int userID = this.userDB.getIDUser(user, pass);
        Cookie cookie = new Cookie("id", String.valueOf(userID));
        if (userID == 0)
        {
            response.addCookie(cookie);
            return new RedirectView("admin");
        }
        else if (userID > 0)
        {
            response.addCookie(cookie);
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

    @GetMapping("/logout")
    public String Logout(@CookieValue(value = "id", defaultValue = "-1") String id, HttpServletResponse response)
    {
        Cookie cookie = new Cookie("id", null);
        response.addCookie(cookie);
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


    //IMAGE MAPPING
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

    @DeleteMapping("delete_image")
    public ResponseEntity<Object> deleteImage(@RequestParam int id) throws IOException {

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


    //ERROR MAPPING
    @GetMapping("/error")
    public String Error(){
        return "error";
    }

}