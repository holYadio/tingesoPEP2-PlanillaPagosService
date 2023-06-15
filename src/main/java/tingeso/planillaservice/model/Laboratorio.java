package tingeso.planillaservice.model;

import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Laboratorio {
    private String proveedor;
    @Column(name = "porcentaje_grasa")
    private String porcentajeGrasa;
    @Column(name = "porcentaje_solido_total")
    private String porcentajeSolidoTotal;
    private String quincena;
}
