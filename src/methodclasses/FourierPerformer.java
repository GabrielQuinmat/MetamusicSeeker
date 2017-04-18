package methodclasses;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;
import pojo.WaveSound;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

/**
 * Created by Gabo on 31/03/2017.
 */
public class FourierPerformer {
    public static final int WINDOW = 1024;
    public static final int DB_MIN = 30;
    public static final int MAX_FREQUENCY = 1000;
    private final WaveSound waveData;
    FastFourierTransformer fft;
    double[] magnitudes;
    public static final int JUMP_SECOND = 4;

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
    * */
    public double[] performFFT(double[] amplitudes) {
        fft = new FastFourierTransformer(DftNormalization.STANDARD);
        double[] divided = divideWindows(amplitudes);
        magnitudes = new double[divided.length / 2];
        int index = 0, x = 0;
        while (index < divided.length) {
            double[] temp = Arrays.copyOfRange(divided, index, index + WINDOW);
            Complex[] complx = fft.transform(temp, TransformType.FORWARD);
            for (int i = 0; i < complx.length / 2; i++) {
                double rr = (complx[i].getReal());
                double ri = (complx[i].getImaginary());
                magnitudes[i + (WINDOW / 2 * x)] = Math.sqrt((rr * rr) + (ri * ri));
            }
            index += WINDOW;
            x++;
        }
        return magnitudes;
    }

