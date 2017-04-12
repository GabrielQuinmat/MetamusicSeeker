package controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ProgressBar;
import methodclasses.SpectrumImageChart;
import stages.ProgressDialog;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by Gabo on 02/04/2017.
 */
public class ProgressController implements Initializable {
    @FXML
    ProgressBar progressBar;

    public void closeStage() {
        progressBar.progressProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.doubleValue() > 8) {
                ProgressDialog.newStage.close();
            }
        });
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(() -> {
            progressBar.progressProperty().bind(SpectrumImageChart.progress);
            closeStage();
        });
    }
}
