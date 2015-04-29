package org.mpardalos.homeworkmanager;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;


public abstract class ActionBarRecyclerViewActivity extends ActionBarActivity {
    protected final RecyclerView.OnItemClickListener mOnClickListener = new AdapterView
            .OnItemClickListener() {
        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
            onListItemClick((ListView) parent, v, position, id);
        }
    };
    private RecyclerView mRecyclerView;

    protected RecyclerView getRecyclerView() {
        if (mRecyclerView == null) {
            mRecyclerView = (RecyclerView) findViewById(android.R.id.list);
        }
        return mRecyclerView;
    }

    protected void onListItemClick(ListView l, View v, int position, long id) {
    }
}
