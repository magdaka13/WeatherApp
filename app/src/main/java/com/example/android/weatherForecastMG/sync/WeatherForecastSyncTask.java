package com.example.android.weatherForecastMG.sync;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.text.format.DateUtils;
import android.util.Log;

import com.example.android.weatherForecastMG.data.WeatherForecastPreferences;
import com.example.android.weatherForecastMG.data.WeatherContract;
import com.example.android.weatherForecastMG.utilities.NetworkUtils;
import com.example.android.weatherForecastMG.utilities.NotificationUtils;
import com.example.android.weatherForecastMG.utilities.OpenWeatherJsonUtils;

import java.net.URL;

import static android.content.ContentValues.TAG;


public class WeatherForecastSyncTask {

    public static final String[] WEATHER_METAR_PROJECTION = {
            WeatherContract.WeatherEntry.COLUMN_METAR_RAW,
            WeatherContract.WeatherEntry.COLUMN_DEWPOINT_C,
            WeatherContract.WeatherEntry.COLUMN_DEWPOINT_F,
            WeatherContract.WeatherEntry.COLUMN_FLIGHT_CATEGORY,
            WeatherContract.WeatherEntry.COLUMN_VISIBILITY_MILES,
            WeatherContract.WeatherEntry.COLUMN_VISIBILITY_METERS
    };

    /**
     * Performs the network request for updated weather, parses the JSON from that request, and
     * inserts the new weather information into our ContentProvider. Will notify the user that new
     * weather has been loaded if the user hasn't been notified of the weather within the last day
     * AND they haven't disabled notifications in the preferences screen.
     *
     * @param context Used to access utility methods and the ContentResolver
     */
    synchronized public static void syncWeather(Context context) {

        try {
            /*
             * The getUrl method will return the URL that we need to get the forecast JSON for the
             * weather. It will decide whether to create a URL based off of the latitude and
             * longitude or off of a simple location as a String.
             */
            URL weatherRequestUrl = NetworkUtils.getUrl(context);

            /* Use the URL to retrieve the JSON */
            String jsonWeatherResponse = NetworkUtils.getResponseFromHttpUrl(weatherRequestUrl);

            /* Parse the JSON into a list of weather values */
            ContentValues[] weatherValues = OpenWeatherJsonUtils
                    .getWeatherContentValuesFromJson(context, jsonWeatherResponse);

            /*
             * In cases where our JSON contained an error code, getWeatherContentValuesFromJson
             * would have returned null. We need to check for those cases here to prevent any
             * NullPointerExceptions being thrown. We also have no reason to insert fresh data if
             * there isn't any to insert.
             */
            if (weatherValues != null && weatherValues.length != 0) {
                /* Get a handle on the ContentResolver to delete and insert data */
                ContentResolver WeatherForecastContentResolver = context.getContentResolver();

                /* Delete old weather data because we don't need to keep multiple days' data */
                WeatherForecastContentResolver.delete(
                        WeatherContract.WeatherEntry.CONTENT_URI,
                        null,
                        null);

                /* Insert our new weather data into WeatherForecast's ContentProvider */
                WeatherForecastContentResolver.bulkInsert(
                        WeatherContract.WeatherEntry.CONTENT_URI,
                        weatherValues);

                /*
                 * Finally, after we insert data into the ContentProvider, determine whether or not
                 * we should notify the user that the weather has been refreshed.
                 */
                boolean notificationsEnabled = WeatherForecastPreferences.areNotificationsEnabled(context);

                /*
                 * If the last notification was shown was more than 1 day ago, we want to send
                 * another notification to the user that the weather has been updated. Remember,
                 * it's important that you shouldn't spam your users with notifications.
                 */
                long timeSinceLastNotification = WeatherForecastPreferences
                        .getEllapsedTimeSinceLastNotification(context);

                boolean oneDayPassedSinceLastNotification = false;

                if (timeSinceLastNotification >= DateUtils.DAY_IN_MILLIS) {
                    oneDayPassedSinceLastNotification = true;
                }

                /*
                 * We only want to show the notification if the user wants them shown and we
                 * haven't shown a notification in the past day.
                 */
                if (notificationsEnabled && oneDayPassedSinceLastNotification) {
                    NotificationUtils.notifyUserOfNewWeather(context);
                }

            /* If the code reaches this point, we have successfully performed our sync */

            /*METAR data */
                URL METARRequestUrl = NetworkUtils.getMETARUrl(context);
            /* Use the URL to retrieve the JSON */
                String jsonWeatherResponseMETAR = NetworkUtils.getResponseFromHttpUrlWithHeader(METARRequestUrl);
                Log.v(TAG, "METAR JSON " + jsonWeatherResponseMETAR);

            /* Parse the JSON into a list of weather values */
                ContentValues[] weatherValuesMETAR = OpenWeatherJsonUtils
                        .getWeatherContentValuesFromJsonMETAR(context, jsonWeatherResponseMETAR);

                if (weatherValuesMETAR != null && weatherValues.length != 0) {
                /* Get a handle on the ContentResolver to delete and insert data */
                    ContentResolver WeatherMETARContentResolver = context.getContentResolver();


                /* update WeatherForecast's ContentProvider */
//                    WeatherMETARContentResolver.updateMETARdata(
  //                          WeatherContract.WeatherEntry.CONTENT_URI,
    //                        weatherValuesMETAR,
      //                      WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
        ///                    WEATHER_METAR_PROJECTION

           //                 );
                }
            /*METAR data-end */



            }

        } catch (Exception e) {
            /* Server probably invalid */
            e.printStackTrace();
        }
    }
}