import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;


public class VistaUsuario 
{
    private final Scanner sc;
    private int siguienteIdObjeto = 1;
    private Sistema sistema;

    private String nombre;
    private String correo;
    private int carnet;

    public VistaUsuario(Sistema sistema) 
    {
        this.sc = new Scanner(System.in);
        this.sistema = sistema;
    }

    public int IniciarVistaUsuario() 
    {
        System.out.println("====== Lost But Found - UVG ======");
        System.out.println("Bienvenido al sistema de objetos perdidos.");
        System.out.println("\n--- Inicio ---");
        System.out.println("1. Registrarse");
        System.out.println("2. Iniciar sesión");
        System.out.println("3. Salir");
        System.out.print("Seleccione una opción: ");

        int opcion = pedirNumero();
        return opcion;
    }

    public int pedirNumero()
    {
    try {
        int opcion = sc.nextInt();
        sc.nextLine(); // limpiar buffer
        if (opcion <= 0) {
            System.out.println("Entrada inválida. Por favor ingrese una opción válida.");
            return pedirNumero(); // vuelve a pedir
        }
        return opcion;
    } catch (Exception e) {
        sc.nextLine(); // limpiar buffer
        System.out.println("Entrada inválida. Por favor ingrese una opción válida.");
        return pedirNumero(); // vuelve a pedir
    }
    }


    public int verMenu()
    {
        

        System.out.println("\n--- Menú de Usuario ---");
        System.out.println("1. Reportar objeto que perdiste o que encontraste");
        System.out.println("2. Busqueda de objetos sin dueño");
        System.out.println("3. Validacion y reclamo de objeto");
        System.out.println("4. Canjear premios");
        System.out.println("5. Ver perfil y puntos");
        System.out.println("6. Eliminar objetos perdidos");
        System.out.println("7. Salir");
        System.out.println("Seleccione una opción: ");

        int opcion=pedirNumero();
        return opcion;
    }

    public int menuReclamos() 
    {
    System.out.println("\n--- Validación y Reclamo de Objetos ---");
    System.out.println("1. Reclamar objeto");
    System.out.println("2. Validar reclamo (Solo administradores)");
    System.out.println("3. Volver al menú principal");
    System.out.print("Seleccione una opción: ");
    
    int opcion=pedirNumero();
    return opcion;
    }

    // filtros 
    public int verFiltros() 
    {
        System.out.println("\n--- Filtros de Búsqueda de Objetos Sin Dueño ---");
        System.out.println("1. Filtrar por tipo de objeto");
        System.out.println("2. Filtrar por fecha de reporte");
        System.out.println("3. Filtrar por ubicacion");
        System.out.println("4. Volver al menú principal");
        System.out.print("Seleccione una opción: ");
        
        int opcion=pedirNumero();
        return opcion;
    }


public String filtroTipo() 
{
    System.out.println("Filtra por varias categorías usando números separados por coma.");
    System.out.println("0 = Regresar al menú de filtros");
    System.out.println("[1] Documento  [2] Electronico  [3] Accesorio  [4] Ropa  [5] Utiles  [6] Recipientes  [7] Otros");
    System.out.print("Categorías a filtrar: ");

    String linea = sc.nextLine().trim();

    // 0 = regresar al menú de filtros
    if (linea.equals("0")) {
        return null;
    }

    // solo números 1–7 separados por coma
    if (!linea.matches("^\\s*[1-7]\\s*(,\\s*[1-7]\\s*)*$")) {
        System.out.println("Selección inválida. Usa números del 1 al 7 separados por coma (ej. 1,3,5).");
        return filtroTipo(); // reintenta
    }

    // convierte "1,3,5" a algo como "Documento|Accesorio|Utiles"
    String tipos = mapearNumerosACategorias(linea);
    if (tipos == null || tipos.isEmpty()) {
        System.out.println("Selección inválida. Intenta de nuevo.");
        return filtroTipo(); // reintenta
    }

    return tipos;
}


