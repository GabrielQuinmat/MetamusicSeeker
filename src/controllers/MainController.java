package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import managers.FXMLScenes;
import managers.StageManager;
import packers.SongList;
import pojo.Song;
import stages.Main;

import javax.swing.*;
import java.io.*;
import java.net.URL;
import java.util.ResourceBundle;


public class MainController implements Initializable {


    StageManager stageManager;
    Window window;

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
    ListView<Song> songsItems;
    @FXML
    TextArea songInfo;
    @FXML
    Button playButton;
    @FXML
    Button stopButton;
    @FXML
    Button fowardButton;
    @FXML
    Button backwardButton;
    @FXML
    Button analyzeButton;
    @FXML
    TextArea textArea;

    ObservableList<String> songNames = FXCollections.observableArrayList();
    SongList songs;


    public void getSongsFromList() {
        try {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream("packers/p/los.dat"));
            songs = (SongList) in.readObject();
            for (Song song :
                    songs) {
                songNames.add(song.getPath());
                songsItems.getItems().add(song);
            }
        } catch (IOException e) {
            songs = new SongList();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        window = Main.window;
        getSongsFromList();
        openMI.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Selecciona un Archivo de Audio");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Archivos de Audio", "*.wav", "*.mp3"));
            File selectedFile = fileChooser.showOpenDialog(window);
            if (selectedFile != null) {
                Song song = new Song(selectedFile.getPath());
                songs.add(song);
                songNames.add(song.getPath());
                songsItems.getItems().add(song);
            } else
                JOptionPane.showMessageDialog(null, "No se seleccionó ningún archivo.");
        });

        saveMI.setOnAction(event -> {
            try {
                ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("slsi.dat"));
                out.writeObject(songs);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        analyzeButton.setOnAction(event -> {
            Song songClicked = songsItems.getSelectionModel().getSelectedItem();
            if (songClicked != null) {
                SpectrumController.song = songClicked;
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
}
