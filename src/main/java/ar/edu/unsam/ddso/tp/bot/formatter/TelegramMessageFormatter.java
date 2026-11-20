package ar.edu.unsam.ddso.tp.bot.formatter;

import ar.edu.unsam.ddso.tp.dto.PiezaFisicaResponseDTO;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

/** Formateador de mensajes en texto plano para el canal de Telegram. */
@Component
public class TelegramMessageFormatter {

    private static final int MAX_LONGITUD_TELEGRAM = 4000;
    private static final int MAX_LONGITUD_DESCRIPCION = 100;

    public String formatearBienvenida() {
        return """
                ========================================
                🏛️ ARCHIVO DIGITAL - ACERVO PATRIMONIAL
                ========================================

                Bienvenido al sistema de consulta de piezas historicas y digitalizaciones.

                Escribi /ayuda para conocer los comandos y filtros de busqueda disponibles.
                """;
    }

    public String formatearAyuda() {
        return """
                ========================================
                📖 GUIA DE COMANDOS DISPONIBLES
                ========================================

                /buscar <termino>
                Busca piezas por nombre, descripcion o codigo.
                Ejemplo: /buscar acta

                /buscar tag:<etiqueta>
                Busca piezas que contengan una etiqueta.
                Ejemplo: /buscar tag:FOTOGRAFIA

                /buscar <termino> p:<numero>
                Navega a una pagina especifica de resultados.
                Ejemplo: /buscar acta p:2

                /buscar <termino> tag:<etiqueta> p:<numero>
                Combina termino libre, etiqueta y paginacion.
                Ejemplo: /buscar fundacional tag:MANUSCRITO p:1

                /ayuda
                Muestra este mensaje de ayuda.
                """;
    }

    public String formatearUsoBuscar() {
        return """
                ⚠️ Debes indicar al menos un termino o etiqueta para buscar.

                Ejemplos de uso:
                • /buscar acta
                • /buscar tag:FOTOGRAFIA
                • /buscar expediente p:2
                """;
    }

    public String formatearError(String mensajeError) {
        return "❌ Ocurrio un error al procesar tu solicitud: " + mensajeError;
    }

    public String formatearPaginaInexistente(int paginaSolicitada, int totalPaginas) {
        return "⚠️ La pagina solicitada ("
                + paginaSolicitada
                + ") no existe. Total de paginas disponibles: "
                + totalPaginas
                + ".";
    }

    public String formatearResultados(
            Page<PiezaFisicaResponseDTO> pagina, String queryOriginal, int paginaSolicitadaHumana) {
        if (pagina == null || pagina.getTotalElements() == 0) {
            return "🔍 No se encontraron piezas fisicas con el criterio indicado: \""
                    + (queryOriginal != null ? queryOriginal : "")
                    + "\".";
        }

        if (paginaSolicitadaHumana > pagina.getTotalPages()) {
            return formatearPaginaInexistente(paginaSolicitadaHumana, pagina.getTotalPages());
        }

        StringBuilder sb = new StringBuilder();
        sb.append("========================================\n");
        sb.append("🏛️ RESULTADOS DE BUSQUEDA\n");
        if (queryOriginal != null && !queryOriginal.isBlank()) {
            sb.append("Criterio: \"").append(queryOriginal).append("\"\n");
        }
        sb.append("Pagina ")
                .append(pagina.getNumber() + 1)
                .append(" de ")
                .append(pagina.getTotalPages())
                .append(" (Total: ")
                .append(pagina.getTotalElements())
                .append(" piezas)\n");
        sb.append("========================================\n\n");

        List<PiezaFisicaResponseDTO> piezas = pagina.getContent();
        for (int i = 0; i < piezas.size(); i++) {
            PiezaFisicaResponseDTO p = piezas.get(i);
            sb.append("[").append(i + 1).append("] ");
            sb.append(p.codigoInventario() != null ? p.codigoInventario() : "SIN-CODIGO");
            sb.append(" - ");
            sb.append(p.nombre() != null ? p.nombre() : "Sin nombre");
            sb.append("\n");

            sb.append("• Material: ")
                    .append(p.tipoMaterial() != null ? p.tipoMaterial() : "No especificado");
            sb.append(" | Estado: ")
                    .append(p.estado() != null ? p.estado().name() : "No especificado");
            sb.append("\n");

            sb.append("• Ingreso: ")
                    .append(p.fechaIngreso() != null ? p.fechaIngreso().toString() : "S/F");
            sb.append(" | Digitalizaciones: ")
                    .append(p.cantidadDigitalizaciones());
            sb.append("\n");

            if (p.descripcion() != null && !p.descripcion().isBlank()) {
                sb.append("• Detalle: ").append(truncarDescripcion(p.descripcion())).append("\n");
            }

            if (i < piezas.size() - 1) {
                sb.append("\n----------------------------------------\n\n");
            }
        }

        sb.append("\n========================================\n");
        boolean hayNavegacion = false;
        String queryBase = queryOriginal != null ? queryOriginal.trim() : "";

        if (pagina.hasPrevious()) {
            int anterior = pagina.getNumber(); // en base 1 es igual al numero en base 0
            sb.append("⬅️ Pagina anterior: /buscar ").append(queryBase).append(" p:").append(anterior).append("\n");
            hayNavegacion = true;
        }

        if (pagina.hasNext()) {
            int siguiente = pagina.getNumber() + 2; // en base 1 es actual + 2
            sb.append("➡️ Pagina siguiente: /buscar ").append(queryBase).append(" p:").append(siguiente).append("\n");
            hayNavegacion = true;
        }

        if (!hayNavegacion) {
            sb.append("Fin de resultados.\n");
        }
        sb.append("========================================");

        return asegurarLimiteLongitud(sb.toString());
    }

    private String truncarDescripcion(String texto) {
        String limpio = texto.replace("\n", " ").trim();
        if (limpio.length() <= MAX_LONGITUD_DESCRIPCION) {
            return limpio;
        }
        return limpio.substring(0, MAX_LONGITUD_DESCRIPCION) + "...";
    }

    private String asegurarLimiteLongitud(String mensaje) {
        if (mensaje.length() <= MAX_LONGITUD_TELEGRAM) {
            return mensaje;
        }
        return mensaje.substring(0, MAX_LONGITUD_TELEGRAM)
                + "\n\n[... Mensaje truncado para respetar el limite de Telegram ...]";
    }
}
