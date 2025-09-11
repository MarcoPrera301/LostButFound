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

    public Sistema() {
        listaObjetos = new ArrayList<>();
        listaUsuarios = new ArrayList<>();
        listaPremios = new ArrayList<>();
        listaAdministradores = new ArrayList<>();
        vistaUsuario = new VistaUsuario();

        // Preparar “BD” CSV
        asegurarCSVUsuariosConCabecera();
    }

    public void iniciarSistema() {
        vistaUsuario.IniciarVistaUsuario();
        // Si no hay usuarios, crea un admin por defecto y luego entra a login
        crearAdminPorDefectoSiVacio();
        vistaUsuario.mostrarLoginConsola(this);
    }

    public boolean registrarObjeto(Objeto objeto) {
        if (objeto.esValido()) {
            listaObjetos.add(objeto);
            return true;
        }
        return false;
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

    public void registrarPremio(Premio premio) {
        listaPremios.add(premio);
    }

    public void registrarAdministrador(Administrador administrador) {
        listaAdministradores.add(administrador);
    }

    public String registrarObjetoEncontrado() {
        Objeto objeto = new Objeto(
            vistaUsuario.solicitarDescripcion(),
            vistaUsuario.solicitarTipoObjeto(),
            "Encontrado",
            vistaUsuario.solicitarFechaEncontrado(),
            vistaUsuario.solicitarUbicacionObjeto(),
            vistaUsuario.siguienteIdObjeto(),
            "UsuarioX"
        );

        if (registrarObjeto(objeto)) {
            return "Objeto registrado exitosamente.";
        } else {
            return "Error al registrar el objeto. Por favor, intente de nuevo.";
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

    // Autenticación básica (texto plano)
    public Optional<Usuario> autenticarUsuarioCSV(String correo, String contrasenaPlano) {
        Optional<Usuario> u = buscarUsuarioPorCorreoCSV(correo);
        if (u.isPresent() && u.get().getContrasena().equals(contrasenaPlano)) {
            return u;
        }
        return Optional.empty();
    }

    // ================== CSV: utilidades para el arranque ==================

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
            boolean ok = insertarUsuarioCSV("Admin", "admin@uvg.edu.gt", "1234", "ADMIN");
            if (ok) {
                System.out.println(" Admin por defecto creado: admin@uvg.edu.gt / 1234");
            } else {
                System.err.println(" No se pudo crear el admin por defecto.");
            }
        }
    }
}