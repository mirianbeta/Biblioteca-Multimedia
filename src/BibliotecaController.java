
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.input.MouseEvent;

public class BibliotecaController {

    @FXML
    private Button btnAbrirBiblioteca, btnCloseBiblioteca, btnCloseEditor, btnFilter, btnPause, btnPlay,
            btnSeleccionarCarpeta, btnStop, btnTamaño03, btnTamaño05, btnTamaño08, btnTamaño1, btnVelocidad05,
            btnVelocidad1, btnVelocidad15, btnVelocidad20;

    @FXML
    private ListView<String> listaArchivos;

    @FXML
    private Pane pestañaBiblioteca, pestañaEditor;

    @FXML
    private StackPane pantalla;

    @FXML
    private MediaView mediaView;

    @FXML
    private ImageView imageViewAudio;

    @FXML
    private HBox visionado;

    @FXML
    private Slider sliderTiempo;

    @FXML
    private Label tituloArchivo;

    private Stage stage;
    private String rutaDirectorio;
    private String rutaArchivo;
    private MediaPlayer mediaPlayer;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
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
    private void mostrarBiblioteca() {
        pestañaBiblioteca.setVisible(true);
    }

    @FXML
    private void cerrarBiblioteca() {
        pestañaBiblioteca.setVisible(false);
    }

    @FXML
    private void reproducirArchivoSeleccionado(MouseEvent event) {
        // Verificar que el doble clic ha ocurrido
        if (event.getClickCount() == 2) {
            String archivoSeleccionado = listaArchivos.getSelectionModel().getSelectedItem();

            if (archivoSeleccionado != null) {
                // Crear el objeto File con la ruta completa del archivo seleccionado
                File archivo = new File(rutaDirectorio + File.separator + archivoSeleccionado);

                if (archivo.exists()) {
                    // Llamar al método para reproducir el archivo
                    reproducirArchivo(archivo);
                }
            }
        }
    }

    @FXML
    private void configurarComponentes() {

        // Inicializar el slider y asignarle un listener para que se actualice con el
        // progreso del vídeo/canción
        sliderTiempo.setValue(0); // Iniciar el slider en 0 al principio
        sliderTiempo.setBlockIncrement(1); // Incremento cuando se arrastra el slider
        sliderTiempo.setMax(100); // Definir un rango máximo, por ejemplo, el 100% de la duración del archivo

        // Esperamos a que JavaFX haya completado la construcción de la vista antes de
        // manipular el mediaView
        if (mediaView != null) {
            pantalla.getChildren().add(mediaView); // Agregar mediaView al pane
        }

        pantalla.widthProperty().addListener((obs, oldVal, newVal) -> {
            mediaView.setFitWidth(newVal.doubleValue());
            imageViewAudio.setFitWidth(newVal.doubleValue());
        });

        pantalla.heightProperty().addListener((obs, oldVal, newVal) -> {
            mediaView.setFitHeight(newVal.doubleValue());
            imageViewAudio.setFitHeight(newVal.doubleValue());
        });

        // Asignación de eventos a los botones
        btnSeleccionarCarpeta.setOnAction(e -> seleccionarCarpeta());

        // Evento para manejar el doble clic sobre los archivos
        listaArchivos.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) { // Verifica si fue un doble clic
                String archivoSeleccionado = listaArchivos.getSelectionModel().getSelectedItem();
                if (archivoSeleccionado != null) {
                    // Crear el objeto File con la ruta completa del archivo
                    File archivo = new File(rutaDirectorio + File.separator + archivoSeleccionado);

                    if (archivo.exists()) {
                        reproducirArchivo(archivo); // Reproducir el archivo seleccionado
                    }
                }
            }
        });
    }

    /*
     * private void reproducirArchivo(File archivo) {
     * String archivoRuta = archivo.toURI().toString();
     * Media media = new Media(archivoRuta);
     * if (mediaPlayer != null) {
     * mediaPlayer.stop(); // Detener cualquier reproducción anterior
     * }
     * mediaPlayer = new MediaPlayer(media);
     * mediaView.setMediaPlayer(mediaPlayer); // Asignar el mediaPlayer al MediaView
     * mediaPlayer.play(); // Reproducir el archivo
     * }
     */

    private void reproducirArchivo(File archivo) {
        String archivoRuta = archivo.toURI().toString();
        Media media = new Media(archivoRuta);

        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }

        mediaPlayer = new MediaPlayer(media);
        mediaView.setMediaPlayer(mediaPlayer);

        // Actualizar el título
        Platform.runLater(() -> {
            tituloArchivo.setText(archivo.getName()); // Establecer el nombre del archivo como texto
            tituloArchivo.setVisible(true); // Asegurarse de que el título sea visible
        });

        mediaPlayer.currentTimeProperty().addListener((observable, oldValue, newValue) -> {
            // Actualiza el slider con el progreso de la reproducción
            double porcentaje = (newValue.toSeconds() / mediaPlayer.getTotalDuration().toSeconds()) * 100;
            sliderTiempo.setValue(porcentaje);
        });

        if (archivo.getName().endsWith(".mp4")) {
            Platform.runLater(() -> {
                mediaView.setPreserveRatio(true);
                mediaView.setFitWidth(pantalla.getWidth());
                mediaView.setFitHeight(pantalla.getHeight());
            });
            mediaView.setVisible(true);
            imageViewAudio.setVisible(false);
        } else if (archivo.getName().endsWith(".mp3")) {
            imageViewAudio.setImage(new Image(getClass().getResource("/img/corchea.png").toExternalForm()));
            Platform.runLater(() -> {
                imageViewAudio.setFitWidth(pantalla.getWidth());
                imageViewAudio.setFitHeight(pantalla.getHeight());
            });
            imageViewAudio.setVisible(true);
            mediaView.setVisible(false);
        }

        mediaPlayer.play();
    }

    @FXML
    private void play() {
        if (mediaPlayer != null) {
            mediaPlayer.play();
        }
    }

    @FXML
    private void pause() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
        }
    }

    @FXML
    private void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
    }

    @FXML
    private void cambiarTiempo() {
        if (mediaPlayer != null) {
            double porcentaje = sliderTiempo.getValue(); // Obtener el valor del slider (porcentaje)
            double tiempo = (mediaPlayer.getTotalDuration().toSeconds() * porcentaje) / 100; // Convertir a tiempo real
            mediaPlayer.seek(Duration.seconds(tiempo)); // Cambiar la posición de reproducción
        }
    }

    @FXML
    private void cambiarVelocidad05() {
        if (mediaPlayer != null) {
            mediaPlayer.setRate(0.5);
        }
    }

    @FXML
    private void cambiarVelocidad1() {
        if (mediaPlayer != null) {
            mediaPlayer.setRate(1);
        }
    }

    @FXML
    private void cambiarVelocidad15() {
        if (mediaPlayer != null) {
            mediaPlayer.setRate(1.5);
        }
    }

    @FXML
    private void cambiarVelocidad2() {
        if (mediaPlayer != null) {
            mediaPlayer.setRate(2);
        }
    }
}
