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
