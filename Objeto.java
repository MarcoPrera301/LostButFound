import java.time.LocalDate;

public class Objeto 
{
    private String descripcion;
    private String tipo;
    private String estado;
    private LocalDate fechaEncontrado;  
    private String lugarEncontrado;
    private LocalDate fechaDevolucion;  
    private int id;
    private String reportadoPor;
    private String usuarioQueReclama;

    public Objeto(String descripcion, String tipo, String estado, LocalDate fechaEncontrado, String lugarEncontrado, int id, String reportadoPor) {
        this.descripcion = descripcion;
        this.tipo = tipo;
        this.estado = estado;
        this.fechaEncontrado = fechaEncontrado;
        this.lugarEncontrado = lugarEncontrado;
        this.id = id;
        this.reportadoPor = reportadoPor;
        this.usuarioQueReclama = null; 
    }
}