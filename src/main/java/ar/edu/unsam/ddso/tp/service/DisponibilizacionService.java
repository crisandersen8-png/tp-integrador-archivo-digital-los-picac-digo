package ar.edu.unsam.ddso.tp.service;

import ar.edu.unsam.ddso.tp.dto.PaginaResponseDTO;
import ar.edu.unsam.ddso.tp.dto.PiezaFisicaResponseDTO;
import ar.edu.unsam.ddso.tp.exception.RangoFechasInvalidoException;
import ar.edu.unsam.ddso.tp.model.PiezaFisica;
import ar.edu.unsam.ddso.tp.repository.PiezaFisicaRepository;
import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Servicio que orquesta la busqueda avanzada paginada de piezas fisicas. */
@Service
public class DisponibilizacionService {

    private final PiezaFisicaRepository piezaFisicaRepository;

    public DisponibilizacionService(PiezaFisicaRepository piezaFisicaRepository) {
        this.piezaFisicaRepository = piezaFisicaRepository;
    }

    // ---------- Consultas ----------

    @Transactional(readOnly = true)
    public PaginaResponseDTO<PiezaFisicaResponseDTO> buscarPiezas(
            String termino,
            LocalDate fechaDesde,
            LocalDate fechaHasta,
            String tag,
            Pageable pageable) {
        if (fechaDesde != null && fechaHasta != null && fechaDesde.isAfter(fechaHasta)) {
            throw new RangoFechasInvalidoException(
                    "La fecha desde no puede ser posterior a la fecha hasta");
        }
        Page<PiezaFisica> pagina =
                piezaFisicaRepository.buscarAvanzado(
                        termino, fechaDesde, fechaHasta, tag, pageable);
        return ArchivoDigitalMapper.aPaginaPiezaFisicaResponseDTO(pagina);
    }
}