    public LocalDate filtroFecha1() {
        DateTimeFormatter DMY_STRICT = new DateTimeFormatterBuilder()
                .parseStrict()
                .appendPattern("d-M-uuuu")
                .toFormatter()
                .withResolverStyle(ResolverStyle.STRICT);

        LocalDate fecha = null;
        while (fecha == null) {
            System.out.println("Ingrese la primera fecha (DD/MM/YYYY o DD-MM-YYYY):");
            String input = sc.nextLine().trim();

            if (input.isEmpty()) {
                System.out.println("La fecha no puede estar vacía.");
                continue;
            }

            String normalized = input.replace('/', '-');

            if (!normalized.matches("\\d{1,2}-\\d{1,2}-\\d{4}")) {
                System.out.println("Formato inválido. Use por ejemplo 06/11/2025 o 6-11-2025.");
                continue;
            }

            try {
                fecha = LocalDate.parse(normalized, DMY_STRICT);
            } catch (DateTimeParseException e) {
                System.out.println("Fecha inválida. Verifique día/mes/año (ej.: 29/02 solo en año bisiesto).");
            }
        }
        return fecha;
    }

    public String filtroUbicacion() {
    System.out.println("Ingrese la ubicación a buscar (o pulsa 1 para regresar):");
    String ubic = sc.nextLine().trim();
    if (ubic.equals("1")) return null;   // permite regresar al menú de filtros
    return ubic;
}

    public LocalDate filtroFecha2() {
        DateTimeFormatter DMY_STRICT = new DateTimeFormatterBuilder()
                .parseStrict()
                .appendPattern("d-M-uuuu")
                .toFormatter()
                .withResolverStyle(ResolverStyle.STRICT);

        LocalDate fecha = null;
        while (fecha == null) {
            System.out.println("Ingrese la segunda fecha (DD/MM/YYYY o DD-MM-YYYY):");
            String input = sc.nextLine().trim();

            if (input.isEmpty()) {
                System.out.println("La fecha no puede estar vacía.");
                continue;
            }

            String normalized = input.replace('/', '-');

            if (!normalized.matches("\\d{1,2}-\\d{1,2}-\\d{4}")) {
                System.out.println("Formato inválido. Use por ejemplo 06/11/2025 o 6-11-2025.");
                continue;
            }

            try {
                fecha = LocalDate.parse(normalized, DMY_STRICT);
            } catch (DateTimeParseException e) {
                System.out.println("Fecha inválida. Verifique día/mes/año (ej.: 29/02 solo en año bisiesto).");
            }
        }
        return fecha;
    }

    public void mensaje(String mensaje) 
    {
        System.out.println(mensaje);
    }

    // ----- Login -----

    public String solicitarCorreo() {
        System.out.print("Correo: ");
        return sc.nextLine().trim();
    }

    public String solicitarContrasena() {
        System.out.print("Contraseña: ");
        return sc.nextLine();
    }

    /** Pide credenciales y autentica contra el CSV usando Sistema */
    public boolean mostrarLoginConsola(Sistema sistema) 
    {
        String correo = solicitarCorreo();
        String contrasena = solicitarContrasena();
        this.correo = correo;
        Optional<Usuario> usuarioOpcional = sistema.autenticarUsuarioCSV(correo, contrasena);
        
        if (usuarioOpcional.isPresent()) {
            Usuario u = usuarioOpcional.get();
            sistema.setUsuarioActual(u); // ← ESTABLECER USUARIO ACTUAL
            System.out.println("Bienvenido, " + u.getNombre() + " (" + u.getRol() + ")");
            return true; 
        } else {
            System.out.println("Credenciales inválidas.");
            return false; 
        }
    }

    // ----- Solicitud de datos para crear Objeto -----
     // Si el usuario escribe "1", significa que quiere regresar al menú principal. En ese caso, se devuelve null para que el sistema lo detecte y cancele el flujo actual.

