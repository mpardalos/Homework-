package mpardalos.org.homeworkmanager;

import android.content.Intent;
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

import org.joda.time.LocalDate;


public class TaskList extends ActionBarListActivity {

    public static final int ADD_TASK_REQUEST = 2; //request code for the add task activity
    public static final int EDIT_TASK_REQUEST = 3;

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

        ((FloatingActionButton) findViewById(R.id.add_task_button)).attachToListView(getListView());

        Toolbar toolbar = (Toolbar) findViewById(R.id.action_bar);
        setSupportActionBar(toolbar);

        getListView().setOnItemClickListener(mOnClickListener);
    }


    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        l.setSelection(position);
        LocalDate dueDate = (LocalDate) (v.findViewById(R.id.due_date_field).getTag(R.id.due_date_tag));
        String description = (String) ((TextView) v.findViewById(R.id.task_description_field))
                .getText();
        String subject = (String) ((TextView) v.findViewById(R.id.subject_field)).getText();
        int databaseId = (Integer) v.getTag(R.id.database_task_id);

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
            mDatabase.insertTask(data.getStringExtra("description"),
                                 (LocalDate) data.getSerializableExtra("dueDate"),
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
        int itemId = (Integer.parseInt(((View) checkbox.getParent()).getTag(R.id.database_task_id).toString()));
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
        this.adapter.changeCursor(mDatabase.getTasks());
        this.adapter.notifyDataSetChanged();
    }
}
