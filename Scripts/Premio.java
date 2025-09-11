public class Premio 
{
    private String nombre;
    private String descripcion;
    private int puntos;


    
    public Premio(String nombre, String descripcion, int puntos) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.puntos = puntos;
    }

    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public int getPuntos() { return puntos; }

}
