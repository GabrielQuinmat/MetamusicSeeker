package methodclasses;

import java.io.*;

/**
 * Created by Gabo on 02/04/2017.
 */
public class WriterOut {

    private PrintWriter printWriter;

    public void saveText(StringBuilder stringBuilder, String name) throws FileNotFoundException, UnsupportedEncodingException {
        printWriter = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(name), "UTF-8"), true);
        printWriter.print(stringBuilder);
        printWriter.close();
    }

}
