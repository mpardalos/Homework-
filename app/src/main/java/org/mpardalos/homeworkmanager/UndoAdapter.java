package org.mpardalos.homeworkmanager;

public interface UndoAdapter {

    void remove(int position);

    void restore(int position);
}
