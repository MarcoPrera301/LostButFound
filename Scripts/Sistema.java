import java.util.List;
import java.util.ArrayList;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

public class Sistema {
    private List<Objeto> listaObjetos;
    private List<Usuario> listaUsuarios;
    private List<Premio> listaPremios;
    private List<Administrador> listaAdministradores;
    private VistaUsuario vistaUsuario;

    // CSV de usuarios
    private final Path rutaCSVUsuarios = Paths.get("data", "usuarios.csv");
    // CSV de objetos
    private final Path rutaCSVObjetos = Paths.get("data", "objetos.csv");

    public Sistema() {
        listaObjetos = new ArrayList<>();
        listaUsuarios = new ArrayList<>();
        listaPremios = new ArrayList<>();
        listaAdministradores = new ArrayList<>();
        vistaUsuario = new VistaUsuario();

        // Preparar “BD” CSV
        asegurarCSVUsuariosConCabecera();
        asegurarCSVObjetosConCabecera();
    }

    public void iniciarSistema() {
        vistaUsuario.IniciarVistaUsuario(this); // Mostrar la vista con opciones
        // Si no hay usuarios, crea un admin por defecto y luego entra a login
        crearAdminPorDefectoSiVacio();
    }

    public boolean registrarObjeto(Objeto objeto) {
        if (objeto.esValido()) {
            listaObjetos.add(objeto);  // Registrar en memoria

            // Registrar en CSV también
            registrarObjetoCSV(objeto);

            return true;
        }
        return false;
    }

    // Mostrar todos los objetos registrados
    public void mostrarObjetos() {
        System.out.println("Listado de objetos registrados:");

        // Mostrar los objetos en memoria
        if (listaObjetos.isEmpty()) {
            System.out.println("No hay objetos registrados en memoria.");
        } else {
            for (Objeto objeto : listaObjetos) {
                System.out.println("ID: " + objeto.getId() + ", Descripción: " + objeto.getDescripcion() + ", Tipo: " + objeto.getTipo() + ", Estado: " + objeto.getEstado());
            }
        }

        // Mostrar los objetos en el CSV
        try (BufferedReader br = Files.newBufferedReader(rutaCSVObjetos, StandardCharsets.UTF_8)) {
            String linea = br.readLine(); // saltar cabecera
            while ((linea = br.readLine()) != null) {
                if (linea.isBlank()) continue;
                String[] p = linea.split(",", -1);
                if (p.length < 7) continue;  // Verificar que haya suficientes datos

                System.out.println("ID: " + p[0] + ", Descripción: " + p[1] + ", Tipo: " + p[2] + ", Estado: " + p[3] + ", Fecha Encontrado: " + p[4] + ", Ubicación: " + p[5] + ", Reportado por: " + p[6]);
            }
        } catch (IOException e) {
            System.err.println("Error al leer el archivo CSV de objetos: " + e.getMessage());
        }
    }

    // Persistir primero en CSV y luego a memoria
    public void registrarUsuario(Usuario usuario) {
        boolean ok = insertarUsuarioCSV(
            usuario.getNombre(),
            usuario.getCorreo(),
            usuario.getContrasena(),
            usuario.getRol()
        );

        if (ok) {
            buscarUsuarioPorCorreoCSV(usuario.getCorreo())
                .ifPresent(listaUsuarios::add);
        } else {
            System.err.println("No se registró en CSV; no se agrega a memoria.");
        }
    }

    // ================== CSV: preparación ==================
    private void asegurarCSVUsuariosConCabecera() {
        try {
            Files.createDirectories(rutaCSVUsuarios.getParent());
            if (Files.notExists(rutaCSVUsuarios)) {
                try (BufferedWriter bw = Files.newBufferedWriter(
                        rutaCSVUsuarios, StandardCharsets.UTF_8, StandardOpenOption.CREATE)) {
                    bw.write("idUsuario,nombre,correo,contrasena,rol,creadoEn");
                    bw.newLine();
                }
            }
        } catch (IOException e) {
            System.err.println("No se pudo preparar el CSV de usuarios: " + e.getMessage());
        }
    }

    // CSV de objetos
    private void asegurarCSVObjetosConCabecera() {
        try {
            Files.createDirectories(rutaCSVObjetos.getParent());
            if (Files.notExists(rutaCSVObjetos)) {
                try (BufferedWriter bw = Files.newBufferedWriter(
                        rutaCSVObjetos, StandardCharsets.UTF_8, StandardOpenOption.CREATE)) {
                    bw.write("id,descripcion,tipo,estado,fechaEncontrado,lugarEncontrado,reportadoPor");
                    bw.newLine();
                }
            }
        } catch (IOException e) {
            System.err.println("No se pudo preparar el CSV de objetos: " + e.getMessage());
        }
    }

    private String hoy() {
        return new SimpleDateFormat("yyyy-MM-dd").format(new Date());
    }

