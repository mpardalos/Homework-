package org.mpardalos.homeworkmanager;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import com.melnykov.fab.FloatingActionButton;

import net.danlew.android.joda.JodaTimeAndroid;

import org.joda.time.LocalDate;

import java.util.List;


public class TaskList extends ActionBarListActivity {

    public static final int ADD_TASK_REQUEST = 2; //request code for the add task activity
    public static final int EDIT_TASK_REQUEST = 3;

    TaskDatabaseHelper mDatabase;
    TaskAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        JodaTimeAndroid.init(this);
        setContentView(R.layout.activity_task_list);

        this.mDatabase = new TaskDatabaseHelper(this);

        ((FloatingActionButton) findViewById(R.id.add_task_button)).attachToListView(getListView());

        Toolbar toolbar = (Toolbar) findViewById(R.id.action_bar);
        setSupportActionBar(toolbar);

        getListView().setOnItemClickListener(mOnClickListener);
        this.adapter = new TaskAdapter(this, null);
        new TaskLoader().execute((Void) null);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        l.setSelection(position);
        LocalDate dueDate = ((Task) v.getTag(R.id.task_object)).getDueDate();
        String description = (String) ((TextView) v.findViewById(R.id.task_description_field))
                .getText();
        String subject = (String) ((TextView) v.findViewById(R.id.subject_field)).getText();
        int databaseId = ((Task) v.getTag(R.id.task_object)).getDatabaseId();

        //Maybe we should pass the Task object directly instead of its fields
        Intent intent = new Intent(getApplicationContext(), TaskEdit.class);
        intent.putExtra(TaskDatabaseHelper.DUE_DATE, dueDate);
        intent.putExtra(TaskDatabaseHelper.DESCRIPTION, description);
        intent.putExtra(TaskDatabaseHelper.SUBJECT_NAME, subject);
        intent.putExtra("_id", databaseId);

        startActivityForResult(intent, EDIT_TASK_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("Result received, requestCode", String.valueOf(requestCode));

        if (requestCode == ADD_TASK_REQUEST && resultCode == RESULT_OK) {
            LocalDate date = (LocalDate) data.getSerializableExtra("dueDate");
            mDatabase.insertTask(data.getStringExtra("description"),
                                 date,
                                 data.getStringExtra("subject")
                                );
            refreshList();

        } else if (requestCode == EDIT_TASK_REQUEST && resultCode == TaskEdit.RESULT_DELETE_TASK) {
            mDatabase.deleteTask(data.getIntExtra("_id", -1));
            refreshList();
        } else if (requestCode == EDIT_TASK_REQUEST && resultCode == TaskEdit.RESULT_EDIT_TASK) {
            mDatabase.modifyTask(data.getIntExtra("_id", 500),
                                 data.getStringExtra("description"),
                                 (LocalDate) data.getSerializableExtra("dueDate"),
                                 data.getStringExtra("subject"));
            refreshList();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_task_list_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        android.util.Log.i("item clicked", String.valueOf(id));

        switch (id) {
            case R.id.delete_tasks_button:
                deleteAllTasks();
                this.adapter.changeTaskList(mDatabase.getTasks());
                this.adapter.notifyDataSetChanged();
                break;

            case R.id.edit_timetable_button:
                Intent intent = new Intent(this, SetupActivity.class);
                startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Called when a checkbox is clicked.
     * Updates the state of the database entry associated with the item to reflect the state of
     * the checkbox
     */
    public void onItemChecked(View checkbox) {
        int itemId = ((Task) ((View) checkbox.getParent()).getTag(R.id.task_object))
                .getDatabaseId();
        boolean checked = ((CheckBox) checkbox).isChecked();

        mDatabase.setDone(itemId, checked);
    }

    public void deleteAllTasks() {
        mDatabase.deleteAllTasks();
    }

    public void addTask(View view) {
        Intent openTaskAdd = new Intent(this, TaskAdd.class);
        startActivityForResult(openTaskAdd, ADD_TASK_REQUEST);
    }

    private void refreshList() {
        new TaskLoader().execute((Void) null);
    }

    private class TaskLoader extends AsyncTask<Void, Void, List<Task>> {
        protected void onPreExecute() {
            findViewById(R.id.loading).setVisibility(View.VISIBLE);
        }

        //Bad, bad, bad decision, hopefully only temporary
        public List<Task> doInBackground(Void... databaseHelpers) {
            return mDatabase.getTasks();
        }

        protected void onPostExecute(List<Task> result) {
            adapter.changeTaskList(result);
            setListAdapter(adapter);
            adapter.notifyDataSetChanged();
            findViewById(R.id.loading).setVisibility(View.GONE);
        }
    }
}
