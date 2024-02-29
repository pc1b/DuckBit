package dws.duckbit.controlers;

import dws.duckbit.Entities.Combo;
import dws.duckbit.Entities.Leak;
import dws.duckbit.Entities.User;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import dws.duckbit.services.ComboService;
import dws.duckbit.services.LeakService;
import dws.duckbit.services.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.core.io.UrlResource;

import javax.xml.stream.events.DTD;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Controller
public class WebController {
    private static final Path IMAGES_FOLDER = Paths.get("src/main/resources/static/images/profile_images");
    private static final Path LEAKS_FOLDER = Paths.get("src/main/resources/static/leaks");
    private static final Path COMBOS_FOLDER = Paths.get("src/main/resources/static/combo");
    private UserService userDB = new UserService();
    private LeakService leakDB = new LeakService();
    private ComboService comboDB = new ComboService();


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
            List<Leak> leaks = new ArrayList<>();
            if (this.leakDB.getNextId() > 0){
                for(int i = 0; i < this.leakDB.getNextId(); i++) {
                    Leak leak = this.leakDB.getByID(i);
                    leaks.add(leak);
                }
                model.addAttribute("leak", leaks);
            }
            List<Combo> combos = new ArrayList<>();
            for(int i = 0; i < comboDB.getComboSize(); i++){
                    combos.add(comboDB.getByID(i));
            }
            if (!combos.isEmpty())
                model.addAttribute("combos", combos);
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
            int credits = this.userDB.getByID(idNum).getCredits();
            ArrayList<Combo> combos = this.userDB.getByID(idNum).getCombos();
            model.addAttribute("credits", credits);
            model.addAttribute("username", name);
            model.addAttribute("combos", combos);
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

    @GetMapping("/shop")
    public String shop(Model model, @CookieValue(value = "id", defaultValue = "-1") String id) throws IOException {
        /*Calendar date = Calendar.getInstance();
        ArrayList<Leak> leaks = new ArrayList<>();
        leaks.add( new Leak("pepe",date, 0));
        leaks.add( new Leak("ere",date, 1));
        comboDB.addCombo(new Combo("Pepe", leaks, 0, 190));*/
        ArrayList<Combo> combos = new ArrayList<>();
        for(int i = 0; i < comboDB.getComboSize(); i++){
            combos.add(comboDB.getByID(i));
        }
        if (!combos.isEmpty()){
            model.addAttribute("combos", combos);
        }
        String name = this.userDB.getByID(Integer.parseInt(id)).getUser();
        int credits = this.userDB.getByID(Integer.parseInt(id)).getCredits();
        model.addAttribute("credits", credits);
        model.addAttribute("username", name);
        return "shop";
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
        if (image.exists()) {
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, "image/jpeg")
                    .body(image);
        }
        return ResponseEntity.notFound()
                .build();
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

    // LEAKS

    @PostMapping("/upload_leak")
    public RedirectView UploadLeak(@RequestParam String leakName, @RequestParam String leakDate, @RequestParam MultipartFile leak) throws IOException
    {
        Files.createDirectories(LEAKS_FOLDER);
        String nameFile = String.valueOf(this.leakDB.getNextId()) + ".txt";
        Path leakPath = LEAKS_FOLDER.resolve(nameFile);
        leak.transferTo(leakPath);
        this.leakDB.createLeak(leakName, leakDate);
        return new RedirectView("admin");
    }
    // COMBOS

    @PostMapping("/create_combo")
	public String CreateCombo(Model model, @RequestParam String comboName, @RequestParam String price, @RequestParam String ... ids) throws IOException {
        ArrayList<Integer> idS = new ArrayList<Integer>();
        for (String i: ids)
            idS.add(Integer.parseInt(i));
        Combo c = comboDB.createCombo(comboName, idS, Integer.parseInt(price));
        comboDB.addCombo(c);
        String name = this.userDB.getByID(0).getUser();
        model.addAttribute("username", name);
        List<Leak> leaks = new ArrayList<>();
        if (this.leakDB.getNextId() > 0){
            for(int i = 0; i < this.leakDB.getNextId(); i++) {
                Leak leak = this.leakDB.getByID(i);
                leaks.add(leak);
            }
            model.addAttribute("leak", leaks);
        }
        List<Combo> combos = new ArrayList<>();
        for(int i = 0; i < comboDB.getComboSize(); i++){
            combos.add(comboDB.getByID(i));
        }
        if (!combos.isEmpty())
            model.addAttribute("combos", combos);
        return "admin";
    }
    @PostMapping("/buy_combo")
    public String BuyCombo(Model model, @RequestParam int combo, @CookieValue(value = "id", defaultValue = "-1") String id)
    {
        // We must check if a combo exists
        int userID = Integer.parseInt(id);
        int comboPrice = this.comboDB.getComboPrice(combo);
        if (this.userDB.hasEnoughCredits(comboPrice, userID))
        {
            this.userDB.substractCreditsToUser(comboPrice, userID);
            Combo comboBuyed = this.comboDB.getByID(combo);
            this.comboDB.removeByID(combo);
            this.userDB.addComboToUser(comboBuyed, userID);
        }
        int iD = Integer.parseInt(id);
        String name = this.userDB.getByID(iD).getUser();
        int credits = this.userDB.getByID(iD).getCredits();
        ArrayList<Combo> combos = this.userDB.getByID(iD).getCombos();
        model.addAttribute("credits", credits);
        model.addAttribute("username", name);
        model.addAttribute("combos", combos);
        return "user";
    }

    @PostMapping("/download_combo")
    public ResponseEntity<InputStreamResource> downloadCombo(@RequestParam String idCombo, @CookieValue(value = "id", defaultValue = "-1") String id)
            throws MalformedURLException, FileNotFoundException {
        for (Combo combo : userDB.getByID(Integer.parseInt(id)).getCombos()){
            if (Integer.parseInt(idCombo) == combo.getId()){
                String nameFile = idCombo + ".txt";
                Path comboPath = COMBOS_FOLDER.resolve(nameFile);
                Resource comboF = new UrlResource(comboPath.toUri());
                if (comboF.exists()) {
                    File comboFile = comboPath.toFile();
                    FileInputStream fileInputStream = new FileInputStream(comboFile);
                    InputStreamResource inputStreamResource = new InputStreamResource(fileInputStream);
                    return ResponseEntity.ok()
                            .header(HttpHeaders.CONTENT_TYPE, "text/plain; charset=UTF-8")
                            .body(inputStreamResource);
                }
            }
        }
        return ResponseEntity.notFound()
                .build();
    }

    @PostMapping("/add_credits")
    public String AddCredits(Model model, @CookieValue(value = "id", defaultValue = "-1") String id) {
        User user = this.userDB.getByID(Integer.parseInt(id));
        int currentCreds = user.getCredits();
        user.addCredits(500);
        ArrayList<Combo> combos = this.userDB.getByID(Integer.parseInt(id)).getCombos();
        model.addAttribute("username",user.getUser());
        model.addAttribute("combos",combos);
        model.addAttribute("credits", currentCreds + 500);
        return "user";
    }

    //ERROR MAPPING

    @GetMapping("/error")
    public String Error(){
        return "error";
    }
}