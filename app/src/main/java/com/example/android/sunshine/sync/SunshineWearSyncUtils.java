package com.example.android.sunshine.sync;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.example.android.sunshine.data.WeatherContract;
import com.example.android.sunshine.utilities.SunshineDateUtils;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import static com.example.android.sunshine.utilities.NotificationUtils.INDEX_MAX_TEMP;
import static com.example.android.sunshine.utilities.NotificationUtils.INDEX_MIN_TEMP;
import static com.example.android.sunshine.utilities.NotificationUtils.INDEX_WEATHER_ID;
import static com.example.android.sunshine.utilities.NotificationUtils.WEATHER_NOTIFICATION_PROJECTION;

public class SunshineWearSyncUtils {

    private static final String WEATHER_ITEM_PATH = "/weather";
    private static final String HIGH_KEY = "com.example.android.sunshine.high";
    private static final String LOW_KEY = "com.example.android.sunshine.low";
    private static final String CONDITION_KEY = "com.example.android.sunshine.condition";


    public static void sendDataToWear(Context context, GoogleApiClient googleApiClient) {

        if (googleApiClient.isConnected()) {

            /* Build the URI for today's weather in order to show up to date data in notification */
            Uri todaysWeatherUri = WeatherContract.WeatherEntry
                    .buildWeatherUriWithDate(SunshineDateUtils.normalizeDate(System.currentTimeMillis()));

            /*
             * The MAIN_FORECAST_PROJECTION array passed in as the second parameter is defined in our WeatherContract
             * class and is used to limit the columns returned in our cursor.
             */
            Cursor todayWeatherCursor = context.getContentResolver().query(
                    todaysWeatherUri,
                    WEATHER_NOTIFICATION_PROJECTION,
                    null,
                    null,
                    null);

            /*
             * If todayWeatherCursor is empty, moveToFirst will return false. If our cursor is not
             * empty, we want to show the notification.
             */
            if (todayWeatherCursor.moveToFirst()) {

                /* Weather ID as returned by API, used to identify the icon to be used */
                int conditionId = todayWeatherCursor.getInt(INDEX_WEATHER_ID);
                Double high = todayWeatherCursor.getDouble(INDEX_MAX_TEMP);
                Double low = todayWeatherCursor.getDouble(INDEX_MIN_TEMP);

                PutDataMapRequest putDataMapReq = PutDataMapRequest.create(WEATHER_ITEM_PATH);
                DataMap dataMap = putDataMapReq.getDataMap();
                dataMap.putInt(HIGH_KEY, high.intValue());
                dataMap.putInt(LOW_KEY, low.intValue());
                dataMap.putInt(CONDITION_KEY, conditionId);
                PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
                Wearable.DataApi.putDataItem(googleApiClient, putDataReq);
            }
        }
    }
}
