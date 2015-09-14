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

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Parcel;
import android.os.Parcelable;

import org.joda.time.LocalDate;

import java.io.File;

/**
 * Represents a specific homework task/assignment.
 */
public class Task implements Parcelable {
    public static final int NO_DATABASE_ID = -1;

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Task createFromParcel(Parcel in) {
            return new Task(in);
        }

        public Task[] newArray(int size) {
            return new Task[size];
        }
    };

    private String mSubject;
    private String mDescription;
    private LocalDate mDueDate;
    private int mDatabaseId;
    private File mPhotoFile;

    /**
     * Use when the Task already exists in the database, which means that its id is known
     *
     * @param subject     the name of the Subject of the task
     * @param description a Description of the task
     * @param dueDate     date when the task is due
     * @param databaseId  the _id field of the entry of the task in the database, NO_DATABASE_ID if none exists
     * @param photoFile   the file which contains the task's photo
     */
    public Task(String subject, String description, LocalDate dueDate, int databaseId, File photoFile) {
        this.mSubject = subject;
        this.mDescription = description;
        this.mDueDate = dueDate;
        this.mDatabaseId = databaseId;
        this.mPhotoFile = photoFile;
    }

    public Task(Parcel in) {

        this.mSubject = in.readString();
        this.mDescription = in.readString();
        this.mDueDate = (LocalDate) in.readSerializable();
        this.mDatabaseId = in.readInt();
        this.mPhotoFile = (File) in.readSerializable();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mSubject);
        dest.writeString(mDescription);
        dest.writeSerializable(mDueDate);
        dest.writeInt(mDatabaseId);
        dest.writeSerializable(mPhotoFile);
    }

    public String getSubject() {
        return mSubject;
    }

    public String getDescription() {
        return mDescription;
    }

    public LocalDate getDueDate() {
        return mDueDate;
    }

    /**
     * @return the mDatabaseId. Defaults to -1 if it has not been set
     */
    public int getDatabaseId() {
        return mDatabaseId;
    }

    public Bitmap getPhoto() {
        if (mPhotoFile != null) {
            return BitmapFactory.decodeFile(mPhotoFile.getAbsolutePath());
        }

        return null;
    }

    public File getPhotoFile() {
        return mPhotoFile;
    }


}
