package com.inilabs.dnd.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class DbManager extends SQLiteOpenHelper{
	public static final String DATABASE_NAME = "userProfile";
	public static final int DATABASE_VERSION = 1;
	SQLiteDatabase sqliteDataBase;
	Context context;
	
	public DbManager(Context cntxt, String name, CursorFactory factory,
			int version) {
		super(cntxt, name, factory, version);
		context = cntxt;
		
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {	
	}	
}
