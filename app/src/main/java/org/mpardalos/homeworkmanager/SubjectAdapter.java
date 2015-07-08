package org.mpardalos.homeworkmanager;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;


class SubjectAdapter extends RecyclerView.Adapter<SubjectAdapter.SubjectHolder> implements UndoAdapter {

    private ArrayList<String> mSubjects;
    private HashMap<Integer, String> mRemovedSubjects;

    SubjectAdapter(ArrayList<String> subjects) {
        this.mSubjects = subjects;
        this.mRemovedSubjects = new HashMap<>();
    }

    @Override
    public SubjectHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.subject, parent, false);
        return new SubjectHolder(view);
    }

    @Override
    public void onBindViewHolder(SubjectHolder subjectHolder, int position) {
        subjectHolder.title.setText(mSubjects.get(position));
    }

    @Override
    public int getItemCount() {
        return mSubjects.size();
    }

    public void remove(int position) {
        mRemovedSubjects.put(position, mSubjects.get(position));
        mSubjects.remove(position);
    }

    @Override
    public void restore(int position) {
        mSubjects.add(position, mRemovedSubjects.get(position));
    }

    public void add(String name) {
        mSubjects.add(name);
    }

    class SubjectHolder extends RecyclerView.ViewHolder {
        TextView title;

        public SubjectHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.subject_name);
        }
    }
}