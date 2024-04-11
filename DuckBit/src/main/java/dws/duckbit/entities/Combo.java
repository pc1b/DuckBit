package dws.duckbit.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

	private String name;
	private int price;
	private String description;
	@ManyToMany(mappedBy = "combos")
	private List<Leak> leaks = new ArrayList<>();
	@JsonIgnore
	@ManyToOne
	private UserD userD;

// ---------- BUILDER ---------- //

	public Combo(){}
	public Combo(String name,  int price, String description) throws IOException
	{
		super();
		this.name = name;
		this.description = description;
		this.price = price;

	}

// ---------- GET ---------- //

	public Long getId() {
		return this.id;
	}

	public int getPrice()
	{
		return this.price;
	}

	public String getName()
	{
		return this.name;
	}
	public UserD getUser() {
		return userD;
	}

	public List<Leak> getLeaks()
	{
		return this.leaks;
	}

	public String getDescription(){
		return this.description;
	}

// ---------- SET ---------- //

	public void setDescription(String description)
	{
		this.description = description;
	}

	public void setId(long id){
		this.id = id;
	}

	public void setUser(UserD userD) {
		this.userD = userD;
	}

	public void setPrice(int price)
	{
		this.price = price;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public void setLeaks(List<Leak> leaks)
	{
		this.leaks = leaks;
	}



// ---------- ADD AND CREATE ---------- //

	public void createFile(List<Leak> leaks)
	{
		try {
			Files.createDirectories(COMBOS_FOLDER);
		}
		catch (Exception e){
			e.printStackTrace();
		}
		Long id = this.getId();
		Path comboPath = COMBOS_FOLDER.resolve(id+ ".txt");
		if(comboPath.toFile().exists()){
			comboPath.toFile().delete();
		}
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

			for (Leak l: leaks)
			{
				writer.write("-----LEAK FROM " + l.getEnterprise() + " " + l.getDate() + "-----\n");
				Path leakPath = LEAKS_FOLDER.resolve(l.getFilename());
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
	public void addLeak(Leak l)
	{
		this.leaks.add(l);
	}

// ---------- EDIT ---------- //

	public void editCombo(String name, int price, ArrayList<Leak> leaks, String description)
	{
		this.name = name;
		this.price = price;
		this.leaks = leaks;
		this.description = description;
		Path comboPath = COMBOS_FOLDER.resolve(this.id + ".txt");
		if (comboPath.toFile().exists()){
			comboPath.toFile().delete();
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

	@Override
	public String toString() {
		return "Combo{" +
				"id=" + id +
				", name='" + name + '\'' +
				", description='" + description + '\'' +
				", price=" + price +
				", leaks=" + leaks +
				", userD=" + userD +
				'}';
	}
}