    // ================== CSV: integración ==================

    // Buscar por correo (para login y unicidad)
    public Optional<Usuario> buscarUsuarioPorCorreoCSV(String correoBuscado) {
        try (BufferedReader br = Files.newBufferedReader(rutaCSVUsuarios, StandardCharsets.UTF_8)) {
            String linea = br.readLine(); // saltar cabecera
            while ((linea = br.readLine()) != null) {
                if (linea.isBlank()) continue;
                String[] p = linea.split(",", -1);
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
        } catch (IOException ex) {
            System.err.println("Error leyendo CSV de usuarios: " + ex.getMessage());
        }
        return Optional.empty();
    }

    // Siguiente id (máximo + 1)
    private int siguienteIdUsuarioCSV() {
        int max = 0;
        try (BufferedReader br = Files.newBufferedReader(rutaCSVUsuarios, StandardCharsets.UTF_8)) {
            String linea = br.readLine(); // cabecera
            while ((linea = br.readLine()) != null) {
                if (linea.isBlank()) continue;
                String[] p = linea.split(",", -1);
                if (p.length > 0) {
                    try { max = Math.max(max, Integer.parseInt(p[0].trim())); }
                    catch (NumberFormatException ignore) {}
                }
            }
        } catch (IOException ex) {
            System.err.println("Error leyendo IDs del CSV: " + ex.getMessage());
        }
        return max + 1;
    }

    // Insertar (unicidad por correo)
    public boolean insertarUsuarioCSV(String nombre, String correo, String contrasena, String rol) {
        if (nombre == null || nombre.isBlank() ||
            correo == null || correo.isBlank() ||
            contrasena == null || contrasena.isBlank() ||
            rol == null || rol.isBlank()) {
            return false;
        }

        if (buscarUsuarioPorCorreoCSV(correo).isPresent()) {
            System.err.println("Ya existe un usuario con ese correo.");
            return false;
        }

        int id = siguienteIdUsuarioCSV();
        String fila = String.join(",",
                String.valueOf(id),
                nombre.trim(),
                correo.trim(),
                contrasena,       // en producción: guardar hash
                rol.trim(),
                hoy()
        );

        try (BufferedWriter bw = Files.newBufferedWriter(
                rutaCSVUsuarios, StandardCharsets.UTF_8,
                StandardOpenOption.APPEND, StandardOpenOption.CREATE)) {
            bw.write(fila);
            bw.newLine();
            return true;
        } catch (IOException ex) {
            System.err.println("Error escribiendo CSV de usuarios: " + ex.getMessage());
            return false;
        }
    }

    // Insertar objeto en CSV
    private void registrarObjetoCSV(Objeto objeto) {
        String fila = String.join(",",
                String.valueOf(objeto.getId()),
                objeto.getDescripcion(),
                objeto.getTipo(),
                objeto.getEstado(),
                objeto.getFechaEncontrado() != null ? objeto.getFechaEncontrado().toString() : "",
                objeto.getLugarEncontrado(),
                objeto.getReportadoPor()
        );

        try (BufferedWriter bw = Files.newBufferedWriter(
                rutaCSVObjetos, StandardCharsets.UTF_8,
                StandardOpenOption.APPEND, StandardOpenOption.CREATE)) {
            bw.write(fila);
            bw.newLine();
        } catch (IOException ex) {
            System.err.println("Error escribiendo en CSV de objetos: " + ex.getMessage());
        }
    }

    // Autenticación básica (texto plano)
    public Optional<Usuario> autenticarUsuarioCSV(String correo, String contrasenaPlano) {
        Optional<Usuario> u = buscarUsuarioPorCorreoCSV(correo);
        if (u.isPresent() && u.get().getContrasena().equals(contrasenaPlano)) {
            return u;
        }
        return Optional.empty();
    }

    // ================== CSV: utilidades de arranque ==================

    // ¿Hay al menos un usuario (además de la cabecera)?
    private boolean hayUsuariosCSV() {
        try (BufferedReader br = Files.newBufferedReader(rutaCSVUsuarios, StandardCharsets.UTF_8)) {
            String linea = br.readLine(); // cabecera
            while ((linea = br.readLine()) != null) {
                if (!linea.isBlank()) return true;
            }
        } catch (IOException e) {
            System.err.println("Error verificando usuarios CSV: " + e.getMessage());
        }
        return false;
    }

    // Crear admin por defecto si el CSV está vacío
    private void crearAdminPorDefectoSiVacio() {
        if (!hayUsuariosCSV()) {
            boolean ok = insertarUsuarioCSV("Admin", "admin@uvg.edu", "1234", "ADMIN");
            if (ok) {
                System.out.println("✔ Admin por defecto creado: admin@uvg.edu / 1234");
            } else {
                System.err.println("✖ No se pudo crear el admin por defecto.");
            }
        }
    }
}