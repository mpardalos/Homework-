package org.mpardalos.homeworkmanager;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListAdapter;
import android.widget.TextView;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.List;


public class TaskAdapter extends BaseAdapter implements ListAdapter {
    static class TaskViewHolder {
        TextView subject;
        TextView taskDescription;
        TextView dueDate;
        CheckBox checkBox;
    }

    private final LayoutInflater mInflater;
    private final Context mContext;
    private List<Task> mTasks;

    public TaskAdapter(Context context, List<Task> tasks) {
        this.mContext = context;
        this.mInflater = LayoutInflater.from(mContext);
        this.mTasks = tasks;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        Task task = mTasks.get(position);
        TaskViewHolder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.task_entry, parent, false);
            holder = new TaskViewHolder();

            holder.subject = (TextView) convertView.findViewById(R.id.subject_field);
            holder.taskDescription = (TextView) convertView.findViewById(R.id.task_description_field);
            holder.dueDate = (TextView) convertView.findViewById(R.id.due_date_field);
            holder.checkBox = (CheckBox) convertView.findViewById(R.id.task_done_checkbox);
            //Used to store the state of the checkbox.
            //Without this the checkbox resets when the row goes out of the screen
            holder.checkBox.setOnCheckedChangeListener(
                    new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            if (buttonView.getTag(R.id.item_position) != null) {
                                int itemPosition = (Integer) buttonView.getTag(R.id.item_position);
                                mTasks.get(itemPosition).setDone(buttonView.isChecked());
                            }
                        }
                    });

            convertView.setTag(R.id.view_holder, holder);
        } else {
            holder = (TaskViewHolder) convertView.getTag(R.id.view_holder);
        }

        holder.subject.setText(task.getSubject());
        holder.taskDescription.setText(task.getDescription());
        holder.checkBox.setChecked(task.isDone());
        //See the listener added to the checkbox above
        holder.checkBox.setTag(R.id.item_position, position);

        DateTimeFormatter displayFormat = DateTimeFormat.forPattern(mContext.getString(R.string.display_date_format));
        LocalDate date = task.getDueDate();
        holder.dueDate.setText(date.toString(displayFormat));
        //Used to get any info about the task
        //DON'T  get info directly from the child views
        convertView.setTag(R.id.task_object, task);

        return convertView;
    }

    @Override
    public int getCount() {
        return mTasks.size();
    }

    @Override
    public Object getItem(int position) {
        return mTasks.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void changeTaskList(List<Task> Tasks) {
        this.mTasks = Tasks;
    }
}
