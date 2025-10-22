import java.time.LocalDate;

public class Objeto 
{
    public static final String ESTADO_PERDIDO    = "perdido";
    public static final String ESTADO_ENCONTRADO = "encontrado";
    public static final String ESTADO_RECUPERADO = "recuperado";
    public static final String ESTADO_DONADO     = "donado";
    public static final String ESTADO_PENDIENTE_VALIDACION = "pendiente_validacion";


private String descripcion;
private String tipo;
private String estado;
private LocalDate fechaEncontrado;  
private String lugarEncontrado;
private LocalDate fechaDevolucion;  
private int id;
private String reportadoPor;
private String usuarioQueReclama;

    public Objeto(String descripcion, String tipo, String estado, LocalDate fechaEncontrado, String lugarEncontrado, int id, String reportadoPor, String usuarioQueReclama) 
    {
        this.descripcion = descripcion;
        this.tipo = tipo;
        this.estado = estado;
        this.fechaEncontrado = fechaEncontrado;
        this.lugarEncontrado = lugarEncontrado;
        this.id = id;
        this.reportadoPor = reportadoPor;
        this.usuarioQueReclama = usuarioQueReclama; 
    }

    public Objeto(String descripcion, String tipo, String estado, LocalDate fechaEncontrado, String lugarEncontrado, int id, String reportadoPor) 
    {
    this(descripcion, tipo, estado, fechaEncontrado, lugarEncontrado, id, reportadoPor, null);
    }

    public static Objeto nuevoPerdido(int id, String descripcion, String tipo, String reportadoPor) {
        return new Objeto(descripcion, tipo, ESTADO_PERDIDO, null, "", id, reportadoPor);
    }

    public static Objeto nuevoEncontrado(int id, String descripcion, String tipo,
            LocalDate fechaEncontrado, String lugarEncontrado, String reportadoPor) {
        return new Objeto(descripcion, tipo, ESTADO_ENCONTRADO, fechaEncontrado, lugarEncontrado, id, reportadoPor);
    }

    public void setEstadoRecuperado(LocalDate fecha, String usuarioQueReclama) { 
        this.estado = ESTADO_RECUPERADO; 
        this.fechaDevolucion = fecha; 
        this.usuarioQueReclama = usuarioQueReclama;
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

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getReportadoPor() { return reportadoPor; }
    public void setReportadoPor(String reportadoPor) { this.reportadoPor = reportadoPor; }

    public String getUsuarioQueReclama() { return this.usuarioQueReclama; }
    public void setUsuarioQueReclama(String usuarioQueReclama) { this.usuarioQueReclama = usuarioQueReclama; }

public boolean esValido() {
    if ( descripcion == null || descripcion.isEmpty()
    || tipo == null || tipo.isEmpty()
    || estado == null || estado.isEmpty()) {
        return false;
    }

    return switch (estado) {
        case ESTADO_PERDIDO -> reportadoPor != null && !reportadoPor.isEmpty();
        case ESTADO_ENCONTRADO -> fechaEncontrado != null
            && lugarEncontrado != null && !lugarEncontrado.isEmpty()
            && reportadoPor != null && !reportadoPor.isEmpty();
        case ESTADO_RECUPERADO -> fechaDevolucion != null
            && usuarioQueReclama != null && !usuarioQueReclama.isEmpty();
        case ESTADO_DONADO -> true;
        case ESTADO_PENDIENTE_VALIDACION -> usuarioQueReclama != null && !usuarioQueReclama.isEmpty();
        default -> false;
    };
}

public void setEstadoDonado(LocalDate fechaDonacion) {
    this.estado = ESTADO_DONADO;
    this.fechaDevolucion = fechaDonacion;
}

public int diasDesdeEncontrado() {
    if (this.fechaEncontrado == null) return -1;
    return (int) java.time.temporal.ChronoUnit.DAYS.between(this.fechaEncontrado, LocalDate.now());
}

public boolean esCandidatoNoReclamado(int limiteDias) {
    return ESTADO_ENCONTRADO.equals(this.estado)
        && this.fechaEncontrado != null
        && diasDesdeEncontrado() > limiteDias;
}


    /** @deprecated Usa {@link #setEstadoRecuperado(LocalDate, String)} para registrar fecha y usuario. */
    @Deprecated
    public void setEstadoRecuperado(LocalDate fecha) { 
        this.estado = ESTADO_RECUPERADO; 
        this.fechaDevolucion = fecha; 
    }
}