    public String estadoObjeto() {
        // Muestra las opciones de forma numérica
        System.out.println("Qué relación tiene con el objeto?");
        System.out.println("1. Lo busco");
        System.out.println("2. Lo encontré");
        System.out.println("(o pulsa 0 para regresar):");

        String entrada = sc.nextLine().trim();

        // Si el usuario marca 0 regresa al menú
        if (entrada.equals("0")) return null;

        // Programación defensiva
        while (!entrada.equals("1") && !entrada.equals("2")) {
            System.out.println("Entrada inválida. Escriba 1 para Perdido o 2 para Sin dueño (o 0 para regresar):");
            entrada = sc.nextLine().trim();

            if (entrada.equals("0")) return null;
        }

        // Retornar el estado correspondiente
        if (entrada.equals("1")) return Objeto.ESTADO_PERDIDO;
        else return Objeto.ESTADO_ENCONTRADO;
    }



    public String solicitarTipoObjeto() {
        System.out.println("Elige qué tipo de objeto es usando números (puedes elegir más de 1, separados con coma).");
        System.out.println("0 = Regresar");
        System.out.println("[1] Documento  [2] Electronico  [3] Accesorio  [4] Ropa  [5] Utiles  [6] Recipientes  [7] Otros");
        System.out.print("Categorías: ");

        String linea = sc.nextLine().trim();

        // Regresar al menú anterior
        if (linea.equals("0")) return null;

        // validar el patrón "1,2,3"
        if (!linea.matches("^\\s*[1-7]\\s*(,\\s*[1-7]\\s*)*$")) {
            System.out.println("Selección inválida. Usa números del 1 al 7 separados por coma (ej. 1,3,5)");
            return solicitarTipoObjeto(); // reintenta
        }

        String tipos = mapearNumerosACategorias(linea); // Ej: "Documento|Accesorio"
        if (tipos == null || tipos.isEmpty()) {
            System.out.println("Selección inválida. Intenta de nuevo.");
            return solicitarTipoObjeto(); // reintenta
        }

        return tipos;  
    }



    public String solicitarDescripcion() 
    {
        System.out.println("Ingrese una descripción del objeto perdido/encontrado (o pulsa 1 para regresar):");
        String descripcion = sc.nextLine();

        if (descripcion.equals("1")) return null;  

        return descripcion;
    }

    public String solicitarUbicacionObjeto() 
    {
        System.out.println("Ingrese dónde encontró/perdió el objeto (o pulsa 1 para regresar):");
        String ubic = sc.nextLine().trim();
        if (ubic.equals("1")) return null;   // salir al menú
        return ubic;
    }

    public LocalDate solicitarFechaEncontrado() {
        // Acepta solo DD/MM/YYYY o DD-MM-YYYY 
        DateTimeFormatter DMY_STRICT = new DateTimeFormatterBuilder()
                .parseStrict()
                .appendPattern("d-M-uuuu")
                .toFormatter()
                .withResolverStyle(ResolverStyle.STRICT);

        LocalDate fecha = null; // Controla el ciclo
        while (fecha == null) {
            System.out.println("Ingrese la fecha en que encontró/perdió el objeto (DD/MM/YYYY o DD-MM-YYYY) o pulsa 1 para regresar:");
            String input = sc.nextLine().trim();

            if (input.equals("1")) return null; // permite volver al menú

            if (input.isEmpty()) {
                System.out.println("La fecha no puede estar vacía.");
                continue;
            }

            // Normalizamos el separador a '-'
            String normalized = input.replace('/', '-');

            // Validamos formato exacto d-m-aaaa (solo números y separador / o -)
            if (!normalized.matches("\\d{1,2}-\\d{1,2}-\\d{4}")) {
                System.out.println("Formato inválido. Use por ejemplo 06/11/2025 o 6-11-2025.");
                continue;
            }

            try {
                fecha = LocalDate.parse(normalized, DMY_STRICT); // valida fechas reales (29/02, etc.)
            } catch (DateTimeParseException e) {
                System.out.println("Fecha inválida. Verifique día/mes/año (ej.: 29/02 solo en año bisiesto).");
            }
        }

        return fecha;
    }

