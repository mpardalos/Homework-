package mpardalos.org.homeworkmanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import java.io.IOError;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 */
public class TaskDatabaseHelper extends SQLiteAssetHelper {

    public static final String DB_NAME = "database.db";
    public static final int DB_VERSION = 1;

    public static final String TASKS_TABLE = "Tasks";
    public static final String SUBJECTS_TABLE = "Subjects";
    public static final String PERIOD_TABLE = "Periods";
    public static final String TIMETABLE = "TimeTable";

    public static final String TASK_DONE = "Done";
    public static final String SUBJECT_NAME = "SubjName";
    public static final String SUBJECT_ID = "SubjectId";
    public static final String TEACHER_NAME = "TeacherName";
    public static final String DESCRIPTION = "TaskDescr";
    public static final String DUE_DATE = "DueDate";


    Context mContext;

    public TaskDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.mContext = context;
    }

    public Cursor getPeriods() {
        SQLiteDatabase db = getReadableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        qb.setTables(PERIOD_TABLE);
        String[] columns = {"_id", "PeriodStart", "PeriodEnd"};

        Cursor c = qb.query(db, columns, null, null, null, null, null);
        c.moveToFirst();

        return c;
    }

    public Cursor getSubjects() {
        SQLiteDatabase db = getReadableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        qb.setTables(SUBJECTS_TABLE);
        String[] columns = new String[]{"_id", SUBJECT_NAME, TEACHER_NAME};

        Cursor c = qb.query(db, columns, null, null, null, null, null, null);
        c.moveToFirst();

        return c;
    }

    public int getSubjectId(String subject) {
        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT _id FROM " + SUBJECTS_TABLE + " WHERE " + SUBJECT_NAME + "=?";

        String[] selectionArgs = new String[]{subject};

        Cursor c = db.rawQuery(query, selectionArgs);
        c.moveToFirst();
        return c.getInt(c.getColumnIndex("_id"));
    }

    public Cursor getTasks() {
        SQLiteDatabase db = getReadableDatabase();

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
        c.moveToFirst();
        return c;
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

    public void insertTask(String description, Date dueDate, String subject)
            throws IllegalArgumentException {

        if (description == null || dueDate == null || subject == null) {
            throw new IllegalArgumentException("All arguments must be non null");
        }

        SQLiteDatabase db = getWritableDatabase();
        DateFormat dbDateFormat = new SimpleDateFormat(mContext.getResources().getString(R.string.database_date_format));

        ContentValues task = new ContentValues();
        task.put(DESCRIPTION, description);
        task.put(DUE_DATE, dbDateFormat.format(dueDate));
        task.put(SUBJECT_ID, getSubjectId(subject));
        task.put(TASK_DONE, false);

        db.insert(TASKS_TABLE, null, task);
    }

    public void modifyTask(int _id, String description, Date dueDate, String subject) {
        SQLiteDatabase db = getWritableDatabase();
        DateFormat dbDateFormat = new SimpleDateFormat(mContext.getResources().getString(R.string.database_date_format));
        ContentValues task = new ContentValues();
        if (description != null) {
            task.put(DESCRIPTION, description);
        }
        if (dueDate != null) {
            task.put(DUE_DATE, dbDateFormat.format(dueDate));
        }
        if (subject != null) {
            task.put(SUBJECT_ID, getSubjectId(subject));
        }

        String selection = "_id LIKE ?";
        String[] selectionArgs = {String.valueOf(_id)};

        db.update(TASKS_TABLE, task, selection, selectionArgs);
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
}