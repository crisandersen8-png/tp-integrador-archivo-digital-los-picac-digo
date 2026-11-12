package ar.edu.unsam.ddso.tp.service;

import ar.edu.unsam.ddso.tp.dto.DigitalizacionResponseDTO;
import ar.edu.unsam.ddso.tp.dto.PaginaResponseDTO;
import ar.edu.unsam.ddso.tp.dto.PiezaFisicaDetalleResponseDTO;
import ar.edu.unsam.ddso.tp.dto.PiezaFisicaResponseDTO;
import ar.edu.unsam.ddso.tp.dto.TagResponseDTO;
import ar.edu.unsam.ddso.tp.model.Digitalizacion;
import ar.edu.unsam.ddso.tp.model.PiezaFisica;
import ar.edu.unsam.ddso.tp.model.Tag;
import java.util.List;
import org.springframework.data.domain.Page;

/** Mapper centralizado entre entidades de dominio y DTOs de respuesta (Sub-Issue #13). */
public final class ArchivoDigitalMapper {

    private ArchivoDigitalMapper() {}

    public static PiezaFisicaResponseDTO aPiezaFisicaResponseDTO(PiezaFisica pieza) {
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

    public static PiezaFisicaDetalleResponseDTO aPiezaFisicaDetalleResponseDTO(PiezaFisica pieza) {
        List<DigitalizacionResponseDTO> digitalizaciones =
                pieza.getDigitalizaciones() == null
                        ? List.of()
                        : pieza.getDigitalizaciones().stream()
                                .map(ArchivoDigitalMapper::aDigitalizacionResponseDTO)
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

    public static DigitalizacionResponseDTO aDigitalizacionResponseDTO(
            Digitalizacion digitalizacion) {
        Long piezaFisicaId =
                digitalizacion.getPiezaFisica() != null
                        ? digitalizacion.getPiezaFisica().getId()
                        : null;
        List<TagResponseDTO> tags =
                digitalizacion.getTags() == null
                        ? List.of()
                        : digitalizacion.getTags().stream()
                                .map(ArchivoDigitalMapper::aTagResponseDTO)
                                .toList();
        return new DigitalizacionResponseDTO(
                digitalizacion.getId(),
                piezaFisicaId,
                digitalizacion.getUrlRecurso(),
                digitalizacion.getDescripcion(),
                digitalizacion.getFechaDigitalizacion(),
                tags);
    }

    public static TagResponseDTO aTagResponseDTO(Tag tag) {
        return new TagResponseDTO(tag.getId(), tag.getNombre());
    }

    public static PaginaResponseDTO<PiezaFisicaResponseDTO> aPaginaPiezaFisicaResponseDTO(
            Page<PiezaFisica> pagina) {
        List<PiezaFisicaResponseDTO> contenido =
                pagina.getContent().stream()
                        .map(ArchivoDigitalMapper::aPiezaFisicaResponseDTO)
                        .toList();
        return new PaginaResponseDTO<>(
                contenido,
                pagina.getNumber(),
                pagina.getSize(),
                pagina.getTotalElements(),
                pagina.getTotalPages(),
                pagina.isLast());
    }
}
