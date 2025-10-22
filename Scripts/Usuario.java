import java.util.ArrayList;

public class Usuario {
    // --- mínimos para autenticación CSV ---
    private final int idUsuario;
    private String nombre;
    private String correoInstitucional;
    private String contrasena;
    private String rol;

    // --- tus campos existentes ---
    private int carnet;
    private int puntos;
    private ArrayList<Objeto> historialAcciones;
    private ArrayList<Premio> premioReclamado;
    private boolean permiso = false;

    public Usuario(int idUsuario, String nombre, String correo, String contrasena, String rol) {
        this.idUsuario = idUsuario;
        this.nombre = nombre;
        this.correoInstitucional = correo;
        this.contrasena = contrasena;
        this.rol = rol;

        // ✅ Inicializaciones seguras 
        this.historialAcciones = new ArrayList<>();
        this.premioReclamado   = new ArrayList<>();
    }

    public int getIdUsuario() { return idUsuario; }
    public String getNombre() { return nombre; }
    public String getCorreo() { return correoInstitucional; }
    public String getContrasena() { return contrasena; }
    public String getRol() { return rol; }


    public boolean esAdmin() {
        return this.rol != null && this.rol.trim().equalsIgnoreCase("ADMIN");
    }

    public boolean esEstudiante() {
        return this.rol != null && this.rol.equalsIgnoreCase("ESTUDIANTE");
    }

    public void setRol(String nuevoRol) {
        this.rol = (nuevoRol == null ? "" : nuevoRol.trim().toUpperCase());
    }

    public int getPuntos() { return puntos; }

    public void agregarPremio(Premio premio) {
       this.premioReclamado.add(premio);
    }

    public void restarPuntos(int cantidad) {
        this.puntos = Math.max(0, this.puntos - cantidad);
    }

    public boolean puedeReclamarObjetos() {
        return true; // Por defecto, todos los usuarios pueden reclamar
    }
    
    
    public boolean puedeValidarReclamos() 
    {
        return this.esAdmin();
    }
}