package dws.duckbit.controlers;

import dws.duckbit.Entities.Combo;
import dws.duckbit.Entities.Leak;
import dws.duckbit.Entities.User;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import dws.duckbit.services.ComboService;
import dws.duckbit.services.LeakService;
import dws.duckbit.services.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.core.io.UrlResource;

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
    private final Path IMAGES_FOLDER = Paths.get("src/main/resources/static/images/profile_images");
    private final Path LEAKS_FOLDER = Paths.get("src/main/resources/static/leaks");
    private final Path COMBOS_FOLDER = Paths.get("src/main/resources/static/combo");
    private final UserService userDB;
    private final LeakService leakDB;
    private final ComboService comboDB;

    private int soldCombos = 0;

	public WebController(UserService userDB, LeakService leakDB, ComboService comboDB) {
		this.userDB = userDB;
		this.leakDB = leakDB;
		this.comboDB = comboDB;
	}
	// INDEX

    @GetMapping("/")
    public String index()
    {
        return "index";
    }

    // USERS TYPES

    @GetMapping("/admin")
    public ModelAndView Admin(Model model, @CookieValue(value = "id", defaultValue = "-1") String id)
    {
        int idNum = Integer.parseInt(id);
        if (!(this.userDB.IDExists(idNum)))
        {
            idNum = -1;
        }
        if (idNum == 0)
        {
            String name = this.userDB.getByID(idNum).getUser();
            String email = this.userDB.getByID(idNum).getMail();
            model.addAttribute("username", name);
            List<Leak> leaks = new ArrayList<>();
            if (this.leakDB.getNextId() > 0){
                for(int i = 0; i < this.leakDB.getNextId(); i++) {
                    Leak leak = this.leakDB.getByID(i);
                    leaks.add(leak);
                }
                model.addAttribute("leak", leaks);
            }
            Collection<Combo> c = this.comboDB.getAll();
            if (!c.isEmpty()) {
                model.addAttribute("combos", c);
            }
            model.addAttribute("registredUsers", userDB.getSize());
            model.addAttribute("combosCreated", comboDB.getComboSize());
            model.addAttribute("soldCombos", soldCombos);
            model.addAttribute("email", email);
            return new ModelAndView("admin");
        }
        else if (idNum > 0)
        {
            return new ModelAndView("redirect:/user");
        }
        return new ModelAndView("redirect:/login");
    }

    @GetMapping("/user")
    public ModelAndView User(Model model, @CookieValue(value = "id", defaultValue = "-1") String id)
    {
        int idNum = Integer.parseInt(id);
        if (!(this.userDB.IDExists(idNum)))
        {
            idNum = -1;
        }
        if (idNum == 0)
        {
            return new ModelAndView("redirect:/admin");
        }
        else if (idNum > 0)
        {
            String name = this.userDB.getByID(idNum).getUser();
            int credits = this.userDB.getByID(idNum).getCredits();
            String email = this.userDB.getByID(idNum).getMail();
            ArrayList<Combo> combos = this.userDB.getByID(idNum).getCombos();
            model.addAttribute("credits", credits);
            model.addAttribute("username", name);
            model.addAttribute("combos", combos);
            model.addAttribute("email", email);
            return new ModelAndView("user");
        }
        return new ModelAndView("redirect:/login");
    }

    // LOGIN AND REGISTER

    @GetMapping({"/login", "/login/"})
    public ModelAndView Login(Model model, @CookieValue(value = "id", defaultValue = "-1") String id)
    {
        int idNum = Integer.parseInt(id);
        if (!(this.userDB.IDExists(idNum)))
        {
            idNum = -1;
        }
        if (idNum == 0)
        {
            return new ModelAndView("redirect:/admin");
        }
        else if (idNum > 0)
        {
            return new ModelAndView("redirect:/user");
        }
        return new ModelAndView("login");
    }

    @GetMapping({"/register", "/register/"})
    public ModelAndView Register(Model model, @CookieValue(value = "id", defaultValue = "-1") String id)
    {
        int idNum = Integer.parseInt(id);
        if (!(this.userDB.IDExists(idNum)))
        {
            idNum = -1;
        }
        if (idNum == 0)
        {
            return new ModelAndView("redirect:/admin");
        }
        else if (idNum > 0)
        {
            return new ModelAndView("redirect:/user");
        }
        return new ModelAndView("register");
    }

    @PostMapping({"/login", "/login/"})
    public ModelAndView Login(@RequestParam String user, @RequestParam String pass, RedirectAttributes attributes, HttpServletResponse response)
    {
        int userID = this.userDB.getIDUser(user, pass);
        Cookie cookie = new Cookie("id", String.valueOf(userID));
        if (userID == 0)
        {
            response.addCookie(cookie);
            return new ModelAndView("redirect:/admin");
        }
        else if (userID > 0)
        {
            response.addCookie(cookie);
            return new ModelAndView("redirect:/user");
        }
        else
        {
            return new ModelAndView("login");
        }
    }

    @PostMapping({"/register", "/register/"})
    public ModelAndView Register(@RequestParam String user, @RequestParam String pass, @RequestParam String mail)
    {
        if (this.userDB.userExists(user))
        {
            return new ModelAndView("redirect:/register");
        }
        else
        {
            this.userDB.addUser(user, mail, pass);
        }    
        return new ModelAndView("redirect:/login");
    }

    @GetMapping({"/logout", "/logout/"})
    public ModelAndView Logout(@CookieValue(value = "id", defaultValue = "-1") String id, HttpServletResponse response)
    {
        Cookie cookie = new Cookie("id", null);
        response.addCookie(cookie);
        return new ModelAndView("redirect:/login");
    }

    // THE SHOP

    @GetMapping({"/shop", "/shop/"})
    public ModelAndView shop(Model model, @CookieValue(value = "id", defaultValue = "-1") String id) throws IOException {
        /*Calendar date = Calendar.getInstance();
        ArrayList<Leak> leaks = new ArrayList<>();
        leaks.add( new Leak("pepe",date, 0));
        leaks.add( new Leak("ere",date, 1));
        comboDB.addCombo(new Combo("Pepe", leaks, 0, 190));*/
        if (Integer.parseInt(id) == -1)
        {
            return new ModelAndView("redirect:/login");
        }
        Collection<Combo> c = this.comboDB.getAll();
        if (!c.isEmpty())
        {
                model.addAttribute("combos", c);
        }
        String name = this.userDB.getByID(Integer.parseInt(id)).getUser();
        int credits = this.userDB.getByID(Integer.parseInt(id)).getCredits();
        model.addAttribute("credits", credits);
        model.addAttribute("username", name);
        return new ModelAndView("shop");
    }


    //IMAGE MAPPING
    @PostMapping({"/upload_image", "/upload_image/"})
    public ModelAndView uploadImage(@RequestParam String username, @RequestParam MultipartFile image, RedirectAttributes attributes) throws IOException {
        Files.createDirectories(IMAGES_FOLDER);
        String nameFile = username + ".jpg";
        Path imagePath = IMAGES_FOLDER.resolve(nameFile);
        image.transferTo(imagePath);
        if (this.userDB.getByID(0).getUser().equals(username))
        {
            return new ModelAndView("redirect:/admin");
        }
        attributes.addFlashAttribute("username", username);
        return new ModelAndView("redirect:/user");
    }

    @GetMapping({"/download_image", "/download_image/"})
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

    @DeleteMapping({"/delete_image", "/delete_image/"})
    public ResponseEntity<Object> deleteImage(@CookieValue(value = "id", defaultValue = "-1") String id) throws IOException
    {
        int idN = Integer.parseInt(id);
        if(idN >= 0)
        {
            User user = userDB.getByID(idN);
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

    @PostMapping({"/upload_leak", "/upload_leak/"})
    public ModelAndView UploadLeak(@RequestParam String leakName, @RequestParam String leakDate, @RequestParam MultipartFile leak) throws IOException
    {
        Files.createDirectories(LEAKS_FOLDER);
        String nameFile = String.valueOf(this.leakDB.getNextId()) + ".txt";
        Path leakPath = LEAKS_FOLDER.resolve(nameFile);
        leak.transferTo(leakPath);
        this.leakDB.createLeak(leakName, leakDate);
        return new ModelAndView("redirect:/admin");
    }

    // COMBOS

    @PostMapping({"/create_combo", "/create_combo/"} )
	public ModelAndView CreateCombo(Model model, @RequestParam String comboName, @RequestParam String price, @RequestParam String ... ids) throws IOException
    {
        ArrayList<Integer> idS = new ArrayList<Integer>();
        for (String i: ids)
            idS.add(Integer.parseInt(i));
        Combo c = comboDB.createCombo(comboName, idS, Integer.parseInt(price));
        comboDB.addCombo(c);
        return new ModelAndView("redirect:/admin");
    }

    @DeleteMapping({"/delete_combo/{id}", "/delete_combo/{comboID}"})
    public ResponseEntity<Object> deleteCombo(@CookieValue(value = "id", defaultValue = "-1") String id, @PathVariable int comboID) throws IOException
    {
        int idN = Integer.parseInt(id);
        if(idN == 0)
        {
            this.comboDB.deleteCombo(comboID);
            return ResponseEntity.noContent().build();
        }
        else
        {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping({"/buy_combo", "/buy_combo/"})
    public ModelAndView BuyCombo(Model model, @RequestParam int combo, @CookieValue(value = "id", defaultValue = "-1") String id)
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
        soldCombos++;
        return new ModelAndView("redirect:/user");
    }

    @PostMapping({"/download_combo", "/download_combo/"})
    public ResponseEntity<InputStreamResource> downloadCombo(@RequestParam String idCombo, @CookieValue(value = "id", defaultValue = "-1") String id)
            throws MalformedURLException, FileNotFoundException 
    {
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

    @PostMapping({"/edit_combo", "/edit_combo/"})
    public ModelAndView EditCombo(Model model, @RequestParam String comboName, @RequestParam String price, @RequestParam String id, @RequestParam String ... ids) throws IOException
    {
        ArrayList<Integer> idS = new ArrayList<Integer>();
        for (String i: ids)
            idS.add(Integer.parseInt(i));
        Combo c = comboDB.getByID(Integer.parseInt(id));
        ArrayList<Leak> leaksEdit = new ArrayList<>();
        if (c != null) {
            if (this.leakDB.getNextId() > 0) {
                for (int i : idS) {
                    Leak leak = this.leakDB.getByID(i);
                    if (leak != null) {
                        leaksEdit.add(leak);
                    }
                }
                String nameFile = id + ".txt";
                Path comboPath = COMBOS_FOLDER.resolve(nameFile);
                Resource comboF = new UrlResource(comboPath.toUri());
                if (comboF.exists()) {
                    Files.delete(comboPath);
                }
                c.editCombo(comboName, Integer.parseInt(price), leaksEdit);
            }
        }
        return new ModelAndView("redirect:/admin");
    }

    @PostMapping({"/add_credits", "/add_credits/"})
    public ModelAndView AddCredits(Model model, @CookieValue(value = "id", defaultValue = "-1") String id) {
        User user = this.userDB.getByID(Integer.parseInt(id));
        user.addCredits(500);
        return new ModelAndView("redirect:/user");
    }
}