package mpardalos.org.homeworkmanager;

import android.app.ActionBar;
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
import android.widget.TextView;
import android.widget.Toast;

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
        subjectSpinner.setAdapter(subjectAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_task_add, menu);
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            //Doesn't go back if the return value was false
            case android.R.id.home:
                if (!onBackPressed(true)) {
                    return true;
                }
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
        //<AWESOME>When a methods needs the date instead of parsing the text it can get this
        // tag</AWESOME>
        dateInput.setTag(R.id.due_date_tag, date);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    /**
     * @param addNewTask whether to add a new task (if true)
     *                   or just do the usual @onBackPressed (if False)
     * @return true if the required input was found and was able to insert entry to the database
     * false otherwise
     * also, returns true if @addNewTask was false (which would mean that it didn't try to insert
     * a new value)
     */
    public boolean onBackPressed(boolean addNewTask) {
        if (addNewTask) {
            String subject = ((TextView) ((Spinner) findViewById(R.id.subject_input))
                    .getSelectedView().findViewById(android.R.id.text1)).getText().toString();
            Date dueDate = (Date) findViewById(R.id.due_date_input).getTag(R.id.due_date_tag);
            String description = ((EditText) findViewById(R.id.description_input)).getText()
                    .toString();

            Log.i("Task to be added: ", "Subject: " + subject);
            Log.i("Task to be added: ", "Due Date: " + dueDate);
            Log.i("Task to be added: ", "Description: " + description);

            if (dueDate == null) {
                Toast.makeText(this, "Enter due date or use the back button to discard task",
                               Toast.LENGTH_LONG).show();
                return false;
            } else {
                mDatabase.insertTask(description, dueDate, subject);
            }
        }
        return true;
    }
}
//}
