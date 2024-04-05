package dws.duckbit.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dws.duckbit.services.UserService;
import jakarta.persistence.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Combo {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id = null;
	private static final Path LEAKS_FOLDER = Paths.get("src/main/resources/static/leaks");
	private static final Path COMBOS_FOLDER = Paths.get("src/main/resources/static/combo");

	@ManyToMany
	private List<Leak> leaks;

	private String description;
	private String name;
	private int price;
	@JsonIgnore
	@ManyToOne
	private User user;
	//private HashSet<String> enterpriseArray;

// ---------- CONSTRUCTOR ---------- //

	public Combo(){}
	public Combo(String name, ArrayList<Leak> leaks, int price, String description) throws IOException
	{
		super();
		this.name = name;
		this.description = description;
		this.leaks = leaks;
		this.price = price;

	}

// ---------- GET ---------- //

	public Long getId() {
		return this.id;
	}

	public void setId(long id){
		this.id = id;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String getDescription(){
		return this.description;
	}
	public void setDescription(String description)
	{
		this.description = description;
	}

	public void setPrice(int price)
	{
		this.price = price;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public int getComboPrice()
	{
		return this.price;
	}

	public String getName()
	{
		return this.name;
	}


// ---------- DELETE AND REMOVE ---------- //

	public void deleteLeak(Leak l)
	{
		if (this.leaks.remove(l))
		{
			try
			{
				this.createFile();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

// ---------- ADD AND CREATE ---------- //

	public void createFile()
	{
		try {
			Files.createDirectories(COMBOS_FOLDER);
		}
		catch (Exception e){
			e.printStackTrace();
		}
		Long id = this.getId();
		Path comboPath = COMBOS_FOLDER.resolve(id+ ".txt");
		try
		{
			Files.createFile(comboPath);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		try(BufferedWriter writer = Files.newBufferedWriter(comboPath.toAbsolutePath()))
		{
			for (Leak l: this.leaks)
			{
				writer.write("-----LEAK FROM " + l.getEnterprise() + " " + l.getDate() + "-----\n");
				Path leakPath = LEAKS_FOLDER.resolve(l.getId() + ".txt");
				try (BufferedReader reader = Files.newBufferedReader(leakPath.toAbsolutePath()))
				{
					String line;
					while ((line = reader.readLine()) != null)
					{
						writer.write(line);
						writer.newLine();
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

// ---------- EDIT ---------- //

	public void editCombo(String name, int price, ArrayList<Leak> leaks, String description) throws IOException
	{
		System.out.println("traceeeeeeeeeeeeeeeeeeeeeeeee");
		this.name = name;
		this.price = price;
		this.leaks = leaks;
		this.description = description;
		Path comboPath = COMBOS_FOLDER.resolve(this.id + ".txt");
		try(BufferedWriter writer = Files.newBufferedWriter(comboPath.toAbsolutePath()))
		{
			for (Leak l: this.leaks)
			{
				writer.write("-----LEAK FROM " + l.getEnterprise() + " " + l.getDate() + "-----\n");
				Path leakPath = LEAKS_FOLDER.resolve(l.getId() + ".txt");
				try (BufferedReader reader = Files.newBufferedReader(leakPath.toAbsolutePath()))
				{
					String line;
					while ((line = reader.readLine()) != null)
					{
						writer.write(line);
						writer.newLine();
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

// ---------- OHERS ---------- //

	public String leakedInfo()
	{
		Path comboPath = COMBOS_FOLDER.resolve(this.id + ".txt");
		try (BufferedReader reader = Files.newBufferedReader(comboPath.toAbsolutePath()))
		{
			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null)
			{
				sb.append(line);
				sb.append("\n");
			}
			return sb.toString();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}

	public boolean isEnterpriseInCombo(String enterprise)
	{
		for (Leak l: this.leaks)
		{
			if (enterprise.equals(l.getEnterprise()))
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public String toString()
	{
		return "Combo{" +
				"leaks=" + leaks +
				", id=" + id +
				", name='" + name + '\'' +
				", price=" + price +
				'}';
	}
}