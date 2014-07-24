package org.ethack.torrific.lib;

import android.util.Log;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by cedric on 7/20/14.
 */
public class CheckSum {

    private String method;
    private String file;

    public CheckSum(String file, String method) {
        this.file = file;
        this.method = method;
    }

    public CheckSum(String file) {
        this.file = file;
        this.method = "MD5";
    }

    public String hash() {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance(method);
        } catch (NoSuchAlgorithmException e) {
            Log.e("Hash", "No such algorithm: "+method);
            return "E_NOSUCHALGORITHM";
        }
        FileInputStream fis;
        try {
            fis = new FileInputStream(file);
        } catch(FileNotFoundException e) {
            Log.e("Hash", "No such file: "+file);
            return "E_NOSUCHFILE";
        }

        byte[] dataBytes = new byte[1024];
        int nread = 0;

        try {
            while ((nread = fis.read(dataBytes)) != -1) {
                md.update(dataBytes, 0, nread);
            }
        } catch (IOException e) {
            return "E_IOEXCEPTION";
        }

        byte[] mdbytes = md.digest();

        StringBuffer sb = new StringBuffer();
        for (byte mdb: mdbytes) {
            sb.append(Integer.toHexString(0xFF & mdb));
        }
        Log.d("Checksum", sb.toString());
        return sb.toString();
    }
}
