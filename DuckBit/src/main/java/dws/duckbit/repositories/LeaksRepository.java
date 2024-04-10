package dws.duckbit.repositories;


import dws.duckbit.entities.Leak;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeaksRepository extends JpaRepository<Leak, Long> {
    boolean existsLeakByFilename(String filename);
}
