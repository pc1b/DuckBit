package dws.duckbit.Entities;

import java.io.UnsupportedEncodingException;
import java.security.*;
import java.util.Arrays;


public class User
{
    private int ID;
    private String user;
    private String mail;
    private byte[] password;
    private MessageDigest md;


    public User(int ID, String user, String mail, String password)
    {
        try
        {
            this.ID = ID;
            this.user = user;
            this.mail = mail;
            byte[] userPassword = password.getBytes("UTF-8");
            this.password = md.digest(userPassword);
            this.md = MessageDigest.getInstance("MD5");
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
            byte[] MD5HashedPassword = md.digest(inputPassword);
            return (Arrays.equals(MD5HashedPassword, this.password));
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
            return (false);
        }
    }
}
