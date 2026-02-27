package com.company.polizas.repository;

import com.company.polizas.domain.EstadoPoliza;
import com.company.polizas.domain.Poliza;
import com.company.polizas.domain.TipoPoliza;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PolizaRepository extends JpaRepository<Poliza, Long> {

    @EntityGraph(attributePaths = "riesgos")
    List<Poliza> findByTipoAndEstado(TipoPoliza tipo, EstadoPoliza estado);

    @EntityGraph(attributePaths = "riesgos")
    List<Poliza> findByTipo(TipoPoliza tipo);

    @EntityGraph(attributePaths = "riesgos")
    List<Poliza> findByEstado(EstadoPoliza estado);

    @Override
    @EntityGraph(attributePaths = "riesgos")
    List<Poliza> findAll();
}
