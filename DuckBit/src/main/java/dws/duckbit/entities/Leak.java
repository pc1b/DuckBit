package dws.duckbit.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@Entity
public class Leak {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id = null;
	@Lob
	private String enterprise;
	private Calendar date;

	@JsonIgnore
	@ManyToMany(cascade = CascadeType.PERSIST)
	private List<Combo> combos = new ArrayList<>();
	private String filename;


// ---------- CONSTRUCTOR ---------- //

	public Leak(){}
	public Leak(String enterprise, Calendar date, String filename)
	{
		this.enterprise = enterprise;
		this.date = date;
		this.filename = filename;
	}
	
// ---------- GET ---------- //

	public Long getId()
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
	public List<Combo> getCombos() {
		return this.combos;
	}

	public String getFilename()
	{
		return this.filename;
	}

// ---------- SET ---------- //

	public void setEnterprise(String enterprise) {
		this.enterprise = enterprise;
	}

	public void setId(Long id) {
		this.id = id;
	}
	public void setCombos(List<Combo> combos) {
		this.combos = combos;
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
