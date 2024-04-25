package dws.duckbit.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.hibernate.engine.jdbc.BlobProxy;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import dws.duckbit.entities.Combo;
import dws.duckbit.entities.Leak;
import dws.duckbit.entities.UserD;
import dws.duckbit.services.ComboService;
import dws.duckbit.services.LeakService;
import dws.duckbit.services.UserService;
import jakarta.servlet.http.HttpServletRequest;


@Controller
public class WebController
{
    private final Path LEAKS_FOLDER = Paths.get("files/leaks");
    private final Path COMBOS_FOLDER = Paths.get("files/combo");
    private final UserService userService;
    private final LeakService leakService;
    private final ComboService comboService;

    private int soldCombos = 0;

    public WebController(UserService userService, LeakService leakService, ComboService comboService) {
        this.userService = userService;
        this.leakService = leakService;
        this.comboService = comboService;
    }
// ---------- INDEX ---------- //

    @GetMapping("/")
    public String index(Model model, HttpServletRequest request)
    {
        model.addAttribute("admin", request.isUserInRole("ADMIN"));
        model.addAttribute("user", request.isUserInRole("USER"));
        return "index";
    }
    
    //Authorize(ADMIN)
    //Authorize(USER)
    @GetMapping({"/successLogin", "/successLogin/"})
    public ModelAndView succesLogin(HttpServletRequest request)
    {
        if (request.isUserInRole("ADMIN")){
            return new ModelAndView("redirect:/admin");
        }
        if (request.isUserInRole("USER")){
            return new ModelAndView("redirect:/user");
        }
        else
            return new ModelAndView("redirect:/");
    }

// ---------- USERS TYPES ---------- //

    //Authorize(ADMIN)
    @GetMapping({"/admin", "/admin/"})
    public ModelAndView Admin(Model model, HttpServletRequest request)
    {
        String name = this.userService.findByID(1L).get().getUserd();
        String email = this.userService.findByID(1L).get().getMail();
        model.addAttribute("username", name);
        List<Leak> leaks;
        if (this.leakService.getNextId() > 0)
        {
            leaks = this.leakService.findAll();
            model.addAttribute("leak", leaks);
        }
        Collection<Combo> c = this.comboService.getAvilableCombos();
        if (!c.isEmpty())
        {
            model.addAttribute("combos", c);
        }
        model.addAttribute("registredUsers", userService.getSize());
        model.addAttribute("combosCreated", comboService.getComboSize() - soldCombos);
        model.addAttribute("soldCombos", soldCombos);
        model.addAttribute("email", email);
        CsrfToken token = (CsrfToken) request.getAttribute("_csrf");
        model.addAttribute("token", token.getToken());
        return new ModelAndView("admin");
    }

    //Authorize(ADMIN)
    @GetMapping({"/users", "/users/"})
    public ModelAndView Users(Model model, HttpServletRequest request)
    {
        List<UserD> users = userService.findAll();
        model.addAttribute("users", users);
        String name = this.userService.findByID(1l).get().getUserd();
        String email = this.userService.findByID(1l).get().getMail();
        model.addAttribute("username", name);
        model.addAttribute("email", email);
        CsrfToken token = (CsrfToken) request.getAttribute("_csrf");
        model.addAttribute("token", token.getToken());
        return new ModelAndView("users");
    }

    //Authorize(USER)
    @GetMapping({"/user", "/user/"})
    public ModelAndView User(Model model, HttpServletRequest request)
    {
        Long idNum = this.userService.findByUsername(request.getUserPrincipal().getName()).orElseThrow().getID();
        if (!(this.userService.IDExists(idNum)))         // Cookie check (and if user exists)
        {
            idNum = -1L;
        }
        if (idNum == 1)
        {
            return new ModelAndView("redirect:/admin");
        }
        else if (idNum > 1)
        {
            String name = this.userService.findByID(idNum).get().getUserd();
            int credits = this.userService.findByID(idNum).get().getCredits();
            String email = this.userService.findByID(idNum).get().getMail();
            List<Combo> combos = this.userService.findByID(idNum).get().getCombos();
            model.addAttribute("credits", credits);
            model.addAttribute("username", name);
            model.addAttribute("combos", combos);
            model.addAttribute("email", email);
            model.addAttribute("id", idNum);
            CsrfToken token = (CsrfToken) request.getAttribute("_csrf");
            model.addAttribute("token", token.getToken());
            return new ModelAndView("user");
        }
        return new ModelAndView("redirect:/login");
    }

