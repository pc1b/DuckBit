package dws.duckbit.repositories;

import dws.duckbit.entities.Combo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComboRepository extends JpaRepository<Combo, Long> {
	List<Combo> findCombosByUserDNull();
}