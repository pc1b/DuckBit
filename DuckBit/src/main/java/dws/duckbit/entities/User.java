package dws.duckbit.entities;

import jakarta.persistence.*;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.ArrayList;
import java.util.Arrays;

@Entity
public class User
{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id = null;
    private String user;
    private String mail;
    private byte[] password;
    @OneToMany(mappedBy = "user", cascade=CascadeType.ALL, orphanRemoval=true)
    private ArrayList<Combo> combos;
    private int credits = 0;

// ---------- CONSTRUCTOR ---------- //

    public User( String user, String mail, String password)
    {
        try
        {
            this.user = user;
            this.mail = mail;
            byte[] userPassword = password.getBytes(StandardCharsets.UTF_8);
            this.password = MessageDigest.getInstance("MD5").digest(userPassword);
            this.combos = new ArrayList<>();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    protected User() {

    }

// ---------- GET ---------- //

    public String getUser()
    {
        return (this.user);
    }

    public String getMail()
    {
        return (this.mail);
    }

    public Long getID()
    {
        return (this.id);
    }
    public void setId(Long id) {
        this.id = id;
    }


    public ArrayList<Combo> getCombos(){
        return this.combos;
    }

// ---------- ADD AND CREATE ---------- //

    public void addCombosToUser(Combo combo)
    {
        this.combos.add(combo);
    }

// ---------- CREDITS AND MONEY ---------- //

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

// ---------- OTHERS ---------- //

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
            byte[] MD5HashedPassword = MessageDigest.getInstance("MD5").digest(inputPassword);
            return (Arrays.equals(MD5HashedPassword, this.password));
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return (false);
        }
    }

    @Override
    public String toString()
    {
        return "User{" +
                "ID=" + id +
                ", user='" + user + '\'' +
                ", mail='" + mail + '\'' +
                ", combos=" + combos +
                ", credits=" + credits +
                '}';
    }

// ---------- NOT YET IMPLEMENTED ! ---------- // Change username and password
/*
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
    }*/
}
