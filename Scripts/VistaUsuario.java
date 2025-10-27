import java.time.LocalDate;
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
        System.out.print("Seleccione una opción: ");
        int opcion = sc.nextInt();
        sc.nextLine(); // limpiar buffer
        return opcion;
    }


    public int verMenu()
    {
        int opcion;

        System.out.println("\n--- Menú de Usuario ---");
        System.out.println("1. Reportar objeto perdido/encontrado");
        System.out.println("2. Busqueda de objetos encontrados");
        System.out.println("3. Validacion y reclamo de objeto");
        System.out.println("4. Canjear premios");
        System.out.println("5. Ver perfil y puntos");
        System.out.println("6. Eliminar objetos perdidos");
        System.out.println("7. Salir");
        System.out.println("Seleccione una opción: ");

        opcion = sc.nextInt();
        sc.nextLine(); // Limpiar el buffer

        return opcion;
    }

    public int menuReclamos() 
    {
    System.out.println("\n--- Validación y Reclamo de Objetos ---");
    System.out.println("1. Reclamar objeto");
    System.out.println("2. Validar reclamo (Solo administradores)");
    System.out.println("3. Volver al menú principal");
    System.out.print("Seleccione una opción: ");
    
    int opcion = sc.nextInt();
    sc.nextLine();
    return opcion;
    }

    // filtros 
    public int verFiltros() 
    {
        System.out.println("\n--- Filtros de Búsqueda de Objetos Encontrados ---");
        System.out.println("1. Filtrar por tipo de objeto");
        System.out.println("2. Filtrar por fecha de reporte");
        System.out.println("3. Filtrar por ubicacion");
        System.out.println("4. Volver al menú principal");
        System.out.print("Seleccione una opción: ");
        
        int opcion = sc.nextInt();
        sc.nextLine(); // Limpiar el buffer
        return opcion;
    }


    public String filtroTipo() 
    {
        System.out.println("Ingrese el tipo de objeto a buscar (Documento, Electronico, Accesorio, Ropa, Utiles, Recipientes, Otros):");
        String tipo = sc.nextLine().toLowerCase();
        return tipo;
    }

    public LocalDate filtroFecha1() 
    {
        System.out.println("Ingrese una fecha de reporte (YYYY-MM-DD):");
        String fechaStr = sc.nextLine();
        return LocalDate.parse(fechaStr);
    }

    public LocalDate filtroFecha2() 
    {
        System.out.println("Ingrese otra fecha de reporte (YYYY-MM-DD):");
        String fechaStr = sc.nextLine();
        return LocalDate.parse(fechaStr);
    }

    public String filtroUbicacion() 
    {
        System.out.println("Ingrese la ubicación donde se encontró el objeto: (CIT-618, Biblioteca, Edificio H, etc)");
        String ubicacion = sc.nextLine().toLowerCase();
        return ubicacion;
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

    public String estadoObjeto() 
    {
        System.out.println("Reportar objeto perdido/encontrado \nIngrese 'Perdido' o 'Encontrado'");
        String estado = sc.nextLine().trim().toLowerCase();
        return estado.equals("perdido") ? Objeto.ESTADO_PERDIDO : Objeto.ESTADO_ENCONTRADO;
    }


    public String solicitarTipoObjeto() 
    {
        System.out.println("Ingrese qué tipo de objeto es (Documento, Electronico, Accesorio, Ropa, Utiles, Recipientes, Otros):");
        return sc.nextLine().toLowerCase();
    }



    public String solicitarDescripcion() 
    {
        System.out.println("Ingrese una descripción del objeto perdido/encontrado:");
        return sc.nextLine();
    }

    public String solicitarUbicacionObjeto() 
    {
        System.out.println("Ingrese dónde encontro/perdio el objeto:");
        return sc.nextLine();
    }

    public LocalDate solicitarFechaEncontrado() 
    {
        System.out.println("Ingrese la fecha en que encontró el objeto (YYYY-MM-DD):");
        String fechaString = sc.nextLine().trim();
        return LocalDate.parse(fechaString);
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
        // Si no hay sistema (caso raro), sigue usando su contador interno
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
        
        System.out.print("ID del objeto a reclamar: ");
        int idObj = sc.nextInt();
        sc.nextLine();
        
        Usuario usuarioActual = sistema.getUsuarioActual();
        if (usuarioActual == null) {
            System.out.println("Debe iniciar sesión para reclamar objetos.");
            return;
        }
        
        boolean resultado = sistema.reclamarObjeto(idObj, usuarioActual);
        if (resultado) {
            System.out.println("Reclamo enviado exitosamente.");
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
        int idObj = sc.nextInt();
        sc.nextLine();
        
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
        int opcion = sc.nextInt();
        sc.nextLine();
        return opcion;
    }

/** Solicita el nombre del usuario (UI) */
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
        System.out.println("\n--- Objetos Encontrados ---");
        for (Objeto obj : objetos) {
            System.out.println("ID: " + obj.getId() + ", Tipo: " + obj.getTipo() + ", Descripción: " + obj.getDescripcion() +
            ", Estado: " + obj.getEstado() + ", Fecha Encontrado: " + obj.getFechaEncontrado() +
            ", Lugar Encontrado: " + obj.getLugarEncontrado());
        }
    }

    public void eliminarObjetoUI() {
        if (sistema == null || !sistema.esAdminSesion()) {
            System.out.println("Solo un administrador puede eliminar objetos.");
            return;
        }
        System.out.println("== Eliminar objeto ==");
        System.out.print("ID del objeto a eliminar: ");
        int id = sc.nextInt();
        sc.nextLine();

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

    /** Mensajes de error provenientes del sistema. */
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
}