package controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.text.Text;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by Gabo on 11/04/2017.
 */
public class AlertDialogController implements Initializable{

public static StringProperty msg = new SimpleStringProperty("");
    @FXML
    public Text text;
    @FXML
    Button button;

    public void setMessage(String message){
        msg.setValue(message);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        text.textProperty().bind(msg);
        button.setOnAction(event -> button.getScene().getWindow().hide());
    }
}
