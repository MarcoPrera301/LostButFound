import java.util.Scanner;
import java.time.LocalDate;
import java.util.Optional;


public class VistaUsuario 
{
    private final Scanner sc;
    private int siguienteIdObjeto = 1;

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
        System.out.println("Ingrese qué tipo de objeto es (Electrónico, Personal, Identificación, etc.):");
        return sc.nextLine();
    }



    public String solicitarDescripcion() 
    {
        System.out.println("Ingrese una descripción del objeto perdido:");
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
}