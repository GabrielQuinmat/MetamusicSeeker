package managers;

import controllers.AlertDialogController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import stages.AlertDialog;

import java.io.IOException;
import java.net.URL;

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
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fscene.getFXMLName()));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource(fscene.getStylesheet()).toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.initModality(fscene.getModality());
        primaryStage.setTitle(fscene.getTitle());
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
    public void switchScene(FXMLScenes fsceneTo){
        Parent root;
        try {
            root = FXMLLoader.load(getClass().getResource(fsceneTo.getFXMLName()));
            setUserAgentStylesheet(STYLESHEET_CASPIAN);
            Scene scene = prepareScene(root);
            stage.setScene(scene);
            stage.sizeToScene();
            stage.centerOnScreen();
            stage.initStyle(fsceneTo.getStageStyle());
            stage.show();
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
