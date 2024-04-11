package dws.duckbit.repositories;


import dws.duckbit.entities.Combo;
import dws.duckbit.entities.Leak;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LeaksRepository extends JpaRepository<Leak, Long>
{
    boolean existsLeakByFilename(String filename);
    List<Leak> findLeaksByCombos(Combo combo);
}
