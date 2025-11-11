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
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class Sistema {
    private List<Objeto> listaObjetos;
    private List<Usuario> listaUsuarios;
    private List<Premio> listaPremios;
    private List<Administrador> listaAdministradores;
    private VistaUsuario vistaUsuario;
    private Usuario usuarioActual;
    private Usuario usuarioEnSesion;

    public static final String ROL_ADMIN      = "ADMIN";
    public static final String ROL_ESTUDIANTE = "ESTUDIANTE";

    public static final String ACCION_RECLAMAR_OBJETO  = "RECLAMAR_OBJETO";
    public static final String ACCION_GESTION_OBJETOS  = "GESTION_OBJETOS";
    public static final String ACCION_GESTION_PREMIOS  = "GESTION_PREMIOS";
    public static final String ACCION_REPORTES         = "REPORTES";
    public static final String ACCION_ELIMINAR_OBJETOS = "ELIMINAR_OBJETOS";
    private static final int PUNTOS_REPORTE_OBJETO = 10;
    private static final int PUNTOS_REPORTE_ENCONTRADO = 10;

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
        listaObjetos         = leerObjetosDesdeCSV();
        listaUsuarios        = new ArrayList<>();
        listaPremios         = new ArrayList<>();
        listaAdministradores = new ArrayList<>();
        vistaUsuario         = new VistaUsuario(this);
        usuariosCSV          = new UsuariosCSV(rutaCSVUsuarios);

        // Preparar CSV
        asegurarCSVUsuariosConCabecera(); // delega a UsuariosCSV
        asegurarCSVObjetosConCabecera();

        // Admin por defecto si no hay usuarios
        crearAdminPorDefectoSiVacio();
        registrarPremio(new Premio("Descuento Cafetería", "10% en comida", 50));
    }

    // ====== Flujo principal ======
    public void iniciarSistema() 
    {
        boolean login = false;

        while(!login)
        {

        int opcionInicio = vistaUsuario.IniciarVistaUsuario();

        switch (opcionInicio){ 
        case 1: // 1 = REGISTRARSE
            
            
            String nombre = vistaUsuario.solicitarNombrePersona();
            String correo = vistaUsuario.solicitarCorreo();
            String contrasena = vistaUsuario.solicitarContrasena();

            boolean ok = insertarUsuarioCSV(nombre, correo, cifrarMD5(contrasena), "USUARIO");
            if (ok) 
            {
                buscarUsuarioPorCorreoCSV(correo).ifPresent(listaUsuarios::add);
                vistaUsuario.mensaje("Registro exitoso. Ahora inicia sesión.");
            } 
            else
            {
                vistaUsuario.mensaje("No fue posible registrar al usuario. Intente iniciar sesión o volver a registrarse.");
            }
            break;

        case 2:
        // 2 = INICIAR SESIÓN
            String correo1 = vistaUsuario.solicitarCorreo();
            String contrasena1 = vistaUsuario.solicitarContrasena();

            Optional<Usuario> userOpt = autenticarUsuarioCSV(correo1, cifrarMD5(contrasena1));
            if (userOpt.isPresent()) 
            {
                Usuario u = userOpt.get();
                usuarioActual = u;
                usuarioEnSesion = u;
                vistaUsuario.mensaje("Bienvenido, " + u.getNombre());
                donarObjetosVencidos(); //regla de 6 meses
                List<String> avisos = generarNotificacionesInicioSesion(u);   // NUEVO
                if (vistaUsuario != null) vistaUsuario.mostrarNotificaciones(avisos); // NUEVO
                login = true;
            } 
            else 
            {
                vistaUsuario.mensaje("Credenciales inválidas, intentelo de nuevo.");
            }
            break;
        
        case 3: // SALIR

            vistaUsuario.mensaje("Saliendo. ¡Hasta luego!");
            return;
        
        default:
        
            vistaUsuario.mensaje("Opción inválida, intente de nuevo");
            break;
        }    
    }
        

        boolean cierre = false;
        while (!cierre) {
        int opcion = vistaUsuario.verMenu();

        switch (opcion) {
            case 1: // Reportar objeto perdido/encontrado
                String resultado = registrarObjeto1();
                if (!"Operación cancelada.".equals(resultado)) {
                vistaUsuario.mensaje(resultado);
            }
            break;

                
            case 2: // Búsqueda de objetos encontrados
                switch (vistaUsuario.verFiltros()) {
                    case 1:
                        String tipoFiltro = vistaUsuario.filtroTipo();
                        List<Objeto> listafiltradaT = filtrarPorTipo(listaObjetos, tipoFiltro);
                        vistaUsuario.mostrarObjetos(listafiltradaT);
                        break;
                    case 2:
                        LocalDate fecha1 = vistaUsuario.filtroFecha1();
                        LocalDate fecha2 = vistaUsuario.filtroFecha2();
                        List<Objeto> listafiltradaF = filtrarPorFechaEncontrado(listaObjetos, fecha1, fecha2);
                        vistaUsuario.mostrarObjetos(listafiltradaF);
                        break;
                    case 3:
                        String ubicacion = vistaUsuario.filtroUbicacion();
                        if (ubicacion == null) {              //si el usuario regresa
                            vistaUsuario.mensaje("Operación cancelada.");
                            break;
                        }
                        List<Objeto> listafiltradaU = filtrarPorUbicacion(listaObjetos, ubicacion);
                        vistaUsuario.mostrarObjetos(listafiltradaU);
                        break;
                    case 4:
                        vistaUsuario.mensaje("Volviendo al menú principal...");
                        break;
                    default:
                        vistaUsuario.mensaje("Opción no válida. Intente de nuevo.");
                        break;
                }
                break;
                
            case 3: // Validación y reclamo de objeto
                vistaUsuario.reclamarObjetoUI();
                break;
                
            case 4: // Canjear premios
                Optional<Usuario> usuarioOpt = buscarUsuarioPorCorreoCSV(vistaUsuario.getCorreo());
                if (usuarioOpt.isPresent()) {
                    canjearPremio(usuarioOpt.get());
                } else {
                    vistaUsuario.mensaje("Error: no se encontró el usuario en sesión.");
                }
                break;
                
            case 5:
                if (usuarioActual != null) {
                    vistaUsuario.mostrarPerfilUsuario(usuarioActual);
                } else {
                    vistaUsuario.mensaje("Error: no hay usuario en sesión.");
                }
                break;
                
            case 6: // Eliminar objetos perdidos
                if (!tienePermiso(usuarioActual, ACCION_ELIMINAR_OBJETOS)) {
                    vistaUsuario.mensaje("No tienes permiso para eliminar objetos.");
                } else {
                    vistaUsuario.eliminarObjetoUI();
                }
                break;
                
            case 7: // Salir
                cierre = true;
                vistaUsuario.mensaje("Saliendo del sistema. ¡Hasta luego!");
                break;
                
            default:
                vistaUsuario.mensaje("Opción inválida.");
                break;
        }
    }
    }


    public String vista() {
        return "Vista en Mantenimiento...  ...";
    }
    private void otorgarPuntosSiEncontrado(Objeto objeto) {
        if (objeto == null) return;
        if (!Objeto.ESTADO_ENCONTRADO.equalsIgnoreCase(objeto.getEstado())) return;

        if (usuarioActual != null) {
            usuarioActual.sumarPuntos(PUNTOS_REPORTE_ENCONTRADO);
            // Persistir al CSV (migrará header si hace falta)
            usuariosCSV.actualizarPuntosPorCorreo(usuarioActual.getCorreo(), usuarioActual.getPuntos());
            uiInfo("Has ganado " + PUNTOS_REPORTE_ENCONTRADO + " puntos. Total: " + usuarioActual.getPuntos());
        }
    }

    // ====== Objetos ======
    public String registrarObjeto1() 
    {
            String descripcion = vistaUsuario.solicitarDescripcion();
    if (descripcion == null) return "Operación cancelada.";

    String tipo = vistaUsuario.solicitarTipoObjeto();
    if (tipo == null) return "Operación cancelada.";

    String estado = vistaUsuario.estadoObjeto();
    if (estado == null) return "Operación cancelada.";

    LocalDate fecha = vistaUsuario.solicitarFechaEncontrado();
    if (fecha == null) return "Operación cancelada.";

    String lugar = vistaUsuario.solicitarUbicacionObjeto();
    if (lugar == null) return "Operación cancelada.";
    
        Objeto objeto = new Objeto(
            descripcion,
            tipo,
            estado,
            fecha,
            lugar,
            vistaUsuario.siguienteIdObjeto(),
            vistaUsuario.getCorreo()
        );

        if (registrarObjeto(objeto)) 
        {
            boolean okCSV = insertarObjetoCSV(objeto);
            if (okCSV) {
                // sumar y persistir SOLO si es 'encontrado'
                otorgarPuntosSiEncontrado(objeto);
                usuariosCSV.actualizarPuntosPorCorreo(usuarioActual.getCorreo(), usuarioActual.getPuntos());
                return "Objeto registrado correctamente y guardado en CSV.";
            } else {
                return "Objeto registrado, pero error guardando en CSV.";
            }
        } 
        else 
        {
            return "No se pudo registrar el objeto (validación falló).";
        }
    }

    public boolean registrarObjeto(Objeto objeto) {
        if (objeto == null) return false;
        if (objeto.getDescripcion() == null || objeto.getDescripcion().isBlank()) return false;
        if (objeto.getLugarEncontrado() == null || objeto.getLugarEncontrado().isBlank()) return false;
        listaObjetos.add(objeto);
        return true;
    }

    public String verObjetos() {
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

    public String buscarObjeto() {
        // La Vista no tiene solicitarPatronBusqueda(); usamos filtros existentes
        String tipo = vistaUsuario.filtroTipo();
        LocalDate f1 = vistaUsuario.filtroFecha1();
        LocalDate f2 = vistaUsuario.filtroFecha2();
        String ubic = vistaUsuario.filtroUbicacion();
        if (ubic == null) return "Operación cancelada.";  // o maneja el regreso

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
    public void asegurarCSVObjetosConCabecera() {
        try {
            Files.createDirectories(rutaCSVObjetos.getParent());
            if (Files.notExists(rutaCSVObjetos)) {
                try (BufferedWriter bw = Files.newBufferedWriter(
                        rutaCSVObjetos, StandardCharsets.UTF_8, StandardOpenOption.CREATE)) {
                    bw.write("id,descripcion,tipo,estado,fechaEncontrado,lugarEncontrado,fechaDevolucion,reportadoPor,usuarioQueReclama");
                    bw.newLine();
                }
            }
        } catch (IOException e) {
            uiError("Error preparando CSV de objetos: " + e.getMessage());
        }
    }

    public List<Objeto> leerObjetosDesdeCSV() {
        List<Objeto> out = new ArrayList<>();
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
                    String fechaEncontrado = p[4];
                    String lugarEncontrado = p[5];
                    String fechaDevolucion = p[6];
                    String reportadoPor = p[7];
                    String usuarioQueReclama = p[8];

                    Objeto o = new Objeto(
                        descripcion,
                        tipo,
                        estado,
                        (fechaEncontrado == null || fechaEncontrado.isBlank() ? null : LocalDate.parse(fechaEncontrado)),
                        lugarEncontrado,
                        id,
                        reportadoPor
                    );

                    if (fechaDevolucion != null && !fechaDevolucion.isBlank()) {
                        o.setFechaDevolucion(LocalDate.parse(fechaDevolucion));
                    }
                    if (usuarioQueReclama != null && !usuarioQueReclama.isBlank()) {
                        o.setUsuarioQueReclama(usuarioQueReclama);
                        if ("pendiente_validacion".equalsIgnoreCase(estado)) {
                            o.setEstado(Objeto.ESTADO_PENDIENTE_VALIDACION);
                        }
                    }
                    out.add(o);
                } catch (Exception ignore) {
                    // línea inválida: se ignora
                }
            }
        } catch (IOException e) {
            uiError("Error leyendo CSV de objetos: " + e.getMessage());
        }
        return out;
    }

    public boolean insertarObjetoCSV(Objeto o) {
        if (o == null) return false;
        String fila = String.join(",",
                String.valueOf(o.getId()),
                esc(o.getDescripcion()),
                esc(o.getTipo()),
                esc(o.getEstado()),
                esc(o.getFechaEncontrado() == null ? "" : o.getFechaEncontrado().toString()),
                esc(o.getLugarEncontrado()),
                esc(o.getFechaDevolucion() == null ? "" : o.getFechaDevolucion().toString()),
                esc(o.getReportadoPor() == null ? "" : o.getReportadoPor()),
                esc(o.getUsuarioQueReclama() == null ? "" : o.getUsuarioQueReclama())
        );
        try (BufferedWriter bw = Files.newBufferedWriter(
                rutaCSVObjetos, StandardCharsets.UTF_8,
                StandardOpenOption.APPEND, StandardOpenOption.CREATE)) {
            bw.write(fila);
            bw.newLine();
            return true;
        } catch (IOException e) {
            uiError("Error guardando objeto en CSV: " + e.getMessage());
            return false;
        }
    }

    public boolean reescribirObjetosCSV() {
        Path src = rutaCSVObjetos;
        Path tmp = src.resolveSibling(src.getFileName().toString() + ".tmp");
        try (BufferedWriter bw = Files.newBufferedWriter(tmp, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {

            bw.write("id,descripcion,tipo,estado,fechaEncontrado,lugarEncontrado,fechaDevolucion,reportadoPor,usuarioQueReclama");
            bw.newLine();

            for (Objeto o : listaObjetos) {
                if (o == null) continue;
                bw.write(String.join(",",
                    String.valueOf(o.getId()),
                    esc(o.getDescripcion()),
                    esc(o.getTipo()),
                    esc(o.getEstado()),
                    esc(o.getFechaEncontrado() == null ? "" : o.getFechaEncontrado().toString()),
                    esc(o.getLugarEncontrado()),
                    esc(o.getFechaDevolucion() == null ? "" : o.getFechaDevolucion().toString()),
                    esc(o.getReportadoPor() == null ? "" : o.getReportadoPor()),
                    esc(o.getUsuarioQueReclama() == null ? "" : o.getUsuarioQueReclama())
                ));
                bw.newLine();
            }
        } catch (IOException e) {
            uiError("Error preparando escritura de objetos: " + e.getMessage());
            return false;
        }

        try {
            Files.move(tmp, src, java.nio.file.StandardCopyOption.REPLACE_EXISTING, java.nio.file.StandardCopyOption.ATOMIC_MOVE);
            return true;
        } catch (IOException e) {
            uiError("Error finalizando escritura de objetos: " + e.getMessage());
            return false;
        }
    }

    public void donarObjetosVencidos() {
        boolean huboCambios = false;
        LocalDate hoy = LocalDate.now();

        for (Objeto o : listaObjetos) {
            if (o == null) continue;
            // Regla: 6 meses (exclusiva: dona al día siguiente de cumplirse 6 meses)
            if (Objeto.ESTADO_ENCONTRADO.equalsIgnoreCase(o.getEstado())
                && o.getFechaEncontrado() != null
                && o.getFechaEncontrado().plusMonths(6).isBefore(hoy)) {
                o.setEstadoDonado(hoy);
                huboCambios = true;
            }
        }
        if (huboCambios) {
            reescribirObjetosCSV();
        }
    }
    // Notificaciones al iniciar sesión según lo que el usuario reportó como PERDIDO
    public List<String> generarNotificacionesInicioSesion(Usuario u) {
        List<String> out = new ArrayList<>();
        if (u == null || listaObjetos == null || listaObjetos.isEmpty()) return out;

        // 1) Detectar categorías (tipos) que este usuario reportó como PERDIDO
        Set<String> tiposInteres = new HashSet<>();
        String correo = u.getCorreo() == null ? "" : u.getCorreo().trim();
        for (Objeto o : listaObjetos) {
            if (o == null) continue;
            if (Objeto.ESTADO_PERDIDO.equalsIgnoreCase(trim(o.getEstado()))
                    && correo.equalsIgnoreCase(trim(o.getReportadoPor()))
                    && !trim(o.getTipo()).isEmpty()) {
                tiposInteres.add(normalizarTipo(o.getTipo()));
            }
        }
        if (tiposInteres.isEmpty()) return out;

        // 2) Para cada tipo PERDIDO del usuario, contar ENCONTRADOS de ese tipo
        for (String tipo : tiposInteres) {
            int encontrados = 0;
            for (Objeto o : listaObjetos) {
                if (o == null) continue;
                if (Objeto.ESTADO_ENCONTRADO.equalsIgnoreCase(trim(o.getEstado()))
                        && normalizarTipo(o.getTipo()).equals(tipo)) {
                    encontrados++;
                }
            }
            if (encontrados > 0) {
                out.add(mensajePorTipo(tipo, encontrados));
            }
        }
        return out;
    }

    // Helpers de formato
    private static String trim(String s) { return s == null ? "" : s.trim(); }

    // Normaliza categorías conocidas para evitar variaciones (e.g., "recipiente" vs "recipientes")
    private static String normalizarTipo(String tipoRaw) {
        String t = trim(tipoRaw).toLowerCase();
        if (t.startsWith("electr")) return "electronico";
        if (t.startsWith("accesor")) return "accesorio";
        if (t.startsWith("doc"))     return "documento";
        if (t.startsWith("ropa"))    return "ropa";
        if (t.startsWith("util"))    return "utiles";
        if (t.startsWith("recip"))   return "recipientes";
        return t.isEmpty() ? "otros" : t;
    }

    // Mensaje por categoría (incluye el copy especial que pediste)
    private static String mensajePorTipo(String tipo, int count) {
        switch (tipo) {
            case "electronico":
                return "Nuevos dispositivos electrónicos encontrados, ¡revisa!";
            case "accesorio":
                return "Nuevos accesorios encontrados, ¡revisa!";
            case "documento":
                return "Nuevos documentos encontrados, ¡revisa!";
            case "ropa":
                return "Nueva ropa encontrada, ¡revisa!";
            case "utiles":
                return "Nuevos útiles encontrados, ¡revisa!";
            case "recipientes":
                return "Nuevos recipientes (pachones, yetis, etc.) encontrados, ¡revisa!";
            default:
                return "Hay " + count + " objetos encontrados del tipo '" + tipo + "'. ¡Revisa la búsqueda!";
        }
    }

    // ====== Usuarios (con UsuariosCSV) ======
    public void crearAdminPorDefectoSiVacio() {
        if (!hayUsuariosCSV()) {
            boolean ok = insertarUsuarioCSV("Admin", "admin@uvg.edu.gt", cifrarMD5("1234"), "ADMIN");
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

    private Usuario buscarUsuarioPorCorreo(String correo) {
        if (correo == null || listaUsuarios == null) return null;
        for (Usuario u : listaUsuarios) {
            if (u != null && u.getCorreo() != null && u.getCorreo().equalsIgnoreCase(correo)) {
                return u;
            }
        }
        return null;
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
    public void asegurarCSVUsuariosConCabecera() {
        usuariosCSV.asegurarArchivoConCabecera();
    }

    public Optional<Usuario> buscarUsuarioPorCorreoCSV(String correoBuscado) {
        return usuariosCSV.buscarPorCorreo(correoBuscado);
    }

    public boolean insertarUsuarioCSV(String nombre, String correo, String contrasena, String rol) {
        return usuariosCSV.insertarUsuario(nombre, correo, contrasena, rol);
    }

    public boolean hayUsuariosCSV() {
        return usuariosCSV.hayUsuarios();
    }

    // Antes reescribía usuarios.csv perdiendo 'creadoEn'. Se deja por compatibilidad, pero no se usa para cambio de rol.
    public boolean reescribirUsuariosCSV() {
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
    public List<Objeto> obtenerObjetosEnMemoria() 
    {
        return this.listaObjetos;
    }


    //método para calcular id
    public int siguienteIdObjeto() {
        int max = 0;
        if (listaObjetos != null) {
            for (Objeto o : listaObjetos) {
                if (o == null) continue;
                try {
                    int id = o.getId();
                    if (id > max) max = id;
                } catch (Exception ignore) {}
            }
        }
        return max + 1;
    }

    // requerido por VistaUsuario
    public List<Objeto> filtrarPorEstado(List<Objeto> entrada, String estado) {
        List<Objeto> out = new ArrayList<>();
        if (entrada == null) return out;

        String target = (estado == null ? "" : estado.trim());
        for (Objeto o : entrada) {
            if (o == null) continue;
            String est = (o.getEstado() == null ? "" : o.getEstado().trim());
            if (target.isBlank() || est.equalsIgnoreCase(target)) {
                out.add(o);
            }
        }
        return out;
    }


    private List<Objeto> filtrarPorTipo(List<Objeto> objetos, String tipo) 
    {
        List<Objeto> resultado = new ArrayList<>();
        if (objetos == null || tipo == null || tipo.isBlank()) 
        {
            return resultado;
        }
        
        String tipoBuscado = tipo.trim().toLowerCase();
        for (Objeto obj : objetos) 
        {
            if (obj == null) continue;
            
            if (!Objeto.ESTADO_ENCONTRADO.equalsIgnoreCase(obj.getEstado())) continue;

            if (obj.getTipo() != null && obj.getTipo().toLowerCase().contains(tipoBuscado)) {
                resultado.add(obj);
            }

        }
        return resultado;
    }

    public List<Objeto> filtrarPorFechaEncontrado(List<Objeto> objetos, LocalDate fechaInicio, LocalDate fechaFin) 
    {
    List<Objeto> resultado = new ArrayList<>();
    for (Objeto obj : objetos) {
        if (obj != null 
            && obj.getFechaEncontrado() != null
            && obj.getEstado().equalsIgnoreCase(Objeto.ESTADO_ENCONTRADO)) { 

            LocalDate fechaObj = obj.getFechaEncontrado();
            if ((fechaInicio == null || !fechaObj.isBefore(fechaInicio)) &&
                (fechaFin == null || !fechaObj.isAfter(fechaFin))) {
                resultado.add(obj);
            }
        }
    }
    return resultado;
    }

    public List<Objeto> filtrarPorUbicacion(List<Objeto> objetos, String ubicacion) 
    {
        List<Objeto> resultado = new ArrayList<>();
        for (Objeto obj : objetos) {
            if (obj != null 
                && obj.getLugarEncontrado() != null
                && obj.getLugarEncontrado().toLowerCase().contains(ubicacion.toLowerCase()) // 🔹 permite coincidencias parciales
                && obj.getEstado().equalsIgnoreCase(Objeto.ESTADO_ENCONTRADO)) { // 🔹 solo "sin dueño"
                resultado.add(obj);
        }
        }
        return resultado;
    }

    public boolean reclamarObjeto(int idObj, Usuario solicitante) 
    {
        if (solicitante == null) return false;
        for (Objeto o : listaObjetos) 
        {
            if (o == null) continue;
            if (o.getId() == idObj) 
            {
                if (!Objeto.ESTADO_ENCONTRADO.equalsIgnoreCase(o.getEstado())) return false;
                o.setEstado(Objeto.ESTADO_PENDIENTE_VALIDACION);
                o.setUsuarioQueReclama(solicitante.getCorreo());
                return reescribirObjetosCSV();
            }
        }
        return false;
    }

    public List<Objeto> obtenerObjetosPendientesValidacion() 
    {
        List<Objeto> out = new ArrayList<>();
        for (Objeto o : listaObjetos) 
        {
            if (o != null && Objeto.ESTADO_PENDIENTE_VALIDACION.equalsIgnoreCase(o.getEstado())) 
            {
                out.add(o);
            }
        }
        return out;
    }

    public boolean validarReclamoObjeto(int idObj, Usuario administrador) 
    {
        if (administrador == null || !administrador.puedeValidarReclamos()) return false;
        for (Objeto o : listaObjetos) 
        {
            if (o == null) continue;
            if (o.getId() == idObj) 
            {
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
    public void registrarPremio(Premio premio) {
        if (premio != null) listaPremios.add(premio);
    }

    public String entregarPremio() {
        if (usuarioActual == null) return "No hay usuario en sesión.";
        if (!usuarioActual.esAdmin()) return "Solo ADMIN puede entregar premios.";
        return "Premio entregado (simulado).";
    }

    public String reporte() {
        return "Reporte generado (simulado).";
    }

    // ====== utilidades simples ======
    public static String[] parseCSVLine(String line) {
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

    public static String esc(String string) {
        if (string == null) return "";
        boolean q = string.contains(",") || string.contains("\"") || string.contains("\n") || string.contains("\r");
        String out = string.replace("\"", "\"\"");
        return q ? "\"" + out + "\"" : out;
    }

    public void uiError(String msg) {
        if (this.vistaUsuario != null) this.vistaUsuario.error(msg);
    }
    public void uiInfo(String msg) {
        if (this.vistaUsuario != null) this.vistaUsuario.mensaje(msg);
    }

    public void canjearPremio(Usuario usuario)
    {
    if (listaPremios.isEmpty()) {
        vistaUsuario.mensaje("No hay premios disponibles para canjear.");
        return;
    }

    vistaUsuario.mostrarPremiosDisponibles(listaPremios);
    int opcion = vistaUsuario.elegirPremio();

    if (opcion < 1 || opcion > listaPremios.size()) {
        vistaUsuario.mensaje("Opción inválida.");
        return;
    }

    Premio premioSeleccionado = listaPremios.get(opcion - 1);

    if (usuario.getPuntos() >= premioSeleccionado.getPuntos()) {
        usuario.restarPuntos(premioSeleccionado.getPuntos());
        usuario.agregarPremio(premioSeleccionado);
        vistaUsuario.mensaje("¡Canje exitoso! Has obtenido: " + premioSeleccionado.getNombre());
    } else {
        vistaUsuario.mensaje("No tienes suficientes puntos para este premio.");
    }
    }

    private void otorgarPuntosPorReporte(Objeto objeto) {
        if (objeto == null) return;
        if (usuarioActual != null) {
            usuarioActual.sumarPuntos(PUNTOS_REPORTE_OBJETO);
            if (vistaUsuario != null) {
                vistaUsuario.mensaje("Has ganado " + PUNTOS_REPORTE_OBJETO + " puntos. Total: " + usuarioActual.getPuntos());
            }
        }
    }
 
    private static String cifrarMD5(String texto) //metodo para encriptacion en MD5 de las passwords
    {
    if (texto == null || texto.isBlank()) return "";
    try 
        {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(texto.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) 
            {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } 
    catch (NoSuchAlgorithmException e) 
        {
            return texto; // devuelve texto plano si falla
        }
    }
}

