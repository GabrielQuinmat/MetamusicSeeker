package controllers;

import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import methodclasses.FourierPerformer;
import methodclasses.SpectrumImageChart;
import pojo.Song;
import pojo.WaveMP3;
import pojo.WaveSound;

import javax.swing.*;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by Gabo on 02/04/2017.
 */
public class SpectrumController implements Initializable{

    @FXML
    BorderPane borderPane;

    @FXML
    LineChart waveform;
    private Service<Void> backgroundThread;
    public static Song song;
    private FourierPerformer fourierPerformer;
    private double[] lCoffs;
    private double[] rCoffs;
    SpectrumImageChart spectrumImageChart;


    @FXML
    ImageView spectrumImage;
    @FXML
    HBox hSpecBox;
    private ObservableList<XYChart.Series<Integer, Double>> amplitudesL;
    private ObservableList<XYChart.Series<Integer, Double>> amplitudesR;


    private void analyze(){
        backgroundThread = new Service<Void>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        if (song.getWaveSound() != null) {
                            fourierPerformer = new FourierPerformer(song.getWaveSound());
                            lCoffs = fourierPerformer.performFFT(song.getAmplitudesL());
                            rCoffs = fourierPerformer.performFFT(song.getAmplitudesR());


                            fourierPerformer.FFTFormatted(lCoffs, "FFT Left");
                            fourierPerformer.FFTFormatted(rCoffs, "FFT Right");
                        }
                        return null;
                    }
                };
            }
        };
        backgroundThread.setOnSucceeded(event -> {
            paintSpectrum();
        });
        backgroundThread.restart();
    }

    private void paintSpectrum() {
        Service<Void> S2 = new Service<Void>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        spectrumImageChart = new SpectrumImageChart();
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append("Frecuencia de Muestreo: "
                                + song.getWaveSound().getFormat().getSampleRate()).append("\n");
                        stringBuilder.append("Tamaño de Frame: "
                                + song.getWaveSound().getFormat().getFrameSize()).append("\n");
                        stringBuilder.append("Frecuencia de Frames: "
                                + song.getWaveSound().getFormat().getFrameRate()).append("\n");
                        stringBuilder.append("Milisegundos: "
                                + song.getWaveSound().getDurationMiliSec()).append("\n");
                        song.setSonfInfo(stringBuilder.toString());
                        Platform.runLater(() -> {
                            WritableImage image = spectrumImageChart.prepareImage("Espectograma", "Frecuencia", "Tiempo");
                            spectrumImage.setImage(image);
                            try {
                                image = spectrumImageChart.drawSpectrum(image, lCoffs, fourierPerformer.WINDOW,
                                        song.getWaveSound().getFormat().getSampleRate(), (int) song.getWaveSound().getDurationMiliSec());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            spectrumImage.setImage(image);
                        });
                        return null;
                    }
                };
            }
        };
        S2.restart();
        S2.setOnSucceeded(event ->{
            boolean metamusicConclusion = fourierPerformer.analyzeFFT(rCoffs, lCoffs);
            JOptionPane.showMessageDialog(null, song.toString() + " "
                    + ((metamusicConclusion)?"ES ":"NO ES ") + "Metamúsica");
        });
    }

    private void initiateData() {
        song.getWaveSoundData();
    }

    private void paintWaveForm() {
        Service<Void> bg = new Service<Void>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        amplitudesL = FXCollections.observableArrayList();
                        amplitudesR = FXCollections.observableArrayList();
                        XYChart.Series<Integer, Double> dataL = new XYChart.Series();
                        XYChart.Series<Integer, Double> dataR = new XYChart.Series();
                        for (int x = 0; x < song.getAmpRedL().length; x++) {
                            dataL.getData().add(new XYChart.Data(x, song.getAmpRedL()[x]));
                            dataR.getData().add(new XYChart.Data<>(x, song.getAmpRedR()[x]));
                        }
                        amplitudesL.add(dataL);
                        amplitudesR.add(dataR);
                        Platform.runLater(() -> {
                            waveform.setData(amplitudesL);
                        });
                        return null;
                    }
                };
            }
        };
        bg.restart();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        waveform.setCreateSymbols(false);
        initiateData();
        paintWaveForm();
        analyze();
        spectrumImage.fitWidthProperty().bind(borderPane.widthProperty().divide(2));
        spectrumImage.fitHeightProperty().bind(hSpecBox.heightProperty());
    }



}
