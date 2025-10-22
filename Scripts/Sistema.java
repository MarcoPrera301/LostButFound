import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Sistema {
    private List<Objeto> listaObjetos;
    private List<Usuario> listaUsuarios;
    private List<Premio> listaPremios;
    private List<Administrador> listaAdministradores;
    private VistaUsuario vistaUsuario;
    private ObjetosCSV objetosCSV;
    private Usuario usuarioActual;
    private Usuario usuarioEnSesion;

    public static final String ROL_ADMIN      = "ADMIN";
    public static final String ROL_ESTUDIANTE = "ESTUDIANTE";

    public static final String ACCION_RECLAMAR_OBJETO  = "RECLAMAR_OBJETO";
    public static final String ACCION_GESTION_OBJETOS  = "GESTION_OBJETOS";
    public static final String ACCION_GESTION_PREMIOS  = "GESTION_PREMIOS";
    public static final String ACCION_REPORTES         = "REPORTES";
    public static final String ACCION_ELIMINAR_OBJETOS = "ELIMINAR_OBJETOS";

    private final Path rutaCSVUsuarios = Paths.get("data", "usuarios.csv");
    private final Path rutaCSVObjetos  = Paths.get("data", "objetos.csv");

    // Persistencia de usuarios separada
    private final UsuariosCSV usuariosCSV;

    // ===== Getters/Setters básicos =====
    public Usuario getUsuarioActual() { return this.usuarioActual; }
    public void setUsuarioActual(Usuario u) { this.usuarioActual = u; }
    public Usuario getUsuarioEnSesion() { return this.usuarioEnSesion; }
    public void setUsuarioEnSesion(Usuario u) { this.usuarioEnSesion = u; }

    public boolean esAdminSesion() {
        return usuarioEnSesion != null && "ADMIN".equalsIgnoreCase(usuarioEnSesion.getRol());
    }

    public boolean tienePermiso(Usuario u, String accion) {
        if (u == null || accion == null) return false;
        if (u.esAdmin()) return true;
        if (u.esEstudiante()) return ACCION_RECLAMAR_OBJETO.equals(accion);
        return false;
    }

    // Main la usa
    public void setVistaUsuario(VistaUsuario v) {
        this.vistaUsuario = v;
    }

    // ====== Constructor ======
    public Sistema() {
        // Inicializar manejador de CSV de objetos
        objetosCSV = new ObjetosCSV(rutaCSVObjetos);

        listaObjetos         = leerObjetosDesdeCSV();
        listaUsuarios        = new ArrayList<>();
        listaPremios         = new ArrayList<>();
        listaAdministradores = new ArrayList<>();
        vistaUsuario         = new VistaUsuario();
        usuariosCSV          = new UsuariosCSV(rutaCSVUsuarios);

        // Preparar CSV
        asegurarCSVUsuariosConCabecera(); // delega a UsuariosCSV
        asegurarCSVObjetosConCabecera();

        // Admin por defecto si no hay usuarios
        crearAdminPorDefectoSiVacio();
        registrarPremio(new Premio("Descuento Cafetería", "10% en comida", 50));
    }

    // ====== Flujo principal ======
    public void iniciarSistema() {
        int opcionInicio = vistaUsuario.IniciarVistaUsuario();

        boolean login = false;

        if (opcionInicio == 1) { // 1 = REGISTRARSE
            String nombre = vistaUsuario.solicitarNombrePersona();
            String correo = vistaUsuario.solicitarCorreo();
            String contrasena = vistaUsuario.solicitarContrasena();

            boolean ok = insertarUsuarioCSV(nombre, correo, contrasena, "USUARIO");
            if (ok) {
                buscarUsuarioPorCorreoCSV(correo).ifPresent(listaUsuarios::add);
                vistaUsuario.mensaje("Registro exitoso. Ahora inicia sesión.");
            } else {
                vistaUsuario.mensaje("No fue posible registrar al usuario. Intente iniciar sesión o volver a registrarse.");
            }

            // Ahora 2 = INICIAR SESIÓN
            int opcion = vistaUsuario.IniciarVistaUsuario();
            if (opcion == 2) {
                String correoL = vistaUsuario.solicitarCorreo();
                String contrasenaL = vistaUsuario.solicitarContrasena();
                Optional<Usuario> lu = autenticarUsuarioCSV(correoL, contrasenaL);
                if (lu.isPresent()) {
                    usuarioActual = lu.get();
                    usuarioEnSesion = lu.get();
                    login = true;
                } else {
                    vistaUsuario.mensaje("Credenciales inválidas.");
                }
            } else {
                vistaUsuario.mensaje("Opción inválida. Saliendo...");
                return;
            }

        } else if (opcionInicio == 2) { // 2 = INICIAR SESIÓN
            String correo = vistaUsuario.solicitarCorreo();
            String contrasena = vistaUsuario.solicitarContrasena();

            Optional<Usuario> userOpt = autenticarUsuarioCSV(correo, contrasena);
            if (userOpt.isPresent()) {
                Usuario u = userOpt.get();
                usuarioActual = u;
                usuarioEnSesion = u;
                vistaUsuario.mensaje("Bienvenido, " + u.getNombre());
                login = true;
            } else {
                vistaUsuario.mensaje("Credenciales inválidas.");
            }
        } else {
            vistaUsuario.mensaje("Opción inválida. Saliendo...");
            return;
        }

        if (!login) {
            vistaUsuario.mensaje("No se pudo iniciar sesión. Saliendo...");
            return;
        }

        boolean cierre = false;
        while (!cierre) {
            int opcion = vistaUsuario.verMenu();

            if (opcion == 1) {
                String resultado = registrarObjeto1();
                vistaUsuario.mensaje(resultado);
            }
            else if (opcion == 2) {
                vistaUsuario.mostrarObjetos(listaObjetos);
            }
            else if (opcion == 3) {
                String resultado = buscarObjeto();
                vistaUsuario.mensaje(resultado);
            }
            else if (opcion == 4) {
                vistaUsuario.reclamarObjetoUI();
                vistaUsuario.mensaje("Operación de reclamo finalizada.");
            }
            else if (opcion == 5) {
                vistaUsuario.validarReclamoComoAdmin();
                vistaUsuario.mensaje("Validación finalizada.");
            }
            else if (opcion == 6) {
                vistaUsuario.mostrarPremiosDisponibles(listaPremios);
            }
            else if (opcion == 7) {
                String resultado = entregarPremio();
                vistaUsuario.mensaje(resultado);
            }
            else if (opcion == 8) {
                String resultado = reporte();
                vistaUsuario.mensaje(resultado);
            }
            else if (opcion == 9) {
                // Alterna rol simple: ADMIN <-> USUARIO
                String correo = vistaUsuario.solicitarCorreo();
                Optional<Usuario> uopt = buscarUsuarioPorCorreoCSV(correo);
                if (uopt.isPresent()) {
                    String actual = uopt.get().getRol();
                    String nuevo  = ("ADMIN".equalsIgnoreCase(actual)) ? "USUARIO" : "ADMIN";
                    boolean ok = cambiarRolUsuarioCSV(correo, nuevo);
                    vistaUsuario.mensaje(ok ? "Rol actualizado a " + nuevo : "No se pudo actualizar el rol");
                } else {
                    vistaUsuario.mensaje("No se encontró el usuario con ese correo.");
                }
            }
            else if (opcion == 10) {
                vistaUsuario.mensaje(vista());
            }
            else if (opcion == 11) {
                vistaUsuario.mensaje("Saliendo...");
                cierre = true;
            }
            else {
                vistaUsuario.mensaje("Opción inválida.");
            }
        }
    }

    private String vista() {
        return "Vista en Mantenimiento...  ...";
    }

    // ====== Objetos ======
    private String registrarObjeto1() {
        Objeto objeto = new Objeto(
            vistaUsuario.solicitarDescripcion(),
            vistaUsuario.solicitarTipoObjeto(),
            vistaUsuario.estadoObjeto(),
            // La Vista devuelve LocalDate
            vistaUsuario.solicitarFechaEncontrado(),
            vistaUsuario.solicitarUbicacionObjeto(),
            vistaUsuario.siguienteIdObjeto(),
            vistaUsuario.getCorreo()
        );

        if (registrarObjeto(objeto)) {
            boolean okCSV = insertarObjetoCSV(objeto);
            return okCSV ? "Objeto registrado correctamente y guardado en CSV."
                         : "Objeto registrado, pero error guardando en CSV.";
        } else {
            return "No se pudo registrar el objeto (validación falló).";
        }
    }

    private boolean registrarObjeto(Objeto objeto) {
        if (objeto == null) return false;
        if (objeto.getDescripcion() == null || objeto.getDescripcion().isBlank()) return false;
        if (objeto.getLugarEncontrado() == null || objeto.getLugarEncontrado().isBlank()) return false;
        listaObjetos.add(objeto);
        return true;
    }

    private String verObjetos() {
        StringBuilder sb = new StringBuilder();
        sb.append("Objetos registrados:\n");
        for (Objeto o : listaObjetos) {
            if (o == null) continue;
            sb.append("- [").append(o.getId()).append("] ")
              .append(o.getDescripcion()).append(" | Tipo: ").append(o.getTipo())
              .append(" | Estado: ").append(o.getEstado())
              .append(" | Encontrado: ").append(o.getFechaEncontrado())
              .append(" | Lugar: ").append(o.getLugarEncontrado())
              .append(" | Reportado por: ").append(o.getReportadoPor() == null ? "(desconocido)" : o.getReportadoPor())
              .append("\n");
        }
        return sb.toString();
    }

    private String buscarObjeto() {
        // La Vista no tiene solicitarPatronBusqueda(); usamos filtros existentes
        String tipo = vistaUsuario.filtroTipo();
        LocalDate f1 = vistaUsuario.filtroFecha1();
        LocalDate f2 = vistaUsuario.filtroFecha2();
        String ubic = vistaUsuario.filtroUbicacion();

        List<Objeto> resultados = new ArrayList<>();
        for (Objeto o : listaObjetos) {
            if (o == null) continue;

            boolean ok = true;
            if (tipo != null && !tipo.isBlank()) {
                ok &= (o.getTipo() != null && o.getTipo().equalsIgnoreCase(tipo));
            }
            if (f1 != null) {
                ok &= (o.getFechaEncontrado() != null && !o.getFechaEncontrado().isBefore(f1));
            }
            if (f2 != null) {
                ok &= (o.getFechaEncontrado() != null && !o.getFechaEncontrado().isAfter(f2));
            }
            if (ubic != null && !ubic.isBlank()) {
                ok &= (o.getLugarEncontrado() != null && o.getLugarEncontrado().toLowerCase().contains(ubic.toLowerCase()));
            }
            if (ok) resultados.add(o);
        }

        vistaUsuario.mostrarObjetos(resultados);
        return "Búsqueda finalizada. Total: " + resultados.size();
    }

    // ====== CSV de OBJETOS (permanece en Sistema) ======
    private void asegurarCSVObjetosConCabecera() {
        if (objetosCSV == null) objetosCSV = new ObjetosCSV(rutaCSVObjetos);
        objetosCSV.asegurarCSVObjetosConCabecera();
    }

    public List<Objeto> leerObjetosDesdeCSV() {
        if (objetosCSV == null) objetosCSV = new ObjetosCSV(rutaCSVObjetos);
        return objetosCSV.leerObjetosDesdeCSV();
    }

    private boolean insertarObjetoCSV(Objeto o) {
        if (objetosCSV == null) objetosCSV = new ObjetosCSV(rutaCSVObjetos);
        return objetosCSV.insertarObjetoCSV(o);
    }

    private boolean reescribirObjetosCSV() {
        if (objetosCSV == null) objetosCSV = new ObjetosCSV(rutaCSVObjetos);
        return objetosCSV.reescribirObjetosCSV(listaObjetos);
    }

    // ====== Usuarios (con UsuariosCSV) ======
    private void crearAdminPorDefectoSiVacio() {
        if (!hayUsuariosCSV()) {
            boolean ok = insertarUsuarioCSV("Admin", "admin@uvg.edu.gt", "1234", "ADMIN");
            if (!ok) uiError(" No se pudo crear el admin por defecto.");
        }
    }

    public Optional<Usuario> autenticarUsuarioCSV(String correo, String contrasenaPlano) {
        Optional<Usuario> u = buscarUsuarioPorCorreoCSV(correo);
        if (u.isPresent() && u.get().getContrasena().equals(contrasenaPlano)) {
            Usuario usuario = u.get();
            if (usuario.esAdmin()) {
                Administrador admin = new Administrador(
                    usuario.getIdUsuario(),
                    usuario.getNombre(),
                    usuario.getCorreo(),
                    usuario.getContrasena(),
                    "ADMIN" + usuario.getIdUsuario()
                );
                this.usuarioActual = admin;
                this.usuarioEnSesion = admin;
                return Optional.of(admin);
            } else {
                this.usuarioActual = usuario;
                this.usuarioEnSesion = usuario;
                return u;
            }
        }
        return Optional.empty();
    }

    public boolean cambiarRolUsuarioCSV(String correoInstitucional, String nuevoRol) {
        if (correoInstitucional == null || nuevoRol == null) return false;

        // Buscar en memoria
        Usuario target = null;
        for (Usuario u : listaUsuarios) {
            if (u != null && correoInstitucional.equalsIgnoreCase(u.getCorreo())) {
                target = u;
                break; // mantener simple
            }
        }
        if (target == null) {
            Optional<Usuario> uopt = buscarUsuarioPorCorreoCSV(correoInstitucional);
            if (uopt.isPresent()) {
                target = uopt.get();
                listaUsuarios.add(target);
            }
        }
        if (target == null) return false;

        target.setRol(nuevoRol);

        // Persistir en CSV sin perder 'creadoEn'
        boolean ok = usuariosCSV.actualizarRolPorCorreo(target.getCorreo(), target.getRol());
        if (!ok) uiError("No se pudo actualizar el rol en CSV.");
        return ok;
    }

    // ====== Envoltorios (compatibilidad) ======
    private void asegurarCSVUsuariosConCabecera() {
        usuariosCSV.asegurarArchivoConCabecera();
    }

    public Optional<Usuario> buscarUsuarioPorCorreoCSV(String correoBuscado) {
        return usuariosCSV.buscarPorCorreo(correoBuscado);
    }

    public boolean insertarUsuarioCSV(String nombre, String correo, String contrasena, String rol) {
        return usuariosCSV.insertarUsuario(nombre, correo, contrasena, rol);
    }

    private boolean hayUsuariosCSV() {
        return usuariosCSV.hayUsuarios();
    }

    // Antes reescribía usuarios.csv perdiendo 'creadoEn'. Se deja por compatibilidad, pero no se usa para cambio de rol.
    private boolean reescribirUsuariosCSV() {
        try {
            Path p = Paths.get("usuarios.csv");
            StringBuilder sb = new StringBuilder();
            sb.append("idUsuario,nombre,correo,contrasena,rol\n");
            for (Usuario u : listaUsuarios) {
                if (u == null) continue;
                sb.append(u.getIdUsuario()).append(",")
                  .append(esc(u.getNombre())).append(",")
                  .append(esc(u.getCorreo())).append(",")
                  .append(esc(u.getContrasena())).append(",")
                  .append(esc(u.getRol()))
                  .append("\n");
            }
            Files.write(p, sb.toString().getBytes(StandardCharsets.UTF_8),
                        StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            return true;
        } catch (Exception e) {
            uiError("Error reescribiendo usuarios.csv: " + e.getMessage());
            return false;
        }
    }

    // ====== Métodos que usa la Vista ======
    public List<Objeto> obtenerObjetosEnMemoria() {
        return this.listaObjetos;
    }

    // requerido por VistaUsuario
    public List<Objeto> filtrarPorEstado(List<Objeto> entrada, String estado) {
        List<Objeto> out = new ArrayList<>();
        if (entrada == null) return out;
        for (Objeto o : entrada) {
            if (o == null) continue;
            if (estado == null || estado.isBlank() || estado.equalsIgnoreCase(o.getEstado())) {
                out.add(o);
            }
        }
        return out;
    }

    public boolean reclamarObjeto(int idObj, Usuario solicitante) {
        if (solicitante == null) return false;
        for (Objeto o : listaObjetos) {
            if (o == null) continue;
            if (o.getId() == idObj) {
                if (!Objeto.ESTADO_ENCONTRADO.equalsIgnoreCase(o.getEstado())) return false;
                o.setEstado(Objeto.ESTADO_PENDIENTE_VALIDACION);
                o.setUsuarioQueReclama(solicitante.getCorreo());
                return reescribirObjetosCSV();
            }
        }
        return false;
    }

    public List<Objeto> obtenerObjetosPendientesValidacion() {
        List<Objeto> out = new ArrayList<>();
        for (Objeto o : listaObjetos) {
            if (o != null && Objeto.ESTADO_PENDIENTE_VALIDACION.equalsIgnoreCase(o.getEstado())) {
                out.add(o);
            }
        }
        return out;
    }

    public boolean validarReclamoObjeto(int idObj, Usuario administrador) {
        if (administrador == null || !administrador.puedeValidarReclamos()) return false;
        for (Objeto o : listaObjetos) {
            if (o == null) continue;
            if (o.getId() == idObj) {
                if (!Objeto.ESTADO_PENDIENTE_VALIDACION.equalsIgnoreCase(o.getEstado())) return false;
                o.setEstado(Objeto.ESTADO_RECUPERADO);
                o.setFechaDevolucion(LocalDate.now());
                return reescribirObjetosCSV();
            }
        }
        return false;
    }

    public boolean eliminarObjetoPorId(int id) {
        boolean removed = listaObjetos.removeIf(o -> o != null && o.getId() == id);
        return removed && reescribirObjetosCSV();
    }

    // ====== Premios y Reporte (faltaban) ======
    private void registrarPremio(Premio premio) {
        if (premio != null) listaPremios.add(premio);
    }

    private String entregarPremio() {
        if (usuarioActual == null) return "No hay usuario en sesión.";
        if (!usuarioActual.esAdmin()) return "Solo ADMIN puede entregar premios.";
        return "Premio entregado (simulado).";
    }

    private String reporte() {
        return "Reporte generado (simulado).";
    }

    // ====== utilidades simples ======
    private static String[] parseCSVLine(String line) {
        if (line == null) return new String[0];
        ArrayList<String> out = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '\"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '\"') { sb.append('\"'); i++; }
                else { inQuotes = !inQuotes; }
            } else if (c == ',' && !inQuotes) {
                out.add(sb.toString()); sb.setLength(0);
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

    private void uiError(String msg) {
        if (this.vistaUsuario != null) this.vistaUsuario.error(msg);
    }
    private void uiInfo(String msg) {
        if (this.vistaUsuario != null) this.vistaUsuario.mensaje(msg);
    }
}