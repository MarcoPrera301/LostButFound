import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;

/**
 * Encapsula toda la lectura/escritura del archivo Objetos.csv
 * para mantener la clase Sistema más ligera.
 *
 * Estructura de columnas (9):
 * 0=id
 * 1=descripcion
 * 2=tipo
 * 3=estado
 * 4=fechaEncontrado (YYYY-MM-DD) o vacío
 * 5=lugarEncontrado
 * 6=fechaDevolucion (YYYY-MM-DD) o vacío
 * 7=reportadoPor
 * 8=usuarioQueReclama (opcional)
 */
public class ObjetosCSV {

    private final Path rutaCSVObjetos;

    public ObjetosCSV(Path rutaCSVObjetos) {
        this.rutaCSVObjetos = rutaCSVObjetos;
    }

    /** Crea carpeta y archivo con cabecera si no existen. */
    public void asegurarCSVObjetosConCabecera() {
        try {
            Path parent = rutaCSVObjetos.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            if (Files.notExists(rutaCSVObjetos)) {
                try (BufferedWriter bw = Files.newBufferedWriter(
                        rutaCSVObjetos, StandardCharsets.UTF_8, StandardOpenOption.CREATE)) {
                    bw.write("id,descripcion,tipo,estado,fechaEncontrado,lugarEncontrado,fechaDevolucion,reportadoPor,usuarioQueReclama");
                    bw.newLine();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error preparando CSV de objetos: " + e.getMessage(), e);
        }
    }

    /** Lee todos los objetos desde el CSV. */
    public List<Objeto> leerObjetosDesdeCSV() {
        List<Objeto> out = new ArrayList<>();
        if (Files.notExists(rutaCSVObjetos)) {
            return out;
        }
        try (BufferedReader br = Files.newBufferedReader(rutaCSVObjetos, StandardCharsets.UTF_8)) {
            String linea = br.readLine(); // cabecera
            while ((linea = br.readLine()) != null) {
                if (linea.isBlank()) continue;
                String[] p = parseCSVLine(linea);
                if (p.length < 9) continue;
                try {
                    int id = Integer.parseInt(p[0].trim());
                    String descripcion = p[1];
                    String tipo = p[2];
                    String estado = p[3];
                    String fechaEncontradoStr = p[4];
                    String lugarEncontrado = p[5];
                    String fechaDevolucionStr = p[6];
                    String reportadoPor = p[7];
                    String usuarioQueReclama = p[8];

                    LocalDate fechaEncontrado = (fechaEncontradoStr == null || fechaEncontradoStr.isBlank())
                            ? null : LocalDate.parse(fechaEncontradoStr);
                    LocalDate fechaDevolucion = (fechaDevolucionStr == null || fechaDevolucionStr.isBlank())
                            ? null : LocalDate.parse(fechaDevolucionStr);

                    Objeto o = new Objeto(
                            descripcion,
                            tipo,
                            estado,
                            fechaEncontrado,
                            lugarEncontrado,
                            id,
                            reportadoPor
                    );
                    if (fechaDevolucion != null) {
                        o.setFechaDevolucion(fechaDevolucion);
                    }
                    if (usuarioQueReclama != null && !usuarioQueReclama.isBlank()) {
                        o.setUsuarioQueReclama(usuarioQueReclama);
                    }
                    out.add(o);
                } catch (Exception ignore) {
                    // Línea malformada: se ignora
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error leyendo objetos desde CSV: " + e.getMessage(), e);
        }
        return out;
    }

    /** Inserta (append) un objeto al CSV. Asume que el Objeto ya trae su id asignado. */
    public boolean insertarObjetoCSV(Objeto o) {
        if (o == null) return false;
        asegurarCSVObjetosConCabecera();
        String fila = String.join("," ,
                String.valueOf(o.getId()),
                esc(o.getDescripcion()),
                esc(o.getTipo()),
                esc(o.getEstado()),
                (o.getFechaEncontrado() == null ? "" : o.getFechaEncontrado().toString()),
                esc(o.getLugarEncontrado()),
                (o.getFechaDevolucion() == null ? "" : o.getFechaDevolucion().toString()),
                esc(o.getReportadoPor()),
                esc(o.getUsuarioQueReclama())
        );
        try (BufferedWriter bw = Files.newBufferedWriter(
                rutaCSVObjetos, StandardCharsets.UTF_8,
                StandardOpenOption.APPEND, StandardOpenOption.CREATE)) {
            bw.write(fila);
            bw.newLine();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /** Reescribe el CSV completo con la lista provista (operación atómica). */
    public boolean reescribirObjetosCSV(List<Objeto> lista) {
        if (lista == null) lista = Collections.emptyList();
        asegurarCSVObjetosConCabecera();

        Path tmp = rutaCSVObjetos.resolveSibling(rutaCSVObjetos.getFileName().toString() + ".tmp");
        try (BufferedWriter bw = Files.newBufferedWriter(tmp, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {

            bw.write("id,descripcion,tipo,estado,fechaEncontrado,lugarEncontrado,fechaDevolucion,reportadoPor,usuarioQueReclama");
            bw.newLine();
            for (Objeto o : lista) {
                if (o == null) continue;
                String fila = String.join("," ,
                        String.valueOf(o.getId()),
                        esc(o.getDescripcion()),
                        esc(o.getTipo()),
                        esc(o.getEstado()),
                        (o.getFechaEncontrado() == null ? "" : o.getFechaEncontrado().toString()),
                        esc(o.getLugarEncontrado()),
                        (o.getFechaDevolucion() == null ? "" : o.getFechaDevolucion().toString()),
                        esc(o.getReportadoPor()),
                        esc(o.getUsuarioQueReclama())
                );
                bw.write(fila);
                bw.newLine();
            }
        } catch (IOException e) {
            return false;
        }

        try {
            Files.move(tmp, rutaCSVObjetos,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    // ===== utilidades internas =====
    private static String[] parseCSVLine(String line) {
        if (line == null) return new String[0];
        ArrayList<String> out = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '\"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '\"') {
                    sb.append('\"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                out.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        out.add(sb.toString());
        return out.toArray(new String[0]);
    }

    private static String esc(String s) {
        if (s == null) return "";
        boolean q = s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r");
        String out = s.replace("\"", "\"\"");
        return q ? "\"" + out + "\"" : out;
    }
}