    public String solicitarNombreObjeto() 
    {
        System.out.println("Ingrese el nombre del objeto:");
        return sc.nextLine();
    }

    public int siguienteIdObjeto() {
        if (this.sistema != null) {
            // Si VistaUsuario ya tiene un Sistema asociado, usa el id que calcula el Sistema
            return this.sistema.siguienteIdObjeto();
        }
        // Si no hay sistema sigue usando su contador interno
        if (this.siguienteIdObjeto <= 0) this.siguienteIdObjeto = 1;
        return this.siguienteIdObjeto++;
    }


    public String getCorreo() 
    {
        Usuario usuario = sistema.getUsuarioActual();
        return usuario.getCorreo();
    }


    
    public void reclamarObjetoUI() 
    {
        int opcionReclamo;
        
        do {
            opcionReclamo = menuReclamos();
            
            switch (opcionReclamo) {
                case 1:
                    reclamarObjetoComoUsuario();
                    break;
                case 2:
                    validarReclamoComoAdmin();
                    break;
                case 3:
                    System.out.println("Volviendo al menú principal...");
                    break;
                default:
                    System.out.println("Opción no válida.");
                    break;
            }
        } while (opcionReclamo != 3);
    }

    private void reclamarObjetoComoUsuario() 
    {
        System.out.println("== Reclamo de Objeto ==");

        // Mostrar objetos disponibles para reclamar
        List<Objeto> objetosDisponibles = sistema.filtrarPorEstado(
            sistema.obtenerObjetosEnMemoria(), Objeto.ESTADO_ENCONTRADO
        );

        if (objetosDisponibles.isEmpty()) {
            System.out.println("No hay objetos disponibles para reclamar.");
            return;
        }

        mostrarObjetos(objetosDisponibles);

        System.out.print("Ingrese el ID del objeto que desea reclamar (0 para regresar al menú): ");
        int idObj = pedirNumeroConSalida();

        //Si el usuario escribió 0
        if (idObj == -1) {
            System.out.println("Operación cancelada por el usuario. Regresando al menú...");
            return;
        }

        Usuario usuarioActual = sistema.getUsuarioActual();
        if (usuarioActual == null) {
            System.out.println("Debe iniciar sesión para reclamar objetos.");
            return;
        }

        //Verificar si el ID existe antes de intentar reclamar
        boolean existe = objetosDisponibles.stream().anyMatch(o -> o.getId() == idObj);
        if (!existe) {
            System.out.println("ID inexistente. Ingrese un ID válido.");
            reclamarObjetoComoUsuario(); // vuelve a intentarlo de forma recursiva (sin while)
            return;
        }

        boolean resultado = sistema.reclamarObjeto(idObj, usuarioActual);
        if (resultado) {
            System.out.println("Reclamo enviado exitosamente. Pendiente de validación.");
        } else {
            System.out.println("No se pudo procesar el reclamo.");
        }
    }


    public void validarReclamoComoAdmin() 
    {
        System.out.println("== Validación de Reclamos ==");
        
        Usuario usuarioActual = sistema.getUsuarioActual();
        if (usuarioActual == null || !usuarioActual.puedeValidarReclamos()) {
            System.out.println("Acceso denegado. Solo administradores pueden validar reclamos.");
            return;
        }
        
        List<Objeto> pendientes = sistema.obtenerObjetosPendientesValidacion();
        
        if (pendientes.isEmpty()) {
            System.out.println("No hay reclamos pendientes de validación.");
            return;
        }
        
        System.out.println("\n--- Reclamos Pendientes de Validación ---");
        for (Objeto obj : pendientes) {
            System.out.println("ID: " + obj.getId() + 
                        ", Descripción: " + obj.getDescripcion() + 
                        ", Tipo: " + obj.getTipo() +
                        ", Reclamado por: " + (obj.getUsuarioQueReclama() != null ? obj.getUsuarioQueReclama() : "NO REGISTRADO") +
                        ", Estado: " + obj.getEstado());
        }
        
        System.out.print("ID del objeto a validar: ");
        int idObj = pedirNumero();
        
        System.out.print("¿Confirmar validación? (Si/No): ");
        String confirmacion = sc.nextLine().trim();
        
        if (confirmacion.equalsIgnoreCase("Si")) {
            boolean resultado = sistema.validarReclamoObjeto(idObj, usuarioActual);
            if (resultado) {
                System.out.println("Reclamo validado exitosamente.");
            } else {
                System.out.println("No se pudo validar el reclamo.");
            }
        } else {
            System.out.println("Validación cancelada.");
        }
    }


