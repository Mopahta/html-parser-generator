package betbonanza.app.saver;

import java.io.*;

public class FileSaver implements Saver {
    @Override
    public void save (String data) {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter("output.html"));
            bw.write(data);
            bw.close();
        }
        catch (IOException e) {
            System.err.println("Cannot write to file output.html.");
        }
    }
}
