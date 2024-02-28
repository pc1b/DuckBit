package dws.duckbit.Entities;

import java.util.HashMap;

public class ComboService {
	private static final HashMap<Integer, Combo> combos = new HashMap<>();
	public ComboService(){}

	public void addCombo(Combo c){
		combos.put(c.getId(), c);
	}
	public Combo getByID(int id){
		return combos.get(id);
	}
}
