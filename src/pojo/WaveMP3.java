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

    public AudioInputStream getAudioData(File file) throws IOException, UnsupportedAudioFileException {
        this.file = file;
        input = AudioSystem.getAudioInputStream(file);
        AudioFormat baseFormat = input.getFormat();
        AudioFormat decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, baseFormat.getSampleRate(),
                16, baseFormat.getChannels(), baseFormat.getChannels() * 2, baseFormat.getSampleRate(), false);
        din = AudioSystem.getAudioInputStream(decodedFormat, input);
        return din;
    }
}
