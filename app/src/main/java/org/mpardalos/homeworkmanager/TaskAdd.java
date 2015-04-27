package org.mpardalos.homeworkmanager;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
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
        setContentView(R.layout.add_or_edit_task);
        this.mDatabase = new TaskDatabaseHelper(this);

        //Populate subject selection spinner
        Spinner subjectSpinner = (Spinner) findViewById(R.id.subject_input);
        ArrayList<String> subjects = mDatabase.getSubjects();

        ArrayAdapter<String> subjectAdapter =
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, subjects);
        subjectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        subjectSpinner.setAdapter(subjectAdapter);

        autocompleteSubject(subjectSpinner);

        //Auto-complete dueDate based on current subject and its next occurrence
        subjectSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String subject = null;
                if (view != null) {
                    subject = (String) ((TextView) view.findViewById(android.R.id.text1))
                            .getText();
                }

                if (subject != null) {
                    LocalDate dateIterator = LocalDate.now();
                    //Iterate on every day starting from tomorrow until 7 days from today.
                    //(I think a week=7 days everywhere but this should be checked)
                    for (int i = 1; i <= 7; i++) {
                        dateIterator = dateIterator.plusDays(1);
                        List<String> subjectsInDay = mDatabase.getSubjectsInDay(dateIterator
                                                                                        .dayOfWeek()
                                                                                        .getAsText()
                                                                                        .toLowerCase());
                        if (subjectsInDay.contains(subject)) {
                            onDateEntered(dateIterator);
                            break;
                        }
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        Toolbar toolbar = (Toolbar) findViewById(R.id.action_bar);
        setSupportActionBar(toolbar);
    }

    private void autocompleteSubject(Spinner subjectSpinner) {
        //auto-complete subject based on time
        String currentSubject;
        try {
            currentSubject = mDatabase.getSubjectAtDateTime(DateTime.now());
            subjectSpinner.setSelection(getIndex(subjectSpinner, currentSubject));
        } catch (IllegalArgumentException e) {
            Log.d("Subject not set: ", e.getMessage());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.task_add, menu);
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
        LocalDate previousInput = (LocalDate) view.getTag(R.id.due_date);
        if (!(previousInput == null)) {
            Bundle args = new Bundle();
            args.putSerializable("previousInput", previousInput);
            dateInput.setArguments(args);
        } else {
            Bundle args = new Bundle();
            args.putSerializable("previousInput", LocalDate.now());
            dateInput.setArguments(args);
        }
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

    /**
     * @param result_code the result code to be passed with the result. Used for subclasses of this
     *                    activity
     * @return Whether the result was set.
     */
    protected boolean setResultFromInput(int result_code) {
        LocalDate dueDate = (LocalDate) findViewById(R.id.due_date_input).getTag(R.id.due_date);
        //Used by onOptionsItemSelected t show the "please enter due_
        if (dueDate == null) {
            return false;
        }
        String subject = ((TextView) ((Spinner) findViewById(R.id.subject_input))
                .getSelectedView().findViewById(android.R.id.text1)).getText().toString();
        String description = ((EditText) findViewById(R.id.description_input)).getText()
                .toString();

        Log.i("Task to be added: ", "Subject: " + subject);
        Log.i("Task to be added: ", "Due Date: " + dueDate);
        Log.i("Task to be added: ", "Description: " + description);

        Intent result = new Intent();
        result.putExtra(
                "task",
                new Task(subject, description, dueDate, false));
        setResult(result_code, result);
        return true;
    }
}
