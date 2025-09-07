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

    public void registrarObjetoEncontrado() 
    {
        Objeto objeto = vistaUsuario.solicitarDatosObjetoEncontrado();
        if (registrarObjeto(objeto)) {
            System.out.println("Objeto registrado exitosamente.");
        } else {
            System.out.println("Error al registrar el objeto. Por favor, intente de nuevo.");
        }
    }
}
