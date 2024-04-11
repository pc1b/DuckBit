package dws.duckbit.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.sql.Blob;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Entity
public class UserD
{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id = null;
    private String userd;
    private String mail;
    private byte[] password;
    @JsonIgnore
    @OneToMany
    private List<Combo> combos = new ArrayList<>();
    @Lob
    @JsonIgnore
    private Blob imageFile;
    private int credits = 0;

// ---------- CONSTRUCTOR ---------- //

    public UserD(String userd, String mail, String password)
    {
        try
        {
            this.userd = userd;
            this.mail = mail;
            byte[] userPassword = password.getBytes(StandardCharsets.UTF_8);
            this.password = MessageDigest.getInstance("MD5").digest(userPassword);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    protected UserD() {

    }

// ---------- GET AND SET ---------- //

    public String getUserd()
    {
        return (this.userd);
    }


    public Blob getImageFile() {
        return imageFile;
    }

    public void setImageFile(Blob image) {
        this.imageFile = image;
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


    public void setCombos(ArrayList<Combo> combos) {
        this.combos = combos;
    }

    public List<Combo> getCombos(){
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
        return(user.equals(this.userd) && this.comparePassword(password));
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
                ", user='" + userd + '\'' +
                ", mail='" + mail + '\'' +
                ", combos=" + combos +
                ", credits=" + credits +
                '}';
    }

}
