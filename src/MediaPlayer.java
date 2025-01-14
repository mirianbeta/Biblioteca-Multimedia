import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class MediaPlayer extends Application{
    public static void main(String[] args){
        launch(args);
    }

    @Override
    public void start(Stage primaryStage){
        primaryStage.setTitle("Ventana Principal");
        Button btn = new Button("Abrir Nueva Ventana");
        btn.setOnAction(e -> abrirNuevaVenatana());
        primaryStage.setScene(new Scene(btn, 300, 250));
        primaryStage.show();
    }

    private void abrirNuevaVenatana(){
        Stage nuevaVentana = new Stage();
        nuevaVentana.setTitle("Nueva Ventana");
        nuevaVentana.setScene(new Scene(new Button("¡Hola desde la nueva ventana!"), 300, 200));
        nuevaVentana.show();
    }
}

