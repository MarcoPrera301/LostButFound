import java.util.Scanner;

public class VistaUsuario 
{
    private final Scanner sc;
    private int siguienteIdObjeto = 1;

    // Datos básicos del usuario (si luego los usas)
    private String nombre;
    private String correo;
    private int carnet;

    // Constructor correcto
    public VistaUsuario() 
    {
        this.sc = new Scanner(System.in);
    }

    // Firma correcta: void
    public void IniciarVistaUsuario() 
    {
        System.out.println("====== Lost But Found - UVG ======");
        System.out.println("Bienvenido al sistema de objetos perdidos.");
        // Aquí puedes mostrar menús o instrucciones iniciales
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

    // Fecha como String (evitamos LocalDate/LocalDateTime)
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

    // Nombre alineado con tu Sistema: siguienteIdObjeto()
    public int siguienteIdObjeto() 
    {
        return siguienteIdObjeto++;
    }
}