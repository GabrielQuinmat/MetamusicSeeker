package managers;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

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

    /*
    * Se obtiene el Parent del FXML actual, */
    public void switchScene(FXMLScenes fsceneTo){
        Parent root;
        try {
            root = FXMLLoader.load(getClass().getResource(fsceneTo.getFXMLName()));
            Scene scene = prepareScene(root);
            stage.setScene(scene);
            stage.sizeToScene();
            stage.centerOnScreen();
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
