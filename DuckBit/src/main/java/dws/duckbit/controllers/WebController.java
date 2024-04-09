package dws.duckbit.controllers;

import dws.duckbit.entities.UserD;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.core.io.UrlResource;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import dws.duckbit.services.ComboService;
import dws.duckbit.services.LeakService;
import dws.duckbit.services.UserService;
import dws.duckbit.entities.Combo;
import dws.duckbit.entities.Leak;


@Controller
public class WebController
{
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
// ---------- INDEX ---------- //

    @GetMapping("/")
    public String index()
    {
        return "index";
    }

// ---------- USERS TYPES ---------- //

    // Admin default page
    @GetMapping({"/admin", "/admin/"})
    public ModelAndView Admin(Model model, @CookieValue(value = "id", defaultValue = "-1") String id)
    {
        Long idNum = Long.parseLong(id);
        if (!(this.userDB.IDExists(idNum)))
        {
            idNum = -1L;
        }
        if (idNum == 1)
        {
            String name = this.userDB.findByID(idNum).getUserd();
            String email = this.userDB.findByID(idNum).getMail();
            model.addAttribute("username", name);
            List<Leak> leaks = new ArrayList<>();
            if (this.leakDB.getNextId() > 0)
            {
                leaks = this.leakDB.findAll();
                model.addAttribute("leak", leaks);
            }
            Collection<Combo> c = this.comboDB.findAll();
            if (!c.isEmpty())
            {
                model.addAttribute("combos", c);
            }
            model.addAttribute("registredUsers", userDB.getSize());
            model.addAttribute("combosCreated", comboDB.getComboSize());
            model.addAttribute("soldCombos", soldCombos);
            model.addAttribute("email", email);
            return new ModelAndView("admin");
        }
        else if (idNum > 1)
        {
            return new ModelAndView("redirect:/user");
        }
        return new ModelAndView("redirect:/login");
    }

    // Client default page
    @GetMapping({"/user", "/user/"})
    public ModelAndView User(Model model, @CookieValue(value = "id", defaultValue = "-1") String id)
    {
        Long idNum = Long.parseLong(id);
        if (!(this.userDB.IDExists(idNum)))
        {
            idNum = -1L;
        }
        if (idNum == 1)
        {
            return new ModelAndView("redirect:/admin");
        }
        else if (idNum > 1)
        {
            String name = this.userDB.findByID(idNum).getUserd();
            int credits = this.userDB.findByID(idNum).getCredits();
            String email = this.userDB.findByID(idNum).getMail();
            List<Combo> combos = this.userDB.findByID(idNum).getCombos();
            model.addAttribute("credits", credits);
            model.addAttribute("username", name);
            model.addAttribute("combos", combos);
            model.addAttribute("email", email);
            return new ModelAndView("user");
        }
        return new ModelAndView("redirect:/login");
    }

// ---------- LOGIN AND REGISTER ---------- //

    // Login page
    @GetMapping({"/login", "/login/"})
    public ModelAndView Login(Model model, @CookieValue(value = "id", defaultValue = "-1") String id)
    {
        Long idNum = Long.parseLong(id);
        if (!(this.userDB.IDExists(idNum)))
        {
            idNum = -1L;
        }
        if (idNum == 1)
        {
            return new ModelAndView("redirect:/admin");
        }
        else if (idNum > 1)
        {
            return new ModelAndView("redirect:/user");
        }
        return new ModelAndView("login");
    }

    // Register page
    @GetMapping({"/register", "/register/"})
    public ModelAndView Register(Model model, @CookieValue(value = "id", defaultValue = "-1") String id)
    {
        Long idNum = Long.parseLong(id);
        if (!(this.userDB.IDExists(idNum)))
        {
            idNum = -1L;
        }
        if (idNum == 1)
        {
            return new ModelAndView("redirect:/admin");
        }
        else if (idNum > 1)
        {
            return new ModelAndView("redirect:/user");
        }
        return new ModelAndView("register");
    }

    // Form sended to login
    @PostMapping({"/login", "/login/"})
    public ModelAndView Login(@RequestParam String user, @RequestParam String pass, RedirectAttributes attributes, HttpServletResponse response)
    {
        Long userID = this.userDB.getIDUser(user, pass);
        Cookie cookie = new Cookie("id", String.valueOf(userID));
        if (userID == 1)
        {
            response.addCookie(cookie);
            return new ModelAndView("redirect:/admin");
        }
        else if (userID > 1)
        {
            response.addCookie(cookie);
            return new ModelAndView("redirect:/user");
        }
        else
        {
            return new ModelAndView("login");
        }
    }

