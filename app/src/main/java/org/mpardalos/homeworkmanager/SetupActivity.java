package org.mpardalos.homeworkmanager;


import android.app.ListFragment;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;


public class SetupActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.fragment_placeholder, new SubjectSetupFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_setup, menu);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //fragment(s)
    public static class SubjectSetupFragment extends ListFragment {
        private TaskDatabaseHelper mDatabase;

        @Override
        public void onCreate(Bundle bundle) {
            mDatabase = new TaskDatabaseHelper(getActivity().getApplicationContext());
            super.onCreate(bundle);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.fragment_subjects_setup, container, false);

            Cursor subjectCursor = mDatabase.getSubjects();
            ArrayList<String> subjects = new ArrayList<>();
            subjectCursor.moveToPosition(-1);
            int column = subjectCursor.getColumnIndex(TaskDatabaseHelper.SUBJECT_NAME);
            while (subjectCursor.moveToNext()) {
                subjects.add(subjectCursor.getString(column));
            }

            setListAdapter(
                    new ArrayAdapter<String>(getActivity().getApplicationContext(),
                                             R.layout.subject_list_entry, android.R.id.text1,
                                             subjects) {

                        public View getView(int position, View convertView, ViewGroup parent) {
                            View view = super.getView(position, convertView, parent);
                            view.findViewById(R.id.delete_button).setOnClickListener(
                                    new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            onDeleteButtonClicked(v);
                                        }
                                    }
                                                                                    );
                            return view;
                        }

                    }
                          );

            return rootView;
        }

        public void onDeleteButtonClicked(View v) {
            String subject = ((TextView) ((View) v.getParent()).findViewById(android.R.id.text1))
                    .getText().toString();
            mDatabase.deleteSubject(subject);
            ((ArrayAdapter<String>) getListAdapter()).remove(subject);
            ((ArrayAdapter<String>) getListAdapter()).notifyDataSetChanged();
        }
    }
}
