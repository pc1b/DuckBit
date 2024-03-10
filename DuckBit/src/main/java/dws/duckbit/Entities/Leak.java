package dws.duckbit.Entities;

import java.util.Calendar;


public class Leak
{
	private String enterprise;
	private Calendar date;
	private int id;

// ---------- CONSTRUCTOR ---------- //

	public Leak(String enterprise, Calendar date, int id)
	{
		this.enterprise = enterprise;
		this.date = date;
		this.id = id;
	}
	
// ---------- GET ---------- //

	public int getId()
	{
		return this.id;
	}
	
	public String getEnterprise()
	{
		return this.enterprise;
	}
	
	public String getDate()
	{
		return  this.date.get(Calendar.DAY_OF_MONTH) + "/" + (this.date.get(Calendar.MONTH) + 1) + "/" + this.date.get(Calendar.YEAR);
	}

// ---------- OTHERS ---------- //

	@Override
	public String toString()
	{
		return "Leak{" +
				"enterprise='" + enterprise + '\'' +
				", date=" + date +
				", id=" + id +
				'}';
	}
}
