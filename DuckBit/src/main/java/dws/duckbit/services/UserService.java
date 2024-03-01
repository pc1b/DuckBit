package dws.duckbit.services;

import dws.duckbit.Entities.Combo;
import dws.duckbit.Entities.User;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class UserService
{
    private final ArrayList<User> userList;
    private int nextID;

    public UserService()
    {
        this.nextID = 0;
        this.userList = new ArrayList<>();
    }

    public void addUser(String user, String mail, String password)
    {
        User newUser = new User(this.nextID, user, mail, password);
        this.userList.add(newUser);
        this.nextID++;
    }

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

    //If it returns 0, is the admin
    //If it return a positive number, is a user
    //If it returns a negative number, the user does not exists
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

    public int getSize(){
        return this.userList.size();
    }

    //There is no error management. If the id is invalid, it will return an Exception, and the app will die
    public User getByID(int ID)
    {
        return (this.userList.get(ID));
    }

    // CHANGE 

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

    // COMBOS

    public void addComboToUser(Combo combo, int ID)
    {
        this.userList.get(ID).addCombosToUser(combo);
    }

    // $$$$$ CREDIT AND MONEY $$$$$

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
}
