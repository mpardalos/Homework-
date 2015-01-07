package org.mpardalos.homeworkmanager;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ListAdapter;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;


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
        DateTimeFormatter dbFormat = DateTimeFormat.forPattern(context.getResources().getString(R.string.database_date_format));
        DateTimeFormatter displayFormat = DateTimeFormat.forPattern(context.getString(R.string.display_date_format));
        LocalDate date = dbFormat.parseLocalDate(cursor.getString(cursor.getColumnIndex
                (TaskDatabaseHelper.DUE_DATE)));
        dueDate.setText(date.toString(displayFormat));
        dueDate.setTag(R.id.due_date_tag, date);


        CheckBox checkBox = (CheckBox) view.findViewById(R.id.task_done_checkbox);
        checkBox.setChecked(cursor.getInt(cursor.getColumnIndex(TaskDatabaseHelper.TASK_DONE)) !=
                                    0);

        view.setTag(R.id.database_task_id, cursor.getInt(cursor.getColumnIndex("_id")));
    }
}
