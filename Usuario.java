import java.util.ArrayList;

public class Usuario {
    // --- mínimos para autenticación CSV ---
    private final int idUsuario;           // requerido por CSV/Sistema
    private String nombre;
    private String correoInstitucional;    // lo usaremos como "correo"
    private String contrasena;             // texto plano por ahora
    private String rol;                    // "ADMIN" | "USER" | etc.

    private int carnet;
    private int puntos;
    private ArrayList<RegistroObjetos> historialAcciones;
    private ArrayList<Premio> premioReclamado;
    private boolean permiso = false;

    // === Constructor usado por Sistema al leer/escribir CSV ===
    public Usuario(int idUsuario, String nombre, String correo, String contrasena, String rol) {
        this.idUsuario = idUsuario;
        this.nombre = nombre;
        this.correoInstitucional = correo; // mapeamos "correo" a tu campo institucional
        this.contrasena = contrasena;
        this.rol = rol;
    }

    // === Getters requeridos por VistaUsuario/Sistema ===
    public int getIdUsuario() { return idUsuario; }
    public String getNombre() { return nombre; }
    public String getCorreo() { return correoInstitucional; }   // compatibilidad con Sistema
    public String getContrasena() { return contrasena; }
    public String getRol() { return rol; }
}