package tingeso.planillaservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Generated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import tingeso.planillaservice.entity.Planilla;
import tingeso.planillaservice.model.Acopio;
import tingeso.planillaservice.model.Laboratorio;
import tingeso.planillaservice.model.Proveedor;
import tingeso.planillaservice.repository.PlanillaRepository;

import java.util.ArrayList;
import java.util.Collections;
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
        if (acopios.size() == 1) {
            dias++;
        } else if (acopios.size() > 1) {
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

    public List<Laboratorio> getLaboratorios() {
        ResponseEntity<List<Laboratorio>> response = restTemplate.exchange(
                "http://laboratorio-service/laboratorio",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Laboratorio>>() {}
        );
        List<Laboratorio> datosLaboratorio = response.getBody();
        return datosLaboratorio;
    }



    public double getVariacionLeche(String quincena, String codigoProveedor, double klsTotalLeche) {
        double klsLecheAnterior;
        String quincenaAnterior = getLastQuincena(quincena);
        if (quincenaAnterior.isEmpty()) {
            klsLecheAnterior = klsTotalLeche;
        }else{
            System.out.println("quincenaAnterior: "+quincena);
            List<Acopio> datosAcopioQuincena = getAcopios(quincenaAnterior, codigoProveedor);
            if(datosAcopioQuincena.isEmpty()){
                klsLecheAnterior = klsTotalLeche;
            }else {
                klsLecheAnterior = klsTotalLeche(datosAcopioQuincena);
            }

        }
        double variacion = Math.round((((klsLecheAnterior - klsTotalLeche)*100)/klsLecheAnterior)*10000)/10000.0;
        if (variacion <= 0) {
            variacion = 0;
        }
        return variacion;
    }


    public Proveedor getProveedorModel(String codigoProveedor) {
        Proveedor proveedor = restTemplate.getForObject("http://proveedor-service/proveedor/" + codigoProveedor, Proveedor.class);
        return proveedor;
    }

    public List<Acopio> getAcopios(String quincena, String codigoProveedor) {
        String url = "http://acopio-service/acopio/byquincenaproveedor/?quincena={quincena}&proveedor={proveedor}";

        try {
            ResponseEntity<List<Acopio>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<Acopio>>() {},
                    quincena,
                    codigoProveedor
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            } else {
                throw new RuntimeException("Error al realizar la solicitud: " + response.getStatusCode());
            }
        } catch (HttpClientErrorException.NotFound e) {
            return Collections.emptyList();
        }
    }

    public double getPorcentajeVariacionGrasa(String quincena, String codigoProveedor, String porcentajeGrasa) {
        String url = "http://laboratorio-service/laboratorio/getVariacionGrasa/?quincena={quincena}&codigoProveedor={codigoProveedor}&porcentajeGrasa={porcentajeGrasa}";

        ResponseEntity<Double> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                Double.class,
                quincena,
                codigoProveedor,
                porcentajeGrasa
        );
        double variacion = response.getBody();
        return variacion;
    }

    public double getPorcentajeVariacionST(String quincena, String codigoProveedor, String porcentajeSolidoTotal) {
        String url = "http://laboratorio-service/laboratorio/getVariacionSolidosTotales/?quincena={quincena}&codigoProveedor={codigoProveedor}&porcentajeSolidoTOtal={porcentajeSolidoTotal}";

        ResponseEntity<Double> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                Double.class,
                quincena,
                codigoProveedor,
                porcentajeSolidoTotal
        );
        double variacion = response.getBody();
        return variacion;
    }

    public String getLastQuincena(String quincena){
        String quincenaAnterior = "";
        String anioActual= quincena.split("/")[0];
        String mesActual= quincena.split("/")[1];
        String qActual= quincena.split("/")[2];
        if(qActual.equals("Q1")){
            if(mesActual.equals("01")){
                anioActual = Integer.toString(Integer.parseInt(anioActual) - 1);
                mesActual = "12";
            }
            else{
                mesActual = Integer.toString(Integer.parseInt(mesActual));
                if ((mesActual.length() == 1) || (mesActual.equals("10"))) {
                    mesActual = Integer.toString(Integer.parseInt(mesActual) - 1);
                    mesActual = "0" + mesActual;
                }else{
                    mesActual = Integer.toString(Integer.parseInt(mesActual) - 1);
                }
            }
            quincenaAnterior = anioActual + "/" + mesActual + "/" + "Q2";
        }
        else if(qActual.equals("Q2")){
            quincenaAnterior = anioActual + "/" + mesActual + "/" + "Q1";
        }
        return quincenaAnterior;
    }

    public void calcularPagoFinal(){
        List<Laboratorio> datosLaboratorio = getLaboratorios();
        if (datosLaboratorio != null) {
            int i = 0;
            for (int j = 0; j < datosLaboratorio.size(); j++){
                Laboratorio newLaboratorio = new Laboratorio();
                newLaboratorio.setQuincena(datosLaboratorio.get(j).getQuincena());
                newLaboratorio.setProveedor(datosLaboratorio.get(j).getProveedor());
                newLaboratorio.setPorcentajeGrasa(datosLaboratorio.get(j).getPorcentajeGrasa());
                newLaboratorio.setPorcentajeSolidoTotal(datosLaboratorio.get(j).getPorcentajeSolidoTotal());
                calcularPagoQuincena(newLaboratorio);
                i++;
            }
        }
    }

    public void calcularPagoQuincena(Laboratorio laboratorio){
        Planilla newPlanilla = new Planilla();
        String quincena = laboratorio.getQuincena();
        String codigoProveedor = laboratorio.getProveedor();
        Proveedor proveedor = getProveedorModel(codigoProveedor);
        List<Acopio> datosAcopioQuincena = getAcopios(quincena, codigoProveedor);
        String nombreProveedor =proveedor.getNombre();
        double klsTotalLeche = klsTotalLeche(datosAcopioQuincena);
        String diasEnvioLeche = String.valueOf(diasEnvioLeche(datosAcopioQuincena));
        String promedioKilosLecheDiario = String.valueOf(Math.round((klsTotalLeche / 15) * 1000.0) / 1000.0);
        String porcentajeFrecuenciaDiariaEnvioLeche = String.valueOf(getVariacionLeche(quincena, codigoProveedor, klsTotalLeche));
        String porcentajeGrasa = laboratorio.getPorcentajeGrasa();
        double porcentajeVariacionGrasa = getPorcentajeVariacionGrasa(quincena, codigoProveedor, porcentajeGrasa);
        String porcentajeSolidoTotal = laboratorio.getPorcentajeSolidoTotal();
        double porcentajeVariacionSolidoTotal = getPorcentajeVariacionST(quincena, codigoProveedor, porcentajeSolidoTotal);
        double pagoPorLeche = calcularPagoPorCategoria(getProveedorModel(codigoProveedor).getCategoria(), klsTotalLeche);
        double pagoPorGrasa = calcularPagoPorGrasas(porcentajeGrasa,klsTotalLeche);
        double pagoPorSolidosTotales = calcularPagoPorSolidosTotales(porcentajeSolidoTotal,klsTotalLeche);
        double bonificacionPorFrecuencia = calcularBonificacionPorFrecuencia(datosAcopioQuincena,pagoPorLeche);
        double pagoAcopioLeche = pagoPorLeche + pagoPorGrasa + pagoPorSolidosTotales + bonificacionPorFrecuencia;
        double dctoVariacionLeche = calcularDescuentoPorVariacionLeche(Double.parseDouble(porcentajeFrecuenciaDiariaEnvioLeche),pagoAcopioLeche);
        double dctoVariacionGrasa = calcularDescuentoPorVariacionGrasa(porcentajeVariacionGrasa,pagoAcopioLeche);
        double dctoVariacionST = calcularDescuentoPorVariacionSolidosTotales(porcentajeVariacionSolidoTotal,pagoAcopioLeche);
        double dctoTotal = dctoVariacionLeche + dctoVariacionGrasa + dctoVariacionST;
        double pagoTotal = pagoAcopioLeche - dctoTotal;
        double montoRetencion = calcularRetencion(pagoTotal);
        String montoFinal = String.valueOf(pagoTotal - montoRetencion);

        newPlanilla.setQuincena(quincena);
        newPlanilla.setCodigoProveedor(codigoProveedor);
        newPlanilla.setNombreProveedor(nombreProveedor);
        newPlanilla.setKlsTotalLeche(String.valueOf(klsTotalLeche));
        newPlanilla.setDiasEnvioLeche(diasEnvioLeche);
        newPlanilla.setPromedioKilosLecheDiario(promedioKilosLecheDiario);
        newPlanilla.setPorcentajeFrecuenciaDiariaEnvioLeche(porcentajeFrecuenciaDiariaEnvioLeche);
        newPlanilla.setPorcentajeGrasa(porcentajeGrasa);
        newPlanilla.setPorcentajeVariacionGrasa(String.valueOf(porcentajeVariacionGrasa));
        newPlanilla.setPorcentajeSolidoTotal(porcentajeSolidoTotal);
        newPlanilla.setPorcentajeVariacionSolidoTotal(String.valueOf(porcentajeVariacionSolidoTotal));
        newPlanilla.setPagoPorLeche(String.valueOf(pagoPorLeche));
        newPlanilla.setPagoPorGrasa(String.valueOf(pagoPorGrasa));
        newPlanilla.setPagoPorSolidosTotales(String.valueOf(pagoPorSolidosTotales));
        newPlanilla.setBonificacionPorFrecuencia(String.valueOf(bonificacionPorFrecuencia));
        newPlanilla.setDctoVariacionLeche(String.valueOf(dctoVariacionLeche));
        newPlanilla.setDctoVariacionGrasa(String.valueOf(dctoVariacionGrasa));
        newPlanilla.setDctoVariacionST(String.valueOf(dctoVariacionST));
        newPlanilla.setPagoTotal(String.valueOf(pagoTotal));
        newPlanilla.setMontoRetencion(String.valueOf(montoRetencion));
        newPlanilla.setMontoFinal(montoFinal);
        planillaRepository.save(newPlanilla);
    }
}
