package mpardalos.org.homeworkmanager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ListView;


public class TaskList extends Activity {

    TaskDatabaseHelper mDatabase;
    TaskEntryCursorAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_list);

        this.mDatabase = new TaskDatabaseHelper(this);
        this.adapter = new TaskEntryCursorAdapter(this, R.layout.task_entry,
                                                  mDatabase.getTasks(), 0);

        ListView task_view = (ListView) findViewById(R.id.task_list_view);
        task_view.setAdapter(adapter);

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
            case R.id.add_task_button:
                addTask();
                break;

            case R.id.delete_tasks_button:
                deleteTasks();
                adapter.changeCursor(mDatabase.getTasks());
                adapter.notifyDataSetChanged();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void addTask() {
        Intent openTaskAdd = new Intent(this, TaskAdd.class);
        startActivity(openTaskAdd);
    }

    public void onItemChecked(View checkbox) {
        int itemId = (Integer.parseInt(((View) checkbox.getParent()).getTag().toString()));
        boolean checked = ((CheckBox) checkbox).isChecked();

        mDatabase.setDone(itemId, checked);
    }

    public void deleteTasks() {
        mDatabase.deleteTasks();
    }
}
