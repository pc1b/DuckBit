package dws.duckbit.services;

import dws.duckbit.repositories.ComboRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

import dws.duckbit.entities.Combo;
import dws.duckbit.entities.Leak;

import org.springframework.jdbc.core.JdbcTemplate; 

@Service
public class ComboService
{
	private final ComboRepository comboRepository;
	public final LeakService leakService;

	@Autowired
	private JdbcTemplate jdbcTemp;
	private int soldCombos = 0;

// ---------- CONSTRUCTOR ---------- //

	public ComboService(ComboRepository comboRepository, LeakService leakService) throws IOException
	{
		this.comboRepository = comboRepository;
		this.leakService = leakService;
	}

// ---------- GET ---------- //

	public int getSoldCombos()
	{
		return this.soldCombos;
	}

	public Optional<Combo> findById(int id)
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

	@SuppressWarnings("null")
	public Collection<Combo> findAll(String enterprise, int price, int leaksNumber)
	{
		final String SQL = "SELECT * FROM COMBO where price < ?"; 
		Collection<Combo> details = jdbcTemp.query(SQL, new PreparedStatementSetter()
		{
			public void setValues(PreparedStatement preparedStatement) throws SQLException 
			{
				preparedStatement.setInt(1, price); 
			}
		}, new ComboMapper()); 
		return details; 
	}

	// ---------- ADD AND CREATE ---------- //
	
	public Combo save(Combo c)
	{
		if (c == null){
			return null;
		}
		Combo r = this.comboRepository.save(c);
		r.createFile();
		return r;
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