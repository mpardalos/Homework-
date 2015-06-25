package org.mpardalos.homeworkmanager;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;


class SubjectAdapter extends RecyclerView.Adapter<SubjectAdapter.SubjectHolder> {

    private ArrayList<String> mSubjects;

    SubjectAdapter(ArrayList<String> subjects) {
        this.mSubjects = subjects;
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
        mSubjects.remove(position);
    }

    class SubjectHolder extends RecyclerView.ViewHolder {
        TextView title;

        public SubjectHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.subject_name);
        }
    }
}