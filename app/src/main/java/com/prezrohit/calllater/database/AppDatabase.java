package com.prezrohit.calllater.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.prezrohit.calllater.dao.ContactDao;
import com.prezrohit.calllater.helper.DateTimeConverter;
import com.prezrohit.calllater.model.Contact;

@Database( entities = {Contact.class}, version = 1, exportSchema = false)
@TypeConverters({DateTimeConverter.class})
public abstract class AppDatabase extends RoomDatabase {

	private static AppDatabase appDatabase;

	public static AppDatabase getInstance(Context context){
		if(appDatabase == null){
			String DB_NAME = "CallScheduler";
			appDatabase = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, DB_NAME)
					.allowMainThreadQueries()
					.build();
		}
		return appDatabase;
	}

	public abstract ContactDao contactDao();

	public static void destroyInstance() {
		appDatabase = null;
	}

	@Override
	public void clearAllTables() {

	}
}
