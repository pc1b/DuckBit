package dws.duckbit.services;

import dws.duckbit.Entities.Combo;
import dws.duckbit.Entities.Leak;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

@Service
public class ComboService
{
	private HashMap<Integer, Combo> combos = new HashMap<>();
	public int id = 0;
	public final LeakService leakService;

	public ComboService(LeakService leakService) {
		this.leakService = leakService;
	}

	public Combo createCombo(String name, ArrayList<Integer> leaksID, int price) throws IOException {
		ArrayList<Leak> leaks = new ArrayList<>();
		for (int lid : leaksID)
		{
			leaks.add(leakService.getByID(lid));
		}
		Combo combo = new Combo(name, leaks, id, price);
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

	public int getComboSize(){
		return combos.size();
	}

	public Collection<Combo> getAll(){
		return combos.values();
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
