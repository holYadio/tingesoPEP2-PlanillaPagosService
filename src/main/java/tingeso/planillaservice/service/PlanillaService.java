package tingeso.planillaservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Generated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tingeso.planillaservice.entity.Planilla;
import tingeso.planillaservice.model.Acopio;
import tingeso.planillaservice.model.Laboratorio;
import tingeso.planillaservice.model.Proveedor;
import tingeso.planillaservice.repository.PlanillaRepository;

import java.util.List;

@Service
public class PlanillaService {
    private final Logger logg;

    static final double IMPUESTORETENCION = 13; // 13%
    static final double LIMITERETENCION = 950000; // $950.000
    @Autowired
    PlanillaRepository planillaRepository;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    RestTemplate restTemplate;

    public PlanillaService() {
        logg = LoggerFactory.getLogger(Planilla.class);
    }

    /**
     * Obtiene todos los pagos
     * @return pagos Lista de pagos
     */
    public List<Planilla> getAllPlanillas() {
        return planillaRepository.findAll();
    }

    /**
     * Elimina los pagos en la base de datos
     */
    public void deleteAll() {
        planillaRepository.deleteAll();
    }

    /**
     * Calcula el pago por categoria de proveedor
     * @param categoriaProveedor Categoria del proveedor
     * @param klsLeche Kilos de leche
     * @return pago por categoria
     */
    public double calcularPagoPorCategoria(String categoriaProveedor, double klsLeche) {
        return switch (categoriaProveedor) {
            case "A" -> klsLeche * 700;
            case "B" -> klsLeche * 550;
            case "C" -> klsLeche * 400;
            case "D" -> klsLeche * 250;
            default -> 0;
        };
    }

    /**
     * Calcula el pago por porcentaje de Grasas
     * @param grasa cantidad de grasa del acopio de leche
     * @param klsLeche cantidad de kilos de leche del acopio
     * @return pago Cantidad que se le debe pagar al proveedor asociado al porcentaje de grasas de la leche
     */
    public double calcularPagoPorGrasas(String grasa, double klsLeche) {
        double pago = 0;
        int grasas = Integer.parseInt(grasa);
        if (grasas >= 0 && grasas <= 20) {
            pago = 30;
        }
        else if (grasas >= 21 && grasas <= 45) {
            pago = 80;
        }
        else if (grasas >= 46) {
            pago = 120;
        }
        return pago * klsLeche;
    }

    /**
     * Calcula el pago por porcentaje de Solidos
     * @param solido cantidad de solidos del acopio de leche
     * @param klsLeche cantidad de kilos de leche del acopio
     * @return pago Cantidad que se le debe pagar al proveedor asociado al porcentaje de solidos de la leche
     */
    public double calcularPagoPorSolidosTotales(String solido, double klsLeche) {
        double pagoSolido = 0;
        int solidos = Integer.parseInt(solido);
        if (solidos >= 0 && solidos <= 7) {
            pagoSolido = -130;
        }
        else if (solidos >= 8 && solidos <= 18) {
            pagoSolido = -90;
        }
        else if (solidos >= 19 && solidos <= 35) {
            pagoSolido = 95;
        }
        else if (solidos >= 36) {
            pagoSolido = 150;
        }
        return pagoSolido * klsLeche;
    }

    /**
     * Calcula el pago por porcentaje de Proteinas
     * @param datosAcopioEntity lista de objetos que tiene la información del acopio de leche
     * @param pagoAcopioQuincena Cantidad que se le debe pagar al proveedor asociado al acopio de leche
     * @return pago Cantidad que se le debe pagar al proveedor dado su frecuencia de entrega
     */
    public double calcularBonificacionPorFrecuencia(List<Acopio> datosAcopioEntity, double pagoAcopioQuincena){
        double bonificacion = 0;
        int contadorM = 0;
        int contadorT = 0;
        for (Acopio datos : datosAcopioEntity) {
            if (datos.getTurno().equals("M")) {
                contadorM++;
            }
            else if (datos.getTurno().equals("T")) {
                contadorT++;
            }
        }
        if((contadorM > 10) && (contadorT > 10)){
            bonificacion = 20;
        }
        else if(contadorM > 10){
            bonificacion = 12;
        }
        else if(contadorT > 10){
            bonificacion = 8;
        }
        return bonificacion*pagoAcopioQuincena/100;
    }

