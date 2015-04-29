package org.mpardalos.homeworkmanager;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;


public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    protected class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView subject;
        TextView taskDescription;
        TextView dueDate;

        public TaskViewHolder(View itemView) {
            super(itemView);
            subject = (TextView) itemView.findViewById(R.id.subject_field);
            taskDescription = (TextView) itemView.findViewById(R.id.task_description_field);
            dueDate = (TextView) itemView.findViewById(R.id.due_date_field);
        }
    }

    private ArrayList<Task> mTasks;
    private Context mContext;

    public TaskAdapter(Context context, ArrayList<Task> tasks) {
        mContext = context;
        mTasks = tasks;
    }

    @Override
    public TaskViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.task, parent,
                                                                         false);

        return new TaskViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(TaskViewHolder holder, int position) {
        Task task = mTasks.get(position);
        holder.subject.setText(task.getSubject());
        holder.taskDescription.setText(task.getDescription());
        DateTimeFormatter displayFormat = DateTimeFormat.forPattern(mContext.getString(R.string.display_date_format));
        holder.dueDate.setText(task.getDueDate().toString(displayFormat));
        holder.itemView.setTag(R.id.task_object, task);
    }

    @Override
    public int getItemCount() {
        return mTasks.size();
    }

    public void changeTaskList(ArrayList<Task> tasks) {
        this.mTasks = tasks;
    }
}