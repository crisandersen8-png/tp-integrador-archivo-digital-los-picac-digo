package ar.edu.unsam.ddso.tp.service;

import ar.edu.unsam.ddso.tp.dto.DigitalizacionCreateRequestDTO;
import ar.edu.unsam.ddso.tp.dto.DigitalizacionResponseDTO;
import ar.edu.unsam.ddso.tp.dto.PiezaFisicaCreateRequestDTO;
import ar.edu.unsam.ddso.tp.dto.PiezaFisicaDetalleResponseDTO;
import ar.edu.unsam.ddso.tp.dto.PiezaFisicaResponseDTO;
import ar.edu.unsam.ddso.tp.exception.CodigoInventarioDuplicadoException;
import ar.edu.unsam.ddso.tp.exception.PiezaFisicaNoEncontradaException;
import ar.edu.unsam.ddso.tp.model.Digitalizacion;
import ar.edu.unsam.ddso.tp.model.PiezaFisica;
import ar.edu.unsam.ddso.tp.repository.DigitalizacionRepository;
import ar.edu.unsam.ddso.tp.repository.PiezaFisicaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Servicio que orquesta el alta de piezas fisicas y la asociacion de digitalizaciones. */
@Service
public class PiezaFisicaService {

    private final PiezaFisicaRepository piezaFisicaRepository;
    private final DigitalizacionRepository digitalizacionRepository;

    public PiezaFisicaService(
            PiezaFisicaRepository piezaFisicaRepository,
            DigitalizacionRepository digitalizacionRepository) {
        this.piezaFisicaRepository = piezaFisicaRepository;
        this.digitalizacionRepository = digitalizacionRepository;
    }

    // ---------- Consultas ----------

    @Transactional(readOnly = true)
    public PiezaFisicaDetalleResponseDTO obtenerPorId(Long id) {
        PiezaFisica pieza =
                piezaFisicaRepository
                        .findById(id)
                        .orElseThrow(() -> new PiezaFisicaNoEncontradaException(id));
        return ArchivoDigitalMapper.aPiezaFisicaDetalleResponseDTO(pieza);
    }

    // ---------- Operaciones ----------

    @Transactional
    public PiezaFisicaResponseDTO crearPieza(PiezaFisicaCreateRequestDTO request) {
        PiezaFisica pieza = new PiezaFisica();
        pieza.setCodigoInventario(
                request.codigoInventario() != null ? request.codigoInventario().trim() : null);
        pieza.setNombre(request.nombre() != null ? request.nombre().trim() : null);
        pieza.setDescripcion(request.descripcion());
        pieza.setFechaIngreso(request.fechaIngreso());
        pieza.setTipoMaterial(
                request.tipoMaterial() != null ? request.tipoMaterial().trim() : null);
        if (request.estado() != null) {
            pieza.setEstado(request.estado());
        }
        pieza.validar();

        if (piezaFisicaRepository.existsByCodigoInventarioIgnoreCase(pieza.getCodigoInventario())) {
            throw new CodigoInventarioDuplicadoException(pieza.getCodigoInventario());
        }

        PiezaFisica guardada = piezaFisicaRepository.save(pieza);
        return ArchivoDigitalMapper.aPiezaFisicaResponseDTO(guardada);
    }

    @Transactional
    public DigitalizacionResponseDTO agregarDigitalizacion(
            Long piezaId, DigitalizacionCreateRequestDTO request) {
        PiezaFisica pieza =
                piezaFisicaRepository
                        .findById(piezaId)
                        .orElseThrow(() -> new PiezaFisicaNoEncontradaException(piezaId));
        Digitalizacion digitalizacion =
                pieza.agregarDigitalizacion(request.urlRecurso(), request.descripcion());
        Digitalizacion guardada = digitalizacionRepository.save(digitalizacion);
        return ArchivoDigitalMapper.aDigitalizacionResponseDTO(guardada);
    }
}
