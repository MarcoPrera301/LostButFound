import java.util.List;
import java.util.ArrayList;

public class Sistema {
    private List<Objeto> listaObjetos;

    public Sistema() {
        listaObjetos = new ArrayList<>();
    }

    public boolean registrarObjetoPerdido(Objeto objeto) {
        if (objeto.esValido()) {
            listaObjetos.add(objeto); // Registrar objeto

            return true;
        }
        return false;
    }
}
