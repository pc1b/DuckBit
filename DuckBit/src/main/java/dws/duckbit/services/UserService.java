package dws.duckbit.services;

import dws.duckbit.entities.UserD;
import dws.duckbit.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

import dws.duckbit.entities.Combo;


@Service
public class UserService
{
    private final UserRepository userRepository;

// ---------- CONSTRUCTOR ---------- //

    public UserService(UserRepository userRepository)
    {
	    this.userRepository = userRepository;
    }

// ---------- GET ---------- //

    public long getSize()
    {
        return this.userRepository.count();
    }

    public Long getIDUser(String user, String password)
    {
        for (UserD u: this.userRepository.findAll())
        {
            if (u.isUser(user, password))
            {
                return (u.getID());
            }
        }
        return (-1L);
    }
    public Optional<UserD> findByUsername(String username){
        return this.userRepository.findByUserd(username);
    }

    public Optional<UserD> findByID(Long ID)
    {
        return this.userRepository.findById(ID);
    }

// ---------- ADD AND CREATE ---------- //

    public void save(UserD userD){
        this.userRepository.save(userD);
    }
    public void addUser(String user, String mail, String password)
    {
        UserD newUserD = new UserD(user, mail, password);
        this.userRepository.save(newUserD);
    }

    public void addComboToUser(Combo combo, Long ID)
    {
        Optional<UserD> u = this.userRepository.findById(ID);
        u.orElseThrow().addCombosToUser(combo);
        this.userRepository.save(u.get());
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

// ---------- OTHERS ---------- //

    public boolean userExists (String user)
    {
        Optional<UserD> u = this.userRepository.findByUserd(user);
	    return u.isPresent();
    }

    public boolean IDExists(Long ID)
    {
        return this.userRepository.findById(ID).isPresent();

    }

// ---------- NOT YET IMPLEMENTED ! ---------- // Change username and password
/*
    public void changeUserName(int ID, String name, String password)
    {
        for (User u: this.userList)
        {
            if (u.getID() == ID)
            {
                if (u.comparePassword(password))
                {
                    u.changeUserName(name);
                }
            }
        }
    }

    public void changeUserPassword(int ID, String password)
    {
        for (User u: this.userList)
        {
            if (u.getID() == ID)
            {
                if (u.comparePassword(password))
                {
                    u.changeUserPassword(password);
                }
            }
        }
    }*/
}