    //Authorize(ADMIN)
    //Authorize(USER)(DELETE ONLY THE SAME ID)
    @DeleteMapping({"/delete_user/{userID}", "/delete_user/{userID}/"})
    public ModelAndView deleteUser(@PathVariable int userID, HttpServletRequest request) throws IOException
    {
        if (request.isUserInRole("ADMIN"))
        {
            if (userID > 1)
                this.comboService.deleteUser(userID);
            return new ModelAndView("redirect:/users");
        }
        else if (this.userService.findByUsername(request.getUserPrincipal().getName()).orElseThrow().getID() == userID)
        {
            this.comboService.deleteUser(userID);
            return new ModelAndView("redirect:/");
        }
        return new ModelAndView("redirect:/user");
    }

// ---------- LOGIN AND REGISTER ---------- //

    // Login page
    @GetMapping({"/login", "/login/"})
    public ModelAndView Login(Model model, HttpServletRequest request)
    {
        CsrfToken token = (CsrfToken) request.getAttribute("_csrf");
        model.addAttribute("token", token.getToken());
        return new ModelAndView("login");
    }

    // Register page
    @GetMapping({"/register", "/register/"})
    public ModelAndView Register(Model model, HttpServletRequest request)
    {
        if (request.isUserInRole("ADMIN"))
        {
            return new ModelAndView("redirect:/admin");
        }
        else if (request.isUserInRole("USER"))
        {
            return new ModelAndView("redirect:/user");
        }
        CsrfToken token = (CsrfToken) request.getAttribute("_csrf");
        model.addAttribute("token", token.getToken());
        return new ModelAndView("register");
    }

    @GetMapping({"/login_error"})
    public ModelAndView loginError(Model model)
    {
        model.addAttribute("incorrectLogin", true);
        return new ModelAndView("redirect:/login");
    }

    // Form sended to register
    @PostMapping({"/register", "/register/"})
    public ModelAndView Register(@RequestParam String userD, @RequestParam String pass, @RequestParam String mail)
    {
        if (userD.length() > 255){
            ModelAndView modelAndView = new ModelAndView();
            modelAndView.setViewName("register");
            modelAndView.addObject("username2Big", true);
            return modelAndView;
        }
        if (pass.length() > 255){
            ModelAndView modelAndView = new ModelAndView();
            modelAndView.setViewName("register");
            modelAndView.addObject("password2Big", true);
            return modelAndView;
        }
        if (mail.length() > 255){
            ModelAndView modelAndView = new ModelAndView();
            modelAndView.setViewName("register");
            modelAndView.addObject("email2Big", true);
            return modelAndView;
        }
        if (this.userService.userExists(userD))
        {
            ModelAndView modelAndView = new ModelAndView();
            modelAndView.setViewName("register");
            modelAndView.addObject("userExists", true);
            return modelAndView;
        }
        this.userService.addUser(userD, mail, pass, "USER");
        return new ModelAndView("redirect:/login");
    }

// ---------- SHOP ---------- //

    //Authorize(USER)
    @GetMapping({"/shop", "/shop/"})
    public ModelAndView shop(Model model, HttpServletRequest request) throws IOException
    {
        Collection<Combo> c = this.comboService.getAvilableCombos();
        if (!c.isEmpty())
        {
            model.addAttribute("combos", c);
        }
        String name = this.userService.findByUsername(request.getUserPrincipal().getName()).get().getUserd();
        int credits = this.userService.findByUsername(request.getUserPrincipal().getName()).get().getCredits();
        model.addAttribute("credits", credits);
        model.addAttribute("username", name);
        CsrfToken token = (CsrfToken) request.getAttribute("_csrf");
        model.addAttribute("token", token.getToken());
        return new ModelAndView("shop");
    }

    //Authorize(USER)
    @GetMapping({"/query", "/query/"})
    public ModelAndView getMethodName(Model model, HttpServletRequest request,@RequestParam(defaultValue = "") String enterprise, @RequestParam(defaultValue = "-1") Integer price)
    {
        if (enterprise.equals("") && price <= 0)
        {
            return new ModelAndView("redirect:/shop");
        }
        Collection<Combo> c = this.comboService.findAll(enterprise, price);
        model.addAttribute("combos", c);
        String name = this.userService.findByUsername(request.getUserPrincipal().getName()).get().getUserd();
        int credits = this.userService.findByUsername(request.getUserPrincipal().getName()).get().getCredits();
        model.addAttribute("credits", credits);
        model.addAttribute("username", name);
        return new ModelAndView("shop");
    }

// ---------- IMAGES MANIPULATION ---------- //

