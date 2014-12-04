package mpardalos.org.homeworkmanager;

import android.view.Menu;
import android.view.MenuItem;

/**
 * Practically the same as TaskAdd except for te actionBar which also includes a delete button
 */
public class TaskEdit extends TaskAdd {

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_task_details, menu);
        return true;
    }
}
