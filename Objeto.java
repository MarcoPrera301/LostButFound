import java.time.LocalDate;

public class Objeto 
{
    public static final String ESTADO_PERDIDO    = "perdido";
    public static final String ESTADO_ENCONTRADO = "encontrado";
    public static final String ESTADO_RECUPERADO = "recuperado";
    public static final String ESTADO_DONADO     = "donado";


private String descripcion;
private String tipo;
private String estado;
private LocalDate fechaEncontrado;  
private String lugarEncontrado;
private LocalDate fechaDevolucion;  
private String id;
private String reportadoPor;
private String usuarioQueReclama;

    public Objeto(String descripcion, String tipo, String estado, LocalDate fechaEncontrado, String lugarEncontrado, String id, String reportadoPor) 
    {
        this.descripcion = descripcion;
        this.tipo = tipo;
        this.estado = estado;
        this.fechaEncontrado = fechaEncontrado;
        this.lugarEncontrado = lugarEncontrado;
        this.id = id;
        this.reportadoPor = reportadoPor;
        this.usuarioQueReclama = null; 
    }

    public static Objeto nuevoPerdido(String id, String descripcion, String tipo, String reportadoPor) {
        return new Objeto(descripcion, tipo, "perdido", null, "", id, reportadoPor); 
    }

    public static Objeto nuevoEncontrado(String id, String descripcion, String tipo,
        LocalDate fechaEncontrado, String lugarEncontrado, String reportadoPor) {
    return new Objeto(descripcion, tipo, "encontrado", fechaEncontrado, lugarEncontrado, id, reportadoPor); 
    }


    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public LocalDate getFechaEncontrado() { return fechaEncontrado; }
    public void setFechaEncontrado(LocalDate fechaEncontrado) { this.fechaEncontrado = fechaEncontrado; }

    public String getLugarEncontrado() { return lugarEncontrado; }
    public void setLugarEncontrado(String lugarEncontrado) { this.lugarEncontrado = lugarEncontrado; }

    public LocalDate getFechaDevolucion() { return fechaDevolucion; }
    public void setFechaDevolucion(LocalDate fechaDevolucion) { this.fechaDevolucion = fechaDevolucion; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getReportadoPor() { return reportadoPor; }
    public void setReportadoPor(String reportadoPor) { this.reportadoPor = reportadoPor; }

    public String getUsuarioQueReclama() { return usuarioQueReclama; }
    public void setUsuarioQueReclama(String usuarioQueReclama) { this.usuarioQueReclama = usuarioQueReclama; }

    public boolean esValido() {
        return !(this.descripcion.isEmpty() || this.lugarEncontrado.isEmpty() || this.fechaEncontrado == null);
    }    


    public void setEstadoRecuperado(LocalDate fecha) { 
        this.estado = "recuperado"; 
        this.fechaDevolucion = fecha; 
    }
}

