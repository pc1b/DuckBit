package dws.duckbit.entities;

import jakarta.persistence.*;

import java.util.Calendar;
import java.util.List;

@Entity
public class Leak {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id = null;
	private String enterprise;
	private Calendar date;
	@ManyToMany
	private List<Combo> combos;


// ---------- CONSTRUCTOR ---------- //

	public Leak(){}
	public Leak(String enterprise, Calendar date)
	{
		this.enterprise = enterprise;
		this.date = date;
	}
	
// ---------- GET ---------- //

	public Long getId()
	{
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getEnterprise()
	{
		return this.enterprise;
	}
	
	public String getDate()
	{
		return  this.date.get(Calendar.DAY_OF_MONTH) + "/" + (this.date.get(Calendar.MONTH) + 1) + "/" + this.date.get(Calendar.YEAR);
	}
	public List<Combo> getCombos() {
		return this.combos;
	}


// ---------- SET ---------- //

	public void setEnterprise(String enterprise) {
		this.enterprise = enterprise;
	}

	public void setCombos(List<Combo> comobos) {
	this.combos = comobos;
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
