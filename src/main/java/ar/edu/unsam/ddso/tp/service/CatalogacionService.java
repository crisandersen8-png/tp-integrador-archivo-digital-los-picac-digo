package ar.edu.unsam.ddso.tp.service;

import ar.edu.unsam.ddso.tp.dto.AsociarTagRequestDTO;
import ar.edu.unsam.ddso.tp.dto.DigitalizacionResponseDTO;
import ar.edu.unsam.ddso.tp.dto.TagCreateRequestDTO;
import ar.edu.unsam.ddso.tp.dto.TagResponseDTO;
import ar.edu.unsam.ddso.tp.exception.DigitalizacionNoEncontradaException;
import ar.edu.unsam.ddso.tp.exception.TagDuplicadoException;
import ar.edu.unsam.ddso.tp.exception.TagNoEncontradoException;
import ar.edu.unsam.ddso.tp.model.Digitalizacion;
import ar.edu.unsam.ddso.tp.model.Tag;
import ar.edu.unsam.ddso.tp.repository.DigitalizacionRepository;
import ar.edu.unsam.ddso.tp.repository.TagRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Servicio que orquesta la administracion de tags dinamicos y su asociacion a digitalizaciones. */
@Service
public class CatalogacionService {

    private final TagRepository tagRepository;
    private final DigitalizacionRepository digitalizacionRepository;

    public CatalogacionService(
            TagRepository tagRepository, DigitalizacionRepository digitalizacionRepository) {
        this.tagRepository = tagRepository;
        this.digitalizacionRepository = digitalizacionRepository;
    }

    // ---------- Consultas ----------

    @Transactional(readOnly = true)
    public List<TagResponseDTO> listarTags() {
        return tagRepository.findAll().stream().map(this::aTagResponseDTO).toList();
    }

    // ---------- Operaciones ----------

    @Transactional
    public TagResponseDTO crearTag(TagCreateRequestDTO request) {
        Tag tag = new Tag();
        tag.setNombre(request.nombre() != null ? request.nombre().trim() : null);
        tag.validar();

        if (tagRepository.existsByNombreIgnoreCase(tag.getNombre())) {
            throw new TagDuplicadoException(tag.getNombre());
        }

        Tag guardado = tagRepository.save(tag);
        return aTagResponseDTO(guardado);
    }

    @Transactional
    public DigitalizacionResponseDTO asociarTagADigitalizacion(
            Long digitalizacionId, AsociarTagRequestDTO request) {
        Digitalizacion digitalizacion =
                digitalizacionRepository
                        .findById(digitalizacionId)
                        .orElseThrow(
                                () -> new DigitalizacionNoEncontradaException(digitalizacionId));
        Tag tag =
                tagRepository
                        .findById(request.tagId())
                        .orElseThrow(() -> new TagNoEncontradoException(request.tagId()));

        digitalizacion.asociarTag(tag);
        Digitalizacion guardada = digitalizacionRepository.save(digitalizacion);
        return aDigitalizacionResponseDTO(guardada);
    }

    // ---------- Privados ----------

    private TagResponseDTO aTagResponseDTO(Tag tag) {
        return new TagResponseDTO(tag.getId(), tag.getNombre());
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
}

