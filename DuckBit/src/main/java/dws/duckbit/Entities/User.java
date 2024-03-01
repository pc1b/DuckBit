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
    private int credits = 0;

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

    public ArrayList<Combo> getCombos(){
        return this.combos;
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

    // CHANGE USER

    public void changeUserName(String user)
    {
        this.user = user;
    }

    public void changeUserPassword(String password)
    {
        try
        {
            byte[] userPassword = password.getBytes("UTF-8");
            this.md = MessageDigest.getInstance("MD5");
            this.password = md.digest(userPassword);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    // $$$$$ CREDIT AND MONEY $$$$$

    public void addCredits(int plus)
    {
        this.credits = this.credits + plus;
    }

    public void substractCredits(int minus)
    {
        this.credits = this.credits - minus;
    }

    public boolean hasEnoughCredits(int price)
    {
        return (this.credits >= price);
    }

    public int getCredits(){return this.credits;}

    @Override
    public String toString() {
        return "User{" +
                "ID=" + ID +
                ", user='" + user + '\'' +
                ", mail='" + mail + '\'' +
                ", combos=" + combos +
                ", credits=" + credits +
                '}';
    }
}
