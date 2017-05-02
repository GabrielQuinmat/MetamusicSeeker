package pojo;

import javafx.scene.image.Image;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Gabo on 31/03/2017.
 */
public class Song implements Serializable {
    private String Path;
    private double[] amplitudesR;
    private double[] amplitudesL;
    private double[] ampRedL;
    private double[] ampRedR;
    private double[] fftR;
    private double[] fftL;
    private WaveMP3 waveMP3;
    private WaveSound waveSound;
    private String sonfInfo;
    private ArrayList<Image> images;

    private static final int reduceCriteria = 2000;

    public ArrayList<Image> getImages() {
        return images;
    }


    public double[] getAmpRedL() {
        return ampRedL;
    }

    public double[] getAmpRedR() {
        return ampRedR;
    }



    public Song(String path) {
        Path = path;
        images = new ArrayList<>();
        if (path.endsWith(".mp3"))
            waveMP3 = new WaveMP3(Path);
        else
            waveSound = new WaveSound(Path);
    }

    public WaveMP3 getWaveMP3() {
        return waveMP3;
    }

    public WaveSound getWaveSound() {
        return waveSound;
    }

    public String getPath() {
        return Path;
    }

    public void setPath(String path) {
        Path = path;
    }

    public double[] getAmplitudesR() {
        return amplitudesR;
    }

    public void setAmplitudesR(double[] amplitudesR) {
        this.amplitudesR = amplitudesR;
    }

    public double[] getAmplitudesL() {
        return amplitudesL;
    }

    public void setAmplitudesL(double[] amplitudesL) {
        this.amplitudesL = amplitudesL;
    }

    public double[] getFftR() {
        return fftR;
    }

    public void setFftR(double[] fftR) {
        this.fftR = fftR;
    }

    public double[] getFftL() {
        return fftL;
    }

    public void setFftL(double[] fftL) {
        this.fftL = fftL;
    }

    public void getWaveSoundData() throws IOException, UnsupportedAudioFileException {
        if (waveMP3 != null) {
            waveSound = new WaveSound(waveMP3);
        }else{
            waveSound.extractAmplitudeFromFile();
        }
        HashMap<String, double[]> channels = waveSound.separateChannels();
        amplitudesL = channels.get("Channel L");
        amplitudesR = channels.get("Channel R");
        setReductedAmplitudes(amplitudesL, amplitudesR);
    }

    private void setReductedAmplitudes(double[] arr1, double[] arr2) {
        int size = (int)Math.ceil((double)arr1.length/(double)reduceCriteria),
                size2 = (int)Math.ceil((double)arr2.length/(double)reduceCriteria);
        ampRedL = new double[size]; ampRedR = new double[size2];
        for (int x = 0, y = 0; y < arr1.length && y < arr2.length; x++, y += reduceCriteria) {
            ampRedL[x] = arr1[y]; ampRedR[x] = arr2[y];
        }
    }

    public String getSonfInfo() {
        return sonfInfo;
    }

    public void setSonfInfo(String sonfInfo) {
        this.sonfInfo = sonfInfo;
    }

    @Override
    public String toString() {
        return Path;
    }
}
