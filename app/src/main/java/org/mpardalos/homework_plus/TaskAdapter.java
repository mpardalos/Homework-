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

package org.mpardalos.homework_plus;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.HashMap;


public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> implements UndoAdapter {

    private ArrayList<Task> mTasks;
    private Context mContext;
    private HashMap<Integer, Task> mRemovedTasks;

    public TaskAdapter(Context context, ArrayList<Task> tasks) {
        mContext = context;
        mTasks = tasks;
        mRemovedTasks = new HashMap<>();
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

    public void remove(int position) {
        //Move the item from the displayed tasks to the removed so that it can be restored
        mRemovedTasks.put(position, mTasks.get(position));
        mTasks.remove(position);
    }

    @Override
    public void restore(int position) {
        mTasks.add(position, mRemovedTasks.get(position));
    }

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
}