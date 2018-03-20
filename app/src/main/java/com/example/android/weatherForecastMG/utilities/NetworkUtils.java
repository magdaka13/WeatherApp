
package com.example.android.weatherForecastMG.utilities;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.example.android.weatherForecastMG.data.WeatherForecastPreferences;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

/**
 * These utilities will be used to communicate with the weather servers.
 */
public final class NetworkUtils {

    private static final String TAG = NetworkUtils.class.getSimpleName();

    private static final String DYNAMIC_WEATHER_URL =
            "http://api.openweathermap.org/data/2.5/forecast?APPID=9ce6b38410e63ef3deae35742ce27029";

    private static final String METAR_URL =  "https://api.checkwx.com/metar";
    private static final String TAF_URL =  "https://api.checkwx.com/taf";


    private static final String METAR_TAF_KEY="a6605a9620aaac5f49f59559a6";

    private static final String FORECAST_BASE_URL = DYNAMIC_WEATHER_URL;

    /*
     * NOTE: These values only effect responses from OpenWeatherMap, NOT from the fake weather
     * server. They are simply here to allow us to teach you how to build a URL if you were to use
     * a real API.If you want to connect your app to OpenWeatherMap's API, feel free to! However,
     * we are not going to show you how to do so in this course.
     */

    /* The format we want our API to return */
    private static final String format = "json";
    /* The units we want our API to return */
    private static final String units = "metric";
    /* The number of days we want our API to return */
    private static final int numDays = 5;

    /* The query parameter allows us to provide a location string to the API */
    private static final String QUERY_PARAM = "q";

    private static final String LAT_PARAM = "lat";
    private static final String LON_PARAM = "lon";

    /* The format parameter allows us to designate whether we want JSON or XML from our API */
    private static final String FORMAT_PARAM = "mode";
    /* The units parameter allows us to designate whether we want metric units or imperial units */
    private static final String UNITS_PARAM = "units";
    /* The days parameter allows us to designate how many days of weather data we want */
    private static final String DAYS_PARAM = "cnt";

    /**
     * Retrieves the proper URL to query for the weather data. The reason for both this method as
     * well as {@link #buildUrlWithLocationQuery(String)} is two fold.
     * <p>
     * 1) You should be able to just use one method when you need to create the URL within the
     * app instead of calling both methods.
     * 2) Later in WeatherForecast, you are going to add an alternate method of allowing the user
     * to select their preferred location. Once you do so, there will be another way to form
     * the URL using a latitude and longitude rather than just a location String. This method
     * will "decide" which URL to build and return it.
     *
     * @param context used to access other Utility methods
     * @return URL to query weather service
     */
    public static URL getUrl(Context context) {
        if (WeatherForecastPreferences.isLocationLatLonAvailable(context)) {
            double[] preferredCoordinates = WeatherForecastPreferences.getLocationCoordinates(context);
            double latitude = preferredCoordinates[0];
            double longitude = preferredCoordinates[1];
            return buildUrlWithLatitudeLongitude(latitude, longitude);
        } else {
            String locationQuery = WeatherForecastPreferences.getPreferredWeatherLocation(context);
            return buildUrlWithLocationQuery(locationQuery);
        }
    }

    /**
     * Retrieves the proper URL to query for the METAR weather data.
**/
     public static URL getMETARUrl(Context context) {
        if (WeatherForecastPreferences.isLocationLatLonAvailable(context)) {
            double[] preferredCoordinates = WeatherForecastPreferences.getLocationCoordinates(context);
            double latitude = preferredCoordinates[0];
            double longitude = preferredCoordinates[1];
            return buildMETARURLLongitudeLatitude(latitude, longitude);
        }
         else
        {
            return null;
        }

    }

    /**
     * Retrieves the proper URL to query for the METAR weather data.
     **/
    public static URL getTAFUrl(Context context) {
        if (WeatherForecastPreferences.isLocationLatLonAvailable(context)) {
            double[] preferredCoordinates = WeatherForecastPreferences.getLocationCoordinates(context);
            double latitude = preferredCoordinates[0];
            double longitude = preferredCoordinates[1];
            return buildTAFURLLongitudeLatitude(latitude, longitude);
        }
        else
        {
            return null;
        }

    }

