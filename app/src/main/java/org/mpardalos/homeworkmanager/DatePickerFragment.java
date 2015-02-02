package org.mpardalos.homeworkmanager;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.widget.DatePicker;

import net.danlew.android.joda.JodaTimeAndroid;

import org.joda.time.LocalDate;


public class DatePickerFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {

    onDateEnteredListener parent;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        JodaTimeAndroid.init(this.getActivity());

        LocalDate previousInput = (LocalDate) getArguments().getSerializable("previousInput");

        int defaultYear = previousInput.getYear();
        int defaultMonth = previousInput.getMonthOfYear() - 1; //JodaTime uses 0-11,
        // android uses 1-12
        int defaultDay = previousInput.getDayOfMonth();

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
        parent.onDateEntered(new LocalDate(year, month + 1, day));
        //month + 1 because it has to be from 1-12 for joda and android uses 0-11
    }

    public interface onDateEnteredListener {
        public void onDateEntered(LocalDate date);
    }
}
