package dws.duckbit.services;

import dws.duckbit.entities.Combo;
import dws.duckbit.entities.Leak;
import dws.duckbit.entities.UserD;
import jakarta.annotation.PostConstruct;

import org.hibernate.engine.jdbc.BlobProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Optional;

@Component
public class DatabaseInitializer {

	@Autowired
	private LeakService leakService;
	private final Path IMAGES_FOLDER = Paths.get("src/main/resources/static/images");

	@Autowired
	private ComboService comboService;
	@Autowired
	private UserService userService;

	@PostConstruct
	public void init() throws IOException {
		Leak l1 = this.leakService.createLeak("Orange", "2024-10-8", "1.txt");
		Leak l2 = this.leakService.createLeak("URJC", "2024-10-8", "2.txt");
		Leak l3 = this.leakService.createLeak("Amazon", "2024-10-8", "3.txt");
		this.leakService.save(l1);
		this.leakService.save(l2);
		this.leakService.save(l3);

		Combo c1 = new Combo("Combo1", 30, "<p>El mejor <strong>Combo </strong>que vas a ver en tu <strong><em>vida</em></strong>, asi es como te lo <em>digo</em>!!!!</p>");
		Combo c2 = new Combo("Combo2",  40, "<p>El segundo ejor <strong>Combo </strong>que vas a ver en tu <strong><em>vida</em></strong>, asi es como te lo <em>digo</em>!!!!</p>");

		this.comboService.save(c1);
		this.comboService.save(c2);
		l1.getCombos().add(c1);
		l2.getCombos().add(c2);
		l3.getCombos().add(c1);
		l3.getCombos().add(c2);

		this.leakService.save(l1);
		this.leakService.save(l2);
		this.leakService.save(l3);
		this.comboService.save(c1);
		this.comboService.save(c2);



		this.userService.addUser("admin", "admin@duckbit.org", "admin");
		this.userService.addUser("paco", "paco@duckbit.org", "paco");
		this.userService.addUser("juan", "juan@duckbit.org", "juan");

		for (int i = 1; i<=3; i++){
			UserD u =  this.userService.findByID((long)i).orElseThrow();
			Path imagePath;
			if (i == 1){
				imagePath = IMAGES_FOLDER.resolve("admin.jpg");
			}
			else{
				imagePath = IMAGES_FOLDER.resolve("logo.jpg");
			}

			Resource image = new UrlResource(imagePath.toUri());
			u.setImageFile(BlobProxy.generateProxy(image.getInputStream(), image.getFile().length()));
			this.userService.save(u);
		}


	}

}
