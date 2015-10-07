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

package org.mpardalos.homeworkmanager;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import net.danlew.android.joda.JodaTimeAndroid;

import java.util.ArrayList;
import java.util.List;


public class TaskList extends AppCompatActivity {

    public static final int ADD_TASK_REQUEST = 2; //request code for the add task activity
    public static final int EDIT_TASK_REQUEST = 3;
    TaskDatabaseHelper mDatabase;
    TaskAdapter adapter;
    private RecyclerView mTaskRecyclerView;

    private final Runnable mLoadTasksToList = new Runnable() {
        @Override
        public void run() {
            // Only this needs to be done outside the main thread.
            final List<Task> tasks = mDatabase.getTasks();

            // This has to be run inside the main thread using post (
            mTaskRecyclerView.post(new Runnable() {
                @Override
                public void run() {
                    adapter.changeTaskList((ArrayList<Task>) tasks);
                    mTaskRecyclerView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                    findViewById(R.id.loading).setVisibility(View.GONE);
                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        JodaTimeAndroid.init(this);
        setContentView(R.layout.task_list);
        //((CoordinatorLayout) findViewById(R.id.fab_coordinator)).


        this.mDatabase = new TaskDatabaseHelper(this);

        this.mTaskRecyclerView = (RecyclerView) findViewById(R.id.task_list);
        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setOrientation(LinearLayoutManager.VERTICAL);
        mTaskRecyclerView.setLayoutManager(lm);
        mTaskRecyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(this,
                        new RecyclerItemClickListener.OnItemClickListener() {

                            @Override
                            public void onItemClick(View v, int position) {
                                Intent intent = new Intent
                                        (getApplicationContext(),
                                                TaskEdit.class);
                                intent.putExtra("task",
                                        (Task) v.getTag(R.id.task_object));

                                startActivityForResult(intent,
                                        EDIT_TASK_REQUEST);
                            }

                            @Override
                            public void onItemLongPress(View childView,
                                                        int position) {
                            }
                        }));

        ItemTouchHelper.SimpleCallback swipeCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction) {
                mDatabase.deleteTask(((Task) viewHolder.itemView.getTag(R.id.task_object)).getDatabaseId());
                final int original_position = viewHolder.getAdapterPosition();
                final Task deletedTask = (Task) viewHolder.itemView.getTag(R.id.task_object);

                ((UndoAdapter) mTaskRecyclerView.getAdapter()).remove(viewHolder.getAdapterPosition());
                mTaskRecyclerView.getAdapter().notifyDataSetChanged();

                Snackbar undoSB = Snackbar.make(findViewById(R.id.fab_coordinator), R.string.item_deleted, Snackbar.LENGTH_LONG);
                undoSB.setAction(R.string.undo_action, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ((UndoAdapter) mTaskRecyclerView.getAdapter()).restore(original_position);
                        mTaskRecyclerView.getAdapter().notifyDataSetChanged();
                        //TODO make this keep its original position (When it is added back to the DB it's put in the end)
                        mDatabase.insertTask(deletedTask);
                    }
                });
                undoSB.show();
            }
        };

        ItemTouchHelper swipeToDelete = new ItemTouchHelper(swipeCallback);
        swipeToDelete.attachToRecyclerView(mTaskRecyclerView);

        Toolbar toolbar = (Toolbar) findViewById(R.id.action_bar);
        setSupportActionBar(toolbar);

        this.adapter = new TaskAdapter(this, null);
        new Thread(mLoadTasksToList).run();
    }

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
                new AlertDialog.Builder(this)
                        .setTitle(R.string.delete_tasks_confirmation_dialog_title)
                        .setMessage(R.string.delete_tasks_confirmation_dialog_text)
                        .setNegativeButton(android.R.string.no, null)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mDatabase.deleteAllTasks();
                                new Thread(mLoadTasksToList).run();
                                adapter.notifyDataSetChanged();
                            }
                        })
                        .show();
                break;
            case R.id.edit_subjects_button:
                startActivity(new Intent(this, SubjectEdit.class));
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void addTask(View view) {
        Intent openTaskAdd = new Intent(this, TaskAdd.class);
        startActivityForResult(openTaskAdd, ADD_TASK_REQUEST);
    }

    private void refreshList() {
        new Thread(mLoadTasksToList).run();
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