    /**
     * Builds the URL used to talk to the weather server using latitude and longitude of a
     * location.
     *
     * @param latitude  The latitude of the location
     * @param longitude The longitude of the location
     * @return The Url to use to query the weather server.
     */
    private static URL buildUrlWithLatitudeLongitude(Double latitude, Double longitude) {
        Uri weatherQueryUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                .appendQueryParameter(LAT_PARAM, String.valueOf(latitude))
                .appendQueryParameter(LON_PARAM, String.valueOf(longitude))
                .appendQueryParameter(FORMAT_PARAM, format)
                .appendQueryParameter(UNITS_PARAM, units)
                .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
                .build();

        try {
            URL weatherQueryUrl = new URL(weatherQueryUri.toString());
            Log.v(TAG, "URL: " + weatherQueryUrl);
            return weatherQueryUrl;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Builds the URL used to talk to the weather server using a location. This location is based
     * on the query capabilities of the weather provider that we are using.
     *
     * @param locationQuery The location that will be queried for.
     * @return The URL to use to query the weather server.
     */
    private static URL buildUrlWithLocationQuery(String locationQuery) {
        Uri weatherQueryUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                .appendQueryParameter(QUERY_PARAM, locationQuery)
                .appendQueryParameter(FORMAT_PARAM, format)
                .appendQueryParameter(UNITS_PARAM, units)
                .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
                .build();

        try {
            URL weatherQueryUrl = new URL(weatherQueryUri.toString());
            Log.v(TAG, "URL: " + weatherQueryUrl);
            return weatherQueryUrl;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Builds the URL used to talk to the RESTAPI CHECKWX server to retrieve METAR info
     *
     * @param latitude  The latitude of the location
     * @param longitude The longitude of the location
     * @return The Url to use to query the weather server.
     */
    private static URL buildMETARURLLongitudeLatitude(Double latitude, Double longitude) {
        Uri weatherQueryUri = Uri.parse(METAR_URL).buildUpon()
                .appendPath(LAT_PARAM)
                .appendPath(String.valueOf(latitude))
                .appendPath(LON_PARAM)
                .appendPath(String.valueOf(longitude))
                .appendPath("decoded")
                .build();

        try {
            URL weatherQueryUrl = new URL(weatherQueryUri.toString());
            Log.v(TAG, "METAR URL: " + weatherQueryUrl);
            return weatherQueryUrl;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Builds the URL used to talk to the RESTAPI CHECKWX server to retrieve METAR info
     *
     * @param latitude  The latitude of the location
     * @param longitude The longitude of the location
     * @return The Url to use to query the weather server.
     */
    private static URL buildTAFURLLongitudeLatitude(Double latitude, Double longitude) {
        Uri weatherQueryUri = Uri.parse(TAF_URL).buildUpon()
                .appendPath(LAT_PARAM)
                .appendPath(String.valueOf(latitude))
                .appendPath(LON_PARAM)
                .appendPath(String.valueOf(longitude))
                .appendPath("decoded")
                .build();

        try {
            URL weatherQueryUrl = new URL(weatherQueryUri.toString());
            Log.v(TAG, "TAF URL: " + weatherQueryUrl);
            return weatherQueryUrl;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * This method returns the entire result from the HTTP response.
     *
     * @param url The URL to fetch the HTTP response from.
     * @return The contents of the HTTP response, null if no response
     * @throws IOException Related to network and stream reading
     */
    public static String getResponseFromHttpUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            String response = null;
            if (hasInput) {
                response = scanner.next();
            }
            scanner.close();
            return response;
        } finally {
            urlConnection.disconnect();
        }
    }

    /**
     * This method returns the entire result from the HTTP response.
     *
     * @param url The URL to fetch the HTTP response from.
     * @return The contents of the HTTP response, null if no response
     * @throws IOException Related to network and stream reading
     */
    public static String getResponseFromHttpUrlWithHeader(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestProperty("X-API-Key",METAR_TAF_KEY);

        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            String response = null;
            if (hasInput) {
                response = scanner.next();
            }
            scanner.close();
            return response;
        } finally {
            urlConnection.disconnect();
        }
    }

}