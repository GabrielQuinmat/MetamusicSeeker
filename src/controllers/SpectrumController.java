package controllers;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by Gabo on 02/04/2017.
 */
public class SpectrumController implements Initializable{

    @FXML
    LineChart waveform;
    private Service<Void> backgroundThread;

    private void analyze(){
        backgroundThread = new Service<Void>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        //CODE
                        return null;
                    }
                };
            }
        };
        backgroundThread.setOnSucceeded(event -> {

        });

        backgroundThread.restart();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }
}
