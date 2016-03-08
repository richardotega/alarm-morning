package cz.jaro.alarmmorning;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

import java.util.Calendar;

import cz.jaro.alarmmorning.clock.Clock;
import cz.jaro.alarmmorning.clock.SystemClock;
import cz.jaro.alarmmorning.model.Day;

/**
 * The widget.
 */
public class WidgetProvider extends AppWidgetProvider {

    private static final String TAG = WidgetProvider.class.getSimpleName();

    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.v(TAG, "onUpdate()");

        // Perform this loop procedure for each App Widget that belongs to this provider
        for (int i = 0; i < appWidgetIds.length; i++) {
            int appWidgetId = appWidgetIds[i];

            // Create an Intent to launch ExampleActivity
            Intent intent = new Intent(context, AlarmMorningActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
            views.setOnClickPendingIntent(R.id.widget_layout, pendingIntent);

            updateContent(context, views);

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    public static void updateContent(Context context, RemoteViews views) {
        Log.v(TAG, "updateContent()");
        GlobalManager globalManager = new GlobalManager(context);
        Day day = globalManager.getDayWithNextAlarmToRing();

        int iconSrcId;
        String widgetText;

        if (day != null) {
            Calendar alarmTime = day.getDateTime();

            iconSrcId = R.drawable.ic_alarm_white;

            String timeStr = Localization.timeToString(alarmTime.getTime(), context);

            Clock clock = new SystemClock(); // TODO Solve dependency on clock
            Calendar now = clock.now();

            if (RingActivity.onTheSameDate(now, alarmTime)) {
                widgetText = context.getResources().getString(R.string.widget_alarm_text_today, timeStr);
            } else {
                if (inTomorrow(now, alarmTime)) {
                    widgetText = context.getResources().getString(R.string.widget_alarm_text_tomorrow, timeStr);
                } else {
                    int dayOfWeek = alarmTime.get(Calendar.DAY_OF_WEEK);
                    String dayOfWeekText = Localization.dayOfWeekToString(dayOfWeek, clock);
                    if (inNextWeek(now, alarmTime)) {
                        widgetText = context.getResources().getString(R.string.widget_alarm_text_next_week, timeStr, dayOfWeekText);
                    } else {
                        String dateText = Localization.dateToStringVeryShort(alarmTime.getTime());
                        widgetText = context.getResources().getString(R.string.widget_alarm_text_later, timeStr, dayOfWeekText, dateText);
                    }
                }
            }
        } else {
            iconSrcId = R.drawable.ic_alarm_off_white;
            widgetText = context.getResources().getString(R.string.widget_alarm_text_none);
        }
        views.setImageViewResource(R.id.icon, iconSrcId);
        views.setTextViewText(R.id.alarm_time, widgetText);
    }

    private static boolean inTomorrow(Calendar cal1, Calendar cal2) {
        Calendar date = (Calendar) cal1.clone();
        date.add(Calendar.DATE, 1);
        return RingActivity.onTheSameDate(date, cal2);
    }

    private static boolean inNextWeek(Calendar cal1, Calendar cal2) {
        Calendar date = (Calendar) cal1.clone();
        for (int i = 0; i < 6; i++) {
            date.add(Calendar.DATE, 1);
            if (RingActivity.onTheSameDate(date, cal2)) {
                return true;
            }
        }
        return false;
    }
}
