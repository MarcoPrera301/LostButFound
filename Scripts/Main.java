public class Main {
    public static void main(String[] args) {
        Sistema sistema = new Sistema();
        VistaUsuario vista = new VistaUsuario();
        sistema.iniciarSistema(); // crea el admin por defecto
        vista.setSistema(sistema);
    }
}