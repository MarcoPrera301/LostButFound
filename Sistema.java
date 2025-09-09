import java.util.List;
import java.util.ArrayList;
// ===== NUEVO (para preparar CSV) =====
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Sistema {
    private List<Objeto> listaObjetos;
    private List<Usuario> listaUsuarios;
    private List<Premio> listaPremios;
    private List<Administrador> listaAdministradores;
    private VistaUsuario vistaUsuario;

    // ===== NUEVO: ruta del CSV de usuarios =====
    private final Path rutaCSVUsuarios = Paths.get("data", "usuarios.csv");

    public Sistema() 
    {
        listaObjetos = new ArrayList<>();
        listaUsuarios = new ArrayList<>();
        listaPremios = new ArrayList<>();   
        listaAdministradores = new ArrayList<>();
        vistaUsuario = new VistaUsuario();

        // ===== NUEVO: preparar “BD” CSV =====
        asegurarCSVUsuariosConCabecera();
    }

    public void iniciarSistema() 
    {
        vistaUsuario.IniciarVistaUsuario();
    }

    public boolean registrarObjeto(Objeto objeto) 
    {
        if (objeto.esValido()) {
            listaObjetos.add(objeto); // Registrar objeto
            return true;
        }
        return false;
    }

    public void registrarUsuario(Usuario usuario) 
    {
        listaUsuarios.add(usuario);
    }

    public void registrarPremio(Premio premio) 
    {
        listaPremios.add(premio);
    }

    public void registrarAdministrador(Administrador administrador) 
    {
        listaAdministradores.add(administrador);
    }

    public String registrarObjetoEncontrado() 
    {
        Objeto objeto = new Objeto(
            vistaUsuario.solicitarDescripcion(),
            vistaUsuario.solicitarTipoObjeto(),
            "Encontrado",
            vistaUsuario.solicitarFechaEncontrado(),
            vistaUsuario.solicitarUbicacionObjeto(),
            vistaUsuario.siguienteIdObjeto(),
            "UsuarioX"
        ); // Reportado por usuario ficticio

        if (registrarObjeto(objeto)) 
        {
            return "Objeto registrado exitosamente.";
        } 
        else 
        {
            return "Error al registrar el objeto. Por favor, intente de nuevo.";
        }
    }

    // ================== NUEVO: preparación CSV ==================

    // Crea data/ y data/usuarios.csv con cabecera (si no existen).
    private void asegurarCSVUsuariosConCabecera() {
        try {
            Files.createDirectories(rutaCSVUsuarios.getParent());
            if (Files.notExists(rutaCSVUsuarios)) {
                try (BufferedWriter bw = Files.newBufferedWriter(
                        rutaCSVUsuarios, StandardCharsets.UTF_8, StandardOpenOption.CREATE)) {
                    bw.write("idUsuario,nombre,correo,contrasena,rol,creadoEn");
                    bw.newLine();
                }
            }
        } catch (IOException e) {
            System.err.println("No se pudo preparar el CSV de usuarios: " + e.getMessage());
        }
    }

    // Fecha como String (siguiendo tu preferencia)
    private String hoy() {
        return new SimpleDateFormat("yyyy-MM-dd").format(new Date());
    }
}