package dws.duckbit.services;

import dws.duckbit.Entities.User;

import java.util.ArrayList;

public class UserService
{
    private ArrayList<User> userList;
    private int nextID;

    public UserService()
    {
        this.nextID = 0;
        userList = new ArrayList<>();
    }

    public void addUser(String user, String mail, String password)
    {
        User newUser = new User(this.nextID, user, mail, password);
        this.userList.add(newUser);
        this.nextID++;
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

    //There is no error management. If the id is invalid, it will return an Exception, and the app will die
    public User getByID(int ID)
    {
        return (this.userList.get(ID));
    }
}