    // Form sended to register
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

    // Logout
    @GetMapping({"/logout", "/logout/"})
    public ModelAndView Logout(@CookieValue(value = "id", defaultValue = "-1") String id, HttpServletResponse response)
    {
        Cookie cookie = new Cookie("id", null);
        response.addCookie(cookie);
        return new ModelAndView("redirect:/login");
    }

// ---------- SHOP ---------- //

    // Default page for the shop
    @GetMapping({"/shop", "/shop/"})
    public ModelAndView shop(Model model, @CookieValue(value = "id", defaultValue = "-1") String id) throws IOException
    {
        if (Integer.parseInt(id) == -1)
        {
            return new ModelAndView("redirect:/login");
        }
        Collection<Combo> c = this.comboDB.findAll();
        if (!c.isEmpty())
        {
            model.addAttribute("combos", c);
        }
        String name = this.userDB.findByID(Long.parseLong(id)).getUserd();
        int credits = this.userDB.findByID(Long.parseLong(id)).getCredits();
        model.addAttribute("credits", credits);
        model.addAttribute("username", name);
        return new ModelAndView("shop");
    }

    @GetMapping({"/query", "/query/"})
    public ModelAndView getMethodName(Model model, @CookieValue(value = "id", defaultValue = "-1") String id, @RequestParam(defaultValue = "") String enterprise, @RequestParam(defaultValue = "-1") Integer price, @RequestParam(defaultValue = "0") Integer leaksNumber)
    {
        if (Integer.parseInt(id) == -1)
        {
            return new ModelAndView("redirect:/login");
        }
        if (enterprise.equals("") && price <= 0 && leaksNumber <= 0)
        {
            return new ModelAndView("redirect:/shop");
        }
        Collection<Combo> c = this.comboDB.findAll(enterprise, price, leaksNumber);
        if (!c.isEmpty())
        {
            model.addAttribute("combos", c);
        }
        String name = this.userDB.findByID(Long.parseLong(id)).getUserd();
        int credits = this.userDB.findByID(Long.parseLong(id)).getCredits();
        model.addAttribute("credits", credits);
        model.addAttribute("username", name);
        return new ModelAndView("shop");
    }

// ---------- IMAGES MANIPULATION ---------- //

    // Upload a new user image
    @PostMapping({"/upload_image", "/upload_image/"})
    public ModelAndView uploadImage(@RequestParam String username, @RequestParam MultipartFile image, RedirectAttributes attributes) throws IOException
    {
        Files.createDirectories(IMAGES_FOLDER);
        String nameFile = username + ".jpg";
        Path imagePath = IMAGES_FOLDER.resolve(nameFile);
        image.transferTo(imagePath);
        if (this.userDB.findByID(1L).getUserd().equals(username))
        {
            return new ModelAndView("redirect:/admin");
        }
        attributes.addFlashAttribute("username", username);
        return new ModelAndView("redirect:/user");
    }