    /**
     * Calcula el descuento por variacion de leche
     * @param porcentajeVariacionLeche Porcentaje de variacion de leche
     * @param pagoAcopioLeche Cantidad que se le debe pagar al proveedor asociado al acopio de leche
     * @return descuento Cantidad que se le debe descontar al proveedor dado su variacion de leche
     */
    public double calcularDescuentoPorVariacionLeche(double porcentajeVariacionLeche, double pagoAcopioLeche) {
        double descuento=0;
        if ((porcentajeVariacionLeche >= 0) && (porcentajeVariacionLeche <= 8)) {
            descuento = 0;
        } else if ((porcentajeVariacionLeche > 9) && (porcentajeVariacionLeche <= 25)) {
            descuento = 7;
        } else if ((porcentajeVariacionLeche > 25) && (porcentajeVariacionLeche <= 45)) {
            descuento = 15;
        } else if (porcentajeVariacionLeche > 46){
            descuento = 30;
        }
        return  pagoAcopioLeche * descuento / 100;
    }

    /**
     * Calcula el descuento por variacion de grasa
     * @param porcentajeVariacionGrasa Porcentaje de variacion de grasa
     * @param pagoAcopioLeche Cantidad que se le debe pagar al proveedor asociado al acopio de leche
     * @return descuento por variacion de grasa
     */
    public double calcularDescuentoPorVariacionGrasa(double porcentajeVariacionGrasa, double pagoAcopioLeche) {
        double porcentaje = 0;
        if ((porcentajeVariacionGrasa >= 0) && (porcentajeVariacionGrasa <= 15)) {
            porcentaje = 0;
        } else if ((porcentajeVariacionGrasa > 15) && (porcentajeVariacionGrasa <= 25)) {
            porcentaje = 12;
        } else if ((porcentajeVariacionGrasa > 25) && (porcentajeVariacionGrasa <= 40)) {
            porcentaje = 20;
        } else if (porcentajeVariacionGrasa > 40){
            porcentaje = 30;
        }
        return  pagoAcopioLeche * porcentaje / 100;
    }

    /**
     * Calcula el descuento por variacion de solidos totales
     * @param porcentajeVariacionSolidoTotal Porcentaje de variacion de solidos totales
     * @param pagoAcopioLeche Cantidad que se le debe pagar al proveedor asociado al acopio de leche
     * @return descuento Cantidad que se le debe descontar al proveedor asociado al acopio de leche
     */
    public double calcularDescuentoPorVariacionSolidosTotales(double porcentajeVariacionSolidoTotal, double pagoAcopioLeche) {
        double porcentaje = 0;
        if ((porcentajeVariacionSolidoTotal >= 0) && (porcentajeVariacionSolidoTotal <= 6)) {
            porcentaje = 0;
        } else if ((porcentajeVariacionSolidoTotal > 6) && (porcentajeVariacionSolidoTotal <= 12)) {
            porcentaje = 18;
        } else if ((porcentajeVariacionSolidoTotal > 12) && (porcentajeVariacionSolidoTotal <= 35)) {
            porcentaje = 27;
        } else if (porcentajeVariacionSolidoTotal > 35){
            porcentaje = 45;
        }
        return pagoAcopioLeche * porcentaje / 100;
    }

