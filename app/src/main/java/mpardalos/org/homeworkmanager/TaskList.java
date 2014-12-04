package mpardalos.org.homeworkmanager;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;

import java.util.Date;


public class TaskList extends ListActivity {

    public int ADD_TASK_REQUEST = 0; //request code for the add task activity

    TaskDatabaseHelper mDatabase;
    TaskEntryCursorAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_list);

        this.mDatabase = new TaskDatabaseHelper(this);
        this.adapter = new TaskEntryCursorAdapter(this, R.layout.task_entry,
                                                  mDatabase.getTasks(), 0);

        setListAdapter(this.adapter);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i("Result received, requestCode", String.valueOf(requestCode));
        if (requestCode == ADD_TASK_REQUEST && resultCode == RESULT_OK) {
            mDatabase.insertTask(data.getStringExtra("description"),
                                 (Date) data.getSerializableExtra("dueDate"),
                                 data.getStringExtra("subject")
                                );
            this.adapter.changeCursor(mDatabase.getTasks());
            this.adapter.notifyDataSetChanged();
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
            case R.id.add_task_button:
                Intent openTaskAdd = new Intent(this, TaskAdd.class);
                startActivityForResult(openTaskAdd, ADD_TASK_REQUEST);
                break;

            case R.id.delete_tasks_button:
                deleteAllTasks();
                this.adapter.changeCursor(mDatabase.getTasks());
                this.adapter.notifyDataSetChanged();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Called when a checkbox is clicked.
     * Updates the state of the database entry associated with the item to reflect the state of
     * the checkbox
     */
    public void onItemChecked(View checkbox) {
        int itemId = (Integer.parseInt(((View) checkbox.getParent()).getTag().toString()));
        boolean checked = ((CheckBox) checkbox).isChecked();

        mDatabase.setDone(itemId, checked);
    }

    public void deleteAllTasks() {
        mDatabase.deleteAllTasks();
    }
}
