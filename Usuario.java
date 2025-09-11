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


    //CSV helpers
    private static String esc(String s) {                                         // ⭐ agregado
        if (s == null) return "";
        String v = s.replace("\"", "\"\"");
        if (v.contains(",") || v.contains("\"") || v.contains("\n")) return "\"" + v + "\"";
        return v;
    }

    private static String unesc(String s) {                                       // ⭐ agregado
        if (s == null) return "";
        String t = s;
        if (t.startsWith("\"") && t.endsWith("\"") && t.length() >= 2) {
            t = t.substring(1, t.length() - 1).replace("\"\"", "\"");
        }
        return t;
    }

    private static String[] splitCsv(String line, int cols) {                     // ⭐ agregado
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

    //Conversión a CSV
    public String toCsv() {                                                       // ⭐ agregado
        return String.join(",",
                Integer.toString(idUsuario),
                esc(nombre), esc(correoInstitucional),
                esc(contrasena), esc(rol),
                Integer.toString(carnet), Integer.toString(puntos),
                Boolean.toString(permiso));
    }

    public static String csvHeader() {                                            // ⭐ agregado
        return "idUsuario,nombre,correo,contrasena,rol,carnet,puntos,permiso";
    }

    public static Usuario fromCsv(String line) {                                  // ⭐ agregado
        String[] p = splitCsv(line, 8);
        Usuario u = new Usuario(
                Integer.parseInt(p[0]),
                unesc(p[1]), unesc(p[2]), unesc(p[3]), unesc(p[4]));
        u.setCarnet(Integer.parseInt(p[5]));
        u.sumarPuntos(Integer.parseInt(p[6])); // inicia puntos
        u.setPermiso(Boolean.parseBoolean(p[7]));
        return u;
    }


}