    /**
     * Calcula la retencion de un proveedor
     * @param pago Cantidad que se le debe pagar al proveedor
     * @return retencion Cantidad que se le debe retener al proveedor
     */
    public double calcularRetencion(double pago){
        double retencion = 0;
        if (pago > LIMITERETENCION){
            retencion = pago * IMPUESTORETENCION/ 100;
        }
        return retencion;
    }

    public double klsTotalLeche(List<Acopio> acopios) {
        double kls = 0;
        for (Acopio acopio : acopios) {
            kls += Integer.parseInt(acopio.getKlsLeche());
        }
        return kls;
    }

    public double diasEnvioLeche(List<Acopio> acopios) {
        double dias = 0;
        int i = 0;
        if (acopios.size() > 1) {
            dias++;
        } else {
            while (i < (acopios.size())) {
                if (i < acopios.size() - 1) {
                    if ((acopios.get(i).getFecha().equals(acopios.get(i + 1).getFecha())) &&
                            !acopios.get(i).getTurno().equals(acopios.get(i + 1).getTurno())) {
                        i++;
                        dias++;
                    } else if (!acopios.get(i).getFecha().equals(acopios.get(i + 1).getFecha())) {
                        dias++;
                    }
                } else {
                    try {
                        if (!acopios.get(i).getFecha().equals(acopios.get(i - 1).getFecha()) ||
                                !acopios.get(i).getTurno().equals(acopios.get(i - 1).getTurno())) {
                            dias++;
                        }
                    } catch (Exception e) {
                        logg.error("Error: ", e);
                    }
                }
                i++;
            }
        }
        return dias;
    }

    public double getVariacionLeche(String quincena, String codigoProveedor, double klsTotalLeche) {
        double klsLecheAnterior;
        String quincenaAnterior = restTemplate.getForObject("http://laboratorio-service/laboratorio/lastquincena/" + quincena, String.class);
        if (quincenaAnterior == null) {
            klsLecheAnterior = klsTotalLeche;
        }else{
            List<Acopio> datosAcopioQuincena = restTemplate.getForObject("http://acopio-service/acopio/quincena/"+ quincena + "/" + codigoProveedor,List.class);
            klsLecheAnterior = klsTotalLeche(datosAcopioQuincena);

        }
        double variacion = Math.round((((klsLecheAnterior - klsTotalLeche)*100)/klsLecheAnterior)*10000)/10000.0;
        if (variacion <= 0) {
            variacion = 0;
        }
        return variacion;
    }

