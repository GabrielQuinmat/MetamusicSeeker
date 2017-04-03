package pojo;

import java.io.Serializable;

/**
 * Created by Gabo on 31/03/2017.
 */
public class Song implements Serializable {
    private String Path;
    private double[] amplitudesR;
    private double[] amplitudesL;
    private double[] fftR;
    private double[] fftL;

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
}
