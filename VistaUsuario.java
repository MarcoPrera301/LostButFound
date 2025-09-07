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

    public String IniciarVistaUsuario() 
    {
        System.out.println("====== Lost But Found - UVG ======");
        System.out.println("Bienvenido al sistema de objetos perdidos.");
        System.out.println("Por favor, ingrese su nombre:");
        this.nombre = sc.nextLine();

        System.out.println("Ingrese su correo institucional:");
        this.correo = sc.nextLine();

        System.out.println("Ingrese su número de carnet:");
        this.carnet = sc.nextInt();
        sc.nextLine(); // Consumir el salto de línea

        return nombre + "," + correo + "," + carnet;
    }

    public Objeto solicitarDatosObjetoEncontrado() 
    {
        System.out.println("Ingrese una descripcion del objeto perdido:");
        String descripcion = sc.nextLine();

        System.out.println("Ingrese que tipo de objeto es \n(Pachon, Carnet, Cargador, Audifonos, etc..):");
        String tipo = sc.nextLine();

        System.out.println("Ingrese donde encontró el objeto:");
        String ubicacion = sc.nextLine();

        LocalDate fechaEncontrado = LocalDate.parse(sc.nextLine());

        return new Objeto(descripcion, "Encontrado", tipo , fechaEncontrado , ubicacion, this.generarIdObjeto(), nombre);
    }

    public int generarIdObjeto() 
    {
        return siguienteIdObjeto++;
    }
}