    @Generated
    public void calcularPagoFinal(){
        List<Laboratorio> datosLaboratorio = restTemplate.getForObject("http://laboratorio-service/laboratorio", List.class);
        String quincena;
        String codigoProveedor;
        String nombreProveedor;
        double klsTotalLeche;
        String diasEnvioLeche;
        String promedioKilosLecheDiario;
        String porcentajeFrecuenciaDiariaEnvioLeche;
        String porcentajeGrasa;
        double porcentajeVariacionGrasa;
        String porcentajeSolidoTotal;
        double porcentajeVariacionSolidoTotal;
        double pagoPorLeche;
        double pagoPorGrasa;
        double pagoPorSolidosTotales;
        double bonificacionPorFrecuencia;
        double dctoVariacionLeche;
        double dctoVariacionGrasa;
        double dctoVariacionST;
        double pagoTotal;
        double montoRetencion;
        String montoFinal;
        double pagoAcopioLeche;
        double dctoTotal;
        if (datosLaboratorio != null) {
            for (int i = 0; i < (datosLaboratorio.size()); i++) {
                quincena = datosLaboratorio.get(i).getQuincena();
                codigoProveedor = datosLaboratorio.get(i).getProveedor();
                Proveedor proveedor = restTemplate.getForObject("http://proveedor-service/proveedor/" + codigoProveedor, Proveedor.class);
                List<Acopio> datosAcopioQuincena = restTemplate.getForObject("http://acopio-service/acopio/byquincenaproveedor/" + quincena + "/" + "proveedor", List.class);
                nombreProveedor = proveedor.getNombre();
                klsTotalLeche = klsTotalLeche(datosAcopioQuincena);
                diasEnvioLeche = String.valueOf(diasEnvioLeche(datosAcopioQuincena));
                promedioKilosLecheDiario = String.valueOf(Math.round((klsTotalLeche / 15) * 1000.0) / 1000.0);
                porcentajeFrecuenciaDiariaEnvioLeche = String.valueOf(getVariacionLeche(quincena, codigoProveedor, klsTotalLeche));//Preguntar al profe
                porcentajeGrasa = datosLaboratorio.get(i).getPorcentajeGrasa();
                porcentajeVariacionGrasa = restTemplate.getForObject("http://laboratorio-service/laboratorio/getVariacionGrasa/" + quincena + "/" + codigoProveedor + "/" + porcentajeGrasa, double.class);
                porcentajeSolidoTotal = datosLaboratorio.get(i).getPorcentajeSolidoTotal();
                porcentajeVariacionSolidoTotal = restTemplate.getForObject("http://laboratorio-service/laboratorio/getVariacionSolidosTotales/" + quincena + "/" + codigoProveedor + "/" + porcentajeSolidoTotal, double.class);
                pagoPorLeche = calcularPagoPorCategoria(
                        proveedor.getCategoria(),
                        klsTotalLeche);
                pagoPorGrasa = calcularPagoPorGrasas(porcentajeGrasa,
                        klsTotalLeche);
                pagoPorSolidosTotales = calcularPagoPorSolidosTotales(porcentajeSolidoTotal,
                        klsTotalLeche);
                bonificacionPorFrecuencia = calcularBonificacionPorFrecuencia(datosAcopioQuincena,
                        pagoPorLeche);
                pagoAcopioLeche = pagoPorLeche +
                        pagoPorGrasa +
                        pagoPorSolidosTotales +
                        bonificacionPorFrecuencia;
                dctoVariacionLeche = calcularDescuentoPorVariacionLeche(Double.parseDouble(porcentajeFrecuenciaDiariaEnvioLeche),
                        pagoAcopioLeche);
                dctoVariacionGrasa = calcularDescuentoPorVariacionGrasa(porcentajeVariacionGrasa,
                        pagoAcopioLeche);
                dctoVariacionST = calcularDescuentoPorVariacionSolidosTotales(porcentajeVariacionSolidoTotal,
                        pagoAcopioLeche);
                dctoTotal = dctoVariacionLeche +
                        dctoVariacionGrasa +
                        dctoVariacionST;
                pagoTotal = pagoAcopioLeche - dctoTotal;
                montoRetencion = calcularRetencion(pagoTotal);
                montoFinal = String.valueOf(pagoTotal - montoRetencion);
                guardarPagoDB(quincena,
                        codigoProveedor,
                        nombreProveedor,
                        String.valueOf(klsTotalLeche),
                        diasEnvioLeche,
                        promedioKilosLecheDiario,
                        porcentajeFrecuenciaDiariaEnvioLeche,
                        porcentajeGrasa,
                        String.valueOf(porcentajeVariacionGrasa),
                        porcentajeSolidoTotal,
                        String.valueOf(porcentajeVariacionSolidoTotal),
                        String.valueOf(pagoPorLeche),
                        String.valueOf(pagoPorGrasa),
                        String.valueOf(pagoPorSolidosTotales),
                        String.valueOf(bonificacionPorFrecuencia),
                        String.valueOf(dctoVariacionLeche),
                        String.valueOf(dctoVariacionGrasa),
                        String.valueOf(dctoVariacionST),
                        String.valueOf(pagoTotal),
                        String.valueOf(montoRetencion),
                        montoFinal);
            }
        }
    }

