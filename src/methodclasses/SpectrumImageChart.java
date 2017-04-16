package methodclasses;

import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;

import java.util.Arrays;

/**
 * Created by Gabo on 10/04/2017.
 */
public class SpectrumImageChart {
    public static SimpleDoubleProperty progress = new SimpleDoubleProperty(0);

    public static final double WIDTH = 1400;
    public static final double HEIGTH = 1280;
    public static final double BOX_WIDTH_INIT = (WIDTH * .1);
    public static final double BOX_HEIGTH_INIT = (HEIGTH * .1);
    public static final double BOX_WIDTH_END = WIDTH - (WIDTH * .1);
    public static final double BOX_HEIGTH_END = HEIGTH - (HEIGTH * .1);
    public static final double BOX_WIDTH = WIDTH - (WIDTH * .2);
    public static final double BOX_HEIGTH = HEIGTH - (HEIGTH * .2);
    public static final double HORIZONTAL_MARKS_PIXEL = 35;
    public static final double VERTICAL_MARKS_PIXEL = 32;
    public static final double HORIZONTAL_MARKS = 32;
    public static final double VERTICAL_MARKS = 32;

    public static final double MAX_BINAURAL_FREQUENCY = 1000;

    private WritableImage image;
    private Canvas canvas;
    private PixelWriter pixelWriter;
    private GraphicsContext gc;

    public SpectrumImageChart() {
    }


    public WritableImage getImage() {
        return image;
    }

    public void setImage(WritableImage image) {
        this.image = image;
    }

    public Canvas getCanvas() {
        return canvas;
    }

    public WritableImage prepareImage(String title, String labelX, String labelY) {
        progress = new SimpleDoubleProperty(0);
        image = new WritableImage((int) WIDTH, (int) HEIGTH);
        pixelWriter = image.getPixelWriter();
        fillBlank();
        progress.setValue(0.1);
        fillBox();
        progress.setValue(0.2);
        setGrid();
        setMarks();
        progress.setValue(0.3);
        Platform.runLater(() -> {
            setText(title, labelX, labelY);
        });
        progress.setValue(0.5);
        return image;
    }


    public WritableImage drawSpectrum(Image image, double[] fftCofs, int windowSize, double sampleRate,
                                      int milisecs) throws Exception {
        /*Para pintar el espectograma se siguen los siguientes pasos:
        * 1. Se puede hacer dos tipos de imagen de espectograma
        *
        *   - Completo
        *   - Especializado
        *
        * Para el primero, se toma en cuenta primero los siguientes valores: máxima frecuencia, tiempo total,
        * máxima potencia y número de ventanas obtenidos. La máxima frecuencia se usará como medio para deter-
        * minar el máximo punto del eje Y, lo mismo con el tiempo total pero en el eje X; la máxima potencia
        * se usará para un algoritmo de coloreado como un eje Z de valor en la tabla; el número de ventanas
        * ayudará a determinar la cantidad de pixeles necesario para pintar la ventana actual.
        *
         * Para el segundo, se toma en cuenta una sobrecarga del método donde lleva el rango de los 3 ejes.
         *
        2. Una vez obteniendo los datos que son relevantes para la generación del espectograma se comienza
        por hacer el texto en los ejes X y Y. Esto es:
        * Para el primero, se hace una división de la frecuencia máxima y se diviide entre la cantidad de
        * marcas en el eje X. Esto permitirá que el texto escrito al lado de cada marca corresponda a los
        * niveles disponibles de pixeles. Para el segundo, se hace una división entre las la cantidad de
        * ventanas * tiempo (tiempo total) entre la cantidad de marcas en el eje Y.
        *
        * 3. Se hace el cálculo de pixeles por datos necesarios. Es decir, ya que la ventana es de N valores,
        * para llegar a la máxima frecuencia de la ventana, se considera que cada valor del arreglo en un rango
        * de i hasta i + N/2, corresponde a una frecuencia de (MaxSampleRate/1024). Entonces, para poder calcular
        * finalmente la cantidad de pixeles necesarios es
        *                                   sliceV = N_VPIXELS/(N/2)
        *                                   donde N_VPIXELES = cantidad de pixeles verticales disponibles para
        *                                   coloreado
        *
        * Para la cantidad de pixeles horizontales por ventana sucede similar a lo anterior
        *
        *                                   sliceH = N_HPIXELS/(N_W)
        *                                   donde N_HPIXELS = cantidad de pixeles horizontales disponibles para
        *                                   colorear
        *                                   donde N_W = cantidad de ventanas en el arreglo.
        *
        *4. Se procede a procesar el arreglo. Dentro de un ciclo, se hace un análisis y coloreado simultáneo. El
        * coloreado de la imagen es de abajo hacia arriba y de izquierda a derecha.
         * a) Se lee la potencia de la posición i de la ventana X.
          * b) Se procede a un método que determina el color necesario para
         * tal potencia.
         * c) Se pinta un cuadro completo de sliceH de ancho y sliceV de alto, el cual es el mismo color.
         * d) Se procede al siguiente elemento del arrelgo, y a subir los coordenadas del pixel para el
         * siguiente cuadro de color.
         * e)Una vez finalizado el recorrido de la ventana, se reinicia i pero se aumenta en uno X para que
         * el recorrido de ventanas sea por la fórmula
         *
         *                                  i+((WINDOW_SIZE/2)*x)
         *
         *
         * Para el método sobrecargado el coloreado es igual, pero actúa con diferentes parámetros de
         * pixeles sliceH y sliceV debido a la diferencia de parámetros.
         *
        * */

        double freqPerVal = sampleRate / windowSize, maxFreq = sampleRate * 0.5,
                nWindows = fftCofs.length / (windowSize * 0.5), totalTime = nWindows * 5,
                maxPower = getMaxFromArray(fftCofs);
        int sliceV = (int) Math.floor(BOX_HEIGTH / ((double) windowSize / 2)),
                sliceH = (int) Math.ceil(BOX_WIDTH / nWindows);
        setAxisNumeration(image, maxFreq, milisecs);
        progress.setValue(0.6);
        setMarksCanvas();
        paintFFT(fftCofs, sliceH, sliceV, windowSize / 2, nWindows, maxPower);
        setGridCanvas();
        canvas.snapshot(null, (WritableImage) image);
        progress.setValue(0.7);
        return (WritableImage) image;
    }

