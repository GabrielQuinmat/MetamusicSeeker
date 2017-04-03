package methodclasses;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;
import pojo.WaveSound;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

/**
 * Created by Gabo on 02/04/2017.
 */
public class FourierPerformer {

    public static final int WINDOW = 1024;
    private final WaveSound waveData;
    FastFourierTransformer fft;
    double[] magnitudes;
    public static final int JUMP_SECOND = 5;

    public FourierPerformer(WaveSound waveData) {
        this.waveData = waveData;
    }

    /*
    * La ejecución de la Transformada Rápida de Fourier se desarrolla de la siguiente forma:
    * 1. Habiendo entrado el arreglo del canal separado. Se hace un cálculo ce ventanas personalizadas
    * donde se recogen 1024 valores cada 5 segundos. El calculo es saber cuántas ventanas de 5 segundos
    * caben en el arreglo. De no caber los suficientes, se adecúa el ingresando valores cero para completar
    * un valor de 1024 restantes.
    * 2. Se procede a ejecutar el algoritmo de la FFT en cada 1024 valores
    * 3. Ya que el resultado dará la mitad de valores utilizables, se hace un arreglo de la mitad del tamaño y
    * se guardan sólo 512 valores. Estos 512 se podrán representar después para determinar la frecuencia corres-
    * pondiente y el tiempo.
    * */
    public void performFFT(double[] amplitudes) {
        fft = new FastFourierTransformer(DftNormalization.STANDARD);
        double[] divided = divideWindows(amplitudes);
        magnitudes = new double[divided.length/2];
        int index = 0, x = 0;
        while (index < divided.length) {
            double[] temp = Arrays.copyOfRange(divided, index, index + 1024);
            Complex[] complx = fft.transform( temp, TransformType.FORWARD);
            for (int i = 0; i < complx.length/2; i++) {
                double rr = (complx[i].getReal());
                double ri = (complx[i].getImaginary());
                magnitudes[i + (WINDOW/2 * x)] = Math.sqrt((rr * rr) + (ri * ri));
            }
            index += 1024; x++;
        }
    }

    private double[] divideWindows(double[] array) {
        double jump = JUMP_SECOND * waveData.getAudioInputStream().getFormat().getSampleRate();
        float x = Math.round(array.length / jump);
        double[] divided = new double[(int) x * 1024];
        for (int i = 0, y = 0, d = 0, index; i < divided.length; i++, y += 25) {
            index = (int) (y + (d * jump));
            if (y < (25 * WINDOW) && index < array.length) {
                divided[i] = array[index];
            } else if (index > array.length){
                divided[i] = 0.0;
            }else {
                d++;
                y = 0;
            }
        }
        return divided;
    }

    private double[] resizeArray(double[] array) {
        double diff = array.length % WINDOW;
        if (diff == 0)
            return array;
        else
            return Arrays.copyOf(array, array.length + (WINDOW - (int) Math.round(diff)));
    }

    public void FFTFormatted() {
        WriterOut writerOut = new WriterOut();
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0, c = 0; i < magnitudes.length; i++, c++) {
            if (c < 5) {
                stringBuilder.append(i + ": " + String.format("%,.3f", magnitudes[i]) + "      \t");
            } else {
                stringBuilder.append(i + ": " + String.format("%,.3f", magnitudes[i])).append("\n");
                c = -1;
            }
        }
        try {
            writerOut.saveText(stringBuilder, "FFT");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}