    private double[] divideWindows(double[] array) {
        double jump = JUMP_SECOND * waveData.getAudioInputStream().getFormat().getSampleRate();
        float x = Math.round(array.length / jump);
        double[] divided = new double[(int) x * WINDOW];
        for (int i = 0, y = 0, d = 0, loopPrecision = 2, index; i < divided.length; i++, y += loopPrecision) {
            index = (int) (y + (d * jump));
            if (y < (loopPrecision * WINDOW) && index < array.length) {
                divided[i] = array[index];
            } else if (index > array.length) {
                divided[i] = 0.0;
            } else {
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


    public boolean analyzeFFT(double[] coffsR, double[] coffsL) {
        /*Para hacer el análisis de la detección de la metamúsica, se siguen los siguientes pasos:
        * Ya que se tienen ventanas de tiempo de tamaño N, su transformada nos da valores específicos
        * medibles en Hz de su magnitud dada por la ecuacion:
        *                              i * SampleRate / WindowSize = SliceHz
        *                              donde i = Posición de la ventana (Máximo WindowSize/2, por valores espejo)
        * Hace posible analizar sólo valores diferenciales dependiendo el tamaño de la ventana que se escoja
        *  y ya que se necesita analizar cierta cantidad específica de frecuencias, se puede hacer un filtrado
        *  de las mismas.
        *  Las frecuencias deseadas para el análisis de frecuencias debe de ser hasta 1000 Hz
        *  Así,
        *  1. Se hace un filtrado de elementos deseables en los arreglos de coefficientes. Para lo cual se usa
        *  la fórmula anterior como guía para obtener la frecuencia máxima deseada y el valor i último a recoger.
        *  Dando como resultado la fórmula:
        *                           Posición Máxima del Arreglo = FreqMax * (SampleRate / WindowSize)
        *
        *  2. Una vez que se ha filtrado los elementos, se debe hacer una conversión en los elementos del arrelgo,
        *  a decibelios. Por lo cual se procesan los arreglos con la fórmula:
        *
        *                                   db[i] = log10(cFourier[i])
        *
        *  3. Una vez obtenido el valor en decibelios de cada canal de audio, se hace un comparativo entre ambos
           arreglos. Se debe de contener valores similares, haciendo un barrido en los canales buscando valores
           que sean similares en dB, con una diferencia de Hz en los arreglos no mayor a 40Hz, puesto que es la
           mayor diferencia entre frecuencias para considerarse sondio binaural.
           Una vez encontrados todos los valores que entran en la categoría, se meten en una colección de Mapa
           para hacer un seguimiento de los mismos durante toda la duración de la canción. Si en algún momento
           estos valores cambian o pierden su potencia, se descartan. Si en el mapa siguen existiendo valores,
           podemos considerar que el archivo de audio es metamúsica. En caso de que el mapa haya quedado vacío,
           significa que hubo cambios en frecuencias y sus potencias durante la canción, y no se puede considerar
           el archivo de audio como metamúsica.
        *  */
        double[] dBR = new double[coffsR.length], dBL = new double[coffsL.length];
        int maxPos = getMaxPosition();
        dBL = convertToDB(coffsL);
        dBR = convertToDB(coffsR);
        int average = getAverageDB(dBL, dBR), positionNeed = getPositionNeed();
        dBL = deleteLower(dBL);
        dBR = deleteLower(dBR);
        HashMap<Integer, Integer> mapFreq = createFreqMap(dBL, dBR, maxPos, average, positionNeed);
        return followSequence(mapFreq, dBL, dBR, positionNeed, average);
    }

    private boolean followSequence(HashMap<Integer, Integer> mapFreq, double[] arr1,
                                   double[] arr2, int positionNeed, int averageDB) {
        /*El seguimiento del mapa se realiza de la siguiente forma:
        * 1. Se hace un seguimiento de un valor del mapa durante todas las ventanas
        * de tiempo. Es decir, se verifica que la potencia de las posiciones de los arreglos
        * sea en promedio similar y continuo cada WINDOW/2 posiciones.
        * 2. El resultado será que se irán eliminando los valores del mapa que no hayan
        * continuado con su periodicidad. Si algún elemento quedó en el mapa, entonces
         * se mandará un TRUE. Que será la confirmación de que el archivo de audio es
         * metamúsica.*/
        Set<Integer> keys = mapFreq.keySet();
        int criteria = Math.round((float) (averageDB * 0.05));
        while (keys.iterator().hasNext()) {
            int n = keys.iterator().next();
            boolean first = true;
            for (int i = n, x = 0, y = 0, xOld = 0, yOld = 0; i < arr1.length; i += WINDOW / 2) {
                for (int j = 0; j < positionNeed; j++) {
                    x += (int) arr1[i + j];
                    y += (int) arr2[i + j];
                }
                x /= positionNeed;
                y /= positionNeed;
                boolean dif = (!first) ? !(Math.abs(x - xOld) <= criteria && Math.abs(y - yOld) <= criteria) : false;
                if ((!(Math.abs(y - x) <= (criteria)) || dif) && y != 0) {
                    mapFreq.remove(n);
                    break;
                } else {
                    xOld = x;
                    yOld = y;
                }
                x = 0;
                y = 0;
                first = false;
            }
        }
        return mapFreq.isEmpty();
    }

    public int getPositionNeed() {
        return (int) Math.ceil((double) 40 / (waveData.getFormat().getSampleRate() / WINDOW));
    }

    private HashMap<Integer, Integer> createFreqMap(double[] arr1, double[] arr2, int maxPosition,
                                                    int averageDB, int positionNeed) {
        /*El proceso para la creación del mapa de frecuencias es ir comparando cada N posiciones de
        * arreglo, dependiendo de la cantidad de posiciones necesaria para  alcanzar los 40 Hz de rango.
        * 1. Se hace un comparativo entre N posiciones de la izquierda con N posiciones a la derecha.
        * Si en esa iteración, alguno de los elementos ya fue filtrad con 0 DB, se salta a la siguiente
        * iteración.
        * 2. Si cumple con el mínimo de DB, se usa el promedio de Average DB, para comparar que se sean
        * en potencia similares.
        * 3. Si cumplen con un rango similar de DB, se ingresan las posiciones de los arreglos que
        * concordaron en el mapa.*/
        int criteria = Math.round((float) (averageDB * 0.05));
        HashMap<Integer, Integer> freqPosition = new HashMap<>();
        for (int i = 0, x = 0, y = 0; i < maxPosition; i++) {
            for (int j = 0; j < positionNeed; j++) {
                x += (int) arr1[i + j];
                y += (int) arr2[i + j];
            }
            x /= positionNeed;
            y /= positionNeed;
            if (Math.abs(y - x) <= criteria && y != 0) {
                freqPosition.put(i, i + positionNeed);
            }
            x = 0;
            y = 0;
        }
        return freqPosition;
    }

    private int getAverageDB(double[] arr1, double[] arr2) {
        int average = 0;
        for (int i = 0; i < arr1.length; i++) {
            average += arr1[i] + arr2[i];
        }
        average /= arr1.length * 2;
        return average;
    }

    private double[] deleteLower(double[] arr) {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] < DB_MIN)
                arr[i] = 0;
        }
        return arr;
    }

    private double[] convertToDB(double[] arr) {
        double[] newArr = new double[arr.length];
        for (int i = 0; i < arr.length; i++) {
            newArr[i] = (arr[i] != 0) ? 20 * Math.log10(arr[i]) : 0;
        }
        return newArr;
    }

    public int getMaxPosition() {
        return (int) Math.ceil((double) MAX_FREQUENCY / (waveData.getFormat().getSampleRate() / (double) WINDOW));
    }

    public void FFTFormatted(double[] coffs, String title) {
        WriterOut writerOut = new WriterOut();
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0, c = 0, win = 0; i < coffs.length; i++, c++, win++) {

            if (win % (WINDOW / 2) == 0) {
                stringBuilder.append("Ventana: " + (win / (WINDOW * .5)) + " " + (win / (WINDOW * .5)) * 5
                        + " segundos").append("\n");
            }
            if (c < 5) {
                stringBuilder.append(i + ": " + String.format("%,.3f ", coffs[i]) + "      \t");
            } else {
                stringBuilder.append(i + ": " + String.format("%,.3f ", coffs[i])).append("\n");
                c = -1;
            }
        }
        try {
            writerOut.saveText(stringBuilder, title);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

}
