import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class MediaPlayer extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Ventana.fxml"));
            Parent root = loader.load();  // Primero cargamos el FXML
            
            // Ahora obtenemos el controlador correctamente
            BibliotecaController controller = loader.getController();
            controller.setStage(primaryStage);

            primaryStage.setScene(new Scene(root));
            primaryStage.setMaximized(true);
            primaryStage.setResizable(false);
            primaryStage.setTitle("Biblioteca multimedia");
        primaryStage.getIcons().add(new Image("file:src/img/icono.png"));
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
