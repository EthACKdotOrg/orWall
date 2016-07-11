package org.ethack.orwall.lib;

import android.util.Log;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Allows to checksum files.
 * Used for init-script installation.
 */
public class CheckSum {

    private String method;
    private String file;

    /**
     * Class builder — default method is MD5
     *
     * @param file String, path to the file
     */
    public CheckSum(String file) {
        this.file = file;
        this.method = "MD5";
    }

    /**
     * Create the hash
     *
     * @return String, file hash
     */
    public String hash() {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance(method);
        } catch (NoSuchAlgorithmException e) {
            Log.e("Hash", "No such algorithm: " + method);
            return Constants.E_NO_SUCH_ALGO;
        }
        FileInputStream fis;
        try {
            fis = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            Log.e("Hash", "No such file: " + file);
            return Constants.E_NO_SUCH_FILE;
        }

        byte[] dataBytes = new byte[1024];
        int nread;

        try {
            while ((nread = fis.read(dataBytes)) != -1) {
                md.update(dataBytes, 0, nread);
            }
        } catch (IOException e) {
            return "E_IOEXCEPTION";
        }

        byte[] mdbytes = md.digest();

        StringBuffer sb = new StringBuffer();
        for (byte mdb : mdbytes) {
            sb.append(Integer.toHexString(0xFF & mdb));
        }
        Log.d("Checksum", sb.toString());
        return sb.toString();
    }
}
