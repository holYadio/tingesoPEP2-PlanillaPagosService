package tingeso.planillaservice.model;

import javax.persistence.*;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class Proveedor {
    private String nombre;
    private String categoria;
    private String retencion;
}
