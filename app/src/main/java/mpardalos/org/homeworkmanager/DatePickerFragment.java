package mpardalos.org.homeworkmanager;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.widget.DatePicker;

import java.util.Calendar;
import java.util.Date;


public class DatePickerFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {

    onDateEnteredListener parent;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        final Calendar currentDate = Calendar.getInstance();
        int defaultYear = currentDate.get(Calendar.YEAR);
        int defaultMonth = currentDate.get(Calendar.MONTH);
        int defaultDay = currentDate.get(Calendar.DAY_OF_MONTH);

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
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, day);
        parent.onDateEntered(cal.getTime());
    }

    public interface onDateEnteredListener {
        public void onDateEntered(Date date);
    }
}
