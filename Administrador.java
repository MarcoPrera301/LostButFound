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
    public String getNombre() { return nombre; }
    public String getAdminId() { return adminId; }
    public boolean getPermiso() { return permiso; }

    private static String esc(String s) {
        if (s == null) return "";
        String v = s.replace("\"", "\"\"");
        if (v.contains(",") || v.contains("\"") || v.contains("\n")) return "\"" + v + "\"";
        return v;
    }
    private static String unesc(String s) {
        if (s == null) return "";
        String t = s;
        if (t.startsWith("\"") && t.endsWith("\"") && t.length() >= 2) {
            t = t.substring(1, t.length() - 1).replace("\"\"", "\"");
        }
        return t;
    }
