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