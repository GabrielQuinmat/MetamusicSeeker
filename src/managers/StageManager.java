package managers;

import controllers.AlertDialogController;
import controllers.SpectrumMaximizedController;
import controllers.WaveformMaximizedController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import methodclasses.SceneMaster;

import java.io.IOException;

/**
 * Created by Gabo on 02/04/2017.
 */
public class StageManager extends Application{
    private FXMLScenes fscene;
    private Stage stage;


    public StageManager(FXMLScenes FxmlScene) {
        this.fscene = FxmlScene;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        stage = primaryStage;
        primaryStage.initStyle(fscene.getStageStyle());
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fscene.getFXMLName()));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource(fscene.getStylesheet()).toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.initModality(fscene.getModality());
        primaryStage.setTitle(fscene.getTitle());
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/icon/ipnIcon.png")));
        primaryStage.show();
    }


    public void dialogStart(FXMLScenes fscene,String message){
        Stage stage = new Stage();
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fscene.getFXMLName()));
        Parent root = null;
        try {
            root = loader.load();
            AlertDialogController controller = loader.getController();
            controller.setMessage(message);
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource(fscene.getStylesheet()).toExternalForm());
            stage.setScene(scene);
            stage.initModality(fscene.getModality());
            stage.setTitle(fscene.getTitle());
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
    * Se obtiene el Parent del FXML actual, */
    public void switchScene(FXMLScenes fsceneTo, Node node) {
        Parent root;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fsceneTo.getFXMLName()));
            root = loader.load();
            SceneMaster.spectrumSceneRoot = root;
            WaveformMaximizedController controller;
            SpectrumMaximizedController controller1;
            if (loader.getController() instanceof WaveformMaximizedController) {
                controller = loader.getController();
                controller.setLineChart((LineChart) node);
                controller.setBackScene(node.getScene());
            } else {
                controller1 = loader.getController();
                controller1.setImageV((ImageView) node);
                controller1.setBackScene(node.getScene());
            }
            stage = (Stage) node.getScene().getWindow();
            Scene scene = prepareScene(root);
            stage.setScene(scene);
            stage.sizeToScene();
            stage.centerOnScreen();
//            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Scene prepareScene(Parent root) {
        Scene scene = stage.getScene();
        if (scene == null)
            scene = new Scene(root);
        scene.setRoot(root);
        return scene;
    }
}
