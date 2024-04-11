package dws.duckbit.repositories;

import dws.duckbit.entities.Combo;
import dws.duckbit.entities.UserD;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComboRepository extends JpaRepository<Combo, Long> {
	List<Combo> findCombosByUserDNull();
	List<Combo> findCombosByUserD(UserD userD);
}