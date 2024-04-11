package dws.duckbit.services;

import dws.duckbit.entities.UserD;
import dws.duckbit.repositories.ComboRepository;

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
	private final JdbcTemplate jdbcTemp;
	private int soldCombos = 0;

// ---------- CONSTRUCTOR ---------- //

	public ComboService(ComboRepository comboRepository, LeakService leakService, JdbcTemplate jdbcTemp) throws IOException
	{
		this.comboRepository = comboRepository;
		this.leakService = leakService;
		this.jdbcTemp = jdbcTemp;
	}

// ---------- GET ---------- //

	public int getSoldCombos()
	{
		return this.soldCombos;
	}

	public Optional<Combo> findById(Long id)
	{
		return this.comboRepository.findById(id);
	}
	public List<Combo> findByIds(List<Long> ids){
		return this.comboRepository.findAllById(ids);
	}
	public List<Combo> findByUser(UserD u){
		return this.comboRepository.findCombosByUserD(u);
	}


	public int getComboPrice(Long comboID)
	{
		Optional<Combo> c = this.findById(comboID);
		if (c.isPresent())
			return c.get().getPrice();
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

	public List<Combo> getAvilableCombos(){
		return this.comboRepository.findCombosByUserDNull();
	}

	@SuppressWarnings("null")
	public Collection<Combo> findAll(String enterprise, int price)
	{
		String enterprise_column = "l.enterprise";
		String price_column = "c.price";
		if (enterprise.equals(""))
		{
			enterprise_column = "'1'";
		}
		if (price <= 0)
		{
			price_column = "'1'";
		}
		String SQL = "SELECT DISTINCT c.price, c.id, c.description, c.name FROM  leak_combos cl, leak l, combo c WHERE c.id = cl.combos_id AND c.userd_id is null AND cl.leaks_id = l.id AND "+enterprise_column+" = ? AND "+price_column+" < ?";
		Collection<Combo> details = jdbcTemp.query(SQL, new PreparedStatementSetter()
		{
			public void setValues(PreparedStatement preparedStatement) throws SQLException 
			{
				if (enterprise.equals(""))
				{
					preparedStatement.setString(1, "1");
				}
				else
				{
					preparedStatement.setString(1, enterprise);
				}
				if (price <= 0)
				{
					preparedStatement.setString(2, "2");
				}
				else
				{
					preparedStatement.setInt(2, price);
				}
			}
		}, new ComboMapper());
		return details; 
	}

	public void editCombo(Combo c, String name, int price, ArrayList<Leak> leaksEdit, String description){
		List<Leak> leaks = this.leakService.findByCombo(c);
		for (Leak l : leaks){
			l.getCombos().remove(c);
			this.leakService.save(l);
		}
		for (Leak l : leaksEdit){
			l.getCombos().add(c);
			this.leakService.save(l);
		}
		c.editCombo(name, price, leaksEdit, description);
	}
	// ---------- ADD AND CREATE ---------- //
	
	public Combo save(Combo c)
	{
		if (c == null){
			return null;
		}
		Combo r = this.comboRepository.save(c);
		r.createFile(this.leakService.findByCombo(c));
		return r;
	}

	public void updateSoldCombo()
	{
		this.soldCombos++;
	}

	public Combo createCombo(String name, ArrayList<Long> leaksID, int price, String description) throws IOException
	{
		Combo c = new Combo(name, price, description);
		this.comboRepository.save(c);
		for (Long lid : leaksID)
		{
			Optional<Leak> l = this.leakService.findByID(lid);
			if (l.isPresent())
			{
				l.get().getCombos().add(c);
				this.leakService.save(l.get());
			}
			else{
				return null;
			}
		}
		this.comboRepository.save(c);
		return c;
	}

// ---------- DELETE AND REMOVE ---------- //


	public void delete(long id)
	{
		Combo c = this.comboRepository.findById(id).orElseThrow();
		List<Leak> leaks = this.leakService.findByCombo(c);
		for (Leak l : leaks){
			l.getCombos().remove(c);
			this.leakService.save(l);
		}
		this.comboRepository.deleteById(id);
	}


//	public void deleteLeak(Leak l)
//	{
//		for (Combo c : this.comboRepository.findAll())
//		{
//			c.deleteLeak(l);
//		}
//	}
}