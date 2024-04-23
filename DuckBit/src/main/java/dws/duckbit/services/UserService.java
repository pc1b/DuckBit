package dws.duckbit.services;

import dws.duckbit.entities.Combo;
import dws.duckbit.entities.UserD;
import dws.duckbit.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


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

    public Long getIDUser(String user, String password)
    {
        Optional<UserD> u;
        try
        {
            u = this.userRepository.findByUserdAndEncodedPassword(user, password);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return (-1L);
        }
        if (u.isPresent())
        {
            return u.get().getID();
        }
        else
        {
            return (-1L);
        }
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
        //UserD newUserD = new UserD(user, mail, password, roles);

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
