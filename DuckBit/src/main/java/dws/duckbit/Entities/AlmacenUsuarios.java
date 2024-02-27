package dws.duckbit.Entities;

import java.util.ArrayList;

public class AlmacenUsuarios
{
    private ArrayList<User> userList;
    private int nextID;

    public AlmacenUsuarios()
    {
        this.nextID = 0;
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


}
