/*
 * Copyright (C) 2015 Michalis Pardalos
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mpardalos.homeworkmanager;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

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

        Task task = getIntent().getParcelableExtra("task");

        Spinner subjectSpinner = (Spinner) findViewById(R.id.subject_input);
        ((Spinner) findViewById(R.id.subject_input)).setSelection(getIndex(subjectSpinner, task.getSubject()));

        DateTimeFormatter df = DateTimeFormat.fullDate().withLocale(Locale.getDefault());
        EditText dueDateInput = (EditText) findViewById(R.id.due_date_input);

        dueDateInput.setText(task.getDueDate().toString(df));
        dueDateInput.setTag(R.id.due_date, task.getDueDate());

        ((EditText) findViewById(R.id.description_input)).setText(task.getDescription());

        //mPhotoFile has to be set before running loadImageToView
        mPhotoFile = task.getPhotoFile();
        //Put it in listener because it has to be called after views have been sized
        findViewById(android.R.id.content).getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                new Thread(loadImageToImageView).run();
                return true;
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.task_edit, menu);
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
                setResultFromInput(RESULT_DELETE_TASK);
                finish();
                return true;
            case android.R.id.home:
                setResultFromInput(RESULT_EDIT_TASK);
                finish();
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected boolean setResultFromInput(int result_code) {
        LocalDate dueDate = (LocalDate) findViewById(R.id.due_date_input).getTag(R.id.due_date);
        if (dueDate == null) {
            return false;
        }
        String subject = ((TextView) ((Spinner) findViewById(R.id.subject_input))
                .getSelectedView().findViewById(android.R.id.text1)).getText().toString();
        String description = ((EditText) findViewById(R.id.description_input)).getText()
                .toString();
        int databaseId = ((Task) getIntent().getParcelableExtra("task")).getDatabaseId();

        Log.i("Task to be added: ", "Subject: " + subject);
        Log.i("Task to be added: ", "Due Date: " + dueDate);
        Log.i("Task to be added: ", "Description: " + description);
        String photoPath;
        if (mPhotoFile != null) {
            photoPath = mPhotoFile.getAbsolutePath();
        } else {
            photoPath = "None";
        }
        Log.i("Task to be added: ", "Photo path: " + photoPath);

        Intent result = new Intent();
        result.putExtra("task", new Task(subject, description, dueDate, databaseId, mPhotoFile));
        setResult(result_code, result);
        return true;
    }
}
