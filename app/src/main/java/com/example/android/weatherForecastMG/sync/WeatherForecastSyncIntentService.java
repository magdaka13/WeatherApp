package com.example.android.weatherForecastMG.sync;

import android.app.IntentService;
import android.content.Intent;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class WeatherForecastSyncIntentService extends IntentService {

    public WeatherForecastSyncIntentService() {
        super("WeatherForecastSyncIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        WeatherForecastSyncTask.syncWeather(this);
    }
}