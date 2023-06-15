package tingeso.planillaservice.model;

import javax.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Acopio {
    private String fecha;
    private String turno;
    private String proveedor;
    @Column(name = "kls_leche")
    private String klsLeche;
}
