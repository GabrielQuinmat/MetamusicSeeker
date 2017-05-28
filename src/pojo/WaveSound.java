package pojo;

import com.sun.media.sound.WaveFileWriter;
import javafx.stage.FileChooser;

import javax.sound.sampled.*;
import java.io.*;
import java.util.HashMap;

/**
 * Created by Gabo on 31/03/2017.
 */
public class WaveSound implements Serializable {

    private byte[] arrFile;
    private byte[] audioBytes;
    private double[] audioData;
    private ByteArrayInputStream bis;
    private AudioInputStream audioInputStream;
    private AudioFormat format;
    private double durationSec;
    private double durationMSec;
    private double sampleRate;
    private double frameSize;
    private File wavFile;
    private int MAXSIZE = 40000000;

    public WaveSound(String path) {
        wavFile = new File(path);
    }

    public WaveSound(WaveMP3 mp3File) throws IOException, UnsupportedAudioFileException {
        extractAmplitudeDataFromAudioInputStream(mp3File.getAudioData(), mp3File.file);
    }


    public double[] extractAmplitudeFromFile() throws IOException, UnsupportedAudioFileException {
        try {
            // create file input stream
            FileInputStream fis = new FileInputStream(wavFile);
            // create bytearray from file
            arrFile = new byte[(int) wavFile.length()];
            fis.read(arrFile);
        } catch (Exception e) {
            System.out.println("SomeException : " + e.toString());
        }
        return extractAmplitudeFromFileByteArray(arrFile);
    }

    public double[] extractAmplitudeFromFileByteArray(byte[] arrFile) throws IOException, UnsupportedAudioFileException {
        // System.out.println("File : "+wavFile+""+arrFile.length);
        bis = new ByteArrayInputStream(arrFile);
        return extractAmplitudeFromFileByteArrayInputStream(bis);
    }

    /**
     * for extracting amplitude array the format we are using :16bit, 22khz, 1
     * channel, littleEndian,
     *
     * @return PCM audioData
     * @throws Exception
     */
    public double[] extractAmplitudeFromFileByteArrayInputStream(ByteArrayInputStream bis) throws IOException, UnsupportedAudioFileException {
        try {
            audioInputStream = AudioSystem.getAudioInputStream(bis);
        } catch (UnsupportedAudioFileException e) {
            System.out.println("unsupported file type, during extract amplitude");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("IOException during extracting amplitude");
            e.printStackTrace();
        }
        // float milliseconds = (long) ((audioInputStream.getFrameLength() *
        // 1000) / audioInputStream.getFormat().getFrameRate());
        // durationSec = milliseconds / 1000.0;
        return extractAmplitudeDataFromAudioInputStream(audioInputStream);
    }

    public double[] extractAmplitudeDataFromAudioInputStream(AudioInputStream audioInputStream) throws IOException, UnsupportedAudioFileException {
        format = audioInputStream.getFormat();
        int MAXSIZE = 40000000;
        audioBytes = new byte[((audioInputStream.getFrameLength() * format.getFrameSize()) > MAXSIZE) ? MAXSIZE
                : (int) (audioInputStream.getFrameLength() * format.getFrameSize())];
        // calculate durations
        durationMSec = (long) ((audioInputStream.getFrameLength() * 1000) / audioInputStream.getFormat().getFrameRate());
        durationSec = durationMSec / 1000.0;
        sampleRate = audioInputStream.getFormat().getSampleRate();
        frameSize = audioInputStream.getFormat().getFrameSize();
        // System.out.println("The current signal has duration "+durationSec+" Sec");
        try {
            audioInputStream.read(audioBytes);
        } catch (IOException e) {
            System.out.println("IOException during reading audioBytes");
            e.printStackTrace();
        }
        return extractAmplitudeDataFromAmplitudeByteArray(format, audioBytes);
    }

