package cz.jaro.alarmmorning;

import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Calendar;

import cz.jaro.alarmmorning.model.AppAlarm;
import cz.jaro.alarmmorning.model.Day;
import cz.jaro.alarmmorning.model.OneTimeAlarm;

/**
 * Provide views to RecyclerView with data from mDataSet.
 */
public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder> {

    private static final String TAG = CalendarAdapter.class.getSimpleName();

    private final CalendarFragment fragment;

    /**
     * Initialize the Adapter.
     *
     * @param fragment Fragment that contains the widget that uses this adapter
     */
    public CalendarAdapter(CalendarFragment fragment) {
        this.fragment = fragment;
    }

    /**
     * Create new views (invoked by the layout manager)
     */
    @Override
    public CalendarViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.calendar_row_item, viewGroup, false);

        view.setOnClickListener(fragment);

        return new CalendarViewHolder(view);
    }

    /**
     * Replaces the contents of a view (invoked by the layout manager)
     */
    @Override
    public void onBindViewHolder(CalendarViewHolder viewHolder, final int position) {
        Log.v(TAG, "onBindViewHolder(position=" + position);

        AppAlarm appAlarm = fragment.loadPosition(position);
        Day day = null;
        OneTimeAlarm oneTimeAlarm = null;
        if (appAlarm instanceof Day) {
            day = (Day) appAlarm;
        } else if (appAlarm instanceof OneTimeAlarm) {
            oneTimeAlarm = (OneTimeAlarm) appAlarm;
        } else {
            throw new IllegalArgumentException("Unexpected class " + appAlarm.getClass());
        }

        Calendar date = appAlarm.getDateTime();
        int dayOfWeek = date.get(Calendar.DAY_OF_WEEK);
        Resources res = fragment.getResources();

        String dayOfWeekText;
        if (appAlarm instanceof Day) {
            dayOfWeekText = Localization.dayOfWeekToStringShort(res, dayOfWeek);
        } else {
            dayOfWeekText = "";
        }
        viewHolder.getTextDayOfWeek().setText(dayOfWeekText);

        String dateText;
        if (appAlarm instanceof Day) {
            dateText = Localization.dateToStringVeryShort(res, date.getTime());
        } else {
            dateText = oneTimeAlarm.getName() != null ? oneTimeAlarm.getName() : "";
        }
        viewHolder.getTextDate().setText(dateText);

        int backgroundColor;
        com.ibm.icu.util.Calendar c = com.ibm.icu.util.Calendar.getInstance();
        int dayOfWeekType = c.getDayOfWeekType(dayOfWeek);
        switch (dayOfWeekType) {
            case com.ibm.icu.util.Calendar.WEEKEND:
                backgroundColor = res.getColor(R.color.weekend);
                break;

            default:
                backgroundColor = res.getColor(R.color.primary_dark);
        }
        viewHolder.getTextDayOfWeek().setBackgroundColor(backgroundColor);
        viewHolder.getTextDate().setBackgroundColor(backgroundColor);

        String timeText;
        if (appAlarm instanceof Day) {
            if (day.isEnabled()) {
                timeText = Localization.timeToString(day.getHourX(), day.getMinuteX(), fragment.getActivity());
            } else {
                timeText = res.getString(R.string.alarm_unset);
            }
        } else {
            timeText = Localization.timeToString(oneTimeAlarm.getHour(), oneTimeAlarm.getMinute(), fragment.getActivity());
        }
        viewHolder.getTextTime().setText(timeText);

        GlobalManager globalManager = GlobalManager.getInstance();
        int state = globalManager.getState(appAlarm.getDateTime());

        boolean enabled;
        enabled = (appAlarm instanceof Day && !day.isEnabled()) || (state != GlobalManager.STATE_DISMISSED_BEFORE_RINGING && state != GlobalManager.STATE_DISMISSED);
        viewHolder.getTextTime().setEnabled(enabled);

        String stateText;
        if (appAlarm instanceof Day && day.isHoliday() && day.getState() == Day.STATE_RULE) {
            stateText = res.getString(R.string.holiday);
        } else if (appAlarm instanceof Day && !day.isEnabled()) {
            stateText = day.sameAsDefault() && !day.isHoliday() ? "" : res.getString(R.string.alarm_state_changed);
        } else {
            if (state == GlobalManager.STATE_FUTURE) {
                if (appAlarm instanceof Day)
                    stateText = day.sameAsDefault() && !day.isHoliday() ? "" : res.getString(R.string.alarm_state_changed);
                else
                    stateText = "";
            } else if (state == GlobalManager.STATE_DISMISSED_BEFORE_RINGING) {
                if (appAlarm.isPassed(fragment.clock()))
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
                throw new IllegalArgumentException("Unexpected argument " + state);
            }
        }
        viewHolder.getTextState().setText(stateText);

        String messageText;
        if (appAlarm instanceof Day && day.isHoliday() && day.getState() == Day.STATE_RULE) {
            messageText = day.getHolidayDescription();
        } else {
            if (fragment.isPositionWithNextAlarm(position)) {
                long diff = appAlarm.getTimeToRing(fragment.clock());

                TimeDifference timeDifference = TimeDifference.split(diff);

                if (timeDifference.days > 0) {
                    messageText = res.getString(R.string.time_to_ring_message_days, timeDifference.days, timeDifference.hours);
                } else if (timeDifference.hours > 0) {
                    messageText = res.getString(R.string.time_to_ring_message_hours, timeDifference.hours, timeDifference.minutes);
                } else {
                    messageText = res.getString(R.string.time_to_ring_message_minutes, timeDifference.minutes, timeDifference.seconds);
                }
            } else {
                messageText = "";
            }
        }
        viewHolder.getTextComment().setText(messageText);
    }

    /**
     * Return the size of the dataset (invoked by the layout manager)
     */
    @Override
    public int getItemCount() {
        return fragment.getItemCount();
    }

    /**
     * Provide a reference to the type of views that you are using (custom ViewHolder)
     */
    public static class CalendarViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener {
        private final TextView textDayOfWeek;
        private final TextView textDate;
        private final TextView textTime;
        private final TextView textState;
        private final TextView textComment;

        public CalendarViewHolder(View view) {
            super(view);

            textDayOfWeek = (TextView) view.findViewById(R.id.textDayOfWeekCal);
            textDate = (TextView) view.findViewById(R.id.textDate);
            textTime = (TextView) view.findViewById(R.id.textTimeCal);
            textState = (TextView) view.findViewById(R.id.textState);
            textComment = (TextView) view.findViewById(R.id.textComment);

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

        @Override
        public boolean onLongClick(View view) {
            itemView.showContextMenu();
            return true;
        }
    }


    static class TimeDifference {
        long days;
        long hours;
        long minutes;
        long seconds;

        public static TimeDifference split(long diff) {
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