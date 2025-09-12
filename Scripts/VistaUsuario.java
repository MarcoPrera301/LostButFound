import java.time.LocalDate;
import java.util.Optional;
import java.util.Scanner;
import java.util.List;

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

    public void IniciarVistaUsuario() 
    {
        System.out.println("====== Lost But Found - UVG ======");
        System.out.println("Bienvenido al sistema de objetos perdidos.");
        // Aquí puedes mostrar menús o instrucciones iniciales
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
        System.out.println("6. Salir");
        System.out.println("Seleccione una opción: ");

        opcion = sc.nextInt();
        sc.nextLine(); // Limpiar el buffer

        return opcion;
    }

    // filtros 
    public void verFiltros() 
    {
        System.out.println("\n--- Filtros de Búsqueda de Objetos Encontrados ---");
        System.out.println("1. Filtrar por tipo de objeto");
        System.out.println("2. Filtrar por fecha de reporte");
        System.out.println("3. Filtrar por ubicacion");
        System.out.println("4. Volver al menú principal");
        System.out.print("Seleccione una opción: ");
        
        int opcion = sc.nextInt();
        sc.nextLine(); // Limpiar el buffer

        switch (opcion) {
            case 1:
                String tipo = filtroTipo();
                // Aquí iría la lógica para filtrar por tipo
                break;
            case 2:
                LocalDate fecha = filtroFecha();
                // Aquí iría la lógica para filtrar por fecha
                break;
            case 3:
                String ubicacion = filtroUbicacion();
                // Aquí iría la lógica para filtrar por ubicación
            case 4:
                System.out.println("Volviendo al menú principal...");
                return;
            case 5:
                reclamarObjetoUI();
                break;
            default:
                System.out.println("Opción no válida. Intente de nuevo.");
                break;
        }
    }

    public String filtroTipo() 
    {
        System.out.println("Ingrese el tipo de objeto a buscar (Documento, Electronico, Accesorio, Ropa, Utiles, Recipientes, Otros):");
        String tipo = sc.nextLine().toLowerCase();
        return tipo;
    }

    public LocalDate filtroFecha() 
    {
        System.out.println("Ingrese la fecha de reporte (YYYY-MM-DD):");
        String fechaStr = sc.nextLine();
        return LocalDate.parse(fechaStr);
    }

    public String filtroUbicacion() 
    {
        System.out.println("Ingrese la ubicación donde se encontró el objeto: (CIT-618, Biblioteca, Edificio H, etc)");
        String ubicacion = sc.nextLine().toLowerCase();
        return ubicacion;
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
    public boolean mostrarLoginConsola(Sistema sistema) {
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
        System.out.println("Ingrese dónde encontró el objeto:");
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
    private void reclamarObjetoUI() {
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

        java.util.Optional<Usuario> maybe = sistema.autenticarUsuarioCSV(correoLogin, passLogin);
        if (maybe.isEmpty()) {
            System.out.println("Credenciales inválidas.");
            return;
        }
        Usuario u = maybe.get();

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
}