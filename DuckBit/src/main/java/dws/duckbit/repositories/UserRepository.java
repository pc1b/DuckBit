package dws.duckbit.repositories;

import dws.duckbit.entities.UserD;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserD, Long> {
	Optional<UserD> findByUserd(String userd);
}
