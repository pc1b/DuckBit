package dws.duckbit.services;

import dws.duckbit.Entities.Combo;
import dws.duckbit.Entities.Leak;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class ComboService {
	private static HashMap<Integer, Combo> combos = new HashMap<>();
	public static int id = 0;
	public static LeakService leakService = new LeakService();
	public ComboService(){}

	public Combo createCombo(ArrayList<Integer> leaksID){
		ArrayList<Leak> leaks = new ArrayList<>();
		for (int lid : leaksID){
			leaks.add(leakService.getByID(lid));
		}
		Combo combo = new Combo(leaks, id);
		id++;
		return combo;
	}
	public void addCombo(Combo c){
		combos.put(c.getId(), c);
	}
	public Combo getByID(int id){
		return combos.get(id);
	}
}
