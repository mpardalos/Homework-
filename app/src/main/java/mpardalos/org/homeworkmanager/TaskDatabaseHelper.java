package mpardalos.org.homeworkmanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import java.io.File;
import java.io.IOError;

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
    public static final String TEACHER_NAME = "TeacherName";


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

    public Cursor getTasks() {
        SQLiteDatabase db = getReadableDatabase();

        Cursor c = db.rawQuery("SELECT \n" +
                                       "  Subjects.SubjName, " +
                                       "  Tasks.TaskDescr, " +
                                       "  Tasks.\"_id\"," +
                                       "  Tasks.Done, " +
                                       "  Tasks.DueDay " +
                                       "FROM" +
                                       "  Tasks " +
                                       "  INNER JOIN Subjects ON (Tasks.SubjectId = Subjects" +
                                       ".\"_id\")", null);
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

        int count = db.update(
                TASKS_TABLE,
                values,
                selection,
                selectionArgs);

    }

    /**
     * Resets *everything* to the original state by deleting the database
     */
    public void resetDB() throws IOError {
        File database = new File(this.mContext.getFilesDir().getPath() +
                                         "mpardalos.org.homeworkmanager/databases/database.db");
        /*
        boolean result = false;
        try {
            result = mContext.deleteDatabase("database.db");
        } catch (Exception e) {
            android.util.Log.e("resetDB", "Failed to delete database: ", e);
        }
        */
        close();
        if (!database.delete()) {
            android.util.Log.e("resetDB", "Failed to delete database");
        }
        getReadableDatabase();
    }
/*
    public void addTask (String taskName) {
        SQLiteDatabase db = getWritableDatabase();

        db.rawQuery("")
    }
*/
}
