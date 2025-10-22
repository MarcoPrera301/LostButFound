import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class UsuariosCSV {

    private final Path rutaCSVUsuarios;

    public UsuariosCSV(Path rutaCSVUsuarios) {
        this.rutaCSVUsuarios = rutaCSVUsuarios;
    }

    /** Crea carpeta/archivo y escribe cabecera si no existe. */
    public void asegurarArchivoConCabecera() {
        try {
            Files.createDirectories(rutaCSVUsuarios.getParent());
            if (Files.notExists(rutaCSVUsuarios)) {
                try (BufferedWriter bw = Files.newBufferedWriter(
                        rutaCSVUsuarios, StandardCharsets.UTF_8, StandardOpenOption.CREATE)) {
                    bw.write("idUsuario,nombre,correo,contrasena,rol,creadoEn");
                    bw.newLine();
                }
            }
        } catch (IOException ignore) {}
    }

    /** Busca un usuario por correo; si existe, lo devuelve. */
    public Optional<Usuario> buscarPorCorreo(String correoBuscado) {
        if (correoBuscado == null || correoBuscado.isBlank()) return Optional.empty();
        try (BufferedReader br = Files.newBufferedReader(rutaCSVUsuarios, StandardCharsets.UTF_8)) {
            br.readLine(); // header
            String linea;
            while ((linea = br.readLine()) != null) {
                if (linea.isBlank()) continue;
                String[] p = parseCSVLine(linea);
                if (p.length < 6) continue;

                String correo = p[2].trim();
                if (correo.equalsIgnoreCase(correoBuscado.trim())) {
                    int id = Integer.parseInt(p[0].trim());
                    String nombre = p[1].trim();
                    String contrasena = p[3];
                    String rol = p[4].trim();
                    return Optional.of(new Usuario(id, nombre, correo, contrasena, rol));
                }
            }
        } catch (IOException ignore) {}
        return Optional.empty();
    }

    /** Inserta un usuario (verifica unicidad por correo). */
    public boolean insertarUsuario(String nombre, String correo, String contrasena, String rol) {
        if (nombre == null || nombre.isBlank() ||
            correo == null || correo.isBlank() ||
            contrasena == null || contrasena.isBlank() ||
            rol == null || rol.isBlank()) {
            return false;
        }

        if (buscarPorCorreo(correo).isPresent()) {
            return false;
        }

        int id = siguienteId();
        String fila = String.join(",",
                String.valueOf(id),
                esc(nombre),
                esc(correo),
                esc(contrasena),
                esc(rol.toUpperCase(Locale.ROOT)),
                esc(hoy())
        );

        try (BufferedWriter bw = Files.newBufferedWriter(
                rutaCSVUsuarios, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            bw.write(fila);
            bw.newLine();
            return true;
        } catch (IOException ex) {
            return false;
        }
    }

    /** Actualiza el rol en CSV por correo (conserva creadoEn). */
    public boolean actualizarRolPorCorreo(String correoObjetivo, String nuevoRol) {
        if (correoObjetivo == null || nuevoRol == null) return false;

        Path src = rutaCSVUsuarios;
        Path tmp = src.resolveSibling(src.getFileName().toString() + ".tmp");
        boolean cambiado = false;

        try (BufferedReader br = Files.newBufferedReader(src, StandardCharsets.UTF_8);
             BufferedWriter bw = Files.newBufferedWriter(tmp, StandardCharsets.UTF_8,
                     StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {

            String header = br.readLine();
            if (header == null) return false;
            bw.write(header); bw.newLine();

            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) { bw.newLine(); continue; }
                String[] p = parseCSVLine(line);
                if (p.length < 6) { bw.write(line); bw.newLine(); continue; }

                String correo = p[2].trim();
                if (correo.equalsIgnoreCase(correoObjetivo.trim())) {
                    p[4] = nuevoRol.trim().toUpperCase(Locale.ROOT); // rol
                    cambiado = true;
                    String out = String.join(",",
                        p[0].trim(),  // idUsuario
                        esc(p[1]),    // nombre
                        esc(p[2]),    // correo
                        esc(p[3]),    // contrasena
                        esc(p[4]),    // rol (nuevo)
                        esc(p[5])     // creadoEn
                    );
                    bw.write(out); bw.newLine();
                } else {
                    bw.write(line); bw.newLine();
                }
            }
        } catch (IOException e) {
            return false;
        }

        try {
            Files.move(tmp, src, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            return false;
        }
        return cambiado;
    }

    /** ¿Hay al menos un usuario (además de la cabecera)? */
    public boolean hayUsuarios() {
        try (BufferedReader br = Files.newBufferedReader(rutaCSVUsuarios, StandardCharsets.UTF_8)) {
            br.readLine(); // header
            String linea;
            while ((linea = br.readLine()) != null) {
                if (!linea.isBlank()) return true;
            }
        } catch (IOException ignore) {}
        return false;
    }

    /** Siguiente id = max(id) + 1. */
    private int siguienteId() {
        int max = 0;
        try (BufferedReader br = Files.newBufferedReader(rutaCSVUsuarios, StandardCharsets.UTF_8)) {
            br.readLine(); // header
            String linea;
            while ((linea = br.readLine()) != null) {
                if (linea.isBlank()) continue;
                String[] p = parseCSVLine(linea);
                if (p.length > 0) {
                    try { max = Math.max(max, Integer.parseInt(p[0].trim())); }
                    catch (NumberFormatException ignore) {}
                }
            }
        } catch (IOException ignore) {}
        return max + 1;
    }

    private static String hoy() {
        return new SimpleDateFormat("yyyy-MM-dd").format(new Date());
    }

    // ====== utilidades CSV ======
    private static String[] parseCSVLine(String line) {
        if (line == null) return new String[0];
        List<String> out = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '\"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '\"') {
                    sb.append('\"'); i++;
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
