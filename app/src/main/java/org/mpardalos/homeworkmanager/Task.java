package org.mpardalos.homeworkmanager;

import org.joda.time.LocalDate;

public class Task {
    private String subject;
    private String description;
    private LocalDate dueDate;
    private int databaseId;
    private boolean done;

    public Task(String subject, String description, LocalDate dueDate, int databaseId,
                boolean done) {
        this.subject = subject;
        this.description = description;
        this.dueDate = dueDate;
        this.databaseId = databaseId;
        this.done = done;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public int getDatabaseId() {
        return databaseId;
    }
}
