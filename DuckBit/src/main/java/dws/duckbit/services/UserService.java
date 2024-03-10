package dws.duckbit.services;

import org.springframework.stereotype.Service;

import java.util.ArrayList;

import dws.duckbit.Entities.Combo;
import dws.duckbit.Entities.User;


@Service
public class UserService
{
    private final ArrayList<User> userList;
    private int nextID;

// ---------- CONSTRUCTOR ---------- //

    public UserService()
    {
        this.nextID = 0;
        this.userList = new ArrayList<>();
        this.addUser("admin", "admin@duckbit.org", "admin");
        this.addUser("paco", "paco@duckbit.org", "paco");
        this.addUser("juan", "juan@duckbit.org", "juan");
    }

// ---------- GET ---------- //

    public int getSize()
    {
        return this.userList.size();
    }

    public int getIDUser(String user, String password)
    {
        for (User u: this.userList)
        {
            if (u.isUser(user, password))
            {
                return (u.getID());
            }
        }
        return (-1);
    }

    public User getByID(int ID)
    {
        if (ID >= this.getSize() || ID < 0)
        {
            return null;
        }
        return (this.userList.get(ID));
    }

// ---------- ADD AND CREATE ---------- //

    public void addUser(String user, String mail, String password)
    {
        User newUser = new User(this.nextID, user, mail, password);
        this.userList.add(newUser);
        this.nextID++;
    }

    public void addComboToUser(Combo combo, int ID)
    {
        this.userList.get(ID).addCombosToUser(combo);
    }

// ---------- CREDITS AND MONEY ---------- //

    public boolean hasEnoughCredits(int price, int ID)
    {
        return (this.userList.get(ID).hasEnoughCredits(price));
    }

    public void addCreditsToUser(int plus, int ID)
    {
        this.userList.get(ID).addCredits(plus);
    }

    public void substractCreditsToUser(int minus, int ID)
    {
        this.userList.get(ID).substractCredits(minus);
    }   

// ---------- OTHERS ---------- //

    public boolean userExists (String user)
    {
        for (User u: this.userList)
        {
            if (u.getUser().equals(user))
            {
                return (true);
            }
        }
        return (false);
    }

    public boolean IDExists(int ID)
    {
        for (User u: this.userList)
        {
            if (u.getID() == ID)
            {
                return (true);
            }
        }
        return (false);
    }

// ---------- NOT YET IMPLEMENTED ! ---------- // Change username and password

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
    }
}
