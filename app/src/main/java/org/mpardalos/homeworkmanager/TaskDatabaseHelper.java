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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class TaskDatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "data.db";
    private static final int DB_VERSION = 3;

    public static final String SUBJECT_NAME = "SubjName";
    private static final String TASKS_TABLE = "Tasks";
    private static final String SUBJECTS_TABLE = "Subjects";
    private static final String TIMETABLE = "TimeTable";
    private static final String TASK_DONE = "Done";
    private static final String SUBJECT_ID = "SubjectId";
    private static final String TASK_DESCRIPTION = "TaskDescr";
    private static final String DUE_DATE = "DueDate";
    private static final String DAY_OF_WEEK = "WeekDay";
    private static final String TASK_PHOTO_LOCATION = "TaskPhoto";

    private static final String createSubjects = "CREATE TABLE " + SUBJECTS_TABLE +
            " (" +
            "_id INTEGER PRIMARY KEY NOT NULL, " +
            SUBJECT_NAME + " TEXT NOT NULL" +
            ");";

    private static final String createTasks = "CREATE TABLE " + TASKS_TABLE +
            "(" +
            "_id INTEGER PRIMARY KEY NOT NULL, " +
            TASK_DESCRIPTION + " TEXT," +
            TASK_PHOTO_LOCATION + " TEXT, " +
            DUE_DATE + " TEXT, " +
            TASK_DONE + " INTEGER NOT NULL, " +
            SUBJECT_ID + " INTEGER NOT NULL, " +
            "FOREIGN KEY (" + SUBJECT_ID + ") REFERENCES " + SUBJECTS_TABLE + "(_id) DEFERRABLE INITIALLY DEFERRED" +
            ");";

    private static final String createTimeTable = "CREATE TABLE " + TIMETABLE +
            "(" +
            "_id INTEGER PRIMARY KEY NOT NULL, " +
            DAY_OF_WEEK + " TEXT NOT NULL, " +
            SUBJECT_ID + " INTEGER NOT NULL, " +
            "FOREIGN KEY (" + SUBJECT_ID + ") REFERENCES " + SUBJECTS_TABLE + " (_id) DEFERRABLE INITIALLY DEFERRED" +
            ");";


    private static HashMap<String, Integer> subjectIdMap;
    /**
     * Set to true whenever modifying tasks in the DB so that subjectIdMap is then rebuilt
     */
    private static boolean subjectsChanged;

    private final Context mContext;

    public void onCreate(SQLiteDatabase db) {
        Log.d("create db", createSubjects);
        Log.d("create db", createTasks);
        Log.d("create db", createTimeTable);
        db.execSQL(createSubjects);
        db.execSQL(createTasks);
        db.execSQL(createTimeTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public TaskDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.mContext = context;
    }

    public List<String> getSubjectsInDay(String day) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT " + SUBJECTS_TABLE + "." + SUBJECT_NAME + " FROM " +
                SUBJECTS_TABLE +
                " INNER JOIN " + TIMETABLE + " ON " +
                "(" + TIMETABLE + "." + SUBJECT_ID + "==" + SUBJECTS_TABLE
                + "._id)" +
                " WHERE " + TIMETABLE + "." + DAY_OF_WEEK + "== ?;"
                , new String[]{day.toLowerCase()});
        c.moveToPosition(-1);
        List<String> subjectList = new ArrayList<>();
        int index = c.getColumnIndex(SUBJECT_NAME);
        while (c.moveToNext()) {
            subjectList.add(c.getString(index));
        }
        c.close();
        return subjectList;
    }

    public ArrayList<String> getSubjects() {
        SQLiteDatabase db = getReadableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        qb.setTables(SUBJECTS_TABLE);
        String[] columns = new String[]{"_id", SUBJECT_NAME};

        Cursor c = qb.query(db, columns, null, null, null, null, null, null);
        c.moveToPosition(-1);

        ArrayList<String> subjects = new ArrayList<>();
        while (c.moveToNext()) {
            //Gets the subject that the cursor is currently pointing to
            subjects.add(c.getString(1));
        }

        return subjects;
    }

    public int getSubjectId(String subject) {
        //Builds the Hashmap the first time it is used
        if (subjectIdMap == null || subjectsChanged) {
            SQLiteDatabase db = getReadableDatabase();
            String query = "SELECT " + SUBJECT_NAME + ", _id FROM " + SUBJECTS_TABLE;
            Cursor c = db.rawQuery(query, null);
            // initialize it to -1 so we can iterate on it and not miss the first item
            c.moveToPosition(-1);
            int idColumn = c.getColumnIndex("_id");
            int nameColumn = c.getColumnIndex(SUBJECT_NAME);
            subjectIdMap = new HashMap<>();

            while (c.moveToNext()) {
                subjectIdMap.put(c.getString(nameColumn), c.getInt(idColumn));
            }
            c.close();
        }
        return subjectIdMap.get(subject);

    }

    public List<Task> getTasks() {
        SQLiteDatabase db = getReadableDatabase();
        DateTimeFormatter dbFormat = DateTimeFormat.forPattern(mContext.getString(R.string.database_date_format));

        Cursor c = db.rawQuery(
                "SELECT " + SUBJECTS_TABLE + "." + SUBJECT_NAME +
                        "," + TASKS_TABLE + "." + TASK_DESCRIPTION +
                        "," + TASKS_TABLE + "." + "\"_id\"" +
                        "," + TASKS_TABLE + "." + TASK_DONE +
                        "," + TASKS_TABLE + "." + DUE_DATE +
                        "," + TASKS_TABLE + "." + TASK_PHOTO_LOCATION +
                        " FROM " +
                        TASKS_TABLE +
                        " INNER JOIN " + SUBJECTS_TABLE + " ON (" + TASKS_TABLE + "." +
                        "SubjectId" + "=" + SUBJECTS_TABLE + "." + "\"_id\"" + ")", null);
        c.moveToPosition(-1);

        int subjColumn = c.getColumnIndex(SUBJECT_NAME);
        int descriptionColumn = c.getColumnIndex(TASK_DESCRIPTION);
        int idColumn = c.getColumnIndex("_id");
        int dateColumn = c.getColumnIndex(DUE_DATE);
        int photoLocationColumn = c.getColumnIndex(TASK_PHOTO_LOCATION);

        List<Task> tasks = new ArrayList<>();
        File photoFile;
        while (c.moveToNext()) {
            try {
                photoFile = new File((c.getString(photoLocationColumn)));
            } catch (NullPointerException e) {
                photoFile = null;
            }
            tasks.add(
                    new Task(c.getString(subjColumn),
                            c.getString(descriptionColumn),
                            dbFormat.parseLocalDate(c.getString(dateColumn)),
                            c.getInt(idColumn),
                            photoFile
                    ));
        }
        c.close();

        return tasks;
    }

    public void insertTask(Task task) throws IllegalArgumentException {
        String description = task.getDescription();
        LocalDate dueDate = task.getDueDate();
        String subject = task.getSubject();
        String photoPath = null;
        if (task.getPhotoFile() != null) {
            photoPath = task.getPhotoFile().getAbsolutePath();
        }


        if (description == null || dueDate == null || subject == null) {
            throw new IllegalArgumentException("All arguments must be non null");
        }

        SQLiteDatabase db = getWritableDatabase();
        DateTimeFormatter dbDateFormat = DateTimeFormat.forPattern(mContext.getResources()
                .getString(R.string.database_date_format));

        ContentValues taskCV = new ContentValues();
        taskCV.put(TASK_DESCRIPTION, description);
        taskCV.put(DUE_DATE, dueDate.toString(dbDateFormat));
        taskCV.put(SUBJECT_ID, getSubjectId(subject));
        taskCV.put(TASK_DONE, false);
        taskCV.put(TASK_PHOTO_LOCATION, photoPath);

        db.insert(TASKS_TABLE, null, taskCV);
    }

    public void modifyTask(Task task) {
        if (task.getDatabaseId() == -1) {
            throw new IllegalArgumentException("Could not modify task. Task does not have a " +
                    "database entry.");
        }
        SQLiteDatabase db = getWritableDatabase();
        DateTimeFormatter dbDateFormat = DateTimeFormat.forPattern(mContext.getResources()
                .getString(R.string.database_date_format));
        ContentValues taskCV = new ContentValues();
        if (task.getDescription() != null) {
            taskCV.put(TASK_DESCRIPTION, task.getDescription());
        }
        if (task.getDueDate() != null) {
            taskCV.put(DUE_DATE, task.getDueDate().toString(dbDateFormat));
        }
        if (task.getSubject() != null) {
            taskCV.put(SUBJECT_ID, getSubjectId(task.getSubject()));
        }
        if (task.getPhotoFile() != null) {
            taskCV.put(TASK_PHOTO_LOCATION, task.getPhotoFile().getAbsolutePath());
        }

        String selection = "_id LIKE ?";
        String[] selectionArgs = {String.valueOf(task.getDatabaseId())};

        db.update(TASKS_TABLE, taskCV, selection, selectionArgs);
    }

    public void deleteAllTasks() throws IOError {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TASKS_TABLE, null, null);
    }

    /**
     * Delete a task from the database
     *
     * @param taskId the _id of the task to be deleted. If it is -1 then nothing is deleted
     */
    public void deleteTask(int taskId) {
        if (!(taskId == -1)) {
            SQLiteDatabase db = getWritableDatabase();
            db.delete(TASKS_TABLE, "_id=" + String.valueOf(taskId), null);
            Log.d("Deleted task. Id", String.valueOf(taskId));
        } else {
            Log.d("deleteTask", "Not deleting any task");
        }
    }

    public void deleteSubject(int subjectId) {
        if (!(subjectId == -1)) {
            SQLiteDatabase db = getWritableDatabase();
            db.delete(SUBJECTS_TABLE, "_id=" + String.valueOf(subjectId), null);
            Log.d("Deleted subject. Id", String.valueOf(subjectId));
        } else {
            Log.d("deleteSubject", "Not deleting any subject");
        }
        subjectsChanged = true;
    }

    public void deleteSubject(String subjectName) {
        int subjectId = getSubjectId(subjectName);
        deleteSubject(subjectId);
    }

    /**
     * Convenience call for addSubject(-1, name)
     *
     * @param name The name of the subject to be added
     */
    public void addSubject(String name) {
        addSubject(-1, name);
    }

    /**
     * Add a subject to the database.
     *
     * @param id   The value for the _id column, if it is -1 it will be left to be decided by the DB.
     *             Also, if the id provided is in use, it is logged and a new id is used instead (as if -1 was passed)
     * @param name The name of the subject to be added
     */
    public void addSubject(int id, String name) {
        ContentValues subjectCV = new ContentValues();
        // Let the db decide the id if -1 was provided
        if (id >= 0) {
            subjectCV.put("_id", id);
        }
        subjectCV.put(SUBJECT_NAME, name);
        try {
            getWritableDatabase().insertOrThrow(SUBJECTS_TABLE, null, subjectCV);
        } catch (SQLiteConstraintException e) {
            Log.e("Database Helper", "subject id provided was in use. Using a new one.", e);
            subjectCV.clear();
            subjectCV.put(SUBJECT_NAME, name);
            getWritableDatabase().insert(SUBJECTS_TABLE, null, subjectCV);
        } finally {
            subjectsChanged = true;
        }

    }
}