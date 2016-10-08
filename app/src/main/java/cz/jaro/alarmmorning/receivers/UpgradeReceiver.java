package cz.jaro.alarmmorning.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.util.Log;

import cz.jaro.alarmmorning.GlobalManager;
import cz.jaro.alarmmorning.R;
import cz.jaro.alarmmorning.WakeLocker;
import cz.jaro.alarmmorning.checkalarmtime.CheckAlarmTime;
import cz.jaro.alarmmorning.nighttimebell.CustomAlarmTone;
import cz.jaro.alarmmorning.nighttimebell.NighttimeBell;

/**
 * This receiver is called by the operating system on the app upgrade.
 */
public class UpgradeReceiver extends BroadcastReceiver {

    private static final String TAG = UpgradeReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        WakeLocker.acquire(context);

        Log.v(TAG, "onReceive()");

        // Update default values of preferences
        PreferenceManager.setDefaultValues(context, R.xml.preferences, false);

        Log.i(TAG, "Setting alarm on update");
        GlobalManager globalManager = new GlobalManager(context);
        globalManager.forceSetAlarm();

        Log.i(TAG, "Starting CheckAlarmTime on update");
        CheckAlarmTime checkAlarmTime = CheckAlarmTime.getInstance(context);
        checkAlarmTime.checkAndRegisterCheckAlarmTime();

        Log.i(TAG, "Starting NighttimeBell on update");
        NighttimeBell nighttimeBell = NighttimeBell.getInstance(context);
        nighttimeBell.checkAndRegister();

        Log.i(TAG, "Installing files");
        CustomAlarmTone customAlarmTone = new CustomAlarmTone(context);
        customAlarmTone.install();

        WakeLocker.release();
    }

}