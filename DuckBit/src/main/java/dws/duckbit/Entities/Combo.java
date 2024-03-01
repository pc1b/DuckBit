package dws.duckbit.Entities;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;

public class Combo {
	private static final Path LEAKS_FOLDER = Paths.get("src/main/resources/static/leaks");
	private static final Path COMBOS_FOLDER = Paths.get("src/main/resources/static/combo");

	private ArrayList<Leak> leaks;
	private int id;
	private String name;
	private int price;
	private HashSet<String> enterpriseArray;

	public Combo(String name, ArrayList<Leak> leaks, int id, int price) throws IOException {
		this.name = name;
		this.leaks = leaks;
		this.id = id;
		this.price = price;
		createCombo();
		enterpriseArray = new HashSet<>();
		for (Leak l: leaks)
		{
			this.enterpriseArray.add(l.getEnterprise());
		}
	}

	public void deleteLeak(Leak l){
		this.leaks.remove(l);
		try{
			this.createCombo();
		}catch (Exception e){
			e.printStackTrace();
		}

	}
	public int getId() {
		return this.id;
	}

	private void createCombo() throws IOException {
		Files.createDirectories(COMBOS_FOLDER);
		Path comboPath = COMBOS_FOLDER.resolve(this.id + ".txt");
		try {
			Files.createFile(comboPath);
		}catch (Exception e) {
			e.printStackTrace();
		}
		try(BufferedWriter writer = Files.newBufferedWriter(comboPath.toAbsolutePath())) {
			for (Leak l : this.leaks){
				writer.write("-----LEAK FROM " + l.getEnterprise() + " " + l.getDate() + "-----\n");
				Path leakPath = LEAKS_FOLDER.resolve(l.getId() + ".txt");
				try (BufferedReader reader = Files.newBufferedReader(leakPath.toAbsolutePath())) {
					String line;
					while ((line = reader.readLine()) != null) {
						writer.write(line);
						writer.newLine();
					}
				}catch (Exception e) {
					e.printStackTrace();
				}
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getLeakedInfo(){
		Path comboPath = COMBOS_FOLDER.resolve(this.id + ".txt");
		try (BufferedReader reader = Files.newBufferedReader(comboPath.toAbsolutePath())) {
			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				sb.append(line);
				sb.append("\n");
			}
			return sb.toString();
		}catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public int getComboPrice()
	{
		return this.price;
	}

	public String getName(){
		return this.name;
	}

	// ENTERPRISE

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
	public String toString() {
		return "Combo{" +
				"leaks=" + leaks +
				", id=" + id +
				", name='" + name + '\'' +
				", price=" + price +
				", enterpriseArray=" + enterpriseArray +
				'}';
	}
}
