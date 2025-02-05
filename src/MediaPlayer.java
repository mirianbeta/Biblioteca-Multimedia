import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * Clase principal de la aplicación MediaPlayer que carga la interfaz de usuario definida en FXML.
 * Extiende {@link Application} de JavaFX y configura la ventana principal.
 */
public class MediaPlayer extends Application {

    /**
     * Método de inicio de la aplicación JavaFX.
     * Carga el archivo FXML, obtiene el controlador y configura la ventana principal.
     * 
     * @param primaryStage La ventana principal de la aplicación.
     */
    @Override
    public void start(Stage primaryStage) {
        try {
            // Carga la interfaz desde el archivo FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Ventana.fxml"));
            Parent root = loader.load();  
            
            // Obtiene el controlador y le pasa el escenario
            BibliotecaController controller = loader.getController();
            controller.setStage(primaryStage);

            // Configura la ventana principal
            primaryStage.setScene(new Scene(root));
            primaryStage.setMaximized(true);
            primaryStage.setResizable(false);
            primaryStage.setTitle("Biblioteca multimedia");

            // Agrega el icono de la aplicación
            try {
                primaryStage.getIcons().add(new Image("file:src/img/icono.png"));
            } catch (IllegalArgumentException e) {
                System.err.println("Error al cargar el icono: " + e.getMessage());
            }

            primaryStage.show();
        } catch (IOException e) {
            System.err.println("Error al cargar la interfaz: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Error inesperado en la aplicación: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Método principal que lanza la aplicación JavaFX.
     * 
     * @param args Argumentos de la línea de comandos.
     */
    public static void main(String[] args) {
        launch(args);
    }
}