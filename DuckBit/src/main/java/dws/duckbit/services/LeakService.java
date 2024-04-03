package dws.duckbit.services;

import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;

import dws.duckbit.entities.Leak;


@Service
public class LeakService
{
	private final HashMap<Long, Leak> leaks = new HashMap<>();
	private int id = 0;
	public LeakService(){}

// ---------- CONSTRUCTOR ---------- //

	public Leak createLeak(String enterprise, String date)
	{
		try
		{
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Date fechaDate = sdf.parse(date);

			Calendar calendar = Calendar.getInstance();
			calendar.setTime(fechaDate);
			Leak l = new Leak(enterprise, calendar);
			this.id++;
			addLeak(l);
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

	public int getSize()
	{
		return this.leaks.size();
	}

	public Leak getByID(int id)
	{
		if (id >= this.getSize())
		{
			return null;
		}
		return this.leaks.get(id);
	}

	public Collection<Leak> getAll()
	{
		return this.leaks.values();
	}

// ---------- ADD AND CREATE ---------- //

	public void addLeak(Leak l)
	{
		this.leaks.put(l.getId(), l);
	}

// ---------- DELETE AND REMOVE ---------- //

	public void deleteLeak(Leak l)
	{
		this.leaks.remove(l.getId());
	}
}
