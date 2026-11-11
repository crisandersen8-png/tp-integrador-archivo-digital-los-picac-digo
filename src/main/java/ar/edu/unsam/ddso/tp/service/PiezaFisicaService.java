package ar.edu.unsam.ddso.tp.service;

import ar.edu.unsam.ddso.tp.dto.DigitalizacionCreateRequestDTO;
import ar.edu.unsam.ddso.tp.dto.DigitalizacionResponseDTO;
import ar.edu.unsam.ddso.tp.dto.PiezaFisicaCreateRequestDTO;
import ar.edu.unsam.ddso.tp.dto.PiezaFisicaDetalleResponseDTO;
import ar.edu.unsam.ddso.tp.dto.PiezaFisicaResponseDTO;
import ar.edu.unsam.ddso.tp.dto.TagResponseDTO;
import ar.edu.unsam.ddso.tp.exception.CodigoInventarioDuplicadoException;
import ar.edu.unsam.ddso.tp.exception.PiezaFisicaNoEncontradaException;
import ar.edu.unsam.ddso.tp.model.Digitalizacion;
import ar.edu.unsam.ddso.tp.model.PiezaFisica;
import ar.edu.unsam.ddso.tp.model.Tag;
import ar.edu.unsam.ddso.tp.repository.DigitalizacionRepository;
import ar.edu.unsam.ddso.tp.repository.PiezaFisicaRepository;
import java.util.List;
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
        return aPiezaFisicaDetalleResponseDTO(pieza);
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
        return aPiezaFisicaResponseDTO(guardada);
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
        return aDigitalizacionResponseDTO(guardada);
    }

    // ---------- Privados ----------

    private PiezaFisicaResponseDTO aPiezaFisicaResponseDTO(PiezaFisica pieza) {
        int cantidad =
                pieza.getDigitalizaciones() == null ? 0 : pieza.getDigitalizaciones().size();
        return new PiezaFisicaResponseDTO(
                pieza.getId(),
                pieza.getCodigoInventario(),
                pieza.getNombre(),
                pieza.getDescripcion(),
                pieza.getFechaIngreso(),
                pieza.getTipoMaterial(),
                pieza.getEstado(),
                cantidad);
    }

    private PiezaFisicaDetalleResponseDTO aPiezaFisicaDetalleResponseDTO(PiezaFisica pieza) {
        List<DigitalizacionResponseDTO> digitalizaciones =
                pieza.getDigitalizaciones() == null
                        ? List.of()
                        : pieza.getDigitalizaciones().stream()
                                .map(this::aDigitalizacionResponseDTO)
                                .toList();
        return new PiezaFisicaDetalleResponseDTO(
                pieza.getId(),
                pieza.getCodigoInventario(),
                pieza.getNombre(),
                pieza.getDescripcion(),
                pieza.getFechaIngreso(),
                pieza.getTipoMaterial(),
                pieza.getEstado(),
                digitalizaciones);
    }

    private DigitalizacionResponseDTO aDigitalizacionResponseDTO(Digitalizacion digitalizacion) {
        Long piezaFisicaId =
                digitalizacion.getPiezaFisica() != null
                        ? digitalizacion.getPiezaFisica().getId()
                        : null;
        List<TagResponseDTO> tags =
                digitalizacion.getTags() == null
                        ? List.of()
                        : digitalizacion.getTags().stream().map(this::aTagResponseDTO).toList();
        return new DigitalizacionResponseDTO(
                digitalizacion.getId(),
                piezaFisicaId,
                digitalizacion.getUrlRecurso(),
                digitalizacion.getDescripcion(),
                digitalizacion.getFechaDigitalizacion(),
                tags);
    }

    private TagResponseDTO aTagResponseDTO(Tag tag) {
        return new TagResponseDTO(tag.getId(), tag.getNombre());
    }
}
