package dws.duckbit.services;

import dws.duckbit.Entities.Combo;
import dws.duckbit.Entities.Leak;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class ComboService
{
	private static HashMap<Integer, Combo> combos = new HashMap<>();
	public static int id = 0;
	public static LeakService leakService = new LeakService();
	public ComboService(){}

	public Combo createCombo(ArrayList<Integer> leaksID, int price)
	{
		ArrayList<Leak> leaks = new ArrayList<>();
		for (int lid : leaksID)
		{
			leaks.add(leakService.getByID(lid));
		}
		Combo combo = new Combo(leaks, id, price);
		id++;
		return combo;
	}

	public void addCombo(Combo c)
	{
		combos.put(c.getId(), c);
	}
	
	public Combo getByID(int id)
	{
		return combos.get(id);
	}

	public void removeByID(int id)
	{
		combos.remove(id);
	}

	public int getComboPrice(int comboID)
	{
		return (combos.get(comboID).getComboPrice());
	}

	// ENTERPRISES

	public ArrayList<Integer> getCombosIDsForEnterprise(String enterprise)
	{
		ArrayList<Integer> list = new ArrayList<>();
		Collection<Combo> listOfValues = combos.values();
		int value = 0;
		for (Combo c: listOfValues)
		{
			if (c.isEnterpriseInCombo(enterprise))
			{
				list.add(value);
			}
			value++;
		}
		return list;
	}
}
