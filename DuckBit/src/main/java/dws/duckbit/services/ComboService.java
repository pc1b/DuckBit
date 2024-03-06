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
	private int soldCombos = 0;

	public ComboService(LeakService leakService) throws IOException{
		this.leakService = leakService;
		this.leakService.addLeak(this.leakService.createLeak("Orange", "2024-10-8"));
		this.leakService.addLeak(this.leakService.createLeak("URJC", "2024-10-8"));
		this.leakService.addLeak(this.leakService.createLeak("Amazon", "2024-10-8"));
		ArrayList<Integer> id = new ArrayList<>();
		id.add(0);
		id.add(2);
		this.addCombo(this.createCombo("Combo1", id, 30));
		id.remove(0);
		id.add(1);
		this.addCombo(this.createCombo("Combo2", id, 40));

	}

	public void deleteCombo(Combo c){
		this.combos.remove(c.getId());
	}
	public int getSoldCombos() {
		return this.soldCombos;
	}

	public void updateSoldCombo(){
		this.soldCombos++;
	}

	public Combo createCombo(String name, ArrayList<Integer> leaksID, int price) throws IOException {
		ArrayList<Leak> leaks = new ArrayList<>();
		for (int lid : leaksID)
		{
			Leak l = this.leakService.getByID(lid);
			if (l != null){
				leaks.add(l);
			}
			else{
				return  null;
			}
		}
		Combo combo = new Combo(name, leaks, this.id, price);
		this.id++;
		return combo;
	}

	public void deleteCombo(int id)
	{
		this.combos.remove(id);
	}

	public void deleteLeak(Leak l){
		for (Combo c : this.combos.values()){
			c.deleteLeak(l);
		}
	}
	public void addCombo(Combo c)
	{
		this.combos.put(c.getId(), c);
	}
	
	public Combo getByID(int id)
	{
		return this.combos.get(id);
	}

	public void removeByID(int id)
	{
		this.combos.remove(id);
	}

	public int getComboPrice(int comboID)
	{
		return (this.combos.get(comboID).getComboPrice());
	}

	public int getComboSize(){
		return this.combos.size();
	}

	public Collection<Combo> getAll(){
		return this.combos.values();
	}

	// ENTERPRISES

	public ArrayList<Integer> getCombosIDsForEnterprise(String enterprise)
	{
		ArrayList<Integer> list = new ArrayList<>();
		Collection<Combo> listOfValues = this.combos.values();
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
