package cz.jaro.alarmmorning;

import android.app.FragmentManager;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;

import cz.jaro.alarmmorning.model.AlarmDataSource;
import cz.jaro.alarmmorning.model.Day;

/**
 * Provide views to RecyclerView with data from mDataSet.
 */
public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder> implements TimePickerDialog.OnTimeSetListener {

    private static final String TAG = CalendarAdapter.class.getSimpleName();

    private static final int POSITION_UNSET = -1;

    private CalendarActivity calendarActivity;
    private Calendar today;
    private AlarmDataSource datasource;
    private Day changingDay;
    private int positionNextAlarm;

    // TODO Change time of ringing alarm

    // TODO Show not dismissed alarms from previous days

    // TODO Add menu item "Dismiss" in "near future" and when snoozed

    /**
     * Initialize the Adapter.
     */
    public CalendarAdapter(CalendarActivity calendarActivity) {
        datasource = new AlarmDataSource(calendarActivity);
        datasource.open();

        this.calendarActivity = calendarActivity;

        today = getToday();
        updatePositionNextAlarm(0);
    }

    /**
     * Create new views (invoked by the layout manager)
     */
    @Override
    public CalendarViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.calendar_row_item, viewGroup, false);

        return new CalendarViewHolder(v, calendarActivity.getFragmentManager(), this);
    }

    /**
     * Replaces the contents of a view (invoked by the layout manager)
     */
    @Override
    public void onBindViewHolder(CalendarViewHolder viewHolder, final int position) {
        Calendar date = addDays(today, position);

        Resources res = calendarActivity.getResources();

        int dayOfWeek = date.get(Calendar.DAY_OF_WEEK);
        String dayOfWeekText = Localization.dayOfWeekToString(dayOfWeek);
        viewHolder.getTextDayOfWeek().setText(dayOfWeekText);

        String dateText = Localization.dateToString(date.getTime());
        viewHolder.getTextDate().setText(dateText);

        Day day = datasource.loadDay(date);
        String timeText;
        if (day.isEnabled()) {
            timeText = Localization.timeToString(day.getHourX(), day.getMinuteX(), calendarActivity);
        } else {
            timeText = res.getString(R.string.alarm_unset);
        }
        viewHolder.getTextTime().setText(timeText);

        boolean enabled = true;
        if (position == 0) {
            GlobalManager globalManager = new GlobalManager(calendarActivity);
            if (globalManager.isValid()) {
                int state = globalManager.getState();

                if (state == GlobalManager.STATE_DISMISSED_BEFORE_RINGING || state == GlobalManager.STATE_DISMISSED) {
                    enabled = false;
                }
            } else {
                enabled = !day.isPassed();
            }
        }
        viewHolder.getTextTime().setEnabled(enabled);

        String stateText;
        if (position == 0) {
            GlobalManager globalManager = new GlobalManager(calendarActivity);
            if (globalManager.isValid()) {
                int state = globalManager.getState();

                if (state == GlobalManager.STATE_FUTURE) {
                    switch (day.getState()) {
                        case AlarmDataSource.DAY_STATE_DEFAULT:
                            stateText = "";
                            break;
                        default:
                            stateText = res.getString(R.string.alarm_state_changed);
                            break;
                    }
                } else if (state == GlobalManager.STATE_DISMISSED_BEFORE_RINGING) {
                    if (day.isPassed())
                        stateText = res.getString(R.string.alarm_state_passed);
                    else
                        stateText = res.getString(R.string.alarm_state_dismissed_before_ringing);
                } else if (state == GlobalManager.STATE_RINGING) {
                    stateText = res.getString(R.string.alarm_state_ringing);
                } else if (state == GlobalManager.STATE_SNOOZED) {
                    stateText = res.getString(R.string.alarm_state_snoozed);
                } else if (state == GlobalManager.STATE_DISMISSED) {
                    stateText = res.getString(R.string.alarm_state_passed);
                } else {
                    // This is generally an error, because the state should be properly set. However, when upgrading the app (and probably on boot), the activity may become visible BEFORE the receiver that sets the system alarm and state.
                    stateText = "";
                }
            } else {
                if (day.isPassed()) {
                    stateText = res.getString(R.string.alarm_state_passed);
                } else {
                    switch (day.getState()) {
                        case AlarmDataSource.DAY_STATE_DEFAULT:
                            stateText = "";
                            break;
                        default:
                            stateText = res.getString(R.string.alarm_state_changed);
                            break;
                    }
                }
            }
        } else {
            switch (day.getState()) {
                case AlarmDataSource.DAY_STATE_DEFAULT:
                    stateText = "";
                    break;
                default:
                    stateText = res.getString(R.string.alarm_state_changed);
                    break;
            }
        }
        viewHolder.getTextState().setText(stateText);

        String messageText;
        if (position == positionNextAlarm) {
            long diff = day.getTimeToRing();

            TimeDifference timeDifference = TimeDifference.getTimeUnits(diff);

            if (timeDifference.days > 0) {
                messageText = String.format(res.getString(R.string.time_to_ring_message_days), timeDifference.days, timeDifference.hours);
            } else if (timeDifference.hours > 0) {
                messageText = String.format(res.getString(R.string.time_to_ring_message_hours), timeDifference.hours, timeDifference.minutes);
            } else {
                messageText = String.format(res.getString(R.string.time_to_ring_message_minutes), timeDifference.minutes, timeDifference.seconds);
            }
        } else {
            messageText = "";
        }
        viewHolder.getTextComment().setText(messageText);

        viewHolder.setDay(day);
    }

    private void calcPositionNextAlarm(int initialPosition) {
        for (int position = initialPosition; position < AlarmDataSource.HORIZON_DAYS; position++) {

            Calendar date = addDays(today, position);

            Day day = datasource.loadDay(date);

            if (day.isEnabled()) {
                positionNextAlarm = position;
                return;
            }
        }
        positionNextAlarm = POSITION_UNSET;
        Log.d(TAG, "Next alarm is not displayed");
    }

    protected void updatePositionNextAlarm(int initialPosition) {
        int oldPositionNextAlarm = positionNextAlarm;
        calcPositionNextAlarm(initialPosition);

        if (oldPositionNextAlarm != positionNextAlarm) {
            Log.d(TAG, "Next alarm is at position " + positionNextAlarm);

            if (oldPositionNextAlarm != POSITION_UNSET)
                notifyItemChanged(oldPositionNextAlarm);
            if (positionNextAlarm != POSITION_UNSET)
                notifyItemChanged(positionNextAlarm);
        }
    }

    /**
     * Return the size of the dataset (invoked by the layout manager)
     */
    @Override
    public int getItemCount() {
        return AlarmDataSource.HORIZON_DAYS;
    }

    public void onResume() {
        Calendar today2 = getToday();

        if (!today.equals(today2)) {
            today = today2;
            notifyDataSetChanged();
        } else {
            notifyItemChanged(positionNextAlarm);
        }
    }

    public void onDestroy() {
        datasource.close();
    }

    public void onSystemTimeChange() {
        Log.v(TAG, "onSystemTimeChange()");

        // Update time to next alarm
        if (positionNextAlarm != POSITION_UNSET)
            notifyItemChanged(positionNextAlarm);

        // Shift items when date changes
        Calendar today2 = getToday();

        if (!today.equals(today2)) {
            int diffInDays = -1;
            for (int i = 1; i < AlarmDataSource.HORIZON_DAYS; i++) {
                Calendar date = addDays(today, i);
                if (today2.equals(date)) {
                    diffInDays = i;
                    break;
                }
            }

            today = today2;

            if (diffInDays != -1) {
                notifyItemRangeRemoved(0, diffInDays);
            } else {
                notifyDataSetChanged();
            }
        }
    }

    public void onTimeOrTimeZoneChange() {
        Log.d(TAG, "onTimeOrTimeZoneChange()");
        today = getToday();
        notifyDataSetChanged();
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        changingDay.setState(AlarmDataSource.DAY_STATE_ENABLED);
        changingDay.setHour(hourOfDay);
        changingDay.setMinute(minute);

        save(changingDay);
    }

    public void onLongClick() {
        changingDay.reverse();

        save(changingDay);
    }

    private void save(Day day) {
        datasource.saveDay(day);

        refresh();

        String toastText = formatToastText(day);
        Context context = calendarActivity.getBaseContext();
        Toast.makeText(context, toastText, Toast.LENGTH_LONG).show();
    }

    private void refresh() {
        updatePositionNextAlarm(0);

        Context context = calendarActivity.getBaseContext();
        SystemAlarm systemAlarm = SystemAlarm.getInstance(context);
        systemAlarm.setSystemAlarm();
    }

    private String formatToastText(Day day) {
        Resources res = calendarActivity.getResources();
        String toastText;

        if (!day.isEnabled()) {
            toastText = res.getString(R.string.time_to_ring_toast_off);
        } else {
            long diff = day.getTimeToRing();

            if (diff < 0) {
                toastText = res.getString(R.string.time_to_ring_toast_passed);
            } else {
                TimeDifference timeDifference = TimeDifference.getTimeUnits(diff);
                if (timeDifference.days > 0) {
                    toastText = String.format(res.getString(R.string.time_to_ring_toast_days), timeDifference.days, timeDifference.hours, timeDifference.minutes);
                } else if (timeDifference.hours > 0) {
                    toastText = String.format(res.getString(R.string.time_to_ring_toast_hours), timeDifference.hours, timeDifference.minutes);
                } else {
                    toastText = String.format(res.getString(R.string.time_to_ring_toast_minutes), timeDifference.minutes, timeDifference.seconds);
                }
            }
        }
        return toastText;
    }

    public static Calendar addDays(Calendar today, int numberOfDays) {
        Calendar date = (Calendar) today.clone();
        date.add(Calendar.DAY_OF_MONTH, numberOfDays);
        return date;
    }

    public static Calendar getToday() {
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        return today;
    }

    public void setChangingDay(Day changingDay) {
        this.changingDay = changingDay;
    }

    /**
     * Provide a reference to the type of views that you are using (custom ViewHolder)
     */
    public static class CalendarViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        private final FragmentManager fragmentManager;
        private final CalendarAdapter calendarAdapter;
        private final TextView textDayOfWeek;
        private final TextView textDate;
        private final TextView textTime;
        private final TextView textState;
        private final TextView textComment;
        private Day day;

        public CalendarViewHolder(View view, final FragmentManager fragmentManager, CalendarAdapter calendarAdapter) {
            super(view);

            this.fragmentManager = fragmentManager;
            this.calendarAdapter = calendarAdapter;

            textDayOfWeek = (TextView) view.findViewById(R.id.textDayOfWeekCal);
            textDate = (TextView) view.findViewById(R.id.textDate);
            textTime = (TextView) view.findViewById(R.id.textTimeCal);
            textState = (TextView) view.findViewById(R.id.textState);
            textComment = (TextView) view.findViewById(R.id.textComment);

            view.setOnClickListener(this);
            view.setOnLongClickListener(this);

        }

        public TextView getTextDayOfWeek() {
            return textDayOfWeek;
        }

        public TextView getTextDate() {
            return textDate;
        }

        public TextView getTextTime() {
            return textTime;
        }

        public TextView getTextState() {
            return textState;
        }

        public TextView getTextComment() {
            return textComment;
        }

        public void setDay(Day day) {
            this.day = day;
        }

        @Override
        public void onClick(View view) {
            calendarAdapter.setChangingDay(day);

            TimePickerFragment fragment = new TimePickerFragment();

            fragment.setOnTimeSetListener(calendarAdapter);

            // Preset time
            Bundle bundle = new Bundle();
            bundle.putInt(TimePickerFragment.HOURS, day.getHourX());
            bundle.putInt(TimePickerFragment.MINUTES, day.getMinuteX());
            fragment.setArguments(bundle);

            fragment.show(fragmentManager, "timePicker");
        }

        @Override
        public boolean onLongClick(View view) {
            calendarAdapter.setChangingDay(day);
            calendarAdapter.onLongClick();
            return true;
        }
    }

    private static class TimeDifference {
        long days;
        long hours;
        long minutes;
        long seconds;

        public static TimeDifference getTimeUnits(long diff) {
            TimeDifference timeDifference = new TimeDifference();

            long remaining = diff;
            long length;

            length = 24 * 60 * 60 * 1000;
            timeDifference.days = remaining / length;
            remaining = remaining % length;

            length = 60 * 60 * 1000;
            timeDifference.hours = remaining / length;
            remaining = remaining % length;

            length = 60 * 1000;
            timeDifference.minutes = remaining / length;
            remaining = remaining % length;

            length = 1000;
            timeDifference.seconds = remaining / length;

            return timeDifference;
        }
    }
}

