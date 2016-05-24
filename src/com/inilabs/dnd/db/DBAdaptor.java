package com.inilabs.dnd.db;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class DBAdaptor {

	private Context context;
	private SQLiteDatabase database;
	private DbManager dbHelper;

	public DBAdaptor(Context context) {
		this.context = context;

	}

	public SQLiteDatabase open() throws SQLException {
		if (dbHelper == null) {
			dbHelper = new DbManager(context, DbManager.DATABASE_NAME, null,
					DbManager.DATABASE_VERSION);
		}
		if (database == null) {
			database = dbHelper.getWritableDatabase();
		} else {
			if (!database.isOpen()) {
				database = dbHelper.getWritableDatabase();
			}
		}
		return database;
	}

	public void close() {
		try {
			dbHelper.close();
			database.close();
		} catch (Exception e) {

		}
	}
}