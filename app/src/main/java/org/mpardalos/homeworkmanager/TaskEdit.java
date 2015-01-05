package org.mpardalos.homeworkmanager;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Spinner;

import net.danlew.android.joda.JodaTimeAndroid;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Locale;


/**
 * This Activity is used to edit tasks that already exist, the user can also delete the task
 * being edited
 * When started with startActivityForResult
 */
public class TaskEdit extends TaskAdd {
    public static final int RESULT_DELETE_TASK = 4;
    public static final int RESULT_EDIT_TASK = 5;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        JodaTimeAndroid.init(this);
        Spinner subjectSpinner = (Spinner) findViewById(R.id.subject_input);
        ((Spinner) findViewById(R.id.subject_input)).setSelection(getIndex(subjectSpinner,
                                                                           getIntent()
                                                                                   .getStringExtra
                                                                                           (TaskDatabaseHelper.SUBJECT_NAME)));

        DateTimeFormatter df = DateTimeFormat.fullDate().withLocale(Locale.getDefault());
        EditText dueDateInput = (EditText) findViewById(R.id.due_date_input);

        dueDateInput.setText(((LocalDate) getIntent().getSerializableExtra(TaskDatabaseHelper
                                                                                   .DUE_DATE))
                                     .toString(df));
        dueDateInput.setTag(R.id.due_date_tag, getIntent().getSerializableExtra
                (TaskDatabaseHelper.DUE_DATE));

        ((EditText) findViewById(R.id.description_input)).setText(getIntent().getStringExtra
                (TaskDatabaseHelper.DESCRIPTION));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_task_edit, menu);
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
            case R.id.action_delete_task:
                setResultDeleteTask();
                finish();
                return true;
            case android.R.id.home:
                setResultFromInput(RESULT_EDIT_TASK);
                finish();
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    private void setResultDeleteTask() {
        Intent intent = new Intent();
        int _id = getIntent().getIntExtra("_id", -1);

        //Set the result code to RESULT_DELETE_TASK only if an _id was passed to the activity
        if (!(_id == -1)) {
            intent.putExtra("_id", _id);
            setResult(RESULT_DELETE_TASK, intent);
        } else {
            setResult(RESULT_CANCELED);
        }
    }
}
