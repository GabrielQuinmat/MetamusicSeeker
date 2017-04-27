package controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import managers.FXMLScenes;
import managers.StageManager;
import methodclasses.FourierPerformer;
import methodclasses.SceneMaster;
import methodclasses.SpectrumImageChart;
import pojo.Song;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.io.File;
import java.io.IOException;
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
    public static SpectrumImageChart spectrumImageChart;

    MenuItem item1 = new MenuItem("Zoom en la Región de Interes");
    MenuItem item2 = new MenuItem("Abrir Gráfica en Nueva Ventana");
    MenuItem item3 = new MenuItem("Guardar Imagen");


    @FXML
    ImageView spectrumImage;
    @FXML
    HBox hSpecBox;
    private ObservableList<XYChart.Series<Integer, Double>> amplitudes;


    private void analyze(){
        backgroundThread = new Service<Void>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        if (song.getWaveSound() != null) {
                            fourierPerformer = new FourierPerformer(song.getWaveSound());

                            SceneMaster.fourierPerformer = fourierPerformer;

                            lCoffs = fourierPerformer.performFFT(song.getAmplitudesL());
                            rCoffs = fourierPerformer.performFFT(song.getAmplitudesR());

                            song.setFftL(lCoffs);
                            song.setFftR(rCoffs);

//                            fourierPerformer.FFTFormatted(lCoffs, "FFT Left.txt");
//                            fourierPerformer.FFTFormatted(rCoffs, "FFT Right.txt");
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
                            song.getImages().add(image);
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
                    + ((metamusicConclusion) ? "ES " : "NO ES ") + "Metamúsica");
        });
    }

    private void paintSpectZoomed() {
        Service<Void> service = new Service<Void>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        Platform.runLater(() -> {
                            WritableImage image = spectrumImageChart.prepareImage("Espectograma", "Frecuencia", "Tiempo");
                            try {
                                image = spectrumImageChart.drawSpectrumZoomed(image, lCoffs, fourierPerformer.WINDOW,
                                        fourierPerformer.getMaxPosition(), (int) song.getWaveSound().getDurationMiliSec());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            spectrumImage.setImage(image);
                            song.getImages().add(image);
                        });
                        return null;
                    }
                };
            }
        };
        service.restart();
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
                        amplitudes = FXCollections.observableArrayList();
                        XYChart.Series<Integer, Double> dataL = new XYChart.Series();
                        dataL.setName("Canal Izquierdo");
                        XYChart.Series<Integer, Double> dataR = new XYChart.Series();
                        dataR.setName("Canal Derecho");
                        for (int x = 0; x < song.getAmpRedL().length; x++) {
                            dataL.getData().add(new XYChart.Data(x, song.getAmpRedL()[x]));
                            dataR.getData().add(new XYChart.Data<>(x, song.getAmpRedR()[x]));
                        }
                        amplitudes.addAll(dataL, dataR);
                        Platform.runLater(() -> {
                            WritableImage image = new WritableImage((int) waveform.getWidth(), (int) waveform.getHeight());
                            waveform.setData(amplitudes);
                            waveform.snapshot(null, image);
                            song.getImages().add(image);
                        });
                        return null;
                    }
                };
            }
        };
        bg.restart();
    }

    private void handleClick(MouseEvent event) {
        if (event.getButton() == MouseButton.SECONDARY) {
            final ContextMenu contextMenu = new ContextMenu();
            item1.setOnAction(event2 -> {
                //Zoom en la región de interes
                paintSpectZoomed();
            });
            item2.setOnAction(event2 -> {
                //Abrir gráfica en otra ventana
                if (event.getSource() instanceof ImageView)
                    maximizeScene((ImageView) event.getSource());
                else
                    maximizeScene((LineChart) event.getSource());
            });
            item3.setOnAction(event1 -> {
                File outfile = new File("snapshot.png");
                try {
                    ImageIO.write(SwingFXUtils.fromFXImage(((ImageView) event.getSource()).snapshot(null,
                            null), null), "png", outfile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            contextMenu.getItems().addAll(item1, item2, item3);
            contextMenu.show(((Node) (event.getSource())), event.getScreenX(), event.getScreenY());
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        waveform.setCreateSymbols(false);
        waveform.setAnimated(false);
        initiateData();
        paintWaveForm();
        analyze();
        SceneMaster.imageSpectrum = spectrumImage;
        SceneMaster.waveform = waveform;
        spectrumImage.fitWidthProperty().bind(borderPane.widthProperty().divide(2));
        spectrumImage.fitHeightProperty().bind(hSpecBox.heightProperty());
        spectrumImage.setOnMouseClicked(event -> handleClick(event));
        waveform.setOnMouseClicked(event -> handleClick(event));
    }

    private void maximizeScene(ImageView source) {
        StageManager stageManager = new StageManager(FXMLScenes.SPECTRUM_MAXIMIZED);
        stageManager.switchScene(FXMLScenes.SPECTRUM_MAXIMIZED, source);
    }

    private void maximizeScene(LineChart source) {
        StageManager stageManager = new StageManager(FXMLScenes.WAVEFORM_MAXIMIZED);
        stageManager.switchScene(FXMLScenes.WAVEFORM_MAXIMIZED, source);
    }

    private void initiateProgressDialogs() {
        StageManager stageManager = new StageManager(FXMLScenes.PROGRESS);
        try {
            stageManager.start(new Stage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
