package dws.duckbit.Entities;

import java.util.ArrayList;

public class ResponseUser {
	private int id;
	private String user;
	private String mail;
	private ArrayList<Combo> combos;

	public ResponseUser(int id, String user, String mail, ArrayList<Combo> combos) {
		this.id = id;
		this.user = user;
		this.mail = mail;
		this.combos = combos;
	}
}
