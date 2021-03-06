package cz.jaro.alarmmorning;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.support.v4.app.NavUtils;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ListView;

import cz.jaro.alarmmorning.checkalarmtime.CheckAlarmTime;
import cz.jaro.alarmmorning.graphics.AppCompatPreferenceActivity;
import cz.jaro.alarmmorning.graphics.RelativeTimePreference;
import cz.jaro.alarmmorning.graphics.TimePreference;
import cz.jaro.alarmmorning.holiday.HolidayHelper;
import cz.jaro.alarmmorning.nighttimebell.NighttimeBell;
import cz.jaro.alarmmorning.wizard.Wizard;

public class SettingsActivity extends AppCompatPreferenceActivity {

    private static final String TAG = AppCompatPreferenceActivity.class.getSimpleName();

    // TODO Reregister system alarm if the preference for future time changes
    // TODO Reregister system alarm if the alarm is snoozed and the preference for snooze time changes

    /**
     * Value is ringtone URI.
     */
    public static final String PREF_RINGTONE = "pref_ringtone";

    /**
     * Value is 0 .. PREF_VOLUME_MAX. Percents of max volume.
     */
    public static final String PREF_VOLUME = "pref_volume";

    /**
     * Increase volume by 1% every second, until the volume is reached.
     */
    public static final String PREF_VOLUME_INCREASING = "pref_volume_increasing";

    /**
     * Vibrate.
     */
    public static final String PREF_VIBRATE = "pref_vibrate";

    /**
     * Value is in minutes.
     */
    public static final String PREF_SNOOZE_TIME = "pref_snooze_time";

    /**
     * Value is in minutes.
     */
    public static final String PREF_NEAR_FUTURE_TIME = "pref_near_future_time";

    /**
     * Value is in minutes.
     */
    public static final String PREF_NAP_TIME = "pref_nap_time";

    public static final String PREF_ACTION_ON_BUTTON = "pref_action_on_button";
    public static final String PREF_ACTION_ON_MOVE = "pref_action_on_move";
    public static final String PREF_ACTION_ON_FLIP = "pref_action_on_flip";
    public static final String PREF_ACTION_ON_SHAKE = "pref_action_on_shake";
    public static final String PREF_ACTION_ON_PROXIMITY = "pref_action_on_proximity";

    public static final String PREF_ACTION_NOTHING = "0";
    public static final String PREF_ACTION_MUTE = "1";
    public static final String PREF_ACTION_SNOOZE = "2";
    public static final String PREF_ACTION_DISMISS = "3";

    public static final String PREF_CHECK_ALARM_TIME = "pref_check_alarm_time";
    public static final String PREF_CHECK_ALARM_TIME_AT = "pref_check_alarm_time_at";
    public static final String PREF_CHECK_ALARM_TIME_GAP = "pref_check_alarm_time_gap";

    public static final String PREF_NIGHTTIME_BELL = "pref_nighttime_bell";
    public static final String PREF_NIGHTTIME_BELL_AT = "pref_nighttime_bell_at";
    public static final String PREF_NIGHTTIME_BELL_RINGTONE = "pref_nighttime_bell_ringtone";

    public static final String PREF_HOLIDAY = "pref_holiday";

    public static final String PREF_START_WIZARD = "pref_start_wizard";

    public static final String PREF_RINGTONE_DEFAULT = "content://settings/system/alarm_alert";
    public static final int PREF_VOLUME_DEFAULT = 8;
    public static final boolean PREF_VOLUME_INCREASING_DEFAULT = true;
    public static final boolean PREF_VIBRATE_DEFAULT = true;
    public static final int PREF_SNOOZE_TIME_DEFAULT = 10;
    public static final int PREF_NEAR_FUTURE_TIME_DEFAULT = 120;
    public static final String PREF_ACTION_DEFAULT = PREF_ACTION_NOTHING;
    public static final int PREF_NAP_TIME_DEFAULT = 30;
    public static final boolean PREF_CHECK_ALARM_TIME_DEFAULT = true;
    public static final String PREF_CHECK_ALARM_TIME_AT_DEFAULT = "22:00";
    public static final int PREF_CHECK_ALARM_TIME_GAP_DEFAULT = 60;

    public static final boolean PREF_NIGHTTIME_BELL_DEFAULT = true;
    public static final String PREF_NIGHTTIME_BELL_AT_DEFAULT = "22:00";
    public static final String PREF_NIGHTTIME_BELL_RINGTONE_DEFAULT = "raw://church_clock_strikes_3";

    public static final String PREF_HOLIDAY_NONE = HolidayHelper.PATH_TOP;
    public static final String PREF_HOLIDAY_DEFAULT = PREF_HOLIDAY_NONE;

    public static final int PREF_VOLUME_MAX = 10;

