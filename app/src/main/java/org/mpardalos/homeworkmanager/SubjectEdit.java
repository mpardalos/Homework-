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

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;


public class SubjectEdit extends AppCompatActivity {
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
            public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction) {
                //The methods after this modify the viewHolder so we have to keep the original position
                final int originalPosition = viewHolder.getAdapterPosition();

                mDatabase.deleteSubject(((SubjectAdapter.SubjectHolder) viewHolder).title.getText().toString());
                ((SubjectAdapter) mSubjectList.getAdapter()).remove(viewHolder.getAdapterPosition());
                mSubjectList.getAdapter().notifyDataSetChanged();

                Snackbar.make(mSubjectList, R.string.item_deleted, Snackbar.LENGTH_LONG)
                        .setAction(R.string.undo_action, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                ((SubjectAdapter) mSubjectList.getAdapter()).restore(originalPosition);
                                mSubjectList.getAdapter().notifyDataSetChanged();
                                mDatabase.addSubject(((SubjectAdapter.SubjectHolder) viewHolder).title.getText().toString());
                            }
                        }).show();
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

    public void onAddSubjectButtonPressed(View v) {
        String name = ((TextView) findViewById(R.id.new_subject_input)).getText().toString();
        ((SubjectAdapter) mSubjectList.getAdapter()).add(name);
        mSubjectList.getAdapter().notifyDataSetChanged();
        mSubjectList.scrollToPosition(mSubjectList.getAdapter().getItemCount() - 1);
        mDatabase.addSubject(name);

        ((EditText) findViewById(R.id.new_subject_input)).setText("");
    }
}


