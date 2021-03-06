
package com.example.android.weatherForecastMG.utilities;

import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import com.example.android.weatherForecastMG.data.WeatherForecastPreferences;
import com.example.android.weatherForecastMG.data.WeatherContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;

import static android.content.ContentValues.TAG;

/**
 * Utility functions to handle OpenWeatherMap JSON data.
 */
public final class OpenWeatherJsonUtils {

    /* Location information */
    private static final String OWM_CITY = "city";
    private static final String OWM_COORD = "coord";

    /* Location coordinate */
    private static final String OWM_LATITUDE = "lat";
    private static final String OWM_LONGITUDE = "lon";

    /* Weather information. Each day's forecast info is an element of the "list" array */
    private static final String OWM_LIST = "list";

    private static final String OWM_MAIN="main";
    private static final String OWM_PRESSURE = "pressure";
    private static final String OWM_HUMIDITY = "humidity";
    private static final String OWM_WINDSPEED = "speed";
    private static final String OWM_WIND_DIRECTION = "deg";

    /* All temperatures are children of the "temp" object */
   // private static final String OWM_TEMPERATURE = "temp";

    /* Max temperature for the day */
    private static final String OWM_MAX = "temp_max";
    private static final String OWM_MIN = "temp_min";

    private static final String OWM_WEATHER = "weather";
    private static final String OWM_WEATHER_ID = "id";

    private static final String OWM_MESSAGE_CODE = "cod";


    private static final String METAR_CLOUDS="clouds";

