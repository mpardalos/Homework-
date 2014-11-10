package mpardalos.org.homeworkmanager;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;


public class TaskAdapter extends BaseAdapter {
    //two item lists containing the title and the description
    private List<String[]> tasks = Collections.emptyList();
    private Context context;

    public TaskAdapter(Context context) {
        this.context = context;
    }

    public void UpdateTasks(List<String[]> tasks) {
        this.tasks = tasks;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return tasks.size();
    }

    @Override
    public String[] getItem(int position) {
        return tasks.get(position);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.task_list_entry, parent, false);
        }

        convertView.setTag(i);

        TextView title = (TextView) convertView.findViewById(R.id.task_title_field);
        TextView description = (TextView) convertView.findViewById(R.id.task_description_field);

        title.setText(getItem(i)[0]);
        android.util.Log.i("title", title.getText().toString());
        description.setText(getItem(i)[1]);
        android.util.Log.i("description", description.getText().toString());

        return convertView;
    }

}
