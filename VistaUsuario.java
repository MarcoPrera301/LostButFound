import java.util.Scanner;

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
    public void mostrarLoginConsola(Sistema sistema) {
        String correo = solicitarCorreo();
        String contrasena = solicitarContrasena();

        sistema.autenticarUsuarioCSV(correo, contrasena).ifPresentOrElse(
            u -> System.out.println("✅ Bienvenido, " + u.getNombre() + " (" + u.getRol() + ")"),
            () -> System.out.println("❌ Credenciales inválidas.")
        );
    }

    // ----- Solicitud de datos para crear Objeto -----

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

    public String solicitarFechaEncontrado() 
    {
        System.out.println("Ingrese la fecha en que encontró el objeto (YYYY-MM-DD):");
        return sc.nextLine().trim();
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
}