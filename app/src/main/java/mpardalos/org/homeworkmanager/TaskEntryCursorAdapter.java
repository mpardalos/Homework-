package mpardalos.org.homeworkmanager;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ListAdapter;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TaskEntryCursorAdapter extends ResourceCursorAdapter implements ListAdapter {

    public TaskEntryCursorAdapter(Context context, int layout, Cursor c, int flags) {
        super(context, layout, c, flags);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView taskTitle = (TextView) view.findViewById(R.id.subject_field);
        taskTitle.setText(cursor.getString(cursor.getColumnIndex(TaskDatabaseHelper.SUBJECT_NAME)));

        TextView taskDescription = (TextView) view.findViewById(R.id.task_description_field);
        taskDescription.setText(cursor.getString(cursor.getColumnIndex(TaskDatabaseHelper
                                                                               .DESCRIPTION)));

        TextView dueDate = (TextView) view.findViewById(R.id.due_date_field);
        SimpleDateFormat dbFormat = new SimpleDateFormat(context.getResources().getString(R.string.database_date_format));
        java.text.DateFormat displayFormat = SimpleDateFormat.getDateInstance();
        Date date;
        try {
            date = dbFormat.parse(cursor.getString(cursor.getColumnIndex(TaskDatabaseHelper
                                                                                 .DUE_DATE)));
            dueDate.setText(displayFormat.format(date));
        } catch (ParseException e) {
            Log.e("Error parsing date from database", "unable to parse entry with id " +
                    String.valueOf(cursor.getInt(cursor.getColumnIndex("_id"))));
            dueDate.setText("");
        }


        CheckBox checkBox = (CheckBox) view.findViewById(R.id.task_done_checkbox);
        checkBox.setChecked(cursor.getInt(cursor.getColumnIndex(TaskDatabaseHelper.TASK_DONE)) !=
                                    0);

        view.setTag(cursor.getInt(cursor.getColumnIndex("_id")));
    }
}
