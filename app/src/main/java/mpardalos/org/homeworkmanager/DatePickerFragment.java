package mpardalos.org.homeworkmanager;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.widget.DatePicker;

import net.danlew.android.joda.JodaTimeAndroid;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;


public class DatePickerFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {

    onDateEnteredListener parent;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        JodaTimeAndroid.init(this.getActivity());

        DateTime currentDate = DateTime.now();
        int defaultYear = currentDate.getYear();
        int defaultMonth = currentDate.getMonthOfYear();
        int defaultDay = currentDate.getDayOfMonth();

        return new DatePickerDialog(getActivity(), this, defaultYear, defaultMonth, defaultDay);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            parent = (onDateEnteredListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                                                 + "must implement onUserFinished");
        }
    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
        parent.onDateEntered(new LocalDate(year, month, day));
    }

    public interface onDateEnteredListener {
        public void onDateEntered(LocalDate date);
    }
}
