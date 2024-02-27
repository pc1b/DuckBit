package dws.duckbit.Entities;

import java.io.FileReader;
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
	}
	private void createCombo(){
		for (Leak l : this.leaks){
			Path leakPath = LEAKS_FOLDER.resolve(l.getId() + ".txt");
			try{
				FileReader reader = new FileReader(leakPath.toString());
			}catch (Exception e){
				System.out.println("file not found");
				
			}

		}
	}

}
