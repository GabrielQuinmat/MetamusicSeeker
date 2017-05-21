package controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.Event;
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
import javafx.stage.FileChooser;
import methodclasses.FourierPerformer;
import methodclasses.SceneMaster;
import methodclasses.SpectrumImageChart;
import pojo.Song;

import javax.imageio.ImageIO;
import javax.sound.sampled.UnsupportedAudioFileException;
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
    private XYChart.Series<Integer, Double> dataL, dataR;

    MenuItem item1 = new MenuItem("Zoom en la Región de Interes");
    MenuItem item2 = new MenuItem("Abrir Gráfica en Nueva Ventana");
    MenuItem item3 = new MenuItem("Guardar Imagen");
    MenuItem item4 = new MenuItem("Cambiar de Canal");

    private int WaveFormState = 0;


    @FXML
    ImageView spectrumImage;
    @FXML
    HBox hSpecBox;
    private ObservableList<XYChart.Series<Integer, Double>> amplitudes;
    private int channelIndex = 0;


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
                        SpectrumImageChart spectrumImageChart1 = new SpectrumImageChart();
                        Platform.runLater(() -> {
                            WritableImage image = spectrumImageChart.prepareImage("Espectograma Izquierdo",
                                    "Frecuencia (KHz)", "Tiempo (Segundos)"),
                                    image2 = spectrumImageChart1.prepareImage("Espectograma Derecho",
                                            "Frecuencia (KHz)", "Tiempo (Segundos)");
                            spectrumImage.setImage(image);
                            try {
                                image = spectrumImageChart.drawSpectrum(image, lCoffs, fourierPerformer.WINDOW,
                                        song.getWaveSound().getFormat().getSampleRate(), (int) song.getWaveSound().getDurationMiliSec());
                                image2 = spectrumImageChart1.drawSpectrum(image2, rCoffs, fourierPerformer.WINDOW,
                                        song.getWaveSound().getFormat().getSampleRate(), (int) song.getWaveSound().getDurationMiliSec());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            spectrumImage.setImage(image);
                            song.getImages().add(image);
                            song.getImages().add(image2);
                        });
                        return null;
                    }
                };
            }
        };
        S2.restart();
        S2.setOnSucceeded(event ->{
            SpectrumImageChart.progress.set(0.9);
            boolean metamusicConclusion = fourierPerformer.analyzeFFT(rCoffs, lCoffs);
            JOptionPane.showMessageDialog(null, song.toString() + " "
                    + ((metamusicConclusion) ? "ES " : "NO ES ") + "Metamúsica");
            paintSpectZoomed();
            SpectrumImageChart.progress.set(1);
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
                            SpectrumImageChart spectrumImageChart1 = new SpectrumImageChart();
                            WritableImage image = spectrumImageChart.prepareImage("Espectograma Izquierdo", "Frecuencia (Hz)",
                                    "Tiempo (Segundos)"),
                                    image1 = spectrumImageChart1.prepareImage("Espectograma Derecho", "Frecuencia (Hz)",
                                    "Tiempo (Segundos)");
                            try {
                                image = spectrumImageChart.drawSpectrumZoomed(image, lCoffs, fourierPerformer.WINDOW,
                                        fourierPerformer.getMaxPosition(), (int) song.getWaveSound().getDurationMiliSec());
                                image1 = spectrumImageChart1.drawSpectrumZoomed(image1, rCoffs, fourierPerformer.WINDOW,
                                        fourierPerformer.getMaxPosition(), (int) song.getWaveSound().getDurationMiliSec());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            song.getImages().add(image);
                            song.getImages().add(image1);
                        });
                        return null;
                    }
                };
            }
        };
        service.restart();
    }

    private void initiateData() {
        try {
            song.getWaveSoundData();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
        }
    }

    private void paintWaveForm() {
        Service<Void> bg = new Service<Void>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        amplitudes = FXCollections.observableArrayList();
                        dataL = new XYChart.Series();
                        dataL.setName("Canal Izquierdo");
                        dataR = new XYChart.Series();
                        dataR.setName("Canal Derecho");
                        for (int x = 0; x < song.getAmpRedL().length; x++) {
                            dataL.getData().add(new XYChart.Data(x, song.getAmpRedL()[x]));
                            dataR.getData().add(new XYChart.Data<>(x, song.getAmpRedR()[x]));
                        }
                        amplitudes.addAll(dataL, dataR);
                        Platform.runLater(() -> {
                            WritableImage image = new WritableImage((int) waveform.getWidth(), (int) waveform.getHeight());
                            waveform.setData(amplitudes);
                        });
                        return null;
                    }
                };
            }
        };
        bg.restart();
    }

    private void handleClickImage(MouseEvent event) {
        if (event.getButton() == MouseButton.SECONDARY) {
            final ContextMenu contextMenu = new ContextMenu();
            rewriteMenuText();
            item1.setOnAction(event2 -> {
                //Zoom en la región de interes
                channelIndex = (channelIndex == 0) ? 2 : 3;
                spectrumImage.setImage(song.getImages().get(channelIndex));
            });
            item2.setOnAction(event2 -> {
                //Abrir gráfica en otra ventana
                maximizeScene(0);
            });
            item3.setOnAction(event1 -> {
                snapshotImage(event);
            });
            item4.setOnAction(event1 -> {
                if (channelIndex < 2)
                    channelIndex = (channelIndex == 0) ? 1 : 0;
                else
                    channelIndex = (channelIndex == 2) ? 3 : 2;
                spectrumImage.setImage(song.getImages().get(channelIndex));
            });
            contextMenu.getItems().addAll(item1, item2, item3, item4);
            contextMenu.show(((Node) (event.getSource())), event.getScreenX(), event.getScreenY());
        }
    }

    private void rewriteMenuText() {
        item1.setText("Zoom en la Región de Interés");
        item2.setText("Abrir Gráfica en Nueva Ventana");
        item3.setText("Guardar Imagen");
        item4.setText("Cambiar de Canal");
    }

    private void snapshotImage(Event event) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Seleccione la Ruta y el Nombre para la Captura de Pantalla");
        fc.setInitialFileName("snapshot.png");
        File outfile = fc.showSaveDialog(null);
        try {
            ImageIO.write(SwingFXUtils.fromFXImage(((ImageView) event.getSource()).snapshot(null,
                    null), null), "png", outfile);
            JOptionPane.showMessageDialog(null, "Se ha guardado el archivo");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Ha ocurrido un error al guardar la captura");
            e.printStackTrace();
        }
    }

    private void handleClickWaveForm(MouseEvent event) {
        if (event.getButton() == MouseButton.SECONDARY) {
            if (WaveFormState == 3) WaveFormState = 0;
            final ContextMenu contextMenu = new ContextMenu();
            String[] texts = {"Cambiar a Canal Izquierdo", "Cambiar a Canal Derecho", "Cambiar a Ambos Canales"};
            item1.setText(texts[0]);
            item1.setOnAction(event2 -> {
                //Zoom en la región de interes
                changeSeries(WaveFormState++);
            });
            item2.setOnAction(event2 -> {
                //Abrir gráfica en otra ventana
                maximizeScene(1);
            });
            item3.setOnAction(event1 -> {
                File outfile = new File("snapshot.png");
                try {
                    ImageIO.write(SwingFXUtils.fromFXImage(((LineChart) event.getSource()).snapshot(null,
                            null), null), "png", outfile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            contextMenu.getItems().addAll(item1, item2, item3);
            contextMenu.show(((Node) (event.getSource())), event.getScreenX(), event.getScreenY());
        }
    }

    private void handleClickWaveFormMax(MouseEvent event) {
        if (event.getButton() == MouseButton.SECONDARY) {
            if (WaveFormState == 3) WaveFormState = 0;
            String[] texts = {"Cambiar a Canal Izquierdo", "Cambiar a Canal Derecho", "Cambiar a Ambos Canales"};
            final ContextMenu contextMenu = new ContextMenu();
            item1.setText("Regresar a la vista anterior");
            item2.setText(texts[0]);
            item3.setText("Guardar imagen");
            item1.setOnAction(event1 -> {
                returnState(1);
            });
            item2.setOnAction(event2 -> {
                //Zoom en la región de interes
                changeSeries(WaveFormState++);
            });
            item3.setOnAction(event1 -> snapshotImage(event));
            contextMenu.getItems().addAll(item1, item2, item3);
            contextMenu.show(((Node) (event.getSource())), event.getScreenX(), event.getScreenY());
        }
    }

    private void handleClickImageMax(MouseEvent event) {
        if (event.getButton() == MouseButton.SECONDARY) {
            final ContextMenu contextMenu = new ContextMenu();
            item1.setText("Regresar a la vista anterior");
            item2.setText("Zoom en la zona de interés");
            item3.setText("Guardar imagen");
            item1.setOnAction(event1 -> {
                returnState(0);
            });
            item2.setOnAction(event1 -> {
                channelIndex = (channelIndex == 0) ? 2 : 3;
                spectrumImage.setImage(song.getImages().get(channelIndex));
            });
            item3.setOnAction(event1 -> snapshotImage(event));
            item4.setOnAction(event1 -> {
                if (channelIndex < 2)
                    channelIndex = (channelIndex == 0) ? 1 : 0;
                else
                    channelIndex = (channelIndex == 2) ? 3 : 2;
                spectrumImage.setImage(song.getImages().get(channelIndex));
            });
            contextMenu.getItems().addAll(item1, item2, item3, item4);
            contextMenu.show(((Node) (event.getSource())), event.getScreenX(), event.getScreenY());
        }
    }


    private void returnState(int indexRecovered) {
        switch (indexRecovered) {
            case 0:
                hSpecBox.getChildren().add(0, waveform);
                spectrumImage.fitWidthProperty().bind(borderPane.widthProperty().divide(2));
                spectrumImage.fitHeightProperty().bind(hSpecBox.heightProperty());
                spectrumImage.setOnMouseClicked(event -> handleClickImage(event));
                break;
            case 1:
                hSpecBox.getChildren().add(1, spectrumImage);
                waveform.setOnMouseClicked(event -> handleClickWaveForm(event));
                break;
        }
    }


    private void changeSeries(int i) {
        waveform.getData().clear();
        amplitudes.clear();
        if (i != 2) {
            amplitudes.add((i == 0) ? dataL : dataR);
            waveform.setData(amplitudes);
        } else {
            amplitudes.addAll(dataL, dataR);
            waveform.setData(amplitudes);
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
        spectrumImage.setOnMouseClicked(event -> handleClickImage(event));
        waveform.setOnMouseClicked(event -> handleClickWaveForm(event));
    }

    private void maximizeScene(int indexRemoved) {
        switch (indexRemoved) {
            case 0:
                hSpecBox.getChildren().remove(indexRemoved);
                spectrumImage.fitWidthProperty().bind(borderPane.widthProperty());
                spectrumImage.fitHeightProperty().bind(hSpecBox.heightProperty());
                spectrumImage.setOnMouseClicked(event -> handleClickImageMax(event));
                break;
            case 1:
                hSpecBox.getChildren().remove(indexRemoved);
                waveform.setOnMouseClicked(event -> handleClickWaveFormMax(event));
                break;
        }

    }




}
