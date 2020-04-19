package com.prezrohit.calllater.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.prezrohit.calllater.model.Contact;

import java.util.List;

@Dao
public interface ContactDao {

	@Insert
	long insert(Contact contact);

	@Query("SELECT * FROM contact WHERE id = :id")
	Contact getContactById(long id);

	@Query("SELECT * FROM contact")
	LiveData<List<Contact>> getAllContact();

	@Query("UPDATE contact SET status = :status WHERE id = :id")
	void updateStatus(int id, int status);

	@Query("UPDATE contact SET hangup_value = :hangupValue, schedule_time = :scheduleTime where id = :id")
	void updateScheduleAndHangupTime(int id, int hangupValue, String scheduleTime);

	@Delete
	void deleteContact(Contact contact);
}