    /**
     * This method parses JSON from a web response and returns an array of Strings
     * describing the weather over various days from the forecast.
     * <p/>
     * Later on, we'll be parsing the JSON into structured data within the
     * getFullWeatherDataFromJson function, leveraging the data we have stored in the JSON. For
     * now, we just convert the JSON into human-readable strings.
     *
     * @param forecastJsonStr JSON response from server
     *
     * @return Array of Strings describing weather data
     *
     * @throws JSONException If JSON data cannot be properly parsed
     */
    public static ContentValues[] getWeatherContentValuesFromJson(Context context, String forecastJsonStr)
            throws JSONException {

        JSONObject forecastJson = new JSONObject(forecastJsonStr);

        /* Is there an error? */
        if (forecastJson.has(OWM_MESSAGE_CODE)) {
            int errorCode = forecastJson.getInt(OWM_MESSAGE_CODE);

            switch (errorCode) {
                case HttpURLConnection.HTTP_OK:
                    break;
                case HttpURLConnection.HTTP_NOT_FOUND:
                    /* Location invalid */
                    return null;
                default:
                    /* Server probably down */
                    return null;
            }
        }

        JSONArray jsonWeatherArray = forecastJson.getJSONArray(OWM_LIST);

        JSONObject cityJson = forecastJson.getJSONObject(OWM_CITY);

        JSONObject cityCoord = cityJson.getJSONObject(OWM_COORD);
        double cityLatitude = cityCoord.getDouble(OWM_LATITUDE);
        double cityLongitude = cityCoord.getDouble(OWM_LONGITUDE);

        WeatherForecastPreferences.setLocationDetails(context, cityLatitude, cityLongitude);

        ContentValues[] weatherContentValues = new ContentValues[jsonWeatherArray.length()];

        /*
         * OWM returns daily forecasts based upon the local time of the city that is being asked
         * for, which means that we need to know the GMT offset to translate this data properly.
         * Since this data is also sent in-order and the first day is always the current day, we're
         * going to take advantage of that to get a nice normalized UTC date for all of our weather.
         */
//        long now = System.currentTimeMillis();
//        long normalizedUtcStartDay = WeatherForecastDateUtils.normalizeDate(now);

        long normalizedUtcStartDay = WeatherForecastDateUtils.getNormalizedUtcDateForToday();

        for (int i = 0; i < jsonWeatherArray.length(); i++) {

            long dateTimeMillis;
            double pressure;
            int humidity;
            double windSpeed;
            double windDirection;

            double high;
            double low;

            int weatherId;

            /* Get the JSON object representing the day */
            JSONObject dayForecast = jsonWeatherArray.getJSONObject(i);

            /*
             * We ignore all the datetime values embedded in the JSON and assume that
             * the values are returned in-order by day (which is not guaranteed to be correct).
             */
            dateTimeMillis = normalizedUtcStartDay + WeatherForecastDateUtils.DAY_IN_MILLIS * i;

            pressure = dayForecast.getJSONObject(OWM_MAIN).getDouble(OWM_PRESSURE);
            humidity = dayForecast.getJSONObject(OWM_MAIN).getInt(OWM_HUMIDITY);
            windDirection=dayForecast.getJSONObject("wind").getDouble("deg");
            windSpeed=dayForecast.getJSONObject("wind").getDouble("speed");;

            /*
             * Description is in a child array called "weather", which is 1 element long.
             * That element also contains a weather code.
             */
            JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);

            weatherId = weatherObject.getInt(OWM_WEATHER_ID);

            /*
             * Temperatures are sent by Open Weather Map in a child object called "temp".
             *
             * Editor's Note: Try not to name variables "temp" when working with temperature.
             * It confuses everybody. Temp could easily mean any number of things, including
             * temperature, temporary variable, temporary folder, temporary employee, or many
             * others, and is just a bad variable name.
             */
            JSONObject temperatureObject = dayForecast.getJSONObject(OWM_MAIN);
            high = temperatureObject.getDouble(OWM_MAX);
            low = temperatureObject.getDouble(OWM_MIN);

            ContentValues weatherValues = new ContentValues();
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DATE, dateTimeMillis);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_HUMIDITY, humidity);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_PRESSURE, pressure);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WIND_SPEED, windSpeed);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DEGREES, windDirection);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP, high);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP, low);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID, weatherId);

            weatherContentValues[i] = weatherValues;
        }

        return weatherContentValues;
    }

    /*
    Parses JSON from METAR weather data
     */
    public static ContentValues[] getWeatherContentValuesFromJsonMETAR(Context context, String forecastJsonStr)
            throws JSONException {

        JSONObject forecastJson = new JSONObject(forecastJsonStr);

        /* Is there an error? */
        if (forecastJson.has(OWM_MESSAGE_CODE)) {
            int errorCode = forecastJson.getInt(OWM_MESSAGE_CODE);

            switch (errorCode) {
                case HttpURLConnection.HTTP_OK:
                    break;
                case HttpURLConnection.HTTP_NOT_FOUND:
                    /* Location invalid */
                    return null;
                default:
                    /* Server probably down */
                    return null;
            }
        }

        JSONArray jsonWeatherArrayData = forecastJson.getJSONArray("data");
JSONObject data= jsonWeatherArrayData.getJSONObject(0);
String metar_raw=data.getString("raw_text");

        Log.v(TAG, "METAR raw: "+metar_raw);

        JSONArray clouds=data.getJSONArray("clouds");

for (int i=0; i< clouds.length();i++) {
    String clouds_code = clouds.getJSONObject(i).getString("code");
    String clouds_text = clouds.getJSONObject(i).getString("text");
    String clouds_feet_agl = clouds.getJSONObject(i).getString("base_feet_agl");
    String clouds_meters_agl = clouds.getJSONObject(i).getString("base_meters_agl");

    Log.v(TAG, "Clouds text(code): "+clouds_text+"("+clouds_code+")");
    Log.v(TAG, "Clouds above ground level feet(meters): "+clouds_feet_agl+"("+clouds_meters_agl+")");

}

        JSONObject conditions=data.getJSONObject("conditions");
        String conditions_code=conditions.getString("code");
        String conditions_text=conditions.getString("text");

        JSONObject dew_point=data.getJSONObject("dewpoint");
        String dew_point_C=dew_point.getString("celsius");
        String dew_point_F=dew_point.getString("fahrenheit");

        String flight_category=data.getString("flight_category");
        JSONObject visibility=data.getJSONObject("visibility");
        String visibility_miles=visibility.getString("miles");
        String visibility_meters=visibility.getString("meters");

        Log.v(TAG, "Conditions code(text): "+conditions_code+"("+conditions_text+")");
        Log.v(TAG, "Dew point C(F): "+dew_point_C+"("+dew_point_F+")");
        Log.v(TAG, "Flight category: "+flight_category);
        Log.v(TAG, "Visibility miles(metres): "+visibility_miles+"("+visibility_meters+")");

        ContentValues[] weatherContentValues = new ContentValues[jsonWeatherArrayData.length()];


        return weatherContentValues;
    }
}