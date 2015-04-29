package org.mpardalos.homeworkmanager;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;

import com.melnykov.fab.FloatingActionButton;

import net.danlew.android.joda.JodaTimeAndroid;

import java.util.ArrayList;
import java.util.List;


public class TaskList extends ActionBarActivity {

    private class TaskLoader extends AsyncTask<Void, Void, List<Task>> {
        //Bad, bad, bad decision, hopefully only temporary
        public List<Task> doInBackground(Void... databaseHelpers) {
            return mDatabase.getTasks();
        }

        protected void onPreExecute() {
            findViewById(R.id.loading).setVisibility(View.VISIBLE);
        }

        protected void onPostExecute(List<Task> result) {
            adapter.changeTaskList((ArrayList<Task>) result);
            mTaskRecyclerView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
            findViewById(R.id.loading).setVisibility(View.GONE);
        }
    }

    private RecyclerView mTaskRecyclerView;
    public static final int ADD_TASK_REQUEST = 2; //request code for the add task activity
    public static final int EDIT_TASK_REQUEST = 3;
    TaskDatabaseHelper mDatabase;
    TaskAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        JodaTimeAndroid.init(this);
        setContentView(R.layout.task_list);

        this.mDatabase = new TaskDatabaseHelper(this);
        this.mTaskRecyclerView = (RecyclerView) findViewById(R.id.task_list);
        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setOrientation(LinearLayoutManager.VERTICAL);
        mTaskRecyclerView.setLayoutManager(lm);

        ((FloatingActionButton) findViewById(R.id.add_task_button))
                .attachToRecyclerView(mTaskRecyclerView);
        // (mTaskRecyclerView);

        Toolbar toolbar = (Toolbar) findViewById(R.id.action_bar);
        setSupportActionBar(toolbar);

        //TODO Find another way to do the onClick Listeners
        //mTaskRecyclerView.setOnItemClickListener(mOnClickListener);
        this.adapter = new TaskAdapter(this, null);
        new TaskLoader().execute();
    }

    /*
    public void onListItemClick(ListView l, View v, int position, long id) {
        l.setSelection(position);
        Intent intent = new Intent(getApplicationContext(), TaskEdit.class);
        intent.putExtra("task", (Task) v.getTag(R.id.task_object));

        startActivityForResult(intent, EDIT_TASK_REQUEST);
    }
    */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.task_list, menu);
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
                this.adapter.changeTaskList((ArrayList<Task>) mDatabase.getTasks());
                this.adapter.notifyDataSetChanged();
                break;
/*
            case R.id.edit_timetable_button:
                Intent intent = new Intent(this, EditSubjects.class);
                startActivity(intent);
*/
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Called when a checkbox is clicked.
     * Updates the state of the database entry associated with the item to reflect the state of
     * the checkbox
     */
    public void onItemChecked(View checkbox) {
        int databaseId = ((Task) ((View) checkbox.getParent()).getTag(R.id.task_object))
                .getDatabaseId();
        boolean checked = ((CheckBox) checkbox).isChecked();

        mDatabase.setDone(databaseId, checked);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ADD_TASK_REQUEST && resultCode == RESULT_OK) {
            mDatabase.insertTask((Task) data.getParcelableExtra("task"));//task must implement
            // Parcelable
            refreshList();

        } else if (requestCode == EDIT_TASK_REQUEST && resultCode == TaskEdit.RESULT_DELETE_TASK) {
            mDatabase.deleteTask(((Task) data.getParcelableExtra("task")).getDatabaseId());
            refreshList();
        } else if (requestCode == EDIT_TASK_REQUEST && resultCode == TaskEdit.RESULT_EDIT_TASK) {
            mDatabase.modifyTask((Task) data.getParcelableExtra("task"));
            refreshList();
        }
    }
}
