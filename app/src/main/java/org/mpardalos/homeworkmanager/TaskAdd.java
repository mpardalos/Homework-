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

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import net.danlew.android.joda.JodaTimeAndroid;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class TaskAdd extends AppCompatActivity implements DatePickerFragment.onDateEnteredListener {

    protected TaskDatabaseHelper mDatabase;
    protected File mPhotoFile;

    private static final int IMAGE_CAPTURE_REQUEST = 1;
    private static final String TASK_PHOTO_BITMAP = "image";
    private final String PHOTO_FILE_PATH = "photo_file";
    protected final Runnable loadImageToImageView = new Runnable() {
        @Override
        public void run() {
            final ImageView imageView = (ImageView) findViewById(R.id.image_preview);

            //Find the correct sample size

            BitmapFactory.Options findSampleSizeOptions = new BitmapFactory.Options();
            findSampleSizeOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(mPhotoFile.getAbsolutePath(), findSampleSizeOptions);

            final int sourceWidth = findSampleSizeOptions.outWidth;
            final int sourceHeight = findSampleSizeOptions.outHeight;
            final int targetWidth = imageView.getWidth(); // < 2048 ? imageView.getWidth() : 2048;
            final int targetHeight = imageView.getHeight();// < 2048 ? imageView.getHeight() : 2048;


            // Set it to 1. If the source size is ok then just use 1. If that is not good set it to 2.
            // If that is not good enough still then keep doubling it until it is.
            // (BitmapFactory.Options.inSampleSize has to be a power of 2)
            int sampleSize = 1;
            if (sourceHeight > targetHeight || sourceWidth > targetWidth) {
                sampleSize = 2;
                while ((sourceWidth / sampleSize) > targetWidth || (sourceHeight / sampleSize) > targetHeight) {
                    sampleSize *= 2;
                }
            }

            final BitmapFactory.Options loadImageOptions = new BitmapFactory.Options();
            loadImageOptions.inSampleSize = sampleSize;
            final Bitmap image = BitmapFactory.decodeFile(mPhotoFile.getAbsolutePath(), loadImageOptions);

            imageView.post(new Runnable() {
                @Override
                public void run() {
                    imageView.setImageBitmap(image);
                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        JodaTimeAndroid.init(this);
        setContentView(R.layout.add_or_edit_task);
        // Restore the image if it was saved
        if (savedInstanceState != null) {
            this.mPhotoFile = new File(savedInstanceState.getString(PHOTO_FILE_PATH));
            ((ImageView) findViewById(R.id.image_preview)).setImageBitmap((Bitmap) savedInstanceState.getParcelable(TASK_PHOTO_BITMAP));
        }

        this.mDatabase = new TaskDatabaseHelper(this);

        //Populate subject selection spinner
        Spinner subjectSpinner = (Spinner) findViewById(R.id.subject_input);
        ArrayList<String> subjects = mDatabase.getSubjects();

        ArrayAdapter<String> subjectAdapter =
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, subjects);
        subjectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        subjectSpinner.setAdapter(subjectAdapter);

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
                if (findViewById(R.id.due_date_input).getTag(R.id.due_date) == null) {
                    Toast.makeText(this, R.string.enter_due_date_toast, Toast.LENGTH_LONG).show();
                    return true;
                } else if (((Spinner) findViewById(R.id.subject_input)).getSelectedView() == null) {
                    Toast.makeText(this, R.string.no_subject_selected, Toast.LENGTH_LONG).show();
                    return true;
                }
                setResultFromInput(RESULT_OK);
                finish();
                return true;

            case R.id.add_photo:
                if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
                    Toast.makeText(this, R.string.camera_unavailable, Toast.LENGTH_SHORT).show();
                    return true;
                }
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    // If there was a photo file previously try to delete it and log if it wasn't possible
                    if (mPhotoFile != null) {
                        if (!mPhotoFile.delete()) {
                            Log.w("Old Photo", "Could not delete old photo");
                        }
                    }

                    try {
                        mPhotoFile = createPhotoFile();
                    } catch (IOException e) {
                        Toast.makeText(this, R.string.cannot_write_photo_to_disk, Toast.LENGTH_SHORT).show();
                    }

                    if (mPhotoFile != null) {
                        Log.i("Photo Path", mPhotoFile.getAbsolutePath());
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mPhotoFile));
                        startActivityForResult(takePictureIntent, IMAGE_CAPTURE_REQUEST);
                    }
                }

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == IMAGE_CAPTURE_REQUEST && resultCode == RESULT_OK) {
            new Thread(loadImageToImageView).run();
        }
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
        String subject = ((TextView) ((Spinner) findViewById(R.id.subject_input))
                .getSelectedView().findViewById(android.R.id.text1)).getText().toString();
        String description = ((EditText) findViewById(R.id.description_input)).getText()
                .toString();

        Log.i("Task to be added: ", "Subject: " + subject);
        Log.i("Task to be added: ", "Due Date: " + dueDate);
        Log.i("Task to be added: ", "Description: " + description);

        Intent result = new Intent();
        result.putExtra("task", new Task(subject, description, dueDate, Task.NO_DATABASE_ID, mPhotoFile));
        setResult(result_code, result);
        return true;
    }

    private File createPhotoFile() throws IOException {
        File directory = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        return new File(directory, timeStamp + ".jpg");
    }

    public void onPhotoClick(View v) {
        if (mPhotoFile != null) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(mPhotoFile), "image/*");
            startActivity(intent);
        }
    }

    @Override
    protected void onSaveInstanceState (Bundle outState) {
        Drawable drawable = ((ImageView) findViewById(R.id.image_preview)).getDrawable();
        if (drawable != null) {
            outState.putString(PHOTO_FILE_PATH, mPhotoFile.getAbsolutePath());
            outState.putParcelable(TASK_PHOTO_BITMAP, ((BitmapDrawable) drawable).getBitmap() );
        }
        super.onSaveInstanceState(outState);
    }
}
