import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class Sistema {
    private List<Objeto> listaObjetos;
    private List<Usuario> listaUsuarios;
    private List<Premio> listaPremios;
    private List<Administrador> listaAdministradores;
    private VistaUsuario vistaUsuario;
    private Usuario usuarioActual;
    private Usuario usuarioEnSesion;

    public static final String ROL_ADMIN = "ADMIN";
    public static final String ROL_ESTUDIANTE = "ESTUDIANTE";


    public static final String ACCION_RECLAMAR_OBJETO = "RECLAMAR_OBJETO";
    public static final String ACCION_GESTION_OBJETOS = "GESTION_OBJETOS";
    public static final String ACCION_GESTION_PREMIOS  = "GESTION_PREMIOS";
    public static final String ACCION_REPORTES         = "REPORTES";
    public static final String ACCION_ELIMINAR_OBJETOS = "ELIMINAR_OBJETOS";

    public Usuario getUsuarioActual() {
        return this.usuarioActual;
    }

    public void setUsuarioActual(Usuario u) {
        this.usuarioActual = u;
    }

    public boolean esAdminSesion() {
        return usuarioEnSesion != null 
            && "ADMIN".equalsIgnoreCase(usuarioEnSesion.getRol());
    }

    public boolean tienePermiso(Usuario u, String accion) {
        if (u == null || accion == null) return false;

        if (u.esAdmin()) {
            return true;
        }

        if (u.esEstudiante()) {
            return ACCION_RECLAMAR_OBJETO.equals(accion);
        }
        return false;
    }

    // CSV de usuarios
    private final Path rutaCSVUsuarios = Paths.get("data", "usuarios.csv");
    private final Path rutaCSVObjetos  = Paths.get("data", "objetos.csv");

    public Sistema() {
        listaObjetos = leerObjetosDesdeCSV();
        listaUsuarios = new ArrayList<>();
        listaPremios = new ArrayList<>();
        listaAdministradores = new ArrayList<>();
        vistaUsuario = new VistaUsuario();

        // Preparar “BD” CSV
        asegurarCSVUsuariosConCabecera();
        asegurarCSVObjetosConCabecera();
    }

    public void iniciarSistema() {
        int opcionInicio = vistaUsuario.IniciarVistaUsuario();
        // Si no hay usuarios, crea un admin por defecto
        crearAdminPorDefectoSiVacio();
        registrarPremio(new Premio("Descuento Cafetería", "10% en comida", 50));
        registrarPremio(new Premio("Horas Beca", "5 horas beca", 250));

        boolean login = false;
        if (opcionInicio == 1) {
            // Registro con rol predeterminado USUARIO
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
            login = vistaUsuario.mostrarLoginConsola(this);
        } else if (opcionInicio == 2) {
            login = vistaUsuario.mostrarLoginConsola(this);
        } else {
            vistaUsuario.mensaje("Opción inválida. Saliendo...");
            return;
        }

        if (!login) 
        {
        vistaUsuario.mensaje("No se pudo iniciar sesión. Saliendo del sistema.");
        return;
        }

        boolean cierre = false;

        while (!cierre) 
        {
        if (login) 
            {
                int opcion = vistaUsuario.verMenu();

                if(opcion==1)  
                {
                    String resultado = registrarObjeto1();
                    vistaUsuario.mensaje(resultado);
                }
                else if(opcion==2)  
                {
                    switch (vistaUsuario.verFiltros()) {
                        case 1:
                            String tipo = vistaUsuario.filtroTipo();
                            List<Objeto> listafiltradaT = filtrarPorTipo(listaObjetos, tipo);
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
                            List<Objeto> listafiltradaU = filtrarPorUbicacion(listaObjetos, ubicacion);
                            vistaUsuario.mostrarObjetos(listafiltradaU);
                            
                        case 4:
                            vistaUsuario.mensaje("Volviendo al menú principal...");
                            return;
                        default:
                            vistaUsuario.mensaje("Opción no válida. Intente de nuevo.");
                            break;
                    }
                }
                else if(opcion==3)  
                {
                    vistaUsuario.reclamarObjetoUI();
                }
                else if(opcion==4)  
                {
                    Optional<Usuario> usuarioOpt = buscarUsuarioPorCorreoCSV(vistaUsuario.getCorreo());
                    if (usuarioOpt.isPresent()) {
                        canjearPremio(usuarioOpt.get());
                    } else {
                        vistaUsuario.mensaje("Error: no se encontró el usuario en sesión.");
                    }
                }
                else if(opcion==5)  
                {
                    Optional<Usuario> usuarioOpt = buscarUsuarioPorCorreoCSV(vistaUsuario.getCorreo());
                    if (usuarioOpt.isPresent()) {
                        vistaUsuario.mostrarPerfilUsuario(usuarioOpt.get()); // --- NUEVO ---
                    } else {
                        vistaUsuario.mensaje("Error: no se encontró el usuario en sesión.");
                    }
                }
                else if(opcion==6)  
                {
                    if (!tienePermiso(usuarioActual, ACCION_ELIMINAR_OBJETOS)) {
                        uiInfo("No tienes permiso para eliminar objetos.");
                    } else {
                        vistaUsuario.eliminarObjetoUI();
                    }
                }
                else if(opcion==7)
                { 
                    cierre = true;
                    vistaUsuario.mensaje("Saliendo del sistema. ¡Hasta luego!");
                } 
            }
        }
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
            vistaUsuario.mensaje("No se registró en CSV; no se agrega a memoria.");
        }
    }

    public boolean iniciarSesion(String nombre, String contrasena) {
        for (Usuario u : listaUsuarios) {
            if (u.getNombre().equalsIgnoreCase(nombre) && u.getContrasena().equals(contrasena)) {
                this.usuarioEnSesion = u;
                return true;
            }
        }
        return false;
    }

    public void cerrarSesion() {
        this.usuarioEnSesion = null;
    }

    public Usuario getUsuarioEnSesion() {
        return this.usuarioEnSesion;
    }

    public void registrarPremio(Premio premio) {
        listaPremios.add(premio);
    }

    public void registrarAdministrador(Administrador administrador) {
        listaAdministradores.add(administrador);
    }

    public String registrarObjeto1() {
        Objeto objeto = new Objeto(
            vistaUsuario.solicitarDescripcion(),
            vistaUsuario.solicitarTipoObjeto(),
            vistaUsuario.estadoObjeto(),
            vistaUsuario.solicitarFechaEncontrado(),
            vistaUsuario.solicitarUbicacionObjeto(),
            vistaUsuario.siguienteIdObjeto(),
            vistaUsuario.getCorreo()
        );

        if (registrarObjeto(objeto)) {
            boolean okCSV = insertarObjetoCSV(objeto);
            if (!okCSV) { uiInfo("Advertencia: objeto en memoria pero fallo CSV"); }
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
            uiError("No se pudo preparar el CSV de usuarios: " + e.getMessage());
        }
    }

    /** Prepara el CSV de objetos con cabecera si no existe */
    private void asegurarCSVObjetosConCabecera() {
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
            uiError("No se pudo preparar el CSV de objetos: " + e.getMessage());
        }
    }
    // ====== Lectura de objetos desde CSV (sin UI) ======
    /** Lee todos los objetos desde data/objetos.csv. No imprime nada (UI va en VistaUsuario). */
    public List<Objeto> leerObjetosDesdeCSV() 
    {
        List<Objeto> lista = new ArrayList<>();
        try (BufferedReader br = Files.newBufferedReader(rutaCSVObjetos, StandardCharsets.UTF_8)) {
            String linea = br.readLine(); // cabecera
            while ((linea = br.readLine()) != null) {
                if (linea.isBlank()) continue;
                String[] p = parseCSVLine(linea);
                // Esperado: id,descripcion,tipo,estado,fechaEncontrado,lugarEncontrado,fechaDevolucion,reportadoPor,usuarioQueReclama
                if (p.length < 6) continue; // mínimo hasta lugarEncontrado
                String id              = p[0].trim();
                int idInt;
                try { idInt = Integer.parseInt(id); } catch (NumberFormatException e) { uiError("ID inválido en objetos.csv: " + id); continue; }
                String descripcion     = p[1];
                String tipo            = p[2];
                String estado          = p[3];
                LocalDate fechaEn      = (p.length > 4 && !p[4].isBlank()) ? LocalDate.parse(p[4].trim()) : null;
                String lugar           = (p.length > 5) ? p[5] : "";
                // fechaDevolucion (p[6]) y usuarioQueReclama (p[8]) son opcionales para construir la instancia básica
                String reportadoPor    = (p.length > 7) ? p[7] : "";
                String usuarioQueReclama = (p.length > 8 && !p[8].isBlank()) ? p[8] : null; // ← NUEVO: leer usuarioQueReclama
                
                // Usar el constructor actualizado que incluye usuarioQueReclama
                Objeto o = new Objeto(descripcion, tipo, estado, fechaEn, lugar, idInt, reportadoPor, usuarioQueReclama);
                lista.add(o);
            }
        } catch (IOException ex) {
            uiError("Error leyendo objetos.csv: " + ex.getMessage());
        } catch (Exception ex) {
            uiError("Error parseando objetos.csv: " + ex.getMessage());
        }
        return lista;
    }

    /** Devuelve una copia de los objetos en memoria (listaObjetos). */
    public List<Objeto> obtenerObjetosEnMemoria() {
        return new ArrayList<>(listaObjetos);
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
            uiError("Error leyendo CSV de usuarios: " + ex.getMessage());
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
            uiError("Error leyendo IDs del CSV: " + ex.getMessage());
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
            uiError("Ya existe un usuario con ese correo.");
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
            uiError("Error escribiendo CSV de usuarios: " + ex.getMessage());
            return false;
        }
    }

        // Autenticación básica (texto plano)
        public Optional<Usuario> autenticarUsuarioCSV(String correo, String contrasenaPlano) 
        {
            Optional<Usuario> u = buscarUsuarioPorCorreoCSV(correo);
            if (u.isPresent() && u.get().getContrasena().equals(contrasenaPlano)) {
                Usuario usuario = u.get();
                
                // Si es ADMIN, crear instancia de Administrador
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

        // ================== CSV: utilidades para el arranque ==================

        // ¿Hay al menos un usuario (además de la cabecera)?
        private boolean hayUsuariosCSV() {
            try (BufferedReader br = Files.newBufferedReader(rutaCSVUsuarios, StandardCharsets.UTF_8)) {
                String linea = br.readLine(); // cabecera
                while ((linea = br.readLine()) != null) {
                    if (!linea.isBlank()) return true;
                }
            } catch (IOException e) {
                uiError("Error verificando usuarios CSV: " + e.getMessage());
            }
            return false;
        }

        // Crear admin por defecto si el CSV está vacío
        private void crearAdminPorDefectoSiVacio() {
            if (!hayUsuariosCSV()) {
                boolean ok = insertarUsuarioCSV("Admin", "admin@uvg.edu.gt", "1234", "ADMIN");
                if (ok) {
                    vistaUsuario.mensaje(" Admin por defecto creado: admin@uvg.edu.gt / 1234");
                } else {
                    uiError(" No se pudo crear el admin por defecto.");
                }
            }
        }

    public boolean cambiarRolUsuarioCSV(String correoInstitucional, String nuevoRol) {
        if (correoInstitucional == null || nuevoRol == null) return false;

        // 1) Buscar en memoria
        Usuario target = null;
        for (Usuario u : listaUsuarios) {
            if (u != null && correoInstitucional.equalsIgnoreCase(u.getCorreo())) {
                target = u;
                break;
            }
        }
        if (target == null) return false;

        target.setRol(nuevoRol);

        return reescribirUsuariosCSV();
    }

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
            Files.write(p, sb.toString().getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            return true;
        } catch (Exception e) {
            uiError("Error reescribiendo usuarios.csv: " + e.getMessage());
            return false;
        }
    }

    private static String esc(String s) {
        if (s == null) return "";
        boolean q = s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r");
        String out = s.replace("\"", "\"\"");
        return q ? "\"" + out + "\"" : out;
    }

    public boolean validarIdentidadParaReclamo(Usuario solicitante, String correoConfirmado, Integer carnetConfirmado) {
        if (solicitante == null) return false;

        boolean correoOk = (correoConfirmado != null) &&
                correoConfirmado.equalsIgnoreCase(solicitante.getCorreo());

        boolean carnetOk = true;
        try {
            java.lang.reflect.Field f = solicitante.getClass().getDeclaredField("carnet");
            f.setAccessible(true);
            Object v = f.get(solicitante);
            if (v instanceof Integer) {
                int carnetReal = (Integer) v;
                if (carnetReal > 0 && carnetConfirmado != null) {
                    carnetOk = (carnetReal == carnetConfirmado.intValue());
                }
            }
        } catch (Exception ignore) {
            carnetOk = true;
        }

        return correoOk && carnetOk;
    }

    public boolean reclamarObjetoConValidacion(int idObjeto, Usuario solicitante, String correoConfirmado, Integer carnetConfirmado) {
        if (solicitante == null) return false;

        if (!tienePermiso(solicitante, ACCION_RECLAMAR_OBJETO)) {
            uiError("No tiene permiso para reclamar objetos.");
            return false;
        }

        Objeto objetivo = null;
        for (Objeto o : listaObjetos) {
            if (o == null) continue;
            try {
                java.lang.reflect.Field fid = o.getClass().getDeclaredField("id");
                fid.setAccessible(true);
                Object val = fid.get(o);
                if (val instanceof Integer && ((Integer) val) == idObjeto) {
                    objetivo = o; break;
                }
            } catch (Exception e) {
            }
        }
        if (objetivo == null) {
            uiError("Objeto no encontrado para id: " + idObjeto);
            return false;
        }


        boolean identidadOk = validarIdentidadParaReclamo(solicitante, correoConfirmado, carnetConfirmado);
        if (!identidadOk) {
            uiError("Validación de identidad fallida.");
            return false;
        }


        objetivo.setEstadoRecuperado(java.time.LocalDate.now(), solicitante.getCorreo());

        vistaUsuario.mensaje("Objeto reclamado exitosamente por " + solicitante.getCorreo());
        return true;
    }    


//Metodo para canjear premios
public void canjearPremio(Usuario usuario) {
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

    // ====== Utilidad: parsear una línea CSV con comillas ======
    private static String[] parseCSVLine(String line) {
        if (line == null) return new String[0];
        java.util.List<String> out = new java.util.ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    // comilla escapada
                    sb.append('"');
                    i++; // saltar la segunda comilla
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

    // Helper: fecha a 'YYYY-MM-DD' o vacío si es null
    private String fechaStr(java.time.LocalDate d) {
        return (d == null) ? "" : d.toString();
    }

    /** Inserta una fila en data/objetos.csv usando el orden de cabecera:
     * id,descripcion,tipo,estado,fechaEncontrado,lugarEncontrado,fechaDevolucion,reportadoPor,usuarioQueReclama
     */
    private boolean insertarObjetoCSV(Objeto o) {
        // Escapar campos con comillas y comas
        String id              = String.valueOf(o.getId());
        String descripcion     = esc(o.getDescripcion());
        String tipo            = esc(o.getTipo());
        String estado          = esc(o.getEstado());
        String fechaEncontrado = esc(fechaStr(o.getFechaEncontrado()));
        String lugar           = esc(o.getLugarEncontrado());
        String fechaDev        = esc(fechaStr(o.getFechaDevolucion()));
        String reportadoPor    = esc(o.getReportadoPor());
        String usuarioReclama  = esc(o.getUsuarioQueReclama());

        String fila = String.join(",", id, descripcion, tipo, estado,
                                fechaEncontrado, lugar, fechaDev, reportadoPor, usuarioReclama);
        try (java.io.BufferedWriter bw = java.nio.file.Files.newBufferedWriter(
                rutaCSVObjetos, java.nio.charset.StandardCharsets.UTF_8,
                java.nio.file.StandardOpenOption.APPEND, java.nio.file.StandardOpenOption.CREATE)) {
            bw.write(fila);
            bw.newLine();
            return true;
        } catch (java.io.IOException ex) {
            uiError("Error escribiendo objetos.csv: " + ex.getMessage());
            return false;
        }
    }

    // ====== Utilidades y filtros (solo lógica, sin UI) ======
    private static String norm(String s) {
        return (s == null) ? "" : s.trim().toLowerCase();
    }

    /** Filtra por tipo exacto (ignorando mayúsculas/minúsculas). */
    public List<Objeto> filtrarPorTipo(List<Objeto> fuente, String tipo) {
        String t = norm(tipo);
        List<Objeto> out = new ArrayList<>();
        if (fuente == null) return out;
        for (Objeto o : fuente) {
            if (o == null) continue;
            if (norm(o.getTipo()).equals(t)) {
                out.add(o);
            }
        }
        return out;
    }

    /** Filtra por estado exacto (perdido, encontrado, recuperado, donado) ignorando mayúsculas. */
    public List<Objeto> filtrarPorEstado(List<Objeto> fuente, String estado) {
        String e = norm(estado);
        List<Objeto> out = new ArrayList<>();
        if (fuente == null) return out;
        for (Objeto o : fuente) {
            if (o == null) continue;
            if (norm(o.getEstado()).equals(e)) {
                out.add(o);
            }
        }
        return out;
    }

    /** Filtra por ubicación exacta (lugarEncontrado), ignorando mayúsculas/minúsculas. */
    public List<Objeto> filtrarPorUbicacion(List<Objeto> fuente, String ubicacion) {
        String u = norm(ubicacion);
        List<Objeto> out = new ArrayList<>();
        if (fuente == null) return out;
        for (Objeto o : fuente) {
            if (o == null) continue;
            if (norm(o.getLugarEncontrado()).equals(u)) {
                out.add(o);
            }
        }
        return out;
    }

    /** Filtra por rango de fechaEncontrado [desdeIncl, hastaIncl]. Si alguna es null, el rango es abierto por ese extremo. */
    public List<Objeto> filtrarPorFechaEncontrado(List<Objeto> fuente, java.time.LocalDate desdeIncl, java.time.LocalDate hastaIncl) {
        List<Objeto> out = new ArrayList<>();
        if (fuente == null) return out;
        for (Objeto o : fuente) {
            if (o == null) continue;
            java.time.LocalDate f = o.getFechaEncontrado();
            if (f == null) continue; // si no tiene fecha, no entra al rango
            boolean okDesde = (desdeIncl == null) || !f.isBefore(desdeIncl); // f >= desdeIncl
            boolean okHasta = (hastaIncl == null) || !f.isAfter(hastaIncl);  // f <= hastaIncl
            if (okDesde && okHasta) {
                out.add(o);
            }
        }
        return out;
    }

    /** Devuelve una copia ordenada por fechaEncontrado ascendente/descendente. Los null van al final. */
    public List<Objeto> ordenarPorFechaEncontrado(List<Objeto> fuente, boolean asc) {
        List<Objeto> copia = new ArrayList<>();
        if (fuente != null) copia.addAll(fuente);
        java.util.Comparator<Objeto> cmp = (a, b) -> {
            java.time.LocalDate fa = (a == null) ? null : a.getFechaEncontrado();
            java.time.LocalDate fb = (b == null) ? null : b.getFechaEncontrado();
            if (fa == null && fb == null) return 0;
            if (fa == null) return 1; // null al final
            if (fb == null) return -1;
            int base = fa.compareTo(fb);
            return asc ? base : -base;
        };
        copia.sort(cmp);
        return copia;
    }

    public boolean eliminarObjetoPorId(int idObjeto) {
        Usuario enSesion = getUsuarioActual();
        if (!tienePermiso(enSesion, ACCION_GESTION_OBJETOS)) {
            uiInfo("No tienes permiso para eliminar objetos.");
            return false;
        }

        java.util.Iterator<Objeto> it = listaObjetos.iterator();
        while (it.hasNext()) {
            Objeto o = it.next();
            try {

                java.lang.reflect.Field fid = o.getClass().getDeclaredField("id");
                fid.setAccessible(true);
                Object val = fid.get(o);
                if (val instanceof Integer && ((Integer) val) == idObjeto) {
                    it.remove();
                    uiInfo("Objeto eliminado: " + idObjeto);
                    return true;
                }
            } catch (Exception ignore) {}
        }

        uiInfo("No se encontró el objeto con id: " + idObjeto);
        return false;
    }

    public void setVistaUsuario(VistaUsuario v) {
        this.vistaUsuario = v;
        if (v != null) {
            v.setSistema(this);
        }
    }
    public VistaUsuario getVistaUsuario() { return this.vistaUsuario; }

    public static final int LIMITE_DIAS_NO_RECLAMADO = 180;

        public List<Objeto> obtenerCandidatosDonacion(int limiteDias) {
        List<Objeto> out = new ArrayList<>();
        if (listaObjetos == null) return out;
        for (Objeto o : listaObjetos) {
            try {
                if (o != null && o.esCandidatoNoReclamado(limiteDias)) {
                    out.add(o);
                }
            } catch (Exception ignore) {}
        }
        return out;
    }

        public int donarNoReclamados(int limiteDias, Usuario actor) {
        if (actor == null || !actor.esAdmin()) {
            uiError("Permiso denegado: se requiere rol ADMIN para donar objetos no reclamados.");
            return 0;
        }
        List<Objeto> candidatos = obtenerCandidatosDonacion(limiteDias);
        if (candidatos.isEmpty()) return 0;

        LocalDate hoy = LocalDate.now();
        int cambios = 0;
        for (Objeto o : candidatos) {
            try {
                o.setEstadoDonado(hoy);
                cambios++;
            } catch (Exception ignore) {}
        }
        boolean ok = reescribirObjetosCSV();
        if (!ok) {
            uiError("Advertencia: no se pudo persistir objetos donados en objetos.csv");
        }
        return cambios;
    }

    /** Versión práctica con el límite de 6 meses (180 días). */
    public int donarNoReclamadosSemestre(Usuario actor) 
    {
        return donarNoReclamados(LIMITE_DIAS_NO_RECLAMADO, actor);
    }

public boolean reclamarObjeto(int idObjeto, Usuario usuarioReclamante) 
{
    if (usuarioReclamante == null || !usuarioReclamante.puedeReclamarObjetos()) {
        uiError("No tienes permisos para reclamar objetos.");
        return false;
    }
    
    // Buscar el objeto EN LA LISTA y modificarlo directamente
    Objeto objetoEncontrado = null;
    for (Objeto obj : listaObjetos) {
        if (obj != null && obj.getId() == idObjeto) {
            objetoEncontrado = obj;
            break;
        }
    }
    
    if (objetoEncontrado == null) {
        uiError("No se encontró el objeto con ID: " + idObjeto);
        return false;
    }
    
    if (!Objeto.ESTADO_ENCONTRADO.equals(objetoEncontrado.getEstado())) {
        uiError("Este objeto no está disponible para reclamar. Estado actual: " + objetoEncontrado.getEstado());
        return false;
    }
    
    // Modificar el objeto DIRECTAMENTE en la lista
    objetoEncontrado.setEstado(Objeto.ESTADO_PENDIENTE_VALIDACION);
    objetoEncontrado.setUsuarioQueReclama(usuarioReclamante.getCorreo());
    
    System.out.println("DEBUG: Objeto actualizado - ID: " + objetoEncontrado.getId() + 
                    ", Estado: " + objetoEncontrado.getEstado() + 
                    ", Usuario: " + objetoEncontrado.getUsuarioQueReclama());
    
    // GUARDAR EL CAMBIO EN EL CSV
    boolean guardado = reescribirObjetosCSV();
    if (!guardado) {
        uiError("Error al guardar el reclamo en el sistema.");
        return false;
    }
    
    uiInfo("Reclamo enviado para validación. Un administrador revisará tu solicitud.");
    return true;
}

    public boolean validarReclamoObjeto(int idObjeto, Usuario administrador) {
    if (administrador == null || !administrador.puedeValidarReclamos()) {
        uiError("No tienes permisos para validar reclamos.");
        return false;
    }
    
    // Buscar el objeto EN LA LISTA directamente
    Objeto objetoEncontrado = null;
    for (Objeto obj : listaObjetos) {
        if (obj != null && obj.getId() == idObjeto) {
            objetoEncontrado = obj;
            break;
        }
    }
    
    if (objetoEncontrado == null) {
        uiError("No se encontró el objeto con ID: " + idObjeto);
        return false;
    }
    
    if (!Objeto.ESTADO_PENDIENTE_VALIDACION.equals(objetoEncontrado.getEstado())) {
        uiError("Este objeto no tiene reclamos pendientes de validación.");
        return false;
    }
    
    String usuarioReclamante = objetoEncontrado.getUsuarioQueReclama();
    if (usuarioReclamante == null || usuarioReclamante.isEmpty()) {
        uiError("Error: No se encontró información del usuario que reclamó este objeto.");
        return false;
    }
    
    // Modificar el objeto DIRECTAMENTE en la lista
    objetoEncontrado.setEstadoRecuperado(LocalDate.now(), usuarioReclamante);
    
    System.out.println("DEBUG: Validación completada - ID: " + objetoEncontrado.getId() + 
                    ", Estado: " + objetoEncontrado.getEstado() + 
                    ", Usuario: " + objetoEncontrado.getUsuarioQueReclama());
    
    // GUARDAR EL CAMBIO EN EL CSV
    boolean guardado = reescribirObjetosCSV();
    if (!guardado) {
        uiError("Error al guardar la validación en el sistema.");
        return false;
    }
    
    uiInfo("Reclamo validado exitosamente. Objeto marcado como reclamado por: " + usuarioReclamante);
    return true;
}

    private Objeto buscarObjetoPorId(int idObjeto) 
    {
    for (Objeto obj : listaObjetos) {
        if (obj != null && obj.getId() == idObjeto) {
            return obj;
        }
    }
    return null;
    }

    public List<Objeto> obtenerObjetosPendientesValidacion() 
    {
        List<Objeto> pendientes = new ArrayList<>();
        for (Objeto obj : listaObjetos) {
            if (obj != null && Objeto.ESTADO_PENDIENTE_VALIDACION.equals(obj.getEstado())) {
                System.out.println("DEBUG: Objeto pendiente - ID: " + obj.getId() + ", Usuario: " + obj.getUsuarioQueReclama()); // Para debugging
                pendientes.add(obj);
            }
        }
        return pendientes;
    }

    public Usuario obtenerUsuarioActualPorCorreo(String correo) 
    {
    Optional<Usuario> usuarioOpt = buscarUsuarioPorCorreoCSV(correo);
    return usuarioOpt.orElse(null);
    }


        private boolean reescribirObjetosCSV() {
        Path p = rutaCSVObjetos;
        String header = "id,descripcion,tipo,estado,fechaEncontrado,lugarEncontrado,fechaDevolucion,reportadoPor,usuarioQueReclama";
        StringBuilder sb = new StringBuilder(header).append("\n");
        if (listaObjetos != null) {
            for (Objeto o : listaObjetos) {
                if (o == null) continue;
                String id              = String.valueOf(o.getId());
                String descripcion     = esc(o.getDescripcion());
                String tipo            = esc(o.getTipo());
                String estado          = esc(o.getEstado());
                String fechaEncontrado = esc(fechaStr(o.getFechaEncontrado()));
                String lugar           = esc(o.getLugarEncontrado());
                String fechaDev        = esc(fechaStr(o.getFechaDevolucion()));
                String reportadoPor    = esc(o.getReportadoPor());
                String usuarioReclama  = esc(o.getUsuarioQueReclama());

                sb.append(id).append(",")
                  .append(descripcion).append(",")
                  .append(tipo).append(",")
                  .append(estado).append(",")
                  .append(fechaEncontrado).append(",")
                  .append(lugar).append(",")
                  .append(fechaDev).append(",")
                  .append(reportadoPor).append(",")
                  .append(usuarioReclama).append("\n");
            }
        }
        try {
            Files.createDirectories(p.getParent());
            Files.write(p, sb.toString().getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            return true;
        } catch (Exception e) {
            uiError("Error reescribiendo objetos.csv: " + e.getMessage());
            return false;
        }
    }

    /** Redirige mensajes informativos a la vista (si existe). */
    private void uiInfo(String msg) {
        if (this.vistaUsuario != null) {
            this.vistaUsuario.mensaje(msg);
        }
        // No System.out.println aquí por regla del proyecto.
    }

    /** Redirige mensajes de error a la vista (si existe). */
    private void uiError(String msg) {
        if (this.vistaUsuario != null) {
            this.vistaUsuario.error(msg);
        }
    }

}