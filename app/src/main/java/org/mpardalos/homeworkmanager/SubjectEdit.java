package org.mpardalos.homeworkmanager;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;


public class SubjectEdit extends ActionBarActivity {
    TaskDatabaseHelper mDatabase;
    RecyclerView mSubjectList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_subjects);

        this.mDatabase = new TaskDatabaseHelper(this);

        this.mSubjectList = (RecyclerView) findViewById(R.id.subject_list);
        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setOrientation(LinearLayoutManager.VERTICAL);
        mSubjectList.setLayoutManager(lm);
        mSubjectList.setAdapter(new SubjectAdapter(mDatabase.getSubjects()));
        ItemTouchHelper.SimpleCallback callback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                mDatabase.deleteSubject(((SubjectAdapter.SubjectHolder) viewHolder).title.getText().toString());
                ((SubjectAdapter) mSubjectList.getAdapter()).remove(viewHolder.getAdapterPosition());
                mSubjectList.getAdapter().notifyDataSetChanged();
            }
        };
        ItemTouchHelper swipeToDelete = new ItemTouchHelper(callback);
        swipeToDelete.attachToRecyclerView(mSubjectList);

        Toolbar toolbar = (Toolbar) findViewById(R.id.action_bar);
        setSupportActionBar(toolbar);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.edit_subjects, menu);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
