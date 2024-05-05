package dws.duckbit.services;

import dws.duckbit.entities.Combo;
import dws.duckbit.entities.UserD;
import dws.duckbit.repositories.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static org.springframework.http.ResponseEntity.status;


@Service
public class UserService
{
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    //private final ComboService comboService;

// ---------- CONSTRUCTOR ---------- //

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder)
    {
	    this.userRepository = userRepository;
	    //this.comboService = comboService;
	    this.passwordEncoder = passwordEncoder;
    }

// ---------- GET ---------- //

    public long getSize()
    {
        return this.userRepository.count();
    }

    public List<UserD> findAll()
    {
        return this.userRepository.findAll();
    }


    public Optional<UserD> findByUsername(String username)
    {
        return this.userRepository.findByUserd(username);
    }

    public Optional<UserD> findByID(Long ID)
    {
        return this.userRepository.findById(ID);
    }

// ---------- ADD AND CREATE ---------- //

    public void save(UserD userD)
    {
        this.userRepository.save(userD);
    }

    public void addUser(String user, String mail, String password, String... roles)
    {
        UserD newUserD = new UserD(user, mail, passwordEncoder.encode(password), roles);

        this.userRepository.save(newUserD);
    }

    public void addComboToUser(Combo combo, Long ID)
    {
        Optional<UserD> u = this.userRepository.findById(ID);
        u.orElseThrow().addCombosToUser(combo);
        this.userRepository.save(u.get());
    }

    public boolean userExists (String user)
    {
        Optional<UserD> u = this.userRepository.findByUserd(user);
	    return u.isPresent();
    }

    public boolean IDExists(int ID)
    {
        return this.userRepository.findById((long)ID).isPresent();

    }

// ---------- CREDITS AND MONEY ---------- //

    public boolean hasEnoughCredits(int price, Long ID)
    {
        return (this.userRepository.findById(ID).orElseThrow().hasEnoughCredits(price));
    }

    public void addCreditsToUser(int plus, Long ID)
    {
        Optional<UserD> u = this.userRepository.findById(ID);
        u.orElseThrow().addCredits(plus);
        this.userRepository.save(u.get());
    }

    public void substractCreditsToUser(int minus, Long ID)
    {
        Optional<UserD> u = this.userRepository.findById(ID);
        u.orElseThrow().substractCredits(minus);
        this.userRepository.save(u.get());
    }

    public boolean IDExists(Long ID)
    {
        return this.userRepository.findById(ID).isPresent();

    }
    public void delete(Long id){
        this.userRepository.deleteById(id);
    }

    public void editUser(String userd, String userdUpdated, String mail, String password){
        this.userRepository.findByUserd(userd).get().editUser(userdUpdated, mail, passwordEncoder.encode(password));
        this.userRepository.save(this.userRepository.findByUserd(userd).get());
    }

    public int checkUser(String username, String password, String mail){
        if (username.length() > 255)
            return 1;
        if (password.length() > 255)
            return 2;
        if (mail.length() > 255)
            return 3;
        if (this.userExists(username))
            return 4;
        return 0;
    }
    //NOT FINISHED
    public boolean buyCombo(Combo c, Long userid){
        if (this.hasEnoughCredits(c.getPrice(), userid))
        {
            this.substractCreditsToUser(c.getPrice(), userid);
            this.addComboToUser(c, userid);
            c.setUser(this.findByID(userid).get());
            //this.comboService.save(c);
            //this.comboService.updateSoldCombo();
            return true;
        }
        return false;
    }
}
