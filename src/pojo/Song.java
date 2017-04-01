package pojo;

import java.io.Serializable;

/**
 * Created by Gabo on 31/03/2017.
 */
public class Song implements Serializable {
    private String Path;
    private double[] amplitudes;
    private double[] fftR;
    private double[] fftL;

    public String getPath() {
        return Path;
    }

    public void setPath(String path) {
        Path = path;
    }

    public double[] getAmplitudes() {
        return amplitudes;
    }

    public void setAmplitudes(double[] amplitudes) {
        this.amplitudes = amplitudes;
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
}
