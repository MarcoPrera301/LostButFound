public class Administrador
{
    private String nombre;
    private String adminID;
    private boolean permiso = true;
    public Administrador(String nombre, String adminId) {
        this.nombre = nombre;
        this.adminId = adminId;
        this.permiso = true;
    }
