package us.ihmc.dataviewer.util;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


public class StoreTask extends AsyncTask<byte[], Void, File>
{

    private final String TAG = "StoreTask";

    public static File getStorageDirectory ()
    {
        File sdCard = Environment.getExternalStorageDirectory();
        File dir = new File(sdCard.getAbsolutePath() + "/atak/dspro");
        dir.mkdirs();

        return dir;
    }

    @Override
    protected File doInBackground (byte[]... data)
    {
        FileOutputStream outStream = null;

        // Write to SD Card
        try {

            String fileName = new String(data[1]);
            File outFile = new File(getStorageDirectory(), fileName);

            if (data == null || data[0] == null) {
                Log.e(TAG, "Data is null");
                return null;
            }

            outStream = new FileOutputStream(outFile);
            outStream.write(data[0]);
            outStream.flush();
            outStream.close();

            Log.d(TAG, "- - Wrote bytes: " + data[0].length + " to " + outFile.getAbsolutePath());

            return outFile;

        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
        }
        return null;
    }

}
