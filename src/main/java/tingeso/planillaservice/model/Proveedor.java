package tingeso.planillaservice.model;

import javax.persistence.*;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Proveedor {
    @Id
    private String codigo;
    private String nombre;
    private String categoria;
    private String retencion;
}
