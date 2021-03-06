package cz.jaro.alarmmorning.holiday.regiondetector;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Detects the region from IP address. Uses an internet server to get the information.
 */
public class IPRegionDetector extends RegionDetector {

    private static final String TAG = IPRegionDetector.class.getSimpleName();

    private static final String URL = "http://ip-api.com/json";
    public static final String COUNTRY_CODE = "countryCode";
    public static final String LAT = "lat";
    public static final String LON = "lon";

    public IPRegionDetector(Context context) {
        super(context);
    }

    public void detect() {
        try {
            String content = URLHelper.readURL(URL);

            JSONObject json = new JSONObject(content);
            String countryCode = json.getString(IPRegionDetector.COUNTRY_CODE);

            callChangeListener(countryCode, json);
        } catch (IOException e) {
            Log.w(TAG, "Cannot read URL content", e);
        } catch (JSONException e) {
            Log.w(TAG, "Error parsing JSON", e);
        }
    }

}
