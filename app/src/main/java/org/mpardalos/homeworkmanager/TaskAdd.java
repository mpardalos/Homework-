package org.mpardalos.homeworkmanager;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import net.danlew.android.joda.JodaTimeAndroid;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class TaskAdd extends ActionBarActivity implements DatePickerFragment.onDateEnteredListener {

    protected TaskDatabaseHelper mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        JodaTimeAndroid.init(this);
        setContentView(R.layout.activity_task_add_or_edit);
        this.mDatabase = new TaskDatabaseHelper(this);

        //Populate subject selection spinner
        Spinner subjectSpinner = (Spinner) findViewById(R.id.subject_input);
        Cursor subjectCursor = mDatabase.getSubjects();
        List<String> subjects = new ArrayList<>();
        subjectCursor.moveToPosition(-1);
        /* points the cursor to the next entry and also
        stops the loop if we reached the last element */
        while (subjectCursor.moveToNext()) {
            //Gets the subject that the cursor is currently pointing to
            subjects.add(subjectCursor.getString(1));
        }


        ArrayAdapter<String> subjectAdapter =
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, subjects);
        subjectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        subjectSpinner.setAdapter(subjectAdapter);

        //auto-complete subject based on time
        try {
            subjectSpinner.setSelection(getIndex(subjectSpinner, mDatabase.getSubjectAtDateTime
                    (DateTime.now())));
        } catch (IllegalArgumentException e) {
            Log.d("Subject not set: ", e.getMessage());
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.action_bar);
        setSupportActionBar(toolbar);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_task_add, menu);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            //Doesn't go back if setResultFromInput is false
            case android.R.id.home:
                boolean inputComplete = setResultFromInput(RESULT_OK);
                if (inputComplete) {
                    finish();
                } else {
                    Toast.makeText(this, R.string.enter_due_date_toast, Toast.LENGTH_LONG).show();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        finish();
    }

    public void onDueDateClicked(View view) {
        DatePickerFragment dateInput = new DatePickerFragment();
        dateInput.show(getFragmentManager(), "dueDateInput");
    }

    public void onDateEntered(LocalDate date) {
        EditText dateInput = (EditText) findViewById(R.id.due_date_input);
        DateTimeFormatter df = DateTimeFormat.fullDate().withLocale(Locale.getDefault());
        dateInput.setText(date.toString(df));
        dateInput.setTag(R.id.due_date, date);
    }

    //Thanks to @Akhil Jain at from stackoverflow for this method
    protected int getIndex(Spinner spinner, String myString) {
        int index = 0;

        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(myString)) {
                index = i;
                i = spinner.getCount();//will stop the loop, kind of break,
                // by making condition false
            }
        }
        return index;
    }

    protected boolean setResultFromInput(int result_code) {
        String subject = ((TextView) ((Spinner) findViewById(R.id.subject_input))
                .getSelectedView().findViewById(android.R.id.text1)).getText().toString();
        LocalDate dueDate = (LocalDate) findViewById(R.id.due_date_input).getTag(R.id.due_date);
        String description = ((EditText) findViewById(R.id.description_input)).getText()
                .toString();

        Log.i("Task to be added: ", "Subject: " + subject);
        Log.i("Task to be added: ", "Due Date: " + dueDate);
        Log.i("Task to be added: ", "Description: " + description);

        Intent result = new Intent();

        result.putExtra("_id", getIntent().getIntExtra("_id", -1));//The _id will never be -1
        result.putExtra("subject", subject);
        result.putExtra("dueDate", dueDate);
        result.putExtra("description", description);
        setResult(result_code, result);
        return dueDate != null;
    }
}
