package dws.duckbit.services;

import dws.duckbit.repositories.ComboRepository;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

import dws.duckbit.entities.Combo;
import dws.duckbit.entities.Leak;


@Service
public class ComboService
{
	private final ComboRepository comboRepository;
	private HashMap<Long, Combo> combos = new HashMap<>();
	public int id = 0;
	public final LeakService leakService;
	private int soldCombos = 0;

// ---------- CONSTRUCTOR ---------- //
	
	public ComboService(ComboRepository comboRepository, LeakService leakService) throws IOException
	{
		this.comboRepository = comboRepository;
		this.leakService = leakService;
		this.leakService.addLeak(this.leakService.createLeak("Orange", "2024-10-8"));
		this.leakService.addLeak(this.leakService.createLeak("URJC", "2024-10-8"));
		this.leakService.addLeak(this.leakService.createLeak("Amazon", "2024-10-8"));
		ArrayList<Integer> id = new ArrayList<>();
		id.add(0);
		id.add(2);
		this.save(this.createCombo("Combo1", id, 30));
		id.remove(0);
		id.add(1);
		this.save(this.createCombo("Combo2", id, 40));

	}


	public List<Combo> findByIds(List<Long> ids){
		return this.comboRepository.findAllById(ids);
	}

	/*public boolean exist(long id) {
		return shopRepository.existsById(id);
	}*/





// ---------- GET ---------- //

	public int getSoldCombos()
	{
		return this.soldCombos;
	}
	
	public Optional<Combo>  findById(int id)
	{
		return this.comboRepository.findById((long)id);
	}

	public int getComboPrice(int comboID)
	{
		return (this.combos.get(comboID).getComboPrice());
	}

	public int getComboSize()
	{
		return this.combos.size();
	}

	public Collection<Combo> findAll()
	{
		return this.comboRepository.findAll();
	}

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

// ---------- ADD AND CREATE ---------- //
	public Combo save(Combo c)
	{
		return this.comboRepository.save(c);
	}

	public void updateSoldCombo()
	{
		this.soldCombos++;
	}

	public Combo createCombo(String name, ArrayList<Integer> leaksID, int price) throws IOException
	{
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
		Combo combo = new Combo(name, leaks, price);
		this.id++;
		return combo;
	}

// ---------- DELETE AND REMOVE ---------- //


	public void delete(int id)
	{
		this.comboRepository.deleteById((long)id);
	}

	public void removeByID(int id)
	{
		this.combos.remove(id);
	}

	public void deleteLeak(Leak l)
	{
		for (Combo c : this.combos.values())
		{
			c.deleteLeak(l);
		}
	}
}
