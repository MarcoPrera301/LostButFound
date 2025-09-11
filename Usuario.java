import java.util.ArrayList;

public class Usuario {

    private final int idUsuario;
    private String nombre;
    private String correoInstitucional;
    private String contrasena;
    private String rol;

    private int carnet;
    private int puntos;
    private ArrayList<RegistroObjetos> historialAcciones;
    private ArrayList<Premio> premioReclamado;
    private boolean permiso = false;

    public Usuario(int idUsuario, String nombre, String correo, String contrasena, String rol) {
        this.idUsuario = idUsuario;
        this.nombre = nombre;
        this.correoInstitucional = correo;
        this.contrasena = contrasena;
        this.rol = rol;

        this.historialAcciones = new ArrayList<>();
        this.premioReclamado   = new ArrayList<>();
    }

    public int getIdUsuario() { return idUsuario; }
    public String getNombre() { return nombre; }
    public String getCorreo() { return correoInstitucional; }
    public String getContrasena() { return contrasena; }
    public String getRol() { return rol; }
    public int getCarnet() { return carnet; }
    public int getPuntos() { return puntos; }     
    public boolean getPermiso() { return permiso; }
    
    public void setNombre(String nombre) { this.nombre = nombre; }      
    public void setCorreo(String correo) { this.correoInstitucional = correo; } 
    public void setContrasena(String contrasena) { this.contrasena = contrasena; } 
    public void setRol(String rol) { this.rol = rol; }                        
    public void setCarnet(int carnet) { this.carnet = carnet; }                 
    public void setPermiso(boolean permiso) { this.permiso = permiso; }          


}