
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.layout.Pane;
import javafx.scene.control.Button;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class BibliotecaController {

    @FXML
    private Button btnAbrirBiblioteca, btnCloseBiblioteca, btnCloseEditor, btnFilter, btnPause, btnPlay,
            btnSeleccionarCarpeta, btnStop, btnTamaño03, btnTamaño05, btnTamaño08, btnTamaño1, btnVelocidad05,
            btnVelocidad1, btnVelocidad15, btnVelocidad20;

    @FXML
    private ListView<String> listaArchivos;

    @FXML
    private Pane pestañaBiblioteca, pestañaEditor;


    private Stage stage;
    private String rutaDirectorio;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    public void initialize() {
        btnSeleccionarCarpeta.setOnAction(e -> seleccionarCarpeta());
        
    }

    private void seleccionarCarpeta() {
        DirectoryChooser directorioChooser = new DirectoryChooser();
        directorioChooser.setTitle("Seleccionar Carpeta");

        File carpetaSeleccionada = directorioChooser.showDialog(stage);
        if (carpetaSeleccionada != null) {
            rutaDirectorio = carpetaSeleccionada.getAbsolutePath();
            cargarArchivos();
        }
    }

    private void cargarArchivos() {
        List<String> archivos = new ArrayList<>();
        File directorio = new File(rutaDirectorio);

        if (directorio.exists() && directorio.isDirectory()) {
            for (File archivo : directorio.listFiles()) {
                if (archivo.isFile() && (archivo.getName().endsWith(".mp3") || archivo.getName().endsWith(".mp4"))) {
                    archivos.add(archivo.getName());
                }
            }
        }

        listaArchivos.getItems().setAll(archivos);
    }

    @FXML
    private void cerrarEditor() {
        pestañaEditor.setVisible(false);
    }

    @FXML
    private void mostrarEditor() {
        pestañaEditor.setVisible(true);
    }

    @FXML
    private void mostrarBiblioteca(){
        pestañaBiblioteca.setVisible(true);
    }

    @FXML
    private void cerrarBiblioteca(){
        pestañaBiblioteca.setVisible(false);
    }
}