    // Upload a new user image
    @PostMapping({"/upload_image", "/upload_image/"})
    public ModelAndView uploadImage(@RequestParam String username, @RequestParam MultipartFile image, RedirectAttributes attributes) throws IOException 
    {
        UserD user = this.userService.findByUsername(username).orElseThrow();
        user.setImageFile(BlobProxy.generateProxy(image.getInputStream(), image.getSize()));
        this.userService.save(user);
        if (user.getID().equals(1L))
        {
            return new ModelAndView("redirect:/admin");
        }
        attributes.addFlashAttribute("username", username);
        return new ModelAndView("redirect:/user");
    }

    // Download a user image
    @GetMapping({"/download_image", "/download_image/"})
    public ResponseEntity<Object> downloadImage(@RequestParam String username) throws SQLException
    {
        UserD u = this.userService.findByUsername(username).orElseThrow();
        if (u.getImageFile() != null) {

            Resource file = new InputStreamResource(u.getImageFile().getBinaryStream());

            return ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, "image/jpeg")
                    .contentLength(u.getImageFile().length()).body(file);

        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Delete a user image
    @DeleteMapping({"/delete_image", "/delete_image/"})
    public  ModelAndView  deleteImage(RedirectAttributes attributes, HttpServletRequest request) throws IOException
    {

        UserD u = this.userService.findByUsername(request.getUserPrincipal().getName()).get();
        u.setImageFile(null);
        this.userService.save(u);
        if (u.getID().equals(1L))
        {
            return new ModelAndView("redirect:/admin");
        }
        attributes.addFlashAttribute("username", u.getUserd());
        return new ModelAndView("redirect:/user");
    }

// ---------- LEAKS MANIPULATION ---------- //

    // Upload a new leak
    @PostMapping({"/upload_leak", "/upload_leak/"})
    public ModelAndView UploadLeak(@RequestParam String leakName, @RequestParam String leakDate, @RequestParam MultipartFile leak) throws IOException
    {
        String filename = leak.getOriginalFilename();
        int upload = leakService.upload(leak, leakName, this.leakService, leakDate);
        if (upload == 1)
        {
            if (leakName.isEmpty())
            {
                ModelAndView modelAndView = new ModelAndView();
                modelAndView.setViewName("/error");
                modelAndView.addObject("leakEmptyName", true);
                return modelAndView;
            }
            else {
                ModelAndView modelAndView = new ModelAndView();
                modelAndView.setViewName("/error");
                modelAndView.addObject("leakName2Big", true);
                return modelAndView;
            }
        }
        if (upload == 2)
        {
            ModelAndView modelAndView = new ModelAndView();
            modelAndView.setViewName("/error");
            modelAndView.addObject("incorrectFileName", true);
            return modelAndView;
        }
        if (upload == 3)
        {
            ModelAndView modelAndView = new ModelAndView();
            modelAndView.setViewName("/error");
            modelAndView.addObject("incorrectDate", true);
            return modelAndView;
        }
        Leak l = this.leakService.createLeak(leakName, leakDate, filename);
		if (l != null)
		{
			Files.createDirectories(LEAKS_FOLDER);
			Path txtPath = LEAKS_FOLDER.resolve(filename);
			leak.transferTo(txtPath);
		}
        return new ModelAndView("redirect:/admin");
    }

    //Delete leak
    @DeleteMapping({"/delete_leak/{id}", "/delete_leak/{id}/"})
    public ModelAndView deleteLeak(@PathVariable int id) throws IOException
    {
        Optional<Leak> l = this.leakService.findByID(id);
        if (l.isPresent())
            this.leakService.delete(l.get());
        return new ModelAndView("redirect:/admin");
    }

// ---------- COMBOS MANIPULATION ---------- //

    // Create a new combo
    @PostMapping({"/create_combo", "/create_combo/"} )
    public ModelAndView CreateCombo(Model model, @RequestParam String comboName, @RequestParam int price, @RequestParam String description, @RequestParam String ... ids) throws IOException
    {
        int check = comboService.checkCreateCombo(comboName, description, price);
        if (check == 1)
        {
            ModelAndView modelAndView = new ModelAndView();
            modelAndView.setViewName("/error");
            modelAndView.addObject("comboName2Big", true);
            return modelAndView;
        }
        if (check == 2)
        {
            ModelAndView modelAndView = new ModelAndView();
            modelAndView.setViewName("/error");
            modelAndView.addObject("comboDesc2Big", true);
            return modelAndView;
        }
        if (check == 3)
        {
            ModelAndView modelAndView = new ModelAndView();
            modelAndView.setViewName("/error");
            modelAndView.addObject("comboWrongPrice", true);
            return modelAndView;
        }
        ArrayList<Long> idS = new ArrayList<>();
        for (String i: ids)
        {
            idS.add(Long.parseLong(i));
        }
        Combo c = comboService.createCombo(comboName, idS, price, description);
        comboService.save(c);
        return new ModelAndView("redirect:/admin");
    }

    // Delete a combo
    @DeleteMapping({"/delete_combo/{comboID}", "/delete_combo/{comboID}/"})
    public ResponseEntity<Object> deleteCombo(@PathVariable int comboID) throws IOException
    {
        this.comboService.delete(comboID);
        return ResponseEntity.noContent().build();
    }

    // Buy a combo
    @PostMapping({"/buy_combo", "/buy_combo/"})
    public ModelAndView BuyCombo(Model model, @RequestParam Long combo, HttpServletRequest request)
    {
        Long userID = this.userService.findByUsername(request.getUserPrincipal().getName()).orElseThrow().getID();
        Optional<Combo> c = this.comboService.findById(combo);
		if (c.isEmpty())
		{
			return new ModelAndView("redirect:/shop");
		}
        if (!this.comboService.getAvilableCombos().contains(c.get()))
            return new ModelAndView("redirect:/shop");
        int comboPrice = this.comboService.getComboPrice(combo);
        if (this.userService.hasEnoughCredits(comboPrice, userID))
        {
            this.userService.substractCreditsToUser(comboPrice, userID);
            if (c.isPresent())
            {
                Combo comboBought = c.get();
                this.userService.addComboToUser(comboBought, userID);
                comboBought.setUser(this.userService.findByID(userID).get());
                this.comboService.save(comboBought);
                soldCombos++;
            }

        }
        return new ModelAndView("redirect:/user");
    }

    // Download a combo
    @PostMapping({"/download_combo", "/download_combo/"})
    public ResponseEntity<InputStreamResource> downloadCombo(@RequestParam String idCombo, HttpServletRequest request)
            throws MalformedURLException, FileNotFoundException
    {
        Long idNum = this.userService.findByUsername(request.getUserPrincipal().getName()).orElseThrow().getID();
        if (this.userService.IDExists(idNum))
        {
            for (Combo combo : userService.findByID(idNum).get().getCombos())
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
        }
        return ResponseEntity.notFound()
                .build();
    }

    // Edit a combo
    @PostMapping({"/edit_combo", "/edit_combo/"})
    public ModelAndView EditCombo(Model model, @RequestParam String comboName, @RequestParam int price, @RequestParam String id, @RequestParam String description, @RequestParam String ... ids) throws IOException
    {
        ArrayList<Integer> idS = new ArrayList<Integer>();
        for (String i: ids)
        {
            idS.add(Integer.parseInt(i));
        }
        int check = comboService.checkEditCombo(comboName, description, price, Long.parseLong(id), this.comboService, this.leakService, idS);
        if (check == 1)
        {
            ModelAndView modelAndView = new ModelAndView();
            modelAndView.setViewName("/error");
            modelAndView.addObject("comboName2Big", true);
            return modelAndView;
        }
        if (check == 2)
        {
            ModelAndView modelAndView = new ModelAndView();
            modelAndView.setViewName("/error");
            modelAndView.addObject("comboDesc2Big", true);
            return modelAndView;
        }
        if (check == 3)
        {
            ModelAndView modelAndView = new ModelAndView();
            modelAndView.setViewName("/error");
            modelAndView.addObject("comboWrongPrice", true);
            return modelAndView;
        }
        if (check == 4)
        {
            ModelAndView modelAndView = new ModelAndView();
            modelAndView.setViewName("/error");
            modelAndView.addObject("One of the leaks has not been found in the server", true);
            return modelAndView;
        }
        return new ModelAndView("redirect:/admin");
    }

// ---------- CREDITS ---------- //

    @PostMapping({"/add_credits", "/add_credits/"})
    public ModelAndView AddCredits(Model model, HttpServletRequest request)
    {
        Long idNum = this.userService.findByUsername(request.getUserPrincipal().getName()).orElseThrow().getID();
        if (this.userService.IDExists(idNum))
        {
            UserD userD = this.userService.findByID(idNum).get();
            userD.addCredits(500);
            this.userService.save(userD);
        }
        return new ModelAndView("redirect:/user");
    }
}