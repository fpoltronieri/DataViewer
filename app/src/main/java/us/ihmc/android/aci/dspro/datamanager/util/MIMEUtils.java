package us.ihmc.android.aci.dspro.datamanager.util;


/**
 * MIMEUtils.java
 * <p>
 * Lite version of util.MIMEUtils for Android
 *
 * @author Enrico Casini (ecasini@ihmc.us)
 */
public class MIMEUtils {
    public static boolean isText(String mimeType) {
        return mimeType.contains("wordprocessing") || mimeType.contains("opendocument.text") || mimeType.contains("msword");
    }

    public static boolean isSpreadsheet(String mimeType) {
        return mimeType.endsWith(".sheet") || mimeType.contains("spreadsheet") || mimeType.contains("excel");
    }

    public static boolean isPresentation(String mimeType) {
        return mimeType.contains("presentation") || mimeType.contains("ms-powerpoint") || mimeType.contains("slideshow");
    }

    public static boolean isImage(String mimeType) {
        return mimeType.contains("image") || mimeType.contains("IMAGE");
    }

    public static boolean isTrafficCamera(String mimeType) {
        return mimeType.contains(MIMEType.JSONTrafficCamera);
    }

    public static boolean isWeatherStation(String mimeType) {
        return mimeType.contains(MIMEType.JSONWeatherStation);
    }

    public static boolean isEventDetection(String mimeType) {
        return mimeType.contains(MIMEType.JSONEventDetection);
    }
}