package cz.jaro.alarmmorning.model;

/**
 * Represents the alarm clock setting for a particular weekday.
 */
public class Defaults {

    /**
     * Value of the {@code state} field indicating the disabled state.
     */
    public static final int STATE_DISABLED = 0;

    /**
     * Value of the {@code state} field indicating the enabled state.
     */
    public static final int STATE_ENABLED = 1;

    private long id;

    /**
     * The state of the default.
     * <p/>
     * The states are:<br/>
     * {@link #STATE_DISABLED} = unset<br>
     * {@link #STATE_ENABLED} = set (to particular time)
     */
    private int state;

    /**
     * The dey of week is represented by the same number as in {@link java.util.Calendar}. Specifically, the identifier of Monday is {@link java.util.Calendar#MONDAY}
     */
    private int dayOfWeek;

    private int hour;

    private int minute;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(int dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
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

    /**
     * Check if the alarm is enabled on this weekday.
     *
     * @return true if alarm is enabled
     */
    public boolean isEnabled() {
        return state == STATE_ENABLED;
    }

    /**
     * Switches the alarm: if it's enabled, then it's set to disabled. And vice versa.
     */
    public void reverse() {
        if (isEnabled()) {
            setState(STATE_DISABLED);
        } else {
            setState(STATE_ENABLED);
        }
    }
}
