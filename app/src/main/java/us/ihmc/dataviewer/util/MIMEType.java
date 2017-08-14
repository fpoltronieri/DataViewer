package us.ihmc.dataviewer.util;

/**
 * MIMEType.java
 * <p>
 * Interface MimeType represents all possible MimeTypes handled by ATAK DSPro.
 *
 * @author Enrico Casini (ecasini@ihmc.us)
 */

public interface MIMEType {
    String SOIGenITrack = "x-dspro/x-soi-track";
    String SOIGenIITrack = "x-dspro/x-soi-track-info";
    String PhoenixTrack = "x-dspro/x-phoenix-track-info";
    String SOIDataProduct = "x-dspro/x-soi-data-product";
    String PhoenixCoT = "x-dspro/x-phoenix-cot";
    String GHubMissionAlert = "x-dspro/x-soi-ghub-mission-alert";
    String JSONTrafficCamera = "application/json.traffic-camera";
    String JSONWeatherStation = "application/json.weather-station";
    String JSONEventDetection = "application/json.event-detection";
}