    public WritableImage drawSpectrumZoomed(Image image, double[] fftCofs, int windowSize, double windowTilMaxFreq,
                                            int milisecs) throws Exception {
        double maxFreq = MAX_BINAURAL_FREQUENCY,
                nWindows = fftCofs.length / (windowSize * 0.5),
                maxPower = getMaxFromArray(fftCofs);
        int sliceV = (int) Math.floor(BOX_HEIGTH / windowTilMaxFreq),
                sliceH = (int) Math.ceil(BOX_WIDTH / nWindows);
        setAxisNumeration(image, maxFreq, milisecs);
        setMarksCanvas();
        paintFFT(fftCofs, sliceH, sliceV, windowSize / 2, nWindows, maxPower);
        setGridCanvas();
        canvas.snapshot(null, (WritableImage) image);
        progress.setValue(0.7);
        return (WritableImage) image;
    }

    private void paintFFT(double[] arr, int pixelsPerH, int pixelsPerV, int vCriteria, double hCriteria,
                          double zCriteria) {
        double power, widthX = BOX_WIDTH_INIT, heigthY = BOX_HEIGTH_END;
        for (int i = 0, x = 0, index = 0; index < arr.length; i++, index = i + (vCriteria * x)) {
            power = arr[index];
            Color color = getColorFFT(power, zCriteria);
            gc.setFill(Paint.valueOf(color.toString()));
            gc.fillRect(widthX, heigthY - pixelsPerV, pixelsPerH, pixelsPerV);
            heigthY -= pixelsPerV;
            if (heigthY <= BOX_HEIGTH_INIT || i >= vCriteria) {
                x++;
                i = -1;
                widthX += pixelsPerH;
                heigthY = BOX_HEIGTH_END;
            }
        }
    }

    private Color getColorFFT(double value, double maxValue) {
        double r = 0, g = 0, b = 0, difference = value / maxValue;
        if (difference < .3) {
            b = difference * 2;
            g = difference;
        } else if (difference > .3 && difference < .7) {
            g = (difference < 0.5) ? difference : 1 - difference;
            r = (difference < 0.5) ? 1 - difference : difference;
        } else {
            r = difference;
        }
        return new Color(r, g, b, 1);
    }

    private double getMaxFromArray(double[] fftCofs) {
        double[] newArr = fftCofs.clone();
        Arrays.sort(newArr);
        return newArr[newArr.length - 1];
    }

    private void setAxisNumeration(Image image, double maxF, int milisecs) {
        canvas = new Canvas(image.getWidth(), image.getHeight());
        gc = canvas.getGraphicsContext2D();
        gc.setFont(Font.font(9));
        //Left Marks
        double milisecsPerMark = Math.round(milisecs / HORIZONTAL_MARKS), freqPerMark = Math.round(maxF / VERTICAL_MARKS);
        for (double i = BOX_HEIGTH_END, x = 0; i >= BOX_HEIGTH_INIT; i -= VERTICAL_MARKS_PIXEL, x++) {
            gc.strokeText(String.format("%,.2f", freqPerMark * x), BOX_WIDTH_INIT - BOX_WIDTH_INIT * 0.35, i);
        }
        //        Down MARKS
        for (double j = (int) BOX_WIDTH_INIT, x = 0; j <= BOX_WIDTH_END; j += HORIZONTAL_MARKS_PIXEL, x++) {
            if (x % 2 == 0) {
                gc.strokeText(String.format("%,.2f", milisecsPerMark * x * .001), j, BOX_HEIGTH_END + (BOX_HEIGTH_INIT * 0.3));
            }
        }
    }

