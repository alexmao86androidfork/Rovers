package com.schiztech.rovers.app.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import java.util.Locale;

/**
 * Created by schiz_000 on 1/9/2015.
 */
public class RoverlyticsUtils {

    //region Keys

    public static String TOTAL_LAUNCHES = "TOTAL_LAUNCHES";
    public static String TOTAL_TIME = "TOTAL_TIME";
    public static String TOTAL_DISTANCE = "TOTAL_DISTANCE";

    //endregion Keys

    //region Launches

    public static int getLaunches(Context context){
        return PrefUtils.getRoverlyticsLaunchesValue(context);
    }

    public static void addLaunch(Context context){
        int totalLaunches = getLaunches(context);

        PrefUtils.setRoverlyticsLaunchesValue(context, totalLaunches + 1);
    }

    //endregion Launches

    //region Total Time

    public static void addTime(Context context, float timeInSeconds){
        float totalTime = getTime(context);

        if(totalTime + timeInSeconds <= Float.MAX_VALUE) {
            PrefUtils.setRoverlyticsTotalTimeValue(context,totalTime + timeInSeconds);
        }

    }

    public static float getTime(Context context){
        return PrefUtils.getRoverlyticsTotalTimeValue(context);
    }


    public static float getMinutes(float seconds){
        return seconds / SECONDS_IN_MINUTE;
    }

    public static float getHours(float seconds){
        return seconds / (SECONDS_IN_MINUTE * MINUTES_IN_HOUR);
    }

    public static float getDays(float seconds){
        return seconds / (SECONDS_IN_MINUTE * MINUTES_IN_HOUR * HOURS_IN_DAY );
    }

    private static final int SECONDS_IN_MINUTE = 60;
    private static final int MINUTES_IN_HOUR = 60;
    private static final int HOURS_IN_DAY = 24;

    //endregion Total Time

    //region Distance

    public static void addDistance(Context context, float distanceInInches){
        float totalDistance = getDistance(context);
        PrefUtils.setRoverlyticsDistanceValue(context,totalDistance + distanceInInches);
    }

    public static float getDistance(Context context){
        return PrefUtils.getRoverlyticsDistanceValue(context);
    }

    public static float getCentemeters(float inches){
        return inches * CENTEMETERS_IN_INCH;
    }
    public static float getMeters(float inches){
        return getCentemeters(inches) / CENTEMETERS_IN_METER;
    }
    public static float getKilometers(float inches){
        return getCentemeters(inches) / (CENTEMETERS_IN_METER * METERS_IN_KILOMETER);
    }
    public static float getFeet(float inches){
        return inches / INCHES_IN_FEET;
    }
    public static float getYards(float inches){
        return inches / (INCHES_IN_FEET * FEET_IN_YARD);
    }
    public static float getMiles(float inches){
        return inches / (INCHES_IN_FEET * FEET_IN_YARD * YARD_IN_MILE);
    }

    private static final float CENTEMETERS_IN_INCH= 2.54f;
    private static final float CENTEMETERS_IN_METER= 100f;
    private static final float METERS_IN_KILOMETER = 1000f;

    private static final float INCHES_IN_FEET= 12f;
    private static final float FEET_IN_YARD= 3f;
    private static final float YARD_IN_MILE= 1760f;



    public enum DistanceUnits{
        Imperial,
        Metric
    }

    public static DistanceUnits getDistanceUnitByLocale(){
        Locale locale = Locale.getDefault();
        String countryCode = locale.getCountry();
        if ("US".equals(countryCode)) return DistanceUnits.Imperial; // USA
        if ("LR".equals(countryCode)) return DistanceUnits.Imperial; // liberia
        if ("MM".equals(countryCode)) return DistanceUnits.Imperial; // burma

        return DistanceUnits.Metric;

    }



    //endregion Distance

    //region Items Count

    public static int getItemsCount(Context context){
        return PrefUtils.getRoverlyticsItemsCountValue(context);
    }

    public static void setItemsCount(Context context, int value){
        PrefUtils.setRoverlyticsItemsCountValue(context, value);

    }

    //endregion Items Count
}

