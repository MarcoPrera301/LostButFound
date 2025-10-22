public class Administrador extends Usuario
{
    
    private String adminID;
    
    public Administrador(int idUsuario, String nombre, String correo, String contrasena, String adminID) {
        super(idUsuario, nombre, correo, contrasena, "ADMIN");
        this.adminID = adminID;
    }

    
    public String getAdminId() { return adminID; }
    

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

    private static String[] splitCsv(String line, int cols) {
        String[] out = new String[cols];
        int idx = 0; StringBuilder cur = new StringBuilder(); boolean inQ = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '\"') {
                if (inQ && i + 1 < line.length() && line.charAt(i + 1) == '\"') { cur.append('\"'); i++; }
                else inQ = !inQ;
            } else if (c == ',' && !inQ) { out[idx++] = cur.toString(); cur.setLength(0); }
            else cur.append(c);
        }
        out[idx++] = cur.toString();
        while (idx < cols) out[idx++] = "";
        return out;
    }

    public String toCsv() 
    { 
        return String.join(",", esc(getNombre()), esc(adminID), "true"); 
    }

    public static String csvHeader() { return "nombre,adminId,permiso"; }

    public static Administrador fromCsv(String line) 
    {
    String[] p = splitCsv(line, 3);
    // Usar valores por defecto para los campos del CSV antiguo
    return new Administrador(1, unesc(p[0]), "admin@uvg.edu.gt", "temp123", unesc(p[1]));
    }

    @Override
    public boolean puedeValidarReclamos() {
        return true; // Los administradores pueden validar reclamos
    }
}
