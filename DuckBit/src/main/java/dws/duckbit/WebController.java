package dws.duckbit;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

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

}