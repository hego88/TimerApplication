package com.hricigor.timerapplication;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class TimerDbHelper extends SQLiteOpenHelper {

	final static String TABLE_NAME = "timers";
	final static String TIMER_NAME = "name";
	final static String TIMER_VALUE = "value";
	final static String _ID = "_id";
	final static String[] columns = { TIMER_NAME, TIMER_VALUE, _ID };

	final private static String CREATE_CMD =

	"CREATE TABLE " + TABLE_NAME + " (" 
			+ _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " 
			+ TIMER_NAME + " TEXT NOT NULL, " 
			+ TIMER_VALUE + " INTEGER NOT NULL)";

	final private static String NAME = "time_db";
	final private static Integer VERSION = 1;
	final private Context mContext;

	public TimerDbHelper(Context context) {
		super(context, NAME, null, VERSION);
		this.mContext = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		
		db.execSQL(CREATE_CMD);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// N/A
	}

	void deleteDatabase() {
		mContext.deleteDatabase(NAME);
	}
}
