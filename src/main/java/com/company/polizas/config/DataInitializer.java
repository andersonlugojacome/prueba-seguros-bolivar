package com.company.polizas.config;

import com.company.polizas.domain.EstadoPoliza;
import com.company.polizas.domain.EstadoRiesgo;
import com.company.polizas.domain.Poliza;
import com.company.polizas.domain.Riesgo;
import com.company.polizas.domain.TipoPoliza;
import com.company.polizas.repository.PolizaRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final PolizaRepository polizaRepository;

    public DataInitializer(PolizaRepository polizaRepository) {
        this.polizaRepository = polizaRepository;
    }

    @Override
    public void run(String... args) {
        if (polizaRepository.count() > 0) {
            return;
        }

        Poliza individual = new Poliza();
        individual.setTipo(TipoPoliza.INDIVIDUAL);
        individual.setEstado(EstadoPoliza.VIGENTE);
        individual.setVigenciaMeses(12);
        individual.setFechaInicio(LocalDate.now().minusMonths(1));
        individual.setFechaFin(LocalDate.now().plusMonths(11));
        individual.setCanonMensual(new BigDecimal("1000.00"));
        individual.setPrima(new BigDecimal("12000.00"));

        Riesgo riesgoIndividual = new Riesgo();
        riesgoIndividual.setPoliza(individual);
        riesgoIndividual.setEstado(EstadoRiesgo.ACTIVO);
        riesgoIndividual.setDescripcion("Riesgo individual principal");
        individual.setRiesgos(new ArrayList<>(List.of(riesgoIndividual)));

        Poliza colectiva = new Poliza();
        colectiva.setTipo(TipoPoliza.COLECTIVA);
        colectiva.setEstado(EstadoPoliza.VIGENTE);
        colectiva.setVigenciaMeses(6);
        colectiva.setFechaInicio(LocalDate.now().minusMonths(2));
        colectiva.setFechaFin(LocalDate.now().plusMonths(4));
        colectiva.setCanonMensual(new BigDecimal("2000.00"));
        colectiva.setPrima(new BigDecimal("12000.00"));

        Riesgo riesgoC1 = new Riesgo();
        riesgoC1.setPoliza(colectiva);
        riesgoC1.setEstado(EstadoRiesgo.ACTIVO);
        riesgoC1.setDescripcion("Riesgo colectivo A");

        Riesgo riesgoC2 = new Riesgo();
        riesgoC2.setPoliza(colectiva);
        riesgoC2.setEstado(EstadoRiesgo.ACTIVO);
        riesgoC2.setDescripcion("Riesgo colectivo B");
        colectiva.setRiesgos(new ArrayList<>(List.of(riesgoC1, riesgoC2)));

        Poliza cancelada = new Poliza();
        cancelada.setTipo(TipoPoliza.INDIVIDUAL);
        cancelada.setEstado(EstadoPoliza.CANCELADA);
        cancelada.setVigenciaMeses(12);
        cancelada.setFechaInicio(LocalDate.now().minusMonths(12));
        cancelada.setFechaFin(LocalDate.now().minusDays(1));
        cancelada.setCanonMensual(new BigDecimal("900.00"));
        cancelada.setPrima(new BigDecimal("10800.00"));

        Riesgo riesgoCancelado = new Riesgo();
        riesgoCancelado.setPoliza(cancelada);
        riesgoCancelado.setEstado(EstadoRiesgo.CANCELADO);
        riesgoCancelado.setDescripcion("Riesgo historico");
        cancelada.setRiesgos(new ArrayList<>(List.of(riesgoCancelado)));

        polizaRepository.saveAll(List.of(individual, colectiva, cancelada));
    }
}
