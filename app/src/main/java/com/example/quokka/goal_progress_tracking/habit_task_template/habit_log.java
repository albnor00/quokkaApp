package com.example.quokka.goal_progress_tracking.habit_task_template;

import java.io.Serializable;
import java.util.Date;

public class habit_log implements Serializable {
    private String id;
    private int log;
    private String note;
    private Date date;

    public habit_log() {
        // Default constructor required for Firestore deserialization
    }

    public habit_log(String id, int log, String note, Date date){
        this.id = id;
        this.log = log;
        this.note = note;
        this.date = date;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getLog() {
        return log;
    }

    public void setLog(int log) {
        this.log = log;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setDateString(Date Date) {this.date = Date; }
}

