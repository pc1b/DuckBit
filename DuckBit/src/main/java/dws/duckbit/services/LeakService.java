package dws.duckbit.services;

import dws.duckbit.Entities.Leak;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
@Service
public class LeakService {
	private final HashMap<Integer, Leak> leaks = new HashMap<>();
	private int id = 0;
	public LeakService(){}

	public Leak createLeak(String enterprise, String date)
	{
		try
		{
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Date fechaDate = sdf.parse(date);

			Calendar calendar = Calendar.getInstance();
			calendar.setTime(fechaDate);
			Leak l = new Leak(enterprise, calendar, this.id);
			this.id++;
			addLeak(l);
			return l;
		} catch (ParseException e)
		{
			e.printStackTrace();
		}
		return null;
	}

	public void deleteLeak(Leak l){
		this.leaks.remove(l.getId());
	}
	public void addLeak(Leak l)
	{
		this.leaks.put(l.getId(), l);
	}

	public Leak getByID(int id)
	{
		if (id >= this.getSize())
			return null;
		return this.leaks.get(id);
	}

	public int getNextId()
	{
		return this.id;
	}

	public int getSize(){
		return this.leaks.size();
	}


	public Collection<Leak> getAll(){
		return this.leaks.values();
	}
}
