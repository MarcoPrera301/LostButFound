import java.util.Scanner;


public class VistaUsuario 
{
    private final Scanner sc;
    private int siguienteIdObjeto = 1;
    private String nombre;
    private String correo;
    private int carnet;

    public Consola() 
    {
        this.sc = new Scanner(System.in);
    }

    public  IniciarVistaUsuario() 
    {
        System.out.println("====== Lost But Found - UVG ======");
        System.out.println("Bienvenido al sistema de objetos perdidos.");


        // System.out.println("Por favor, para hacer login, ingrese su nombre:");
        // this.nombre = sc.nextLine();

        // System.out.println("Ingrese su correo institucional:");
        // this.correo = sc.nextLine();

        // System.out.println("Ingrese su número de carnet:");
        // this.carnet = sc.nextInt();
        // sc.nextLine();                   // registro de usuario pendiente




    }

  // Metodo para solicitar datos del objeto encontrado, que sera instanciado en Sistema.java
    public String solicitarTipoObjeto() 
    {
        System.out.println("Ingrese que tipo de objeto es \n(Electronico, Objeto Personal, Identificacion, etc..):");
        String tipo = sc.nextLine();
        return tipo;
    }

    public String solicitarDescripcion() 
    {
        System.out.println("Ingrese una descripcion del objeto perdido:");
        String descripcion = sc.nextLine();
        return descripcion;
    }

    public String solicitarUbicacionObjeto() 
    {
        System.out.println("Ingrese donde encontró el objeto:");
        String ubicacion = sc.nextLine();
        return ubicacion;
    }

    public LocalDate solicitarFechaEncontrado() 
    {
        System.out.println("Ingrese la fecha en que encontró el objeto (YYYY-MM-DD):");
        LocalDate fechaEncontrado = LocalDate.parse(sc.nextLine());
        return fechaEncontrado;
    }

    public String solicitarNombreObjeto() 
    {
        System.out.println("Ingrese el nombre del objeto:");
        String nombre = sc.nextLine();
        return nombre;
    }

    public int generarIdObjeto() 
    {
        return siguienteIdObjeto++;
    }
}