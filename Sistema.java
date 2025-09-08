import java.util.List;
import java.util.ArrayList;

public class Sistema {
    private List<Objeto> listaObjetos;
    private List<Usuario> listaUsuarios;
    private List<Premio> listaPremios;
    private List<Administrador> listaAdministradores;
    private VistaUsuario vistaUsuario;
    
    

    public Sistema() 
    {
        listaObjetos = new ArrayList<>();
        listaUsuarios = new ArrayList<>();
        listaPremios = new ArrayList<>();   
        listaAdministradores = new ArrayList<>();
        vistaUsuario = new VistaUsuario();
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
        Objeto objeto = new Objeto(vistaUsuario.solicitarDescripcion(), vistaUsuario.solicitarTipoObjeto(), "Encontrado", vistaUsuario.solicitarFechaEncontrado(),vistaUsuario.solicitarUbicacionObjeto(),vistaUsuario.siguienteIdObjeto(),"UsuarioX"); // Reportado por usuario ficticio
        if (registrarObjeto(objeto)) 
        {
            return "Objeto registrado exitosamente.";
        } 
        else 
        {
            return "Error al registrar el objeto. Por favor, intente de nuevo.";
        }
    }
}
