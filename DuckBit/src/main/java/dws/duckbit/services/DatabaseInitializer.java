package dws.duckbit.services;

import dws.duckbit.entities.Combo;
import dws.duckbit.entities.Leak;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;

@Component
public class DatabaseInitializer {

	@Autowired
	private LeakService leakService;

	@Autowired
	private ComboService comboService;

	@PostConstruct
	public void init() throws IOException {

		this.leakService.save(this.leakService.createLeak("Orange", "2024-10-8"));
		this.leakService.save(this.leakService.createLeak("URJC", "2024-10-8"));
		this.leakService.save(this.leakService.createLeak("Amazon", "2024-10-8"));

		ArrayList<Integer> id = new ArrayList<>();
		id.add(3);
		id.add(2);
		this.comboService.save(this.comboService.createCombo("Combo1", id, 30, "<p>El mejor <strong>Combo </strong>que vas a ver en tu <strong><em>vida</em></strong>, asi es como te lo <em>digo</em>!!!!</p>"));
		id.remove(1);
		id.add(1);
		this.comboService.save(this.comboService.createCombo("Combo2", id, 40, "<p>El segundo mejor <strong>Combo </strong>que vas a ver en tu <strong><em>vida</em></strong>, asi es como te lo <em>digo</em>!!!!</p>"));


	}

}
