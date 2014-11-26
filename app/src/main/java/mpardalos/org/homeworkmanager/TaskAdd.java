package mpardalos.org.homeworkmanager;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class TaskAdd extends Activity implements DatePickerFragment.onDateEnteredListener {

    private TaskDatabaseHelper mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_add);
        this.mDatabase = new TaskDatabaseHelper(this);

        //Populate subject selection spinner
        Spinner subjectSpinner = (Spinner) findViewById(R.id.subject_input);

        Cursor subjectCursor = mDatabase.getSubjects();
        List<String> subjects = new ArrayList<String>();
        if (subjectCursor.moveToFirst()) {
            do {
                //Gets the subject that the cursor is currently pointing to
                subjects.add(subjectCursor.getString(1));
            }
            /* points the cursor to the next entry and also
            stops the loop if we reached the last element */
            while (subjectCursor.moveToNext());
        }

        ArrayAdapter<String> subjectAdapter =
                new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, subjects);

        subjectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        Log.d("Subjects: ", subjects.toString());
        subjectSpinner.setAdapter(subjectAdapter);
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


}
