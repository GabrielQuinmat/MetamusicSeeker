package stages;/**
 * Created by Gabo on 02/04/2017.
 */

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class ProgressDialog extends Application {

    public static Stage newStage;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        newStage = new Stage();
        Parent root = null;
        try {
            root = FXMLLoader.load(getClass().getResource("progressScene.fxml"));
            newStage.setTitle("Progress");
            Scene scene = new Scene(root);
            setUserAgentStylesheet(STYLESHEET_CASPIAN);
            newStage.initStyle(StageStyle.UNDECORATED);
            newStage.setScene(scene);
            newStage.initModality(Modality.APPLICATION_MODAL);
            newStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
