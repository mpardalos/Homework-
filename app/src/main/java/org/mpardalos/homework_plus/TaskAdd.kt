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

package org.mpardalos.homework_plus

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import net.danlew.android.joda.JodaTimeAndroid
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


open class TaskAdd : AppCompatActivity(), DatePickerFragment.onDateEnteredListener {

    protected var mPhotoFile: File? = null
    private val PHOTO_FILE_PATH = "photo_file"

    private var mDatabase: TaskDatabaseHelper = TaskDatabaseHelper(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        JodaTimeAndroid.init(this)
        setContentView(R.layout.add_or_edit_task)
        // Restore the image if it was saved
        val pathString = savedInstanceState?.getString(PHOTO_FILE_PATH)
        if (pathString != null) {
            mPhotoFile = File(pathString)
            findViewById(R.id.image_heading).visibility = View.VISIBLE
            loadImageToImageView(mPhotoFile!!)

        }


        //Populate subject selection spinner
        val subjectSpinner = findViewById(R.id.subject_input) as Spinner
        val subjects = mDatabase.subjects

        val subjectAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, subjects)
        subjectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        subjectSpinner.adapter = subjectAdapter
        val toolbar = findViewById(R.id.action_bar) as Toolbar
        setSupportActionBar(toolbar)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.task_add, menu)
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        when (id) {
        //Doesn't go back if setResultFromInput is false
            android.R.id.home -> {
                if (findViewById(R.id.due_date_input).getTag(R.id.due_date) == null) {
                    Toast.makeText(this, R.string.enter_due_date_toast, Toast.LENGTH_LONG).show()
                    return true
                } else if ((findViewById(R.id.subject_input) as Spinner).selectedView == null) {
                    Toast.makeText(this, R.string.no_subject_selected, Toast.LENGTH_LONG).show()
                    return true
                }
                setResultFromInput(Activity.RESULT_OK)
                finish()
                return true
            }

            R.id.add_photo -> {
                if (!packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
                    Toast.makeText(this, R.string.camera_unavailable, Toast.LENGTH_SHORT).show()
                    return true
                }
                val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                if (takePictureIntent.resolveActivity(packageManager) != null) {
                    // If there was a photo file previously try to delete it and log if it wasn't possible
                    if (mPhotoFile != null) {
                        if (!mPhotoFile!!.delete()) {
                            Log.w("Old Photo", "Could not delete old photo")
                        }
                    }

                    mPhotoFile = createPhotoFile()

                    if (mPhotoFile != null) {
                        Log.i("Photo Path", mPhotoFile!!.absolutePath)
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mPhotoFile))
                        startActivityForResult(takePictureIntent, IMAGE_CAPTURE_REQUEST)
                    } else {
                        Toast.makeText(this, R.string.cannot_write_photo_to_disk, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_CANCELED)
        finish()
    }

    @SuppressWarnings("unused")
    fun onDueDateClicked(view: View) {
        val dateInput = DatePickerFragment()
        val previousInput = view.getTag(R.id.due_date) as LocalDate?
        if (previousInput != null) {
            val args = Bundle()
            args.putSerializable("previousInput", previousInput)
            dateInput.arguments = args
        } else {
            val args = Bundle()
            args.putSerializable("previousInput", LocalDate.now())
            dateInput.arguments = args
        }
        dateInput.show(fragmentManager, "dueDateInput")
    }

    override fun onDateEntered(date: LocalDate) {
        val dateInput = findViewById(R.id.due_date_input) as EditText
        val df = DateTimeFormat.fullDate().withLocale(Locale.getDefault())
        dateInput.setText(date.toString(df))
        dateInput.setTag(R.id.due_date, date)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (requestCode == IMAGE_CAPTURE_REQUEST && resultCode == Activity.RESULT_OK) {
            findViewById(R.id.image_heading).visibility = View.VISIBLE
            loadImageToImageView(mPhotoFile as File)
        }
    }

    //Thanks to @Akhil Jain at from stackoverflow for this method
    protected fun getIndex(spinner: Spinner, myString: String): Int {
        var index = 0

        var i = 0
        while (i < spinner.count) {
            if (spinner.getItemAtPosition(i).toString().equals(myString, ignoreCase = true)) {
                index = i
                i = spinner.count//will stop the loop, kind of break,
                // by making condition false
            }
            i++
        }
        return index
    }

    /**
     * @param result_code the result code to be passed with the result. Used for subclasses of this
     * *                    activity
     * *
     * @return Whether the result was set. This implementation always returns true but the implementation in TaskEdit
     * *         returns either true or false
     */
    protected open fun setResultFromInput(result_code: Int): Boolean {
        val dueDate = findViewById(R.id.due_date_input).getTag(R.id.due_date) as LocalDate
        val subject = ((findViewById(R.id.subject_input) as Spinner).selectedView.findViewById(android.R.id.text1) as TextView).text.toString()
        val description = (findViewById(R.id.description_input) as EditText).text.toString()

        Log.i("Task to be added: ", "Subject: " + subject)
        Log.i("Task to be added: ", "Due Date: " + dueDate)
        Log.i("Task to be added: ", "Description: " + description)

        val result = Intent()
        result.putExtra("task", Task(subject, description, dueDate, Task.NO_DATABASE_ID, mPhotoFile))
        setResult(result_code, result)
        return true
    }

    private fun createPhotoFile(): File {
        val directory = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        @SuppressLint("SimpleDateFormat") val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        return File(directory, timeStamp + ".jpg")
    }

    @SuppressWarnings("unused")
    fun onPhotoClick(v: View) {
        if (mPhotoFile != null) {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(Uri.fromFile(mPhotoFile), "image/*")
            startActivity(intent)
        }
    }

    /**
     * Load an image from a file to the ImageView in the activity (R.id.image_preview) using a separate thread.

     * @param photoFile: The file from which to load the photo
     */
    protected fun loadImageToImageView(photoFile: File) {
        val loadImageToImageView = Runnable {
            val imageView = findViewById(R.id.image_preview) as ImageView

            //Find the correct sample size
            val findSampleSizeOptions = BitmapFactory.Options()
            findSampleSizeOptions.inJustDecodeBounds = true
            BitmapFactory.decodeFile(photoFile.absolutePath, findSampleSizeOptions)

            val sourceWidth = findSampleSizeOptions.outWidth
            val sourceHeight = findSampleSizeOptions.outHeight
            val targetWidth = Math.max(imageView.width, 2048)
            val targetHeight = Math.max(imageView.height, 2048)


            // Set it to 1. If the source size is ok then just use 1. If that is not good set it to 2.
            // If that is not good enough still then keep doubling it until it is.
            // (BitmapFactory.Options.inSampleSize has to be a power of 2)
            var sampleSize = 1
            if (sourceHeight > targetHeight || sourceWidth > targetWidth) {
                sampleSize = 2
                while (sourceWidth / sampleSize > targetWidth || sourceHeight / sampleSize > targetHeight) {
                    sampleSize *= 2
                }
            }

            val loadImageOptions = BitmapFactory.Options()
            loadImageOptions.inSampleSize = sampleSize
            val image = BitmapFactory.decodeFile(photoFile.absolutePath, loadImageOptions)

            imageView.post { imageView.setImageBitmap(image) }
        }
        Thread(loadImageToImageView).run()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        // Save the location of the Photo if it exists. onSaveInstanceState saved only view data, so we need to keep the
        // location of the photo
        if (mPhotoFile != null) {
            outState.putString(PHOTO_FILE_PATH, mPhotoFile!!.absolutePath)
        }
        super.onSaveInstanceState(outState)
    }

    companion object {

        private val IMAGE_CAPTURE_REQUEST = 1
    }
}