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
	public final LeakService leakService;
	private int soldCombos = 0;

// ---------- CONSTRUCTOR ---------- //

	public ComboService(ComboRepository comboRepository, LeakService leakService) throws IOException
	{
		this.comboRepository = comboRepository;
		this.leakService = leakService;
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
	public List<Combo> findByIds(List<Long> ids){
		return this.comboRepository.findAllById(ids);
	}


	public int getComboPrice(int comboID)
	{
		Optional<Combo> c = this.findById(comboID);
		if (c.isPresent())
			return c.get().getComboPrice();
		else
			return 0;
	}

	public long getComboSize()
	{
		return this.comboRepository.count();
	}

	public Collection<Combo> findAll()
	{
		return this.comboRepository.findAll();
	}

	//NOT MADE
	/*public ArrayList<Integer> getCombosIDsForEnterprise(String enterprise)
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
	}*/

	// ---------- ADD AND CREATE ---------- //
	public Combo save(Combo c)
	{
		if (c == null){
			return null;
		}
		return this.comboRepository.save(c);
	}

	public void updateSoldCombo()
	{
		this.soldCombos++;
	}

	public Combo createCombo(String name, ArrayList<Integer> leaksID, int price, String description) throws IOException
	{
		ArrayList<Leak> leaks = new ArrayList<>();
		for (int lid : leaksID)
		{
			Leak l = this.leakService.findByID(lid);
			if (l != null){
				leaks.add(l);
			}
			else{
				return  null;
			}
		}
		return new Combo(name, leaks, price, description);
	}

// ---------- DELETE AND REMOVE ---------- //


	public void delete(long id)
	{
		this.comboRepository.deleteById(id);
	}


	public void deleteLeak(Leak l)
	{
		for (Combo c : this.comboRepository.findAll())
		{
			c.deleteLeak(l);
		}
	}
}