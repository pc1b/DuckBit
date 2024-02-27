package dws.duckbit.Entities;

import java.util.HashMap;
import java.util.Map;

public class ComboControler {
	private static final HashMap<Integer, Combo> combos = new HashMap<>();
	public ComboControler(){}

	public void addCombo(Combo c){
		combos.put(c.getId(), c);
	}
	public Combo getByID(int id){
		return combos.get(id);
	}
}
