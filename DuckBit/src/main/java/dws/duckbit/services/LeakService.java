package dws.duckbit.services;

import dws.duckbit.repositories.LeaksRepository;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import dws.duckbit.entities.Leak;


@Service
public class LeakService
{
	private final LeaksRepository leaksRepository;
	//private final ComboService comboService;
	private int id = 0;
	public LeakService(LeaksRepository leaksRepository){
		this.leaksRepository = leaksRepository;
	}

// ---------- CONSTRUCTOR ---------- //

	public Leak createLeak(String enterprise, String date, String filename)
	{
		try
		{
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Date fechaDate = sdf.parse(date);

			Calendar calendar = Calendar.getInstance();
			calendar.setTime(fechaDate);
			Leak l = new Leak(enterprise, calendar, filename);
			this.id++;
			save(l);
			return l;
		}
		catch (ParseException e)
		{
			e.printStackTrace();
		}
		return null;
	}

// ---------- GET ---------- //

	public int getNextId()
	{
		return this.id;
	}

	public long getSize()
	{
		return this.leaksRepository.count();
	}

	public Optional<Leak> findByID(long id)
	{
		return this.leaksRepository.findById(id);
	}

	public List<Leak> findAll()
	{
		return this.leaksRepository.findAll();
	}

// ---------- ADD AND CREATE ---------- //

	public void save(Leak l)
	{
		this.leaksRepository.save(l);
	}

// ---------- DELETE AND REMOVE ---------- //

	public void delete(Leak l)
	{
		this.leaksRepository.delete(l);
	}
}
