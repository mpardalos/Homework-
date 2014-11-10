package mpardalos.org.homeworkmanager;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;


import java.util.Date;


public class TaskAdd extends Activity implements DatePickerFragment.onDateEnteredListener {
/*
    private static Date getDateFromDatePicker(DatePicker datePicker) {
        int day = datePicker.getDayOfMonth();
        int month = datePicker.getMonth();
        int year = datePicker.getYear();

        Calendar cal = Calendar.getInstance();
        cal.set(year, month, day);

        return cal.getTime();
    }
*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_add);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_task_add, menu);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    public void onDueDateClicked(View view) {
        DatePickerFragment dateInput = new DatePickerFragment();
        dateInput.show(getFragmentManager(), "dueDateInput");
    }

    public void onDateEntered(Date date) {
        EditText dateInput = (EditText) findViewById(R.id.due_date_input);
        java.text.DateFormat df = android.text.format.DateFormat.getDateFormat(this);
        dateInput.setText(df.format(date));
    }

}
