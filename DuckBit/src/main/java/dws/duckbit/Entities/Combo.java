package dws.duckbit.Entities;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Combo {
	private static final Path LEAKS_FOLDER = Paths.get("src/main/resources/static/leaks");
	private static final Path COMBOS_FOLDER = Paths.get("src/main/resources/static/combo");

	private ArrayList<Leak> leaks;
	private int id;
	private int price;

	public Combo(ArrayList<Leak> leaks, int id, int price)
	{
		this.leaks = leaks;
		this.id = id;
		this.price = price;
		createCombo();
	}

	public int getId() {
		return this.id;
	}

	private void createCombo(){
		Path comboPath = COMBOS_FOLDER.resolve(this.id + ".txt");
		try {
			Files.createFile(comboPath);
		}catch (Exception e) {
			e.printStackTrace();
		}
		try(BufferedWriter writer = Files.newBufferedWriter(comboPath.toAbsolutePath())) {
			for (Leak l : this.leaks){
				writer.write("-----LEAK FROM " + l.getEnterprise() + "-----");
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
}
