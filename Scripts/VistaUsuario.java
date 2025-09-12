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

    public VistaUsuario() 
    {
        this.sc = new Scanner(System.in);
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
        System.out.println("7. Asignar roles");
        System.out.println("8. Salir");
        System.out.println("Seleccione una opción: ");

        opcion = sc.nextInt();
        sc.nextLine(); // Limpiar el buffer

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

    public int siguienteIdObjeto() 
    {
        return siguienteIdObjeto++;
    }


    public String getCorreo() {
        return this.correo;
    }

    public void setSistema(Sistema sistema) {
        this.sistema = sistema;
    }
    public void reclamarObjetoUI() {
        System.out.println("== Reclamo de objeto ==");
        System.out.print("ID del objeto a reclamar: ");
        int idObj = sc.nextInt();
        sc.nextLine();

        System.out.print("Confirma tu correo institucional: ");
        String correoConfirmado = sc.nextLine().trim();

        System.out.print("Ingresa tu carnet (o 0 si no aplica): ");
        int carnetConfirmado = sc.nextInt();
        sc.nextLine();

        System.out.print("Correo para autenticar: ");
        String correoLogin = sc.nextLine().trim();
        System.out.print("Contraseña: ");
        String passLogin = sc.nextLine().trim();

        Usuario u = sistema.getUsuarioActual();
        if (u == null) {
            final java.util.Scanner in = new java.util.Scanner(System.in);

            System.out.print("Correo para autenticar: ");
            correoLogin = in.nextLine().trim();

            System.out.print("Contraseña: ");
            passLogin = in.nextLine().trim();

            java.util.Optional<Usuario> maybe = sistema.autenticarUsuarioCSV(correoLogin, passLogin);
            if (maybe.isEmpty()) {
                System.out.println("Credenciales inválidas.");
                return;
            }
            u = maybe.get();
            sistema.setUsuarioActual(u);
        }
        
        boolean ok = sistema.reclamarObjetoConValidacion(
            idObj,
            u,
            correoConfirmado,
            (carnetConfirmado == 0 ? null : Integer.valueOf(carnetConfirmado))
            );

            if (ok) {
                System.out.println("Reclamo realizado.");
            } else {
                System.out.println("No se pudo realizar el reclamo.");
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

    public void asignarRolUI() {
        if (sistema == null || !sistema.esAdminSesion()) {
            System.out.println("Solo un administrador puede asignar roles.");
            return;
        }
        System.out.println("== Asignar rol ==");
        System.out.print("Correo del usuario: ");
        String correo = sc.nextLine().trim();

        System.out.print("Nuevo rol (ADMIN/ESTUDIANTE): ");
        String rol = sc.nextLine().trim().toUpperCase();

        if (!"ADMIN".equals(rol) && !"ESTUDIANTE".equals(rol)) {
            System.out.println("Rol inválido.");
            return;
        }

        boolean ok = sistema.asignarRolAUsuario(correo, rol);
        System.out.println(ok ? "Rol actualizado." : "No se pudo actualizar el rol.");
    }
}