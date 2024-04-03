package dws.duckbit.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.util.Calendar;

@Entity
public class Leak {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id = null;
	private String enterprise;
	private Calendar date;


// ---------- CONSTRUCTOR ---------- //

	public Leak(){}
	public Leak(String enterprise, Calendar date)
	{
		this.enterprise = enterprise;
		this.date = date;
	}
	
// ---------- GET ---------- //

	public long getId()
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