    public void mostrarPremiosDisponibles(List<Premio> premios) {
        System.out.println("\n--- Premios Disponibles ---");
        if (premios.isEmpty()) {
            System.out.println("No hay premios registrados en el sistema.");
            return;
        }
        for (int i = 0; i < premios.size(); i++) {
            Premio p = premios.get(i);
            System.out.println((i + 1) + ". " + p.getNombre() + " - " + p.getDescripcion() + " (" + p.getPuntos() + " pts)");
        }
    }

    public int elegirPremio() {
        System.out.print("Seleccione el número del premio a canjear: ");
        int opcion=pedirNumero();
        return opcion;
    }

    //Solicita el nombre del usuario (UI) 
    public String solicitarNombrePersona() 
    {
        System.out.print("Nombre: ");
        return sc.nextLine().trim();
    }

    public void mostrarObjetos(List<Objeto> objetos) {
        if (objetos.isEmpty()) {
            System.out.println("No se encontraron objetos.");
            return;
        }
        System.out.println("\n--- Objetos Sin dueño ---");
        for (Objeto obj : objetos) {
            System.out.println("ID: " + obj.getId() + ", Tipo: " + obj.getTipo() + ", Descripción: " + obj.getDescripcion() +
            ", Estado: " + obj.getEstado() + ", Fecha Encontrado: " + obj.getFechaEncontrado() +
            ", Lugar Encontrado: " + obj.getLugarEncontrado());
        }
    }

    public void mostrarObjetosConTitulo(String titulo, List<Objeto> objetos) {
        if (objetos == null || objetos.isEmpty()) {
            System.out.println("No hay elementos para mostrar.");
            return;
        }
        System.out.println("\n--- " + titulo + " ---");
        for (Objeto obj : objetos) {
            System.out.println(
                "ID: " + obj.getId() +
                ", Tipo: " + obj.getTipo() +
                ", Descripción: " + obj.getDescripcion() +
                ", Estado: " + obj.getEstado() +
                ", Fecha Encontrado: " + obj.getFechaEncontrado() +
                ", Lugar Encontrado: " + obj.getLugarEncontrado()
            );
        }
    }

    public void mostrarNotificaciones(List<String> notificaciones) {
        if (notificaciones == null || notificaciones.isEmpty()) return;
        System.out.println("\n--- Notificaciones ---");
        for (String n : notificaciones) {
            System.out.println("-" + n);
        }
    }


