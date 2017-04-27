package controllers;

import javafx.animation.FillTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.collections.ObservableMap;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.media.Media;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;
import managers.FXMLScenes;
import managers.StageManager;
import pojo.Song;
import stages.Main;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.ResourceBundle;


public class MainController implements Initializable {


    StageManager stageManager;
    Window window;

    @FXML
    Button openB;
    @FXML
    Button openGraphB;
    @FXML
    Button openInfoB;

    @FXML
    TextFlow songTextFlow;
    @FXML
    TextFlow songInfoTF;

    @FXML
    MenuItem guideMI;
    @FXML
    MenuItem aboutMI;
    @FXML
    MenuItem openMI;
    @FXML
    MenuItem closeMI;
    @FXML
    MenuItem saveMI;
    @FXML
    Button analyzeButton;
    private Song song;
    private Text songPrelude;
    private Text songInfoPrelude;
    private FillTransition fillTransition;
    private Timeline timeline;

    private Font font = Font.font("Palatino Linotype", FontWeight.BOLD, 20);

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        window = Main.window;
        setTextFlow();
        //getSongsFromList();
        openMI.setOnAction(event -> {
            try {
                openAction();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (UnsupportedAudioFileException e) {
                e.printStackTrace();
            }
        });

        saveMI.setOnAction(event -> {
            try {
                ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("slsi.dat"));
                out.writeObject(song);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        analyzeButton.setOnAction(event -> {
            if (song != null) {
                SpectrumController.song = song;
                stageManager = new StageManager(FXMLScenes.SPECTRUM);
                try {
                    stageManager.start(new Stage());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                stageManager = new StageManager(FXMLScenes.ALERT_DIALOG);
                try {
                    stageManager.dialogStart(FXMLScenes.ALERT_DIALOG, "No se ha seleccionado un archivo.");
                } catch (Exception e) {

                }
            }
        });

    }

    private void setTextFlow() {
        songPrelude = new Text("No hay algún archivo de audio cargado");
        songPrelude.setFont(font);
        songTextFlow.setLineSpacing(5);
        songTextFlow.setTextAlignment(TextAlignment.CENTER);
        songTextFlow.getChildren().add(songPrelude);
        songInfoPrelude = new Text("Información General\n");
        songInfoPrelude.setFont(font);
        songInfoTF.setLineSpacing(5);
        songInfoTF.getChildren().add(songInfoPrelude);
    }

    @FXML
    private void openAction() throws IOException, UnsupportedAudioFileException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecciona un Archivo de Audio");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Archivos de Audio", "*.wav", "*.mp3"));
        File selectedFile = fileChooser.showOpenDialog(window);
        if (selectedFile != null) {
            if (AudioSystem.getAudioFileFormat(selectedFile).getFormat().getChannels() == 2) {
                song = new Song(selectedFile.getPath());
                fadeInNode();
                songPrelude.setFill(Color.WHITE);
                songPrelude.setText(selectedFile.getPath());
                songInfoTF.getChildren().removeAll();
                getMetadata(new Media(selectedFile.toURI().toString()));
                getAudioInputInfo(AudioSystem.getAudioFileFormat(selectedFile));
            } else {
                JOptionPane.showMessageDialog(null, "El archivo es monoaural. Debe ser" +
                        " un archivo de audio Estéreo");
            }
        } else
            JOptionPane.showMessageDialog(null, "No se seleccionó ningún archivo.");
    }

    private void getAudioInputInfo(AudioFileFormat audioFileFormat) {
        Text t1 = new Text("Longitud en Bytes: " + audioFileFormat.getByteLength() + "\n"),
                t2 = new Text("Formato: " + audioFileFormat.getFormat() + "\n"),
                t3 = new Text("Longitud en Frame: " + audioFileFormat.getFrameLength() + "\n");
        t1.setFont(font);
        t2.setFont(font);
        t3.setFont(font);
        songInfoTF.getChildren().addAll(t1, t2, t3);
    }

    private void fadeInNode() {
        timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(songTextFlow.opacityProperty(), 0.0)),
                new KeyFrame(new Duration(800), new KeyValue(songTextFlow.opacityProperty(), 1.0)));
        timeline.play();
    }

    private void errorTextFlow() {
        Timeline timeline = new Timeline();
        KeyValue initK = new KeyValue(songTextFlow.backgroundProperty(),
                null);
        KeyValue endK = new KeyValue(songTextFlow.backgroundProperty(),
                new Background(new BackgroundFill(Color.RED, null, null)));
        KeyFrame initFrame = new KeyFrame(Duration.ZERO, initK),
                endFrame = new KeyFrame(Duration.millis(30), endK);
        timeline.getKeyFrames().addAll(initFrame, endFrame);
        timeline.setAutoReverse(true);
        timeline.play();
    }

    public void getMetadata(Media media) {
        ObservableMap<String, Object> metadata = media.getMetadata();
        metadata.forEach((s, o) -> generateNewText(s, o));
    }

    private void generateNewText(String key, Object valueAdded) {
        songInfoTF.getChildren().removeAll();
        try {
            Text t1 = new Text(key), t2 = new Text(valueAdded + "\n");
            t1.setFont(Font.font("Palatino Linotype", FontWeight.BOLD, 20));
            songInfoTF.getChildren().addAll(t1, t2, new Text());
        } catch (Exception e) {
        }
    }
}
