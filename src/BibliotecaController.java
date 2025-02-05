
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
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

import javafx.scene.media.Media;
import javafx.scene.media.MediaException;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.input.MouseEvent;

/**
 * Controlador de la Biblioteca para gestionar archivos multimedia.
 * Proporciona funcionalidades para seleccionar una carpeta, listar archivos,
 * reproducir medios y controlar la reproducción.
 */
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

    /**
     * Asigna el escenario principal.
     * 
     * @param stage Escenario principal.
     */
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    /**
     * Abre un selector de directorios y carga los archivos multimedia de la carpeta
     * seleccionada.
     */
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

    /**
     * Carga los archivos multimedia del directorio seleccionado en la lista de
     * archivos.
     */
    private void cargarArchivos() {
        ObservableList<File> archivos = FXCollections.observableArrayList();
        File directorio = new File(rutaDirectorio);

        try {
            if (!directorio.exists() || !directorio.isDirectory()) {
                throw new IllegalArgumentException("La ruta especificada no es un directorio válido.");
            }

            File[] archivosLista = directorio.listFiles();
            if (archivosLista == null) {
                throw new NullPointerException("Error al listar archivos en el directorio.");
            }

            for (File archivo : archivosLista) {
                try {
                    if (archivo.isFile()
                            && (archivo.getName().endsWith(".mp3") || archivo.getName().endsWith(".mp4"))) {
                        archivos.add(archivo);
                        obtenerDuracion(archivo);
                    }
                } catch (SecurityException e) {
                    System.err.println("No se tiene permiso para acceder al archivo: " + archivo.getName());
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
                        try {
                            String formato = archivo.getName().substring(archivo.getName().lastIndexOf(".") + 1)
                                    .toUpperCase();
                            String duracion = duraciones.getOrDefault(archivo, "Cargando...");
                            setText(String.format("%s | %s | %s", archivo.getName(), formato, duracion));
                        } catch (IndexOutOfBoundsException e) {
                            setText(archivo.getName() + " | Formato desconocido");
                        }
                    }
                }
            });

        } catch (IllegalArgumentException | NullPointerException e) {
            System.err.println("Error al cargar archivos: " + e.getMessage());
        }
    }

    /**
     * Obtiene la duración de un archivo multimedia.
     * 
     * @param archivo Archivo multimedia del cual se quiere obtener la duración.
     */
    private void obtenerDuracion(File archivo) {
        try {
            Media media = new Media(archivo.toURI().toString());
            MediaPlayer tempPlayer = new MediaPlayer(media);

            tempPlayer.setOnReady(() -> {
                double segundos = media.getDuration().toSeconds();
                String duracion = formatearDuracion(segundos);
                duraciones.put(archivo, duracion);
                Platform.runLater(() -> listaArchivos.refresh());
                tempPlayer.dispose();
            });

            tempPlayer.setOnError(() -> {
                duraciones.put(archivo, "Desconocido");
                Platform.runLater(() -> listaArchivos.refresh());
                tempPlayer.dispose();
            });
        } catch (IllegalArgumentException e) {
            duraciones.put(archivo, "Formato no compatible");
            listaArchivos.refresh();
        }
    }

    /**
     * Formatea la duración en segundos en formato mm:ss.
     * 
     * @param segundos Duración en segundos.
     * @return Cadena formateada de duración.
     */
    private String formatearDuracion(double segundos) {
        int minutos = (int) (segundos / 60);
        int segs = (int) (segundos % 60);
        return String.format("%02d:%02d", minutos, segs);
    }

    /**
     * Oculta la pestaña del editor.
     */
    @FXML
    private void cerrarEditor() {
        pestañaEditor.setVisible(false);
    }

    /**
     * Muestra la pestaña del editor.
     */
    @FXML
    private void mostrarEditor() {
        pestañaEditor.setVisible(true);
    }

    /**
     * Muestra la pestaña de la biblioteca.
     */
    @FXML
    private void mostrarBiblioteca() {
        pestañaBiblioteca.setVisible(true);
    }

    /**
     * Oculta la pestaña de la biblioteca.
     */
    @FXML
    private void cerrarBiblioteca() {
        pestañaBiblioteca.setVisible(false);
    }

    /**
     * Reproduce el archivo seleccionado de la lista al hacer doble clic.
     * 
     * @param event Evento de mouse.
     */
    @FXML
    private void reproducirArchivoSeleccionado(MouseEvent event) {
        try {
            if (event.getClickCount() == 2) { // Doble clic
                File archivo = listaArchivos.getSelectionModel().getSelectedItem();

                if (archivo == null) {
                    throw new NullPointerException("No se ha seleccionado ningún archivo.");
                }

                if (!archivo.exists()) {
                    throw new FileNotFoundException("El archivo seleccionado no existe: " + archivo.getAbsolutePath());
                }

                reproducirArchivo(archivo);
            }
        } catch (FileNotFoundException | NullPointerException e) {
            System.err.println("Error al reproducir archivo: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error inesperado al reproducir archivo: " + e.getMessage());
        }
    }

    /**
     * Configura los componentes de la interfaz gráfica, inicializando el slider de
     * tiempo,
     * ajustando la vista multimedia al tamaño del contenedor y asignando eventos a
     * los botones y la lista de archivos.
     * 
     * Se establecen los valores iniciales para el slider de tiempo y se asignan
     * listeners para que su tamaño
     * se ajuste al progreso del vídeo o canción. Además, se maneja la integración
     * de la vista multimedia
     * en el contenedor correspondiente.
     * 
     * Se capturan excepciones para manejar posibles errores en la inicialización de
     * los componentes.
     */
    @FXML
    private void configurarComponentes() {
        try {
            // Inicializar el slider y asignarle un listener para que se actualice con el
            // progreso del vídeo/canción
            sliderTiempo.setValue(0); // Iniciar el slider en 0 al principio
            sliderTiempo.setBlockIncrement(1); // Incremento cuando se arrastra el slider
            sliderTiempo.setMax(100); // Definir un rango máximo

            if (mediaView != null) {
                pantalla.getChildren().add(mediaView); // Agregar mediaView al pane
            } else {
                throw new NullPointerException("El objeto mediaView no ha sido inicializado.");
            }

            // Listeners para ajustar la vista multimedia al tamaño del contenedor
            pantalla.widthProperty().addListener((obs, oldVal, newVal) -> {
                try {
                    mediaView.setFitWidth(newVal.doubleValue());
                    imageViewAudio.setFitWidth(newVal.doubleValue());
                } catch (NullPointerException e) {
                    System.err.println("Error al ajustar el ancho: " + e.getMessage());
                }
            });

            pantalla.heightProperty().addListener((obs, oldVal, newVal) -> {
                try {
                    mediaView.setFitHeight(newVal.doubleValue());
                    imageViewAudio.setFitHeight(newVal.doubleValue());
                } catch (NullPointerException e) {
                    System.err.println("Error al ajustar la altura: " + e.getMessage());
                }
            });

            // Asignación de eventos a los botones
            if (btnSeleccionarCarpeta != null) {
                btnSeleccionarCarpeta.setOnAction(e -> seleccionarCarpeta());
            } else {
                throw new NullPointerException("El botón btnSeleccionarCarpeta no ha sido inicializado.");
            }

            // Evento para manejar el doble clic sobre los archivos
            if (listaArchivos != null) {
                listaArchivos.setOnMouseClicked(event -> {
                    try {
                        reproducirArchivoSeleccionado(event);
                    } catch (Exception e) {
                        System.err.println("Error en el evento de listaArchivos: " + e.getMessage());
                    }
                });
            } else {
                throw new NullPointerException("El componente listaArchivos no ha sido inicializado.");
            }
        } catch (NullPointerException e) {
            System.err.println("Error al configurar componentes: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error inesperado en configurarComponentes: " + e.getMessage());
        }
    }

    /**
     * Reproduce un archivo multimedia.
     * 
     * @param archivo Archivo multimedia a reproducir.
     */
    private void reproducirArchivo(File archivo) {
        try {
            if (archivo == null) {
                throw new NullPointerException("El archivo proporcionado es nulo.");
            }

            if (!archivo.exists()) {
                throw new FileNotFoundException("El archivo no existe: " + archivo.getAbsolutePath());
            }

            String archivoRuta = archivo.toURI().toString();
            Media media;
            media = new Media(archivoRuta);

            if (mediaPlayer != null) {
                mediaPlayer.stop();
            }

            mediaPlayer = new MediaPlayer(media);
            mediaView.setMediaPlayer(mediaPlayer);

            // Actualizar el título
            Platform.runLater(() -> {
                try {
                    if (tituloArchivo != null) {
                        tituloArchivo.setText(archivo.getName()); // Establecer el nombre del archivo como texto
                        HBox.setHgrow(tituloArchivo, Priority.ALWAYS);
                        tituloArchivo.setMaxWidth(Double.MAX_VALUE);
                        tituloArchivo.setVisible(true); // Asegurar visibilidad del título
                    } else {
                        throw new NullPointerException("El componente tituloArchivo no está inicializado.");
                    }
                } catch (Exception e) {
                    System.err.println("Error al actualizar el título del archivo: " + e.getMessage());
                }
            });

            mediaPlayer.currentTimeProperty().addListener((observable, oldValue, newValue) -> {
                try {
                    if (mediaPlayer.getTotalDuration() != null && mediaPlayer.getTotalDuration().toSeconds() > 0) {
                        double porcentaje = (newValue.toSeconds() / mediaPlayer.getTotalDuration().toSeconds()) * 100;
                        sliderTiempo.setValue(porcentaje);
                    }
                } catch (Exception e) {
                    System.err.println("Error al actualizar el progreso de la reproducción: " + e.getMessage());
                }
            });

            if (archivo.getName().endsWith(".mp4")) {
                Platform.runLater(() -> {
                    try {
                        mediaView.setPreserveRatio(true);
                        mediaView.setFitWidth(pantalla.getWidth());
                        mediaView.setFitHeight(pantalla.getHeight());
                        mediaView.setVisible(true);
                        imageViewAudio.setVisible(false);
                    } catch (Exception e) {
                        System.err.println("Error al ajustar el tamaño de mediaView: " + e.getMessage());
                    }
                });
            } else if (archivo.getName().endsWith(".mp3")) {
                try {
                    if (imageViewAudio != null) {
                        Image imagen = new Image(getClass().getResource("/img/corchea.png").toExternalForm());
                        imageViewAudio.setImage(imagen);

                        Platform.runLater(() -> {
                            try {
                                imageViewAudio.setFitWidth(pantalla.getWidth());
                                imageViewAudio.setFitHeight(pantalla.getHeight());
                            } catch (Exception e) {
                                System.err.println("Error al ajustar el tamaño de imageViewAudio: " + e.getMessage());
                            }
                        });

                        imageViewAudio.setVisible(true);
                        mediaView.setVisible(false);
                    } else {
                        throw new NullPointerException("El componente imageViewAudio no está inicializado.");
                    }
                } catch (Exception e) {
                    System.err.println("Error al configurar la imagen para archivos de audio: " + e.getMessage());
                }
            }

            mediaPlayer.play();

        } catch (FileNotFoundException | MediaException e) {
            System.err.println("Error al reproducir archivo multimedia: " + e.getMessage());
        } catch (NullPointerException e) {
            System.err.println("Error de referencia a un objeto nulo: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error inesperado en reproducirArchivo: " + e.getMessage());
        }
    }

    /**
     * Inicia la reproducción del archivo multimedia actual.
     */
    @FXML
    private void play() {
        if (mediaPlayer != null) {
            mediaPlayer.play();
        }
    }

    /**
     * Pausa la reproducción del archivo multimedia actual.
     */
    @FXML
    private void pause() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
        }
    }

    /**
     * Detiene la reproducción del archivo multimedia actual.
     */
    @FXML
    private void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
    }

    /**
     * Cambia la posición de reproducción del mediaPlayer según el valor del slider
     * de tiempo.
     * Convierte el porcentaje del slider en segundos y ajusta la reproducción a ese
     * tiempo.
     */
    @FXML
    private void cambiarTiempo() {
        if (mediaPlayer != null) {
            double porcentaje = sliderTiempo.getValue();
            double tiempo = (mediaPlayer.getTotalDuration().toSeconds() * porcentaje) / 100;
            mediaPlayer.seek(Duration.seconds(tiempo));
        }
    }

    /**
     * Ajusta la velocidad de reproducción del mediaPlayer a 0.5x (mitad de la
     * velocidad normal).
     */
    @FXML
    private void cambiarVelocidad05() {
        if (mediaPlayer != null) {
            mediaPlayer.setRate(0.5);
        }
    }

    /**
     * Restablece la velocidad de reproducción del mediaPlayer a 1x (velocidad
     * normal).
     */
    @FXML
    private void cambiarVelocidad1() {
        if (mediaPlayer != null) {
            mediaPlayer.setRate(1);
        }
    }

    /**
     * Aumenta la velocidad de reproducción del mediaPlayer a 1.5x.
     */
    @FXML
    private void cambiarVelocidad15() {
        if (mediaPlayer != null) {
            mediaPlayer.setRate(1.5);
        }
    }

    /**
     * Aumenta la velocidad de reproducción del mediaPlayer a 2x (doble de la
     * velocidad normal).
     */
    @FXML
    private void cambiarVelocidad2() {
        if (mediaPlayer != null) {
            mediaPlayer.setRate(2);
        }
    }

    /**
     * Reduce el tamaño del mediaView al 30% del tamaño del contenedor pantalla.
     * Ajusta tanto el ancho como el alto y centra la vista en el StackPane.
     */
    @FXML
    private void cambiarTamaño03() {
        double porcentaje = 0.3;
        double nuevoAncho = pantalla.getWidth() * porcentaje;
        double nuevoAlto = pantalla.getHeight() * porcentaje;

        mediaView.setFitWidth(nuevoAncho);
        mediaView.setFitHeight(nuevoAlto);

        StackPane.setAlignment(mediaView, Pos.CENTER);
    }

    /**
     * Reduce el tamaño del mediaView al 50% del tamaño del contenedor pantalla.
     * Ajusta tanto el ancho como el alto y centra la vista en el StackPane.
     */
    @FXML
    private void cambiarTamaño05() {
        double porcentaje = 0.5;
        double nuevoAncho = pantalla.getWidth() * porcentaje;
        double nuevoAlto = pantalla.getHeight() * porcentaje;

        mediaView.setFitWidth(nuevoAncho);
        mediaView.setFitHeight(nuevoAlto);

        StackPane.setAlignment(mediaView, Pos.CENTER);
    }

    /**
     * Ajusta el tamaño del mediaView al 80% del tamaño del contenedor pantalla.
     * Ajusta tanto el ancho como el alto y centra la vista en el StackPane.
     */
    @FXML
    private void cambiarTamaño08() {
        double porcentaje = 0.8;
        double nuevoAncho = pantalla.getWidth() * porcentaje;
        double nuevoAlto = pantalla.getHeight() * porcentaje;

        mediaView.setFitWidth(nuevoAncho);
        mediaView.setFitHeight(nuevoAlto);

        StackPane.setAlignment(mediaView, Pos.CENTER);
    }

    /**
     * Ajusta el tamaño del mediaView al 100% del tamaño del contenedor pantalla.
     * Ajusta tanto el ancho como el alto y centra la vista en el StackPane.
     */
    @FXML
    private void cambiarTamaño1() {
        double porcentaje = 1;
        double nuevoAncho = pantalla.getWidth() * porcentaje;
        double nuevoAlto = pantalla.getHeight() * porcentaje;

        mediaView.setFitWidth(nuevoAncho);
        mediaView.setFitHeight(nuevoAlto);

        StackPane.setAlignment(mediaView, Pos.CENTER);
    }

    /**
     * Cierra la aplicación correctamente.
     * Utiliza Platform.exit() para cerrar JavaFX y System.exit(0) para terminar la
     * JVM.
     */
    @FXML
    private void cerrarAplicacion() {
        Platform.exit();
        System.exit(0);
    }

    /**
     * Actualiza la biblioteca cargando nuevamente los archivos de la ruta
     * seleccionada.
     * Si no hay una ruta establecida, muestra una alerta informando al usuario.
     */
    @FXML
    private void actualizarBiblioteca() {
        if (rutaDirectorio != null) {
            cargarArchivos();
        } else {
            mostrarAlerta("No hay biblioteca seleccionada", "Selecciona una carpeta antes de actualizar.");
        }
    }

    /**
     * Muestra una alerta con un título y mensaje personalizado.
     * 
     * @param titulo  El título de la alerta.
     * @param mensaje El contenido del mensaje a mostrar.
     */
    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.WARNING);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    /**
     * Muestra una ventana con información acerca de la aplicación.
     * Incluye el nombre del creador y la versión actual.
     */
    @FXML
    private void mostrarAcercaDe() {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle("Acerca de esta app");
        alerta.setHeaderText("Información del Programa");
        alerta.setContentText("Creador: Mirian\nVersión: 1.0.0");

        Stage stage = (Stage) alerta.getDialogPane().getScene().getWindow();

        Image icono = new Image("/img/informacion.png");
        stage.getIcons().add(icono);

        alerta.showAndWait();
    }

    /**
     * Alterna la visibilidad del editor y la biblioteca.
     * Si ambos están visibles, los oculta. Si alguno está oculto, los muestra.
     */
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