    public static final String SETTING_ACTION__NOTHING = "Nothing";
    public static final String SETTING_ACTION__MUTE = "Mute";
    public static final String SETTING_ACTION__SNOOZE = "Snooze";
    public static final String SETTING_ACTION__DISMISS = "Dismiss";

    private static final int REQUEST_CODE_WIZARD = 1;

    private RingtonePreference ringtonePreference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        bindPreferenceSummaryToValue(findPreference(PREF_RINGTONE));
        bindPreferenceSummaryToValue(findPreference(PREF_VOLUME));
        bindPreferenceChangeListener(findPreference(PREF_VOLUME_INCREASING));
        bindPreferenceChangeListener(findPreference(PREF_VIBRATE));
        bindPreferenceSummaryToValue(findPreference(PREF_SNOOZE_TIME));
        bindPreferenceSummaryToValue(findPreference(PREF_NEAR_FUTURE_TIME));
        bindPreferenceSummaryToValue(findPreference(PREF_ACTION_ON_BUTTON));
        bindPreferenceSummaryToValue(findPreference(PREF_ACTION_ON_MOVE));
        bindPreferenceSummaryToValue(findPreference(PREF_ACTION_ON_FLIP));
        bindPreferenceSummaryToValue(findPreference(PREF_ACTION_ON_SHAKE));
        bindPreferenceSummaryToValue(findPreference(PREF_ACTION_ON_PROXIMITY));
        bindPreferenceSummaryToValue(findPreference(PREF_CHECK_ALARM_TIME_AT));
        bindPreferenceSummaryToValue(findPreference(PREF_CHECK_ALARM_TIME_GAP));
        bindPreferenceSummaryToValue(findPreference(PREF_NIGHTTIME_BELL_AT));
        bindPreferenceSummaryToValue(findPreference(PREF_NIGHTTIME_BELL_RINGTONE));
        bindPreferenceSummaryToValue(findPreference(PREF_NAP_TIME));
        bindPreferenceSummaryToValue(findPreference(PREF_HOLIDAY));

        final Context context = this;

        // Start/stop services

        Preference prefCheckAlarmTime = findPreference(PREF_CHECK_ALARM_TIME);
        prefCheckAlarmTime.setOnPreferenceChangeListener((preference, newValue) -> {
            analytics(preference, newValue);

            boolean boolValue = (boolean) newValue;
            CheckAlarmTime checkAlarmTime = CheckAlarmTime.getInstance(context);
            if (boolValue) {
                Log.i(TAG, "Starting CheckAlarmTime");
                checkAlarmTime.register();
            } else {
                Log.i(TAG, "Stopping CheckAlarmTime");
                checkAlarmTime.unregister();
            }
            return true;
        });

        Preference prefNighttimeBell = findPreference(PREF_NIGHTTIME_BELL);
        prefNighttimeBell.setOnPreferenceChangeListener((preference, newValue) -> {
            analytics(preference, newValue);

            boolean boolValue = (boolean) newValue;
            NighttimeBell nighttimeBell = NighttimeBell.getInstance(context);
            if (boolValue) {
                Log.i(TAG, "Starting NighttimeBell");
                nighttimeBell.register();
            } else {
                Log.i(TAG, "Stopping NighttimeBell");
                nighttimeBell.unregister();
            }
            return true;
        });

        // Click handlers

        Preference prefStartWizard = findPreference(PREF_START_WIZARD);
        prefStartWizard.setOnPreferenceClickListener(preference -> {
            Analytics analytics = new Analytics(preference.getContext(), Analytics.Event.Start,
                    Analytics.Channel.Activity, Analytics.ChannelName.Settings);
            analytics.set(Analytics.Param.Target, Analytics.TARGET_WIZARD);
            analytics.save();

            Intent intent = new Intent(context, Wizard.class);
            startActivityForResult(intent, REQUEST_CODE_WIZARD);

            return true;
        });

        Preference prefRingtone = findPreference(PREF_RINGTONE);
        prefRingtone.setOnPreferenceClickListener(preference -> {
            ringtonePreference = (RingtonePreference) preference;
            return false;
        });

