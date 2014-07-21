package org.ethack.torrific.lib;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

/**
 * Created by cedric on 7/21/14.
 */
public class StreamBuffer extends Thread {

    private BufferedReader bufferedReader;
    private List<String> outputList = null;

    public StreamBuffer(InputStream inputStream, List<String> outputList) {
        this.bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        this.outputList = outputList;
    }

    @Override
    public void run() {
        try {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                Log.d(StreamBuffer.class.getName(), line);
                if (outputList != null) outputList.add(line);
            }
        } catch (IOException e) {}

        try {
            bufferedReader.close();
        } catch (IOException e) { }
    }
}
