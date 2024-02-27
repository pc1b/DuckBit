package dws.duckbit;

import dws.duckbit.Entities.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.AbstractList;
import java.util.HashMap;
import java.util.Map;

@RestController
public class ApiControler {
	//@Autowired
	private static final AlmacenUsuarios users = new AlmacenUsuarios();
	private static final ComboControler combos = new ComboControler();
	@GetMapping("/api/user")
	public ResponseEntity<ResponseUser> getUser(@RequestParam int id) {
		User u = users.findById(id);
		if (u != null) {
			ResponseUser response = new ResponseUser(u.getID(), u.getUser(), u.getMail(), u.getCombos());
			return ResponseEntity.ok(response);
		} else {
			return ResponseEntity.notFound().build();
		}
	}
	@GetMapping("/api/combo")
	public ResponseEntity<Combo> getCombo(@RequestParam int id) {
		Combo c = combos.getByID(id);
		if (c != null) {
			return ResponseEntity.ok(c);
		} else {
			return ResponseEntity.notFound().build();
		}
	}
}
