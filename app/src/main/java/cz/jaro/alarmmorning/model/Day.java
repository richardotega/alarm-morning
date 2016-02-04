package cz.jaro.alarmmorning.model;

import android.content.Context;

import java.util.Calendar;

import cz.jaro.alarmmorning.clock.Clock;

/**
 * Represents the alarm clock setting for a particular date. The default values are inherited from
 * {@link #defaults}, but can be changed (and stored here).
 * All methods are ment "with respect to the particulat date".
 * <p/>
 * Terminology: We use a slightly different terminology than standard English.<br>
 * We use "Alarm", while English uses "alarm clock".<br>
 * We use "Alarm is enabled", while English uses "alarm is on".<br>
 * We use "Alarm rings", while English uses "alarm goes off".
 * <p/>
 * The alarm time is a combination of {@link #getDate()}, {@link #getHourX()} and {@link
 * #getMinuteX()}. For convenience, the method {@link #getDateTime()} combines al three items
 * together and returns the alarm time..
 */
public class Day {

    private long id;

    private Calendar date;

    /**
     * 0 = as default
     * 1 = set (to particular time)
     * 2 = unset (disabled)
     */
    private int state;

    private int hour;

    private int minute;

    private Defaults defaults;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Calendar getDate() {
        return date;
    }

    public void setDate(Calendar date) {
        this.date = date;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public Defaults getDefaults() {
        return defaults;
    }

    public void setDefaults(Defaults defaults) {
        this.defaults = defaults;
    }

    /**
     * Check if the alarm time is passed, e.g. if the alarm is enabled and the alarm time was in past.
     *
     * @return true if the alarm time is passed
     */
    public boolean isPassed(Clock clock) {
        Calendar now = clock.now();
        if (isEnabled()) {
            if (getDateTime().before(now)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if the alarm is enabled on this date.
     *
     * @return true if the alarm is enabled
     */
    public boolean isEnabled() {
        return state == AlarmDataSource.DAY_STATE_ENABLED ||
                (state == AlarmDataSource.DAY_STATE_DEFAULT && defaults.isEnabled());
    }

    /**
     * Return the hour of the alarm.
     * <p/>
     * If the alarm is enabled, then use this method to get the hour of alarm time.
     *
     * @return hour of the alarm (that should be used if the alarm is enabled)
     */
    public int getHourX() {
        if (!isValid() || state == AlarmDataSource.DAY_STATE_DEFAULT)
            return defaults.getHour();
        else
            return hour;
    }

    /**
     * Return the hour of the alarm.
     * <p/>
     * If the alarm is enabled, then use this method to get the hour of alarm time.
     *
     * @return minute of the alarm
     */
    public int getMinuteX() {
        if (!isValid() || state == AlarmDataSource.DAY_STATE_DEFAULT)
            return defaults.getMinute();
        else
            return minute;
    }

    /**
     * Checks whether the data for this object are valid.
     *
     * @return true if the object was populated by data from a database. If false then use the data from {@code defaults}.
     */
    private boolean isValid() {
        return hour != AlarmDataSource.VALUE_UNSET;
    }

    /**
     * Return the alarm time.
     * <p/>
     * If the alarm is enabled, then use this method to get the minute of alarm time.
     *
     * @return alarm time
     */
    public Calendar getDateTime() {
        // TOTO Refactor to return Date
        Calendar alarmTime = (Calendar) date.clone();

        alarmTime.set(Calendar.HOUR_OF_DAY, getHourX());
        alarmTime.set(Calendar.MINUTE, getMinuteX());
        alarmTime.set(Calendar.SECOND, 0);
        alarmTime.set(Calendar.MILLISECOND, 0);

        return alarmTime;
    }

    /**
     * Switches the alarm:<br>
     * If it's enabled, then it's set to disabled.<br>
     * If it's disabled, then it's set to enabled.<br>
     * If it's set to default and the default is enabled, then it's set to disabled.<br>
     * If it's set to default and the default is disabled, then it's set to enabled.
     */
    public void reverse() {
        switch (getState()) {
            case AlarmDataSource.DAY_STATE_DEFAULT:
                setState(defaults.isEnabled() ? AlarmDataSource.DAY_STATE_DISABLED : AlarmDataSource.DAY_STATE_ENABLED);
                break;

            case AlarmDataSource.DAY_STATE_ENABLED:
                setState(AlarmDataSource.DAY_STATE_DISABLED);
                break;

            case AlarmDataSource.DAY_STATE_DISABLED:
                setState(AlarmDataSource.DAY_STATE_ENABLED);
                break;
        }
    }

    /**
     * Checks if this is the next alarm. Specifically: there is no other alarm between now and
     * alarm
     * time.
     *
     * @param context context
     * @param clock   clock
     * @return true if this is the next alarm
     */
    public boolean isNextAlarm(Context context, Clock clock) {
        Calendar alarmTime1 = getDateTime();

        Calendar alarmTime2 = AlarmDataSource.getNextAlarm(context, clock);

        return alarmTime1.equals(alarmTime2);
    }

    /**
     * Returns the time to alarm time.
     *
     * @return miliseconds between now and the alarm time. The negative value means that alarm time
     * was in past.
     */
    public long getTimeToRing(Clock clock) {
        Calendar alarmTime1 = getDateTime();
        Calendar now = clock.now();

        return alarmTime1.getTimeInMillis() - now.getTimeInMillis();
    }

    /**
     * Check if the alarm is changed on this date.
     * <p/>
     * Note that only the (enabled/disabled) state and time (hour and minute) is compared between
     * with default.
     * <p/>
     * Technically, if 1. the default is enabled
     * and 2. the day state uses the time from default (the state is {@link
     * AlarmDataSource#DAY_STATE_DEFAULT}) and 3. the day is changed to disabled and 4.
     * back to enabled (to {@link AlarmDataSource#DAY_STATE_ENABLED}), then this method returns
     * true. On technical it could be argued that the alarm is changed
     * since the state is {@link AlarmDataSource#DAY_STATE_ENABLED} and not {@link
     * AlarmDataSource#DAY_STATE_DEFAULT}.
     *
     * @return false if the alarm time on this date is changed (compared to default)
     */
    public boolean sameAsDefault() {
        return (state == AlarmDataSource.DAY_STATE_ENABLED && getDefaults().getState() == AlarmDataSource.DEFAULT_STATE_ENABLED && hour == defaults.getHour() && minute == defaults.getMinute()) ||
                (state == AlarmDataSource.DAY_STATE_DISABLED && getDefaults().getState() == AlarmDataSource.DEFAULT_STATE_DISABLED) ||
                state == AlarmDataSource.DAY_STATE_DEFAULT;
    }
}
