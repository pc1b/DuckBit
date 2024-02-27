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
	public Combo(ArrayList<Leak> leaks, int id){
		this.leaks = leaks;
		this.id = id;
		createCombo();
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
				writer.write("-----LEAK FROM " + l.toString() + "-----");
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

}