        Preference prefNighttimeBellRingtone = findPreference(PREF_NIGHTTIME_BELL_RINGTONE);
        prefNighttimeBellRingtone.setOnPreferenceClickListener(preference -> {
            ringtonePreference = (RingtonePreference) preference;
            return false;
        });
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        Toolbar toolbar;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            ViewGroup root = (ViewGroup) findViewById(android.R.id.list).getParent().getParent().getParent();
            toolbar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.toolbar, root, false);
            root.addView(toolbar, 0);
        } else {
            ViewGroup root = (ViewGroup) findViewById(android.R.id.content);
            ListView content = (ListView) root.getChildAt(0);
            root.removeAllViews();
            toolbar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.toolbar, root, false);
            int height;
            TypedValue tv = new TypedValue();
            if (getTheme().resolveAttribute(R.attr.actionBarSize, tv, true)) {
                height = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
            } else {
                height = toolbar.getHeight();
            }
            content.setPadding(0, height, 0, 0);
            root.addView(content);
            root.addView(toolbar);
        }
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            if (!super.onMenuItemSelected(featureId, item)) {
                NavUtils.navigateUpFromSameTask(this);
            }
            return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_WIZARD) {
            if (resultCode == RESULT_OK) {
                finish();
            }
        } else {
            // Propagate to RingtonePreference
            if (ringtonePreference != null) {
                ringtonePreference.onActivityResult(requestCode, resultCode, data);
                ringtonePreference = null;
            }
        }
    }

    /**
     * A preference value change listener that updates the preference's summary to reflect its new value.
     */
    private static OnPreferenceChangeListenerWithAnalytics sBindPreferenceSummaryToValueListener = new OnPreferenceChangeListenerWithAnalytics() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            analytics(preference, value);
            action(preference, value);
            updateSummary(preference, value);
            return true;
        }

        void action(Preference preference, Object value) {
            String stringValue = value.toString();
            String key = preference.getKey();

            if (key.equals(PREF_HOLIDAY)) {
                GlobalManager globalManager = GlobalManager.getInstance();
                globalManager.saveHoliday(stringValue);
            }
        }

        public boolean updateSummary(Preference preference, Object value) {
            String stringValue = value.toString();
            String key = preference.getKey();

            if (preference instanceof RingtonePreference) {
                // For ringtone preferences, look up the correct display value using RingtoneManager.
                if (TextUtils.isEmpty(stringValue)) {
                    // Empty values correspond to 'silent' (no ringtone).
                    preference.setSummary(R.string.pref_ringtone_silent);
                } else {
                    Ringtone ringtone = RingtoneManager.getRingtone(preference.getContext(), Uri.parse(stringValue));

                    if (ringtone == null) {
                        // Clear the summary if there was a lookup error.
                        preference.setSummary(null);
                    } else if (stringValue.equals(PREF_NIGHTTIME_BELL_RINGTONE_DEFAULT)) {
                        // Use string from resources for a raw ringtone
                        String name = preference.getContext().getResources().getString(R.string.alarmtone_title_church_bell);
                        preference.setSummary(name);
                    } else {
                        // Set the summary to reflect the new ringtone display name.
                        String name = ringtone.getTitle(preference.getContext());
                        preference.setSummary(name);
                    }
                }
            } else if (key.equals(PREF_VOLUME)) {
                int intValue = (int) value;
                int volume = getRealVolume(intValue, 100);

                Context context = preference.getContext();
                Resources res = context.getResources();
                String summaryText = res.getString(R.string.pref_summary_volume, volume);

                preference.setSummary(summaryText);
            } else if (key.equals(PREF_SNOOZE_TIME)) {
                int intValue = (int) value;

                Context context = preference.getContext();
                Resources res = context.getResources();
                String summaryText = res.getString(R.string.pref_summary_snooze_time, intValue);

                preference.setSummary(summaryText);
            } else if (preference instanceof RelativeTimePreference) {
                int intValue = (int) value;
                int hours = RelativeTimePreference.valueToHour(intValue);
                int minutes = RelativeTimePreference.valueToMinute(intValue);

                Context context = preference.getContext();
                Resources res = context.getResources();
                String summaryText = res.getString(R.string.pref_summary_relative_time, hours, minutes);

                preference.setSummary(summaryText);
            } else if (preference instanceof TimePreference) {
                int hours = TimePreference.getHour(stringValue);
                int minutes = TimePreference.getMinute(stringValue);

                Context context = preference.getContext();
                Resources res = context.getResources();
                String timeText = Localization.timeToString(hours, minutes, context);
                String summaryText = res.getString(R.string.pref_summary_time_preference, timeText);

                preference.setSummary(summaryText);
            } else if (key.equals(PREF_ACTION_ON_BUTTON) || key.equals(PREF_ACTION_ON_MOVE) || key.equals(PREF_ACTION_ON_FLIP) || key.equals(PREF_ACTION_ON_SHAKE) || key.equals(PREF_ACTION_ON_PROXIMITY)) {
                int intValue = Integer.valueOf(stringValue);

                Context context = preference.getContext();
                Resources res = context.getResources();
                CharSequence summaryText = res.getTextArray(R.array.actionArray)[intValue];

                preference.setSummary(summaryText);
            } else if (key.equals(PREF_HOLIDAY)) {
                String summaryText = HolidayHelper.getInstance().preferenceToDisplayName(stringValue);

                preference.setSummary(summaryText);
            } else {
                // For all other preferences, set the summary to the value's simple string representation.
                preference.setSummary(stringValue);
            }

            // Reset alarms

            if (key.equals(PREF_CHECK_ALARM_TIME_AT)) {
                Context context = preference.getContext();
                CheckAlarmTime checkAlarmTime = CheckAlarmTime.getInstance(context);
                checkAlarmTime.reregister(stringValue);
            } else if (key.equals(PREF_NIGHTTIME_BELL_AT)) {
                Context context = preference.getContext();
                NighttimeBell nighttimeBell = NighttimeBell.getInstance(context);
                nighttimeBell.reregister(stringValue);
            }

            return true;
        }
    };

    static void analytics(Preference preference, Object value) {
        String stringValue = value.toString();
        String key = preference.getKey();

        Analytics analytics = new Analytics(preference.getContext(), Analytics.Event.Change_setting, Analytics.Channel.Activity, Analytics.ChannelName.Settings);
        analytics.set(Analytics.Param.Preference_key, key);
        analytics.set(Analytics.Param.Preference_value, stringValue);
        analytics.save();
    }

    public static int getRealVolume(double volumePreference, int maxVolume) {
        return (int) Math.ceil(((volumePreference / SettingsActivity.PREF_VOLUME_MAX) * maxVolume));
    }

    public static String actionCodeToString(String action) {
        switch (action) {
            case PREF_ACTION_NOTHING:
                return SETTING_ACTION__NOTHING;
            case PREF_ACTION_MUTE:
                return SETTING_ACTION__MUTE;
            case PREF_ACTION_SNOOZE:
                return SETTING_ACTION__SNOOZE;
            case PREF_ACTION_DISMISS:
                return SETTING_ACTION__DISMISS;
            default:
                throw new IllegalArgumentException("Unexpected argument " + action);
        }
    }

    /**
     * Binds a preference's summary to its value. More specifically, when the preference's value is changed, its summary (line of text below the preference
     * title) is updated to reflect the value. The summary is also immediately updated upon calling this method. The exact display format is dependent on the
     * type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        bindPreferenceChangeListener(preference);

        // Trigger the listener immediately with the preference's current value.
        String key = preference.getKey();
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(preference.getContext());
        Object newValue;
        switch (key) {
            case PREF_RINGTONE:
                newValue = defaultSharedPreferences.getString(preference.getKey(), PREF_RINGTONE_DEFAULT);
                break;
            case PREF_VOLUME:
                newValue = defaultSharedPreferences.getInt(preference.getKey(), PREF_VOLUME_DEFAULT);
                break;
            case PREF_SNOOZE_TIME:
                newValue = defaultSharedPreferences.getInt(preference.getKey(), PREF_SNOOZE_TIME_DEFAULT);
                break;
            case PREF_NEAR_FUTURE_TIME:
                newValue = defaultSharedPreferences.getInt(preference.getKey(), PREF_NEAR_FUTURE_TIME_DEFAULT);
                break;
            case PREF_ACTION_ON_BUTTON:
            case PREF_ACTION_ON_MOVE:
            case PREF_ACTION_ON_FLIP:
            case PREF_ACTION_ON_SHAKE:
            case PREF_ACTION_ON_PROXIMITY:
                newValue = defaultSharedPreferences.getString(preference.getKey(), PREF_ACTION_DEFAULT);
                break;
            case PREF_CHECK_ALARM_TIME_AT:
                newValue = defaultSharedPreferences.getString(preference.getKey(), PREF_CHECK_ALARM_TIME_AT_DEFAULT);
                break;
            case PREF_CHECK_ALARM_TIME_GAP:
                newValue = defaultSharedPreferences.getInt(preference.getKey(), PREF_CHECK_ALARM_TIME_GAP_DEFAULT);
                break;
            case PREF_NIGHTTIME_BELL_AT:
                newValue = defaultSharedPreferences.getString(preference.getKey(), PREF_NIGHTTIME_BELL_AT_DEFAULT);
                break;
            case PREF_NIGHTTIME_BELL_RINGTONE:
                newValue = defaultSharedPreferences.getString(preference.getKey(), PREF_NIGHTTIME_BELL_RINGTONE_DEFAULT);
                break;
            case PREF_NAP_TIME:
                newValue = defaultSharedPreferences.getInt(preference.getKey(), PREF_NAP_TIME_DEFAULT);
                break;
            case PREF_HOLIDAY:
                newValue = defaultSharedPreferences.getString(preference.getKey(), PREF_HOLIDAY_DEFAULT);
                break;
            default:
                throw new IllegalArgumentException("Unexpected argument " + key);
        }
        sBindPreferenceSummaryToValueListener.updateSummary(preference, newValue);
    }

    private static void bindPreferenceChangeListener(Preference preference) {
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);
    }
}

interface OnPreferenceChangeListenerWithAnalytics extends Preference.OnPreferenceChangeListener {

    boolean updateSummary(Preference preference, Object value);
}