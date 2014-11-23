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

    private static final String DB_NAME = "database.db";
    private static final int DB_VERSION = 1;

    private static final String TASKS_TABLE = "Tasks";
    private static final String SUBJECTS_TABLE = "Subjects";
    private static final String PERIOD_TABLE = "Periods";
    private static final String TIMETABLE = "TimeTable";

    private static final String TASK_DONE = "Done";


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

    /**
     * Returns a cursor pointing t
     *
     * @return
     */

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
}
