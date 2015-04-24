package org.mpardalos.homeworkmanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.IOError;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class TaskDatabaseHelper extends SQLiteAssetHelper {

    public static final String SUBJECT_NAME = "SubjName";
    private static final String DB_NAME = "database.db";
    private static final int DB_VERSION = 1;
    private static final String TASKS_TABLE = "Tasks";
    private static final String SUBJECTS_TABLE = "Subjects";
    private static final String PERIOD_TABLE = "Periods";
    private static final String TIMETABLE = "TimeTable";
    private static final String TASK_DONE = "Done";
    private static final String SUBJECT_ID = "SubjectId";
    private static final String TEACHER_NAME = "TeacherName";
    private static final String DESCRIPTION = "TaskDescr";
    private static final String DUE_DATE = "DueDate";
    private static final String PERIOD_START = "PeriodStart";
    private static final String PERIOD_END = "PeriodEnd";
    private static final String DAY_OF_WEEK = "WeekDay";

    private static HashMap<String, Integer> subjectIdMap;

    private final Context mContext;

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
        String[] columns = new String[]{"_id", SUBJECT_NAME, TEACHER_NAME};

        Cursor c = qb.query(db, columns, null, null, null, null, null, null);
        c.moveToFirst();

        ArrayList<String> subjects = new ArrayList<>();
        while (c.moveToNext()) {
            //Gets the subject that the cursor is currently pointing to
            subjects.add(c.getString(1));
        }

        return subjects;
    }

    public int getSubjectId(String subject) {
        //Builds the Hashmap the first time it is used
        if (subjectIdMap == null) {
            SQLiteDatabase db = getReadableDatabase();
            String query = "SELECT " + SUBJECT_NAME + ", _id FROM " + SUBJECTS_TABLE;
            Cursor c = db.rawQuery(query, null);
            c.moveToPosition(-1); //initialize it to -1 so we can iterate on it and not miss the
            // first item
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
                        "," + TASKS_TABLE + "." + DESCRIPTION +
                        "," + TASKS_TABLE + "." + "\"_id\"" +
                        "," + TASKS_TABLE + "." + TASK_DONE +
                        "," + TASKS_TABLE + "." + DUE_DATE +
                        " FROM " +
                        TASKS_TABLE +
                        " INNER JOIN " + SUBJECTS_TABLE + " ON (" + TASKS_TABLE + "." +
                        "SubjectId" + "=" + SUBJECTS_TABLE + "." + "\"_id\"" + ")", null);
        c.moveToPosition(-1);

        int subjColumn = c.getColumnIndex(SUBJECT_NAME);
        int descriptionColumn = c.getColumnIndex(DESCRIPTION);
        int idColumn = c.getColumnIndex("_id");
        int doneColumn = c.getColumnIndex(TASK_DONE);
        int dateColumn = c.getColumnIndex(DUE_DATE);

        List<Task> tasks = new ArrayList<>();
        while (c.moveToNext()) {
            tasks.add(new Task(c.getString(subjColumn), c.getString(descriptionColumn),
                               dbFormat.parseLocalDate(c.getString(dateColumn)),
                               c.getInt(idColumn), (c.getInt(doneColumn) != 0)));
        }
        c.close();

        return tasks;
    }

    public void setDone(int taskId, boolean checked) {
        SQLiteDatabase db = getWritableDatabase();
        int value;
        if (checked) {
            value = 1;
        } else {
            value = 0;
        }

        // New value for one column
        ContentValues values = new ContentValues();
        values.put(TASK_DONE, value);

        // Which row to update, based on the ID
        String selection = "_id" + " LIKE ?";
        String[] selectionArgs = {String.valueOf(taskId)};

        db.update(
                TASKS_TABLE,
                values,
                selection,
                selectionArgs);

    }

    public void insertTask(Task task) throws IllegalArgumentException {
        String description = task.getDescription();
        LocalDate dueDate = task.getDueDate();
        String subject = task.getSubject();

        if (description == null || dueDate == null || subject == null) {
            throw new IllegalArgumentException("All arguments must be non null");
        }

        SQLiteDatabase db = getWritableDatabase();
        DateTimeFormatter dbDateFormat = DateTimeFormat.forPattern(mContext.getResources()
                                                                           .getString(R.string.database_date_format));

        ContentValues taskCV = new ContentValues();
        taskCV.put(DESCRIPTION, description);
        taskCV.put(DUE_DATE, dueDate.toString(dbDateFormat));
        taskCV.put(SUBJECT_ID, getSubjectId(subject));
        taskCV.put(TASK_DONE, false);

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
            taskCV.put(DESCRIPTION, task.getDescription());
        }
        if (task.getDueDate() != null) {
            taskCV.put(DUE_DATE, task.getDueDate().toString(dbDateFormat));
        }
        if (task.getSubject() != null) {
            taskCV.put(SUBJECT_ID, getSubjectId(task.getSubject()));
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
    }

    public void deleteSubject(String subjectName) {
        int subjectId = getSubjectId(subjectName);
        deleteSubject(subjectId);
    }

    public String getSubjectAtDateTime(DateTime dateTime) throws IllegalArgumentException {
        if (dateTime.dayOfWeek().getAsText().equals("Saturday") || dateTime.dayOfWeek().getAsText
                ().equals("Sunday")) {
            throw new IllegalArgumentException("There is no lesson on a weekend");
        }

        Cursor periods = getPeriods();
        SQLiteDatabase db = getReadableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        LocalTime requestedTime = dateTime.toLocalTime();
        LocalTime startTime;
        LocalTime endTime;
        DateTimeFormatter timeFormatter = DateTimeFormat.forPattern(mContext.getString(R.string.database_time_format));
        int resultPeriod = -1;

        //Find the period
        periods.moveToPosition(-1);
        while (periods.moveToNext()) {
            startTime = timeFormatter.parseLocalTime(periods.getString(periods.getColumnIndex
                    (PERIOD_START)));
            endTime = timeFormatter.parseLocalTime(periods.getString(periods.getColumnIndex
                    (PERIOD_END)));


            if (requestedTime.compareTo(startTime) >= 0 &&
                    requestedTime.compareTo(endTime.plusMinutes(3)) < 0) {
                resultPeriod = periods.getInt(periods.getColumnIndex("_id"));
                break;
            }
        }

        if (resultPeriod == -1) {
            throw new IllegalArgumentException("There is no lesson at: " + requestedTime.toString
                    (timeFormatter));
        }

        String dayOfWeek = dateTime.dayOfWeek().getAsText().toLowerCase();
        qb.setTables(TIMETABLE);
        String[] selectionArgs = new String[]{dayOfWeek, String.valueOf(resultPeriod)};
        Cursor c = db.rawQuery("SELECT " + SUBJECTS_TABLE + "." + SUBJECT_NAME +
                                       " FROM " + SUBJECTS_TABLE + " INNER JOIN " + TIMETABLE + "" +
                                       " ON (" + SUBJECTS_TABLE + "._id=" + TIMETABLE + "" +
                                       ".SubjectId)" +
                                       " WHERE (" + TIMETABLE + "." + DAY_OF_WEEK + "= ?" + " AND" +
                                       " " + TIMETABLE + ".PeriodId= ? )"
                , selectionArgs);

        c.moveToFirst();
        //Using a temp var so that we can close the Cursor
        String result = c.getString(c.getColumnIndex(SUBJECT_NAME));
        c.close();
        return result;
    }

    Cursor getPeriods() {
        SQLiteDatabase db = getReadableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        qb.setTables(PERIOD_TABLE);
        String[] columns = {"_id", PERIOD_START, PERIOD_END};

        Cursor c = qb.query(db, columns, null, null, null, null, null);
        c.moveToFirst();

        return c;
    }

}