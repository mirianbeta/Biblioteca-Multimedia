
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

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
    private ListView<File> listaArchivos;

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
    private MediaPlayer mediaPlayer;
    private final Map<File, String> duraciones = new HashMap<>();

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void seleccionarCarpeta() {
        try {
            DirectoryChooser directorioChooser = new DirectoryChooser();
            directorioChooser.setTitle("Seleccionar Carpeta");
            File carpetaSeleccionada = directorioChooser.showDialog(stage);

            if (carpetaSeleccionada != null) {
                rutaDirectorio = carpetaSeleccionada.getAbsolutePath();
                cargarArchivos();
            }
        } catch (SecurityException e) {
            mostrarAlerta("Error de acceso", "No tienes permisos para acceder a esta carpeta.");
        }
    }

    private void cargarArchivos() {
        ObservableList<File> archivos = FXCollections.observableArrayList();
        File directorio = new File(rutaDirectorio);

        if (directorio.exists() && directorio.isDirectory()) {
            for (File archivo : directorio.listFiles()) {
                if (archivo.isFile() && (archivo.getName().endsWith(".mp3") || archivo.getName().endsWith(".mp4"))) {
                    archivos.add(archivo);
                    obtenerDuracion(archivo);
                }
            }
        }

        listaArchivos.setItems(archivos);
        listaArchivos.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(File archivo, boolean empty) {
                super.updateItem(archivo, empty);
                if (empty || archivo == null) {
                    setText(null);
                } else {
                    String formato = archivo.getName().substring(archivo.getName().lastIndexOf(".") + 1).toUpperCase();
                    String duracion = duraciones.getOrDefault(archivo, "Cargando...");
                    setText(String.format("%s | %s | %s", archivo.getName(), formato, duracion));
                }
            }
        });
    }

    private void obtenerDuracion(File archivo) {
        Media media = new Media(archivo.toURI().toString());
        MediaPlayer tempPlayer = new MediaPlayer(media);

        tempPlayer.setOnReady(() -> {
            double segundos = media.getDuration().toSeconds();
            String duracion = formatearDuracion(segundos);

            duraciones.put(archivo, duracion); // Guardamos la duración en el mapa
            Platform.runLater(() -> listaArchivos.refresh()); // Refrescamos la UI

            tempPlayer.dispose();
        });

        tempPlayer.setOnError(() -> {
            duraciones.put(archivo, "Desconocido");
            Platform.runLater(() -> listaArchivos.refresh());
            tempPlayer.dispose();
        });
    }

    private String formatearDuracion(double segundos) {
        int minutos = (int) (segundos / 60);
        int segs = (int) (segundos % 60);
        return String.format("%02d:%02d", minutos, segs);
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
        if (event.getClickCount() == 2) { // Doble clic
            File archivo = listaArchivos.getSelectionModel().getSelectedItem();

            if (archivo != null && archivo.exists()) {
                reproducirArchivo(archivo);
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
            reproducirArchivoSeleccionado(event);
        });
    }

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
            HBox.setHgrow(tituloArchivo, Priority.ALWAYS);
            tituloArchivo.setMaxWidth(Double.MAX_VALUE);

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
            double porcentaje = sliderTiempo.getValue();
            double tiempo = (mediaPlayer.getTotalDuration().toSeconds() * porcentaje) / 100;
            mediaPlayer.seek(Duration.seconds(tiempo));
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

    @FXML
    private void cambiarTamaño03() {
        double porcentaje = 0.3;
        double nuevoAncho = pantalla.getWidth() * porcentaje;
        double nuevoAlto = pantalla.getHeight() * porcentaje;

        mediaView.setFitWidth(nuevoAncho);
        mediaView.setFitHeight(nuevoAlto);

        StackPane.setAlignment(mediaView, Pos.CENTER);
    }

    @FXML
    private void cambiarTamaño05() {
        double porcentaje = 0.5;
        double nuevoAncho = pantalla.getWidth() * porcentaje;
        double nuevoAlto = pantalla.getHeight() * porcentaje;

        mediaView.setFitWidth(nuevoAncho);
        mediaView.setFitHeight(nuevoAlto);

        StackPane.setAlignment(mediaView, Pos.CENTER);
    }

    @FXML
    private void cambiarTamaño08() {
        double porcentaje = 0.8;
        double nuevoAncho = pantalla.getWidth() * porcentaje;
        double nuevoAlto = pantalla.getHeight() * porcentaje;

        mediaView.setFitWidth(nuevoAncho);
        mediaView.setFitHeight(nuevoAlto);

        StackPane.setAlignment(mediaView, Pos.CENTER);
    }

    @FXML
    private void cambiarTamaño1() {
        double porcentaje = 1;
        double nuevoAncho = pantalla.getWidth() * porcentaje;
        double nuevoAlto = pantalla.getHeight() * porcentaje;

        mediaView.setFitWidth(nuevoAncho);
        mediaView.setFitHeight(nuevoAlto);

        StackPane.setAlignment(mediaView, Pos.CENTER);
    }

    @FXML
    private void cerrarAplicacion() {
        Platform.exit();
        System.exit(0);
    }

    @FXML
    private void actualizarBiblioteca() {
        if (rutaDirectorio != null) {
            cargarArchivos();
        } else {
            mostrarAlerta("No hay biblioteca seleccionada", "Selecciona una carpeta antes de actualizar.");
        }
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.WARNING);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    @FXML
    private void mostrarAcercaDe() {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle("Acerca de esta app");
        alerta.setHeaderText("Información del Programa");
        alerta.setContentText("Creador: Mirian\nVersión: 1.0.0");

        alerta.showAndWait();
    }

    @FXML
    private void alternarEditorBiblioteca() {
        boolean editorVisible = pestañaEditor.isVisible();
        boolean bibliotecaVisible = pestañaBiblioteca.isVisible();

        if (editorVisible && bibliotecaVisible) {
            pestañaEditor.setVisible(false);
            pestañaBiblioteca.setVisible(false);
        } else {
            pestañaEditor.setVisible(true);
            pestañaBiblioteca.setVisible(true);
        }
    }

}
