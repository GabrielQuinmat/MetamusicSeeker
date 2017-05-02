package pojo;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;

/**
 * Created by Gabo on 31/03/2017.
 */
public class WaveMP3 {
    File file;
    AudioInputStream input;
    AudioInputStream din;

    public WaveMP3(String path) {
        file = new File(path);
    }

    public AudioInputStream getAudioData() {
        try {
            input = AudioSystem.getAudioInputStream(file);
            AudioFormat baseFormat = input.getFormat();
            AudioFormat decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, baseFormat.getSampleRate(),
                    16, baseFormat.getChannels(), baseFormat.getChannels() * 2, baseFormat.getSampleRate(), false);
            din = AudioSystem.getAudioInputStream(decodedFormat, input);
        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return din;
    }

}