    /**
     * Método que guarda el pago final en la base de datos
     * @param quincena Quincena a la que corresponde el pago
     * @param codigoProveedor Código del proveedor al que corresponde el pago
     * @param nombreProveedor Nombre del proveedor al que corresponde el pago
     * @param klsTotalLeche Kilos totales de leche entregados por el proveedor
     * @param diasEnvioLeche Días en los que el proveedor envió leche
     * @param promedioKilosLecheDiario Promedio de kilos de leche entregados por día
     * @param porcentajeFrecuenciaDiariaEnvioLeche Porcentaje de frecuencia diaria de envío de leche
     * @param porcentajeGrasa Porcentaje de grasa de la leche
     * @param porcentajeVariacionGrasa Porcentaje de variación de la grasa de la leche
     * @param porcentajeSolidoTotal Porcentaje de sólidos totales de la leche
     * @param porcentajeVariacionSolidoTotal Porcentaje de variación de los sólidos totales de la leche
     * @param pagoPorLeche Pago por leche
     * @param pagoPorGrasa Pago por grasa
     * @param pagoPorSolidosTotales Pago por sólidos totales
     * @param bonificacionPorFrecuencia Bonificación por frecuencia
     * @param dctoVariacionLeche Descuento por variación de la leche
     * @param dctoVariacionGrasa Descuento por variación de la grasa
     * @param dctoVariacionST Descuento por variación de los sólidos totales
     * @param pagoTotal Pago total
     * @param montoRetencion Monto de la retención
     * @param montoFinal Monto final
     */
    public void guardarPagoDB(String quincena,
                              String codigoProveedor,
                              String nombreProveedor,
                              String klsTotalLeche,
                              String diasEnvioLeche,
                              String promedioKilosLecheDiario,
                              String porcentajeFrecuenciaDiariaEnvioLeche,
                              String porcentajeGrasa,
                              String porcentajeVariacionGrasa,
                              String porcentajeSolidoTotal,
                              String porcentajeVariacionSolidoTotal,
                              String pagoPorLeche,
                              String pagoPorGrasa,
                              String pagoPorSolidosTotales,
                              String bonificacionPorFrecuencia,
                              String dctoVariacionLeche,
                              String dctoVariacionGrasa,
                              String dctoVariacionST,
                              String pagoTotal,
                              String montoRetencion,
                              String montoFinal){
        Planilla pago = new Planilla();
        pago.setQuincena(quincena);
        pago.setCodigoProveedor(codigoProveedor);
        pago.setNombreProveedor(nombreProveedor);
        pago.setKlsTotalLeche(klsTotalLeche);
        pago.setDiasEnvioLeche(diasEnvioLeche);
        pago.setPromedioKilosLecheDiario(promedioKilosLecheDiario);
        pago.setPorcentajeFrecuenciaDiariaEnvioLeche(porcentajeFrecuenciaDiariaEnvioLeche);
        pago.setPorcentajeGrasa(porcentajeGrasa);
        pago.setPorcentajeVariacionGrasa(porcentajeVariacionGrasa);
        pago.setPorcentajeSolidoTotal(porcentajeSolidoTotal);
        pago.setPorcentajeVariacionSolidoTotal(porcentajeVariacionSolidoTotal);
        pago.setPagoPorLeche(pagoPorLeche);
        pago.setPagoPorGrasa(pagoPorGrasa);
        pago.setPagoPorSolidosTotales(pagoPorSolidosTotales);
        pago.setBonificacionPorFrecuencia(bonificacionPorFrecuencia);
        pago.setDctoVariacionLeche(dctoVariacionLeche);
        pago.setDctoVariacionGrasa(dctoVariacionGrasa);
        pago.setDctoVariacionST(dctoVariacionST);
        pago.setPagoTotal(pagoTotal);
        pago.setMontoRetencion(montoRetencion);
        pago.setMontoFinal(montoFinal);
        createPlanilla(pago);
    }

    public void createPlanilla (Planilla planilla){
        planillaRepository.save(planilla);
    }
}
