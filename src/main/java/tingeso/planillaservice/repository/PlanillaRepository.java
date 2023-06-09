package tingeso.planillaservice.repository;

import org.springframework.stereotype.Repository;
import tingeso.planillaservice.entity.Planilla;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface PlanillaRepository extends JpaRepository<Planilla, Integer>{
}
