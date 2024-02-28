package dws.duckbit.services;

import dws.duckbit.Entities.Combo;
import dws.duckbit.Entities.Leak;

import java.util.HashMap;

public class LeakService {
	private static final HashMap<Integer, Leak> leaks = new HashMap<>();
	public LeakService(){}

	public void addLeak(Leak l){
		leaks.put(l.getId(), l);
	}
	public Leak getByID(int id){
		return leaks.get(id);
	}
}
