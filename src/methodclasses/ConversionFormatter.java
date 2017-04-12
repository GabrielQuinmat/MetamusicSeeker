package methodclasses;

import javazoom.jl.converter.Converter;
import javazoom.jl.decoder.JavaLayerException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * Created by Gabo on 11/04/2017.
 */
public class ConversionFormatter {

    public static File convertMP3toWAV(File mp3File){
        Converter converter = new Converter();
        try {
            File convertedFile = new File("temp.wav");
            FileInputStream fis = new FileInputStream(mp3File);
            converter.convert(fis, convertedFile.getAbsolutePath(), null, null);
            return convertedFile;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (JavaLayerException e) {
            e.printStackTrace();
        }
        return null;
    }
}
