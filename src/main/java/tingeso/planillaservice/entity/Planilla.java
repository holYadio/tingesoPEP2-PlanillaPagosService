package tingeso.planillaservice.entity;

import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Planilla {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String quincena;
    private String codigoProveedor;
    private String nombreProveedor;
    private String klsTotalLeche;
    private String diasEnvioLeche;
    private String promedioKilosLecheDiario;
    private String porcentajeFrecuenciaDiariaEnvioLeche;
    private String porcentajeGrasa;
    private String porcentajeVariacionGrasa;
    private String porcentajeSolidoTotal;
    private String porcentajeVariacionSolidoTotal;
    private String pagoPorLeche;
    private String pagoPorGrasa;
    private String pagoPorSolidosTotales;
    private String bonificacionPorFrecuencia;
    private String dctoVariacionLeche;
    private String dctoVariacionGrasa;
    private String dctoVariacionST;
    private String pagoTotal;
    private String montoRetencion;
    private String montoFinal;
}
