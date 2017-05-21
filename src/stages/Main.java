package stages;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    public static Stage window;

    @Override
    public void start(Stage primaryStage) throws Exception {
//        executeCommand();
        Parent root = FXMLLoader.load(getClass().getResource("/fxmls/mainScene.fxml"));
        primaryStage.setTitle("Meta-Music Seeker");
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/stylesheets/mainStyle.css").toExternalForm());
        primaryStage.setScene(scene);
        window = primaryStage;
        window.getIcons().add(new Image(getClass().getResourceAsStream("/icon/ipnIcon.png")));
        window.show();
    }

    private boolean executeCommand() {
        String command = "java -Xmx1g " + System.getProperty("user.dir") + "\\Main.java";
        try {
            Process p = Runtime.getRuntime().exec(command);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }


    public static void main(String[] args) {
        launch(args);
    }
}
