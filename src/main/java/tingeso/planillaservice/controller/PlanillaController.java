package tingeso.planillaservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tingeso.planillaservice.entity.Planilla;
import tingeso.planillaservice.service.PlanillaService;
import java.util.List;

@RestController
@RequestMapping("/planilla")
public class PlanillaController {
    @Autowired
    PlanillaService planillaService;

    @GetMapping
    public ResponseEntity<List<Planilla>> getAll(){
        planillaService.deleteAll();
        planillaService.calcularPagoFinal();
        List<Planilla> planillas = planillaService.getAllPlanillas();
        if(planillas.isEmpty())
            return ResponseEntity.noContent().build();
        return ResponseEntity.ok(planillas);
    }
}
