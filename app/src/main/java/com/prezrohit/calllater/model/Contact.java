package com.prezrohit.calllater.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "contact")
public class Contact {

	@ColumnInfo(name = "name")
	private String name;

	@PrimaryKey(autoGenerate = true)
	@ColumnInfo(name = "id")
	private int id;

	@ColumnInfo(name = "schedule_time")
	private String scheduleTime;

	@ColumnInfo(name = "number")
	private String number;

	@ColumnInfo(name = "status")
	private int status;

	@ColumnInfo(name = "hangup_value")
	private int hangupValue;

	public Contact(String name, String scheduleTime, String number, int status, int hangupValue) {
		this.name = name;
		this.scheduleTime = scheduleTime;
		this.number = number;
		this.status = status;
		this.hangupValue = hangupValue;
	}

	public int getHangupValue() {
		return hangupValue;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getNumber() {
		return number;
	}

	public int getStatus() {
		return status;
	}

	public String getName() {
		return name;
	}

	public String getScheduleTime() {
		return scheduleTime;
	}

	@Override
	public String toString() {
		return "Contact{" +
				"name='" + name + '\'' +
				", id=" + id +
				", scheduleTime='" + scheduleTime + '\'' +
				", number='" + number + '\'' +
				", status=" + status +
				", hangupValue=" + hangupValue +
				'}';
	}
}