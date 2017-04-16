package controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import managers.FXMLScenes;
import managers.StageManager;

import java.net.URL;
import java.util.ResourceBundle;

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
            item1.setOnAction(event2 -> {
                //Zoom en la región de interes
//                paintSpectZoomed();
            });
            item2.setOnAction(event2 -> {
                //Abrir gráfica en otra ventana
                maximizeScene((ImageView) event.getSource());
            });
            contextMenu.getItems().addAll(item1, item2);
            contextMenu.show(((Node) (event.getSource())), event.getScreenX(), event.getScreenY());
        }
    }

    private void maximizeScene(ImageView source) {
        StageManager stageManager = new StageManager(FXMLScenes.SPECTRUM);
        stageManager.switchScene(FXMLScenes.SPECTRUM, source);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        image.setOnMouseClicked(event -> handleClick(event));
    }
}
