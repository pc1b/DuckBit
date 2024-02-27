package dws.duckbit.Entities;

import java.io.UnsupportedEncodingException;
import java.security.*;
import java.util.ArrayList;
import java.util.Arrays;


public class User
{
    private int ID;
    private String user;
    private String mail;
    private byte[] password;
    private MessageDigest md;
    private ArrayList<Combo> combos;

    public User(int ID, String user, String mail, String password)
    {
        try
        {
            this.ID = ID;
            this.user = user;
            this.mail = mail;
            byte[] userPassword = password.getBytes("UTF-8");
            this.md = MessageDigest.getInstance("MD5");
            this.password = md.digest(userPassword);
            this.combos = new ArrayList<>();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public String getUser()
    {
        return (this.user);
    }

    public String getMail()
    {
        return (this.mail);
    }

    public int getID()
    {
        return (this.ID);
    }

    public boolean isUser(String user, String password)
    {
        return(user.equals(this.user) && this.comparePassword(password));
    }

    public boolean comparePassword(String password)
    {
        byte[] inputPassword;
        try
        {
            inputPassword = password.getBytes("UTF-8");
            byte[] MD5HashedPassword = this.md.digest(inputPassword);
            return (Arrays.equals(MD5HashedPassword, this.password));
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
            return (false);
        }
    }

    public void addCombosToUser(Combo combo)
    {
        this.combos.add(combo);
    }
}
