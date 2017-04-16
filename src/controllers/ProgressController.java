package controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ProgressBar;
import methodclasses.SpectrumImageChart;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by Gabo on 02/04/2017.
 */
public class ProgressController implements Initializable {
    @FXML
    ProgressBar progressBar;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        progressBar.progressProperty().bind(SpectrumImageChart.progress);
        progressBar.progressProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.doubleValue() > 8) {
                progressBar.getScene().getWindow().hide();
            }
        });

    }
}