    // Download a user image
    @GetMapping({"/download_image", "/download_image/"})
    public ResponseEntity<Object> downloadImage(@RequestParam String username, Model model) throws MalformedURLException
    {
        String nameFile = username + ".jpg";
        Path imagePath = IMAGES_FOLDER.resolve(nameFile);
        Resource image = new UrlResource(imagePath.toUri());
        if (image.exists())
        {
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, "image/jpeg")
                    .body(image);
        }
        return ResponseEntity.notFound()
                .build();
    }

    // Delete a user image
    @DeleteMapping({"/delete_image", "/delete_image/"})
    public ResponseEntity<Object> deleteImage(@CookieValue(value = "id", defaultValue = "-1") String id) throws IOException
    {
        Long idN = Long.parseLong(id);
        if(idN >= 1)
        {
            UserD userD = userDB.findByID(idN);
            Files.createDirectories(IMAGES_FOLDER);
            String nameFile = userD.getUserd() + ".jpg";
            Path imagePath = IMAGES_FOLDER.resolve(nameFile);
            File img = imagePath.toFile();
            if (img.exists())
            {
                try
                {
                    img.delete();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            return ResponseEntity.noContent().build();

        }
        else
        {
            return ResponseEntity.notFound().build();
        }
    }

// ---------- LEAKS MANIPULATION ---------- //

    // Upload a new leak
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

// ---------- COMBOS MANIPULATION ---------- //

    // Create a new combo
    @PostMapping({"/create_combo", "/create_combo/"} )
    public ModelAndView CreateCombo(Model model, @RequestParam String comboName, @RequestParam String price, @RequestParam String description, @RequestParam String ... ids) throws IOException
    {
        ArrayList<Long> idS = new ArrayList<>();
        for (String i: ids)
        {
            idS.add(Long.parseLong(i));
        }
        Combo c = comboDB.createCombo(comboName, idS, Integer.parseInt(price), description);
        comboDB.save(c);
        return new ModelAndView("redirect:/admin");
    }

    // Delete a combo
    @DeleteMapping({"/delete_combo/{id}", "/delete_combo/{comboID}"})
    public ResponseEntity<Object> deleteCombo(@CookieValue(value = "id", defaultValue = "-1") String id, @PathVariable int comboID) throws IOException
    {
        int idN = Integer.parseInt(id);
        if(idN == 0)
        {
            this.comboDB.delete(comboID);
            return ResponseEntity.noContent().build();
        }
        else
        {
            return ResponseEntity.notFound().build();
        }
    }

    // Buy a combo
    @PostMapping({"/buy_combo", "/buy_combo/"})
    public ModelAndView BuyCombo(Model model, @RequestParam int combo, @CookieValue(value = "id", defaultValue = "-1") String id)
    {
        Long userID = Long.parseLong(id);
        int comboPrice = this.comboDB.getComboPrice(combo);
        if (this.userDB.hasEnoughCredits(comboPrice, userID))
        {
            this.userDB.substractCreditsToUser(comboPrice, userID);
            Optional<Combo> c =   this.comboDB.findById(combo);
            if (c.isPresent()){
                Combo comboBought = c.get();
                this.comboDB.delete(combo);
                this.userDB.addComboToUser(comboBought, userID);
            }

        }
        soldCombos++;
        return new ModelAndView("redirect:/user");
    }

    // Download a combo
    @PostMapping({"/download_combo", "/download_combo/"})
    public ResponseEntity<InputStreamResource> downloadCombo(@RequestParam String idCombo, @CookieValue(value = "id", defaultValue = "-1") String id)
            throws MalformedURLException, FileNotFoundException
    {
        for (Combo combo : userDB.findByID(Long.parseLong(id)).getCombos())
        {
            if (Integer.parseInt(idCombo) == combo.getId())
            {
                String nameFile = idCombo + ".txt";
                Path comboPath = COMBOS_FOLDER.resolve(nameFile);
                Resource comboF = new UrlResource(comboPath.toUri());
                if (comboF.exists())
                {
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

    // Edit a combo
    @PostMapping({"/edit_combo", "/edit_combo/"})
    public ModelAndView EditCombo(Model model, @RequestParam String comboName, @RequestParam String price, @RequestParam String id, @RequestParam String description, @RequestParam String ... ids) throws IOException
    {
        ArrayList<Integer> idS = new ArrayList<Integer>();
        for (String i: ids)
        {
            idS.add(Integer.parseInt(i));
        }
        Optional<Combo> c = comboDB.findById(Integer.parseInt(id));
        ArrayList<Leak> leaksEdit = new ArrayList<>();
        if (c.isPresent())
        {
            if (this.leakDB.getNextId() > 0)
            {
                for (int i : idS)
                {
                    Leak leak = this.leakDB.findByID(i);
                    if (leak != null)
                    {
                        leaksEdit.add(leak);
                    }
                }
                String nameFile = id + ".txt";
                Path comboPath = COMBOS_FOLDER.resolve(nameFile);
                Resource comboF = new UrlResource(comboPath.toUri());
                if (comboF.exists())
                {
                    Files.delete(comboPath);
                }
                c.get().editCombo(comboName, Integer.parseInt(price), leaksEdit, description);
                this.comboDB.save(c.get());
            }
        }
        return new ModelAndView("redirect:/admin");
    }

// ---------- CREDITS ---------- //

    @PostMapping({"/add_credits", "/add_credits/"})
    public ModelAndView AddCredits(Model model, @CookieValue(value = "id", defaultValue = "-1") String id)
    {
        UserD userD = this.userDB.findByID(Long.parseLong(id));
        userD.addCredits(500);
        return new ModelAndView("redirect:/user");
    }
}