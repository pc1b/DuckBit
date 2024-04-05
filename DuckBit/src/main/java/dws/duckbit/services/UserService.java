package dws.duckbit.services;

import dws.duckbit.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;

import dws.duckbit.entities.Combo;
import dws.duckbit.entities.User;


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
        for (User u: this.userRepository.findAll())
        {
            if (u.isUser(user, password))
            {
                return (u.getID());
            }
        }
        return (-1L);
    }

    public User findByID(Long ID)
    {
        Optional<User> u = this.userRepository.findById(ID);
	    return u.orElse((null));
    }

// ---------- ADD AND CREATE ---------- //

    public void addUser(String user, String mail, String password)
    {
        User newUser = new User(user, mail, password);
        this.userRepository.save(newUser);
    }

    public void addComboToUser(Combo combo, Long ID)
    {
        this.userRepository.findById(ID).orElseThrow().addCombosToUser(combo);
    }

// ---------- CREDITS AND MONEY ---------- //

    public boolean hasEnoughCredits(int price, Long ID)
    {
        return (this.userRepository.findById(ID).orElseThrow().hasEnoughCredits(price));
    }

    public void addCreditsToUser(int plus, Long ID)
    {
        this.userRepository.findById(ID).orElseThrow().addCredits(plus);
    }

    public void substractCreditsToUser(int minus, Long ID)
    {
        this.userRepository.findById(ID).orElseThrow().substractCredits(minus);
    }   

// ---------- OTHERS ---------- //

    public boolean userExists (String user)
    {
        Optional<User> u = this.userRepository.findByUser(user);
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