    public double[] extractAmplitudeDataFromAudioInputStream(AudioInputStream audioInputStream, File file) throws IOException, UnsupportedAudioFileException {
        format = audioInputStream.getFormat();
        AudioFileFormat format2 = AudioSystem.getAudioFileFormat(file);
        frameSize = format2.getFormat().getFrameSize();
        sampleRate = format2.getFormat().getSampleRate();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        // calculate durations
        durationMSec = (long) ((format2.getFrameLength() * 1000) / audioInputStream.getFormat().getFrameRate());
        durationSec = durationMSec / 1000.0;
        // System.out.println("The current signal has duration "+durationSec+" Sec");
        try {
            while (true) {
                int n = audioInputStream.read(buffer, 0, buffer.length);
                if (n == -1 || n > MAXSIZE)
                    break;
                baos.write(buffer, 0, n);
            }
            audioInputStream.close();
            audioBytes = baos.toByteArray();
            baos.close();
        } catch (IOException e) {
            System.out.println("IOException during reading audioBytes");
            e.printStackTrace();
        }
        return extractAmplitudeDataFromAmplitudeByteArray(format, audioBytes);
    }

    public double[] extractAmplitudeDataFromAmplitudeByteArray(AudioFormat format, byte[] audioBytes) {
        // convert

        audioData = null;
        if (format.getSampleSizeInBits() == 16) {
            int nlengthInSamples = (audioBytes.length > MAXSIZE) ? MAXSIZE : (audioBytes.length / 2);
            audioData = new double[nlengthInSamples];
            if (format.isBigEndian()) {
                for (int i = 0; i < nlengthInSamples; i++) {
                          /* First byte is MSB (high order) */
                    int MSB = audioBytes[2 * i];
                          /* Second byte is LSB (low order) */
                    int LSB = audioBytes[2 * i + 1];
                    audioData[i] = MSB << 8 | (255 & LSB);
                }
            } else {
                for (int i = 0; i < nlengthInSamples; i++) {
                          /* First byte is LSB (low order) */
                    int LSB = audioBytes[2 * i];
                          /* Second byte is MSB (high order) */
                    int MSB = audioBytes[2 * i + 1];
                    audioData[i] = MSB << 8 | (255 & LSB);
                }
            }
        } else if (format.getSampleSizeInBits() == 8) {
            int nlengthInSamples = (audioBytes.length > MAXSIZE) ? MAXSIZE : (audioBytes.length);
            audioData = new double[nlengthInSamples];
            if (format.getEncoding().toString().startsWith("PCM_SIGN")) {
                // PCM_SIGNED
                for (int i = 0; i < audioBytes.length; i++) {
                    audioData[i] = audioBytes[i];
                }
            } else {
                // PCM_UNSIGNED
                for (int i = 0; i < audioBytes.length; i++) {
                    audioData[i] = audioBytes[i] - 128;
                }
            }
        }
        return audioData;
    }


    public HashMap<String, double[]> separateChannels() {
        int size, sizeFor, frameSize = getFormat().getFrameSize();
        size = audioData.length / 2;
        sizeFor = audioData.length;
        HashMap<String, double[]> channelMap = new HashMap<>();
        double[] channelL = new double[size];
        double[] channelR = new double[size];
        for (int i = 0, x = 0; i + 1 < sizeFor; i += 2, x++) {
            channelL[x] = audioData[i];
            channelR[x] = audioData[i + 1];
        }
        channelMap.put("Channel L", channelL);
        channelMap.put("Channel R", channelR);
        return channelMap;
    }

    public void generateNewSoundFile() throws IOException {
        // Transform byte array for a monoaudio song.
        AudioInputStream audioInputStreamOut = AudioSystem.getAudioInputStream(new AudioFormat(format.getEncoding(),
                format.getSampleRate(), format.getSampleSizeInBits(), 1,
                (int) (frameSize / format.getChannels()), format.getFrameRate(),
                format.isBigEndian()), audioInputStream);
        WaveFileWriter writer = new WaveFileWriter();
        writer.write(audioInputStreamOut, AudioFileFormat.Type.WAVE, saveFile());
    }

    private File saveFile() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Seleccione la Ruta y el Nombre para Guardar el Archivo de Audio");
        File outfile = fc.showSaveDialog(null);
        return outfile;
    }

    public byte[] getAudioBytes() {
        return audioBytes;
    }

    public double getDurationSec() {
        return durationSec;
    }

    public double getDurationMiliSec() {
        return durationMSec;
    }

    public double[] getAudioData() {
        return audioData;
    }

    public AudioFormat getFormat() {
        return format;
    }

    public AudioInputStream getAudioInputStream() {
        return audioInputStream;
    }

    public double getSampleRate() {
        return sampleRate;
    }

    public double getFrameSize() {
        return frameSize;
    }
}
