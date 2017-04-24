package controllers;

import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import managers.FXMLScenes;
import managers.StageManager;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import static controllers.SpectrumController.song;
import static controllers.SpectrumController.spectrumImageChart;
import static methodclasses.SceneMaster.fourierPerformer;

/**
 * Created by Gabo on 12/04/2017.
 */
public class SpectrumMaximizedController implements Initializable {

    @FXML
    ImageView image;
    @FXML
    HBox hBox;

    private Scene backScene;

    public void setImageV(ImageView image) {
        this.image.setImage(image.getImage());
        this.image.setPreserveRatio(false);
        hBox.setFillHeight(true);
        this.image.fitHeightProperty().bind(hBox.heightProperty());
        this.image.fitWidthProperty().bind(hBox.widthProperty());
    }

    public void setBackScene(Scene backScene) {
        this.backScene = backScene;
    }

    private void handleClick(MouseEvent event) {
        if (event.getButton() == MouseButton.SECONDARY) {
            final ContextMenu contextMenu = new ContextMenu();
            MenuItem item1 = new MenuItem("Zoom en la Región de Interés");
            MenuItem item2 = new MenuItem("Regresar a la Vista Anterior");
            MenuItem item3 = new MenuItem("Guardar Imagen");
            item1.setOnAction(event2 -> {
                //Zoom en la región de interes
                if (song.getImages().get(1) != null)
                    paintSpectZoomed();
                else
                    image.setImage(song.getImages().get(2));
            });
            item2.setOnAction(event2 -> {
                //Abrir gráfica en otra ventana
                backToScene();
            });
            item3.setOnAction(event1 -> {
                File outfile = new File("snapshot.png");
                try {
                    ImageIO.write(SwingFXUtils.fromFXImage(((ImageView) event.getSource()).snapshot(null,
                            null), null), "png", outfile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            contextMenu.getItems().addAll(item1, item2, item3);
            contextMenu.show(((Node) (event.getSource())), event.getScreenX(), event.getScreenY());
        }
    }

    private void backToScene() {
        StageManager stageManager = new StageManager(FXMLScenes.SPECTRUM);
        stageManager.back(backScene, image.getScene());
    }

    private void paintSpectZoomed() {
        Service<Void> service = new Service<Void>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        Platform.runLater(() -> {
                            WritableImage imageW = spectrumImageChart.prepareImage("Espectograma", "Frecuencia", "Tiempo");
                            try {
                                imageW = spectrumImageChart.drawSpectrumZoomed(imageW, song.getFftL(), fourierPerformer.WINDOW,
                                        fourierPerformer.getMaxPosition(), (int) song.getWaveSound().getDurationMiliSec());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            image.setImage(imageW);
                            song.getImages().add(imageW);
                        });
                        return null;
                    }
                };
            }
        };
        service.restart();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        image.setOnMouseClicked(event -> handleClick(event));
    }
}
