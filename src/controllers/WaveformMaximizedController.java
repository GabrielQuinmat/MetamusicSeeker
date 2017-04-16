package controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.layout.HBox;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by Gabo on 12/04/2017.
 */
public class WaveformMaximizedController implements Initializable {

    @FXML
    private LineChart lineChart;
    @FXML
    private HBox hBox;

    private Scene backScene;

    public void setBackScene(Scene backScene) {
        this.backScene = backScene;
    }

    public void setLineChart(LineChart lineChart) {
        this.lineChart = lineChart;
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }
}
