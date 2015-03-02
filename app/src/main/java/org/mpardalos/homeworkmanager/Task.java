package org.mpardalos.homeworkmanager;

import android.os.Parcel;
import android.os.Parcelable;

import org.joda.time.LocalDate;

/**
 * Represents a specific homework task/assignment.
 */
public class Task implements Parcelable {
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Task createFromParcel(Parcel in) {
            return new Task(in);
        }

        public Task[] newArray(int size) {
            return new Task[size];
        }
    };

    private String subject;
    private String description;
    private LocalDate dueDate;
    private boolean done;
    private int databaseId;

    /**
     * Use when the Task already exists in the database, which means that its id is known
     *
     * @param subject     the name of the subject of the task
     * @param description a description of the task
     * @param dueDate     date when the task is due
     * @param databaseId  the _id field of the entry of the task in the database
     * @param done        whether the task is done
     */
    public Task(String subject, String description, LocalDate dueDate, int databaseId,
                boolean done) {
        this.subject = subject;
        this.description = description;
        this.dueDate = dueDate;
        this.databaseId = databaseId;
        this.done = done;
    }

    public Task(String subject, String description, LocalDate dueDate, boolean done) {
        /**
         * Use when the task does not yet exist in the database or we don't know its id
         */
        this.subject = subject;
        this.description = description;
        this.dueDate = dueDate;
        this.databaseId = -1;
        this.done = done;
    }

    public Task(Parcel in) {

        this.subject = in.readString();
        this.description = in.readString();
        this.dueDate = (LocalDate) in.readSerializable();
        this.databaseId = in.readInt();
        this.done = in.readInt() != 0;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(subject);
        dest.writeString(description);
        dest.writeSerializable(dueDate);
        dest.writeInt(databaseId);
        dest.writeInt(done ? 1 : 0);
    }


    public String getSubject() {
        return subject;
    }

    public String getDescription() {
        return description;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public boolean isDone() {
        return done;
    }

    /**
     * @return the databaseId. Defaults to -1 if it has not been set
     */
    public int getDatabaseId() {
        return databaseId;
    }
}