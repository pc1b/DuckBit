package dws.duckbit;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
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
    private static int i = 0;
    private String nameFile ="";
    private static final Path IMAGES_FOLDER = Paths.get("../images");
    private String imageName;

    // INDEX

    @GetMapping("/")
    public String index()
    {
        return "index";
    }

    @GetMapping("/admin")
    public String aboutUs()
    {
        return "admin";
    }

    // USERS TYPES

    @GetMapping("/user")
    public String User(Model model)
    {
        model.addAttribute("Name", "Paco");
        return "user";
    }

    // LOGIN AND REGISTER

    @GetMapping("/login")
    public String Form()
    {
        return "login";
    }

    @GetMapping("/register")
    public String Register()
    {
        return "register";
    }

    // SHOP

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
    public String uploadImage(@RequestParam String imageName,
                              @RequestParam MultipartFile image, Model model) throws IOException {
        this.imageName = imageName;
        Files.createDirectories(IMAGES_FOLDER);
        nameFile = "profile" + i + ".jpg";
        i++;
        Path imagePath = IMAGES_FOLDER.resolve(nameFile);
        image.transferTo(imagePath);
        model.addAttribute("imageName", imageName);
        return "image";
    }

    @GetMapping("/image")
    public String viewImage(Model model) {
        model.addAttribute("imageName", imageName);
        return "view_image";
    }

    @GetMapping("/download_image")
    public ResponseEntity<Object> downloadImage(Model model)
            throws MalformedURLException {
        Path imagePath = IMAGES_FOLDER.resolve(nameFile);
        Resource image = new UrlResource(imagePath.toUri());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "image/jpeg")
                .body(image);
    }

}