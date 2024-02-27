package dws.duckbit.Entities;
import java.util.Calendar;

public class Leak {
	private String enterprise;
	private Calendar date;
	private int id;
	public Leak(String enterprise, Calendar date, int id) {
		this.enterprise = enterprise;
		this.date = date;
		this.id = id;
	}
	public int getId(){
		return this.id;
	}
}