    public void eliminarObjetoUI() {
        if (sistema == null || !sistema.esAdminSesion()) {
            System.out.println("Solo un administrador puede eliminar objetos.");
                return;
        }

     // Resumen admin de DONADOS
        List<Objeto> donados = sistema.filtrarPorEstado(sistema.obtenerObjetosEnMemoria(), Objeto.ESTADO_DONADO);
        System.out.println("Objetos DONADOS en el sistema: " + (donados == null ? 0 : donados.size()));

        System.out.print("¿Ver lista completa de donados? (S/N): ");
        String ver = sc.nextLine().trim();
            if (ver.equalsIgnoreCase("S")) {
                if (donados != null && !donados.isEmpty()) {
                    mostrarObjetosConTitulo("Objetos DONADOS", donados);
                } else {
                    System.out.println("No hay objetos donados.");
                }
            }

        List<Objeto> todos = sistema.obtenerObjetosEnMemoria();
        List<Objeto> perdidos = sistema.filtrarPorEstado(todos, Objeto.ESTADO_PERDIDO);
        List<Objeto> encontrados = sistema.filtrarPorEstado(todos, Objeto.ESTADO_ENCONTRADO);

        if (perdidos != null && !perdidos.isEmpty()) {
            mostrarObjetosConTitulo("Objetos PERDIDOS (eliminables)", perdidos);
        }
        if (encontrados != null && !encontrados.isEmpty()) {
            mostrarObjetosConTitulo("Objetos ENCONTRADOS (eliminables)", encontrados);
        }      

        System.out.println("\n== Eliminar objeto ==");
        System.out.print("ID del objeto a eliminar (0 para regresar al menú): ");
        int id = pedirNumero();  // lectura segura
        if (id <= 0) {
            System.out.println("Operación cancelada. Regresando al menú.");
            return;
        }

        System.out.print("Confirmar (S/N): ");
        String ok = sc.nextLine().trim();
        if (!ok.equalsIgnoreCase("S")) {
            System.out.println("Operación cancelada.");
            return;
        }

        boolean res = sistema.eliminarObjetoPorId(id);
        System.out.println(res ? "Eliminado correctamente." : "No se pudo eliminar.");
    }

    public void mostrarPerfilUsuario(Usuario usuario) {
        System.out.println("\n--- Perfil del Usuario ---");
        System.out.println("Nombre: " + usuario.getNombre());
        System.out.println("Correo: " + usuario.getCorreo());
        System.out.println("Rol: " + usuario.getRol());
        System.out.println("Puntos: " + usuario.getPuntos());

        if (usuario.getPuntos() == 0) {
            System.out.println("No tienes puntos acumulados todavía.");
        }

        if (usuario.esAdmin()) {
            System.out.println("Tienes permisos de administrador.");
        }
    }

    //Mensajes de error provenientes del sistema.
    public void error(String mensaje) {
        System.err.println(mensaje);
    }

    public void verPerfilYPuntos() {
        Usuario u = (sistema.getUsuarioEnSesion() != null)
            ? sistema.getUsuarioEnSesion()
            : sistema.getUsuarioActual();

        if (u == null) {
            System.out.println("Debes iniciar sesión para ver tu perfil.");
            return;
        }

        System.out.println("\n--- Perfil ---");
        System.out.println("Nombre:  " + u.getNombre());
        System.out.println("Correo:  " + u.getCorreo());
        System.out.println("Rol:     " + u.getRol());
        System.out.println("Puntos:  " + u.getPuntos());
    }

    // Cambia la selección "1,3,5" a "Documento|Accesorio|Utiles"
private String mapearNumerosACategorias(String linea) {
    java.util.LinkedHashSet<String> set = new java.util.LinkedHashSet<>();
    if (linea == null || linea.isBlank()) return "";

    for (String t : linea.split(",")) {
        String s = t.trim();
        switch (s) {
            case "1": set.add("Documento");   break;
            case "2": set.add("Electronico"); break;
            case "3": set.add("Accesorio");   break;
            case "4": set.add("Ropa");        break;
            case "5": set.add("Utiles");      break;
            case "6": set.add("Recipientes"); break;
            case "7": set.add("Otros");       break;
            default:
                return null; 
        }
    }

    return String.join("|", set);  // ej: "Documento|Accesorio"
}

public int pedirNumeroConSalida() {
    try {
        int opcion = sc.nextInt();
        sc.nextLine(); // limpiar buffer

        if (opcion == 0) {
            return -1; // salir
        }
        if (opcion < 0) {
            System.out.println("Por favor ingrese un número positivo o 0 para regresar:");
            return pedirNumeroConSalida();
        }
        return opcion;
    } catch (Exception e) {
        sc.nextLine(); // limpiar buffer
        System.out.println("Entrada inválida. Ingrese un número válido o 0 para regresar:");
        return pedirNumeroConSalida();
    }
}


}