    private void setMarks() {
//        VERTICAL MARKS
        for (double i = BOX_HEIGTH_INIT; i <= BOX_HEIGTH_END; i += VERTICAL_MARKS_PIXEL) {
            for (double j = BOX_WIDTH_INIT - BOX_WIDTH_INIT * 0.07; j <= BOX_WIDTH_INIT; j++) {
                Color color = new Color(0, 0, 0, 1);
                pixelWriter.setColor((int) j, (int) i, color);
            }
        }
        //        HORIZONTAL MARKS
        for (double j = (int) BOX_WIDTH_INIT; j <= BOX_WIDTH_END; j += HORIZONTAL_MARKS_PIXEL) {
            for (double i = BOX_HEIGTH_END; i <= BOX_HEIGTH_END + (BOX_HEIGTH_INIT * 0.1); i++) {
                Color color = new Color(0, 0, 0, 1);
                pixelWriter.setColor((int) j, (int) i, color);
            }
        }
    }

    private void setMarksCanvas() {
        gc.setFill(Color.BLACK);
//        VERTICAL MARKS
        for (double i = BOX_HEIGTH_INIT; i <= BOX_HEIGTH_END; i += VERTICAL_MARKS_PIXEL) {
            gc.strokeLine(BOX_WIDTH_INIT, i,
                    BOX_WIDTH_INIT - BOX_WIDTH_INIT * 0.07, i);
        }
        //        HORIZONTAL MARKS
        for (double j = (int) BOX_WIDTH_INIT; j <= BOX_WIDTH_END; j += HORIZONTAL_MARKS_PIXEL) {
            gc.strokeLine(j, BOX_HEIGTH_END, j, BOX_HEIGTH_END + (BOX_HEIGTH_INIT * 0.15));
        }
    }

    private void setGrid() {
        for (double i = BOX_HEIGTH_INIT; i < BOX_HEIGTH_END; i++) {
            for (double j = BOX_WIDTH_INIT; j < BOX_WIDTH_END; j++) {
                if (i % VERTICAL_MARKS_PIXEL == 0 || j % HORIZONTAL_MARKS_PIXEL == 0) {
                    Color color = new Color(.01, .01, .01, 0.8);
                    pixelWriter.setColor((int) j, (int) i, color);
                }
            }
        }
    }

    private void setGridCanvas() {
        Color color = new Color(1, 1, 1, 0.1);
        gc.setStroke(Paint.valueOf(color.toString()));
        for (double i = BOX_HEIGTH_INIT; i <= BOX_HEIGTH_END; i += VERTICAL_MARKS_PIXEL) {
            gc.strokeLine(BOX_WIDTH_INIT, i, BOX_WIDTH_END, i);
        }
        for (double i = BOX_WIDTH_INIT; i <= BOX_WIDTH_END; i += HORIZONTAL_MARKS_PIXEL) {
            gc.strokeLine(i, BOX_HEIGTH_INIT, i, BOX_HEIGTH_END);
        }
    }

    private WritableImage setText(String title, String labelX, String labelY) {
        canvas = new Canvas(WIDTH, HEIGTH);
        gc = canvas.getGraphicsContext2D();
        gc.drawImage(image, 0, 0);
        gc.setFont(Font.font(30));
        gc.strokeText(title, BOX_WIDTH * 0.6, BOX_HEIGTH_INIT * 0.5);

        gc.setFont(Font.font(18));
        gc.strokeText(labelY, BOX_WIDTH * 0.6, BOX_HEIGTH_END + (BOX_HEIGTH_INIT * 0.5));

        for (int i = 0; i < labelX.length(); i++) {
            gc.strokeText(labelX.substring(i, i + 1), BOX_WIDTH_INIT * 0.3, (BOX_HEIGTH * 0.5) + (i * 15));
        }
        return canvas.snapshot(null, image);
    }

    private void fillBox() {
        for (double i = BOX_HEIGTH_INIT; i <= BOX_HEIGTH_END; i++) {
            for (double j = BOX_WIDTH_INIT; j <= BOX_WIDTH_END; j++) {
                Color color = new Color(0, 0, 0, 1);
                pixelWriter.setColor((int) j, (int) i, color);
            }
        }
    }

    private void fillBlank() {
        for (int i = 0; i < HEIGTH; i++) {
            for (int j = 0; j < WIDTH; j++) {
                Color color = new Color(1, 1, 1, 1);
                pixelWriter.setColor(j, i, color);
            }
        }
    }

}
