package com.inilabs.dnd.db;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.net.ParseException;
import android.util.Log;

import com.inilabs.dnd.ContactEntity;
import com.inilabs.dnd.utils.ProfileEntity;

public class ProfileDB extends DbManager {

	public static final String TABLE = "profile";
	public static final String TABLE_CONTACT = "contact";
	public static final String COLUMN_ID = "_id";
	public static final String NAME = "pname";
	public static final String STATUS = "status";
	public static final String START_TIME = "starttime";
	public static final String END_TIME = "endtime";
	public static final String PHN_NUMBER = "number";
	public static final String CONTACT_TYPE = "type";


	ContentValues values;
	DBAdaptor dba;
	String qry = "";
	Context context;
	Cursor cursor;
	List<ProfileEntity> list;

	private static final String DATABASE_CREATE = "create table if not exists " + TABLE + "("
			+ COLUMN_ID + " integer primary key autoincrement, " + NAME
			+ " text not null, " + START_TIME + " text not null, " + END_TIME
			+ " text not null, " + STATUS
			+ " integer not null);";

	private static final String CONTACT_TABLE = "create table if not exists " + TABLE_CONTACT + "("
			+ COLUMN_ID + " integer primary key autoincrement, " + NAME
			+ " text not null, "+ CONTACT_TYPE
			+ " text not null, " + PHN_NUMBER
			+ " text not null);";

	public ProfileDB(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
		this.context = context;
		dba = new DBAdaptor(context);
		createTable();

	}

	public void open() throws SQLException {
		sqliteDataBase = dba.open();
	}

	public void close() {
		dba.close();
	}

	@Override
	public void onCreate(SQLiteDatabase db) {

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

	public long createProfile(ProfileEntity profile) {
		long l = 0;
		try {
			open();
			values = new ContentValues();
			values.put(NAME, profile.getProfileName());
			values.put(START_TIME, profile.getStartTime());
			values.put(END_TIME, profile.getEndTime());
			values.put(STATUS, profile.getStatus());
			l = sqliteDataBase.insert(TABLE, null, values);
		} catch (Exception e) {
			Log.v("UserDataBase", "Exception inside insert -> " + e.toString());
		} finally {
			close();
		}
		return l;
	}

	public long updateProfileStatus(int column_id , int status){
		long l = 0;
		try{
			open();
			values = new ContentValues();
			values.put(STATUS, status);
			sqliteDataBase.update(TABLE, values, COLUMN_ID +" = "+ column_id, null);
		}catch(Exception e){
			Log.v("ProfileDB", "Exception inside updateProfile");
		}finally{
			close();
		}

		return l;
	}

	public int updateProfile(ProfileEntity profile){
		int l = 0;
		try{
			open();
			values = new ContentValues();
			values.put(NAME, profile.getProfileName());
			values.put(START_TIME, profile.getStartTime());
			values.put(END_TIME, profile.getEndTime());
			values.put(STATUS, profile.getStatus());
			l = sqliteDataBase.update(TABLE, values, COLUMN_ID +" = "+ profile.getId(), null);
			values.clear();

			values.put(NAME, profile.getProfileName());
			sqliteDataBase.update(TABLE_CONTACT, values, NAME +"=?", new String[]{ profile.getOldProfileName()});
		}catch(Exception e){
			Log.v("ProfileDB", "Exception inside updateProfile");
		}finally{
			close();
		}

		return l;
	}


	public int deleteProfile(ProfileEntity profile){
		int l = 0;
		try{
			open();
			l = sqliteDataBase.delete(TABLE, COLUMN_ID +" = "+ profile.getId(), null);
			l = sqliteDataBase.delete(TABLE_CONTACT, NAME +" ='"+ profile.getProfileName()+"'", null);

		}catch(Exception e){
			Log.v("ProfileDB", "Exception inside DeleteProfile");
		}finally{
			close();
		}

		return l;
	}

	public int deleteContact(ContactEntity contact){
		int l = 0;
		try{
			open(); 
			Cursor c =sqliteDataBase.rawQuery("select * from "+TABLE_CONTACT, null);
			if(c.getCount()>0){
				c.moveToFirst();
				do{
					String cont = c.getString(c.getColumnIndex(NAME)) +"---"+c.getString(c.getColumnIndex(PHN_NUMBER));
					Log.v("Delete", cont);
				}while(c.moveToNext());
			}
			l = sqliteDataBase.delete(TABLE_CONTACT, NAME +" ='"+ contact.getProfileName()+"' AND "+PHN_NUMBER+" ='"+
					contact.getContactNo()+"'", null);
			Log.v("Deleted", ""+l);
		}catch(Exception e){
			Log.v("ProfileDB", "Exception inside DeleteProfile");
		}finally{
			close();
		}

		return l;
	}

	public long insertContacts(ArrayList<ContactEntity> cList,String pName, String type) {
		long l = 0;
		try {
			open();
			//l = sqliteDataBase.delete(TABLE_CONTACT, NAME +" ='"+ pName+"' AND "+CONTACT_TYPE+" ='"+type+"'", null);
			for(int i=0; i<cList.size(); i++){
				values = new ContentValues();
				values.put(NAME, pName);
				values.put(PHN_NUMBER, cList.get(i).getContactNo());
				values.put(CONTACT_TYPE, type);
				l = sqliteDataBase.insert(TABLE_CONTACT, null, values);
				Log.v("Values-->", ""+values);
			}
		} catch (Exception e) {
			Log.v("UserDataBase", "Exception inside insert -> " + e.toString());
		} finally {
			close();
		}
		return l;
	}

	@SuppressWarnings("finally")
	public List<ProfileEntity> getProfiles() {
		list = new ArrayList<ProfileEntity>();
		try {
			open();
			ProfileEntity profile;
			qry = "SELECT * FROM " + TABLE;
			cursor = sqliteDataBase.rawQuery(qry, null);
			if (cursor.getCount() > 0) {
				cursor.moveToFirst();
				do {
					profile = new ProfileEntity();
					profile.setId(cursor.getInt(cursor.getColumnIndex(COLUMN_ID)));
					profile.setProfileName(cursor.getString(cursor.getColumnIndex(NAME)));
					profile.setStartTime(cursor.getString(cursor.getColumnIndex(START_TIME)));
					profile.setEndTime(cursor.getString(cursor.getColumnIndex(END_TIME)));
					profile.setStatus(cursor.getInt(cursor.getColumnIndex(STATUS)));
					list.add(profile);
				} while (cursor.moveToNext());
			}
		} catch (Exception e) {
			Log.v("UserDataBase", "Exception inside retrive -> " + e.toString());
		} finally {
			close();
			return list;
		}
	}

	@SuppressWarnings("finally")
	public String getBlockedContacts(String name) {
		String list = "";
		try {
			open();
			qry = "SELECT * FROM " + TABLE_CONTACT +" where "+NAME+"=?" ;
			cursor = sqliteDataBase.rawQuery(qry, new String[]{name});
			if (cursor.getCount() > 0) {
				cursor.moveToFirst();
				do {
					list += cursor.getString(cursor.getColumnIndex(PHN_NUMBER))+"~";
				} while (cursor.moveToNext());
			}
		} catch (Exception e) {
			Log.v("UserDataBase", "Exception inside retrive -> " + e.toString());
		} finally {
			close();
			Log.v("Block Cont", list);
			return list;
		}
	}

	public void createTable() {
		try {
			open();
			//sqliteDataBase.execSQL("DROP TABLE IF EXISTS " + TABLE);
			sqliteDataBase.execSQL(DATABASE_CREATE);
			sqliteDataBase.execSQL(CONTACT_TABLE);
		} catch (Exception e) {
			Log.v("UserDataBase", "Exception -> " + e.toString());
		} finally {
			close();
		}
	}



	public long deleteRecordsFromDb() {
		long a = 0;
		try {
			open();
			a = sqliteDataBase.delete(TABLE, null, null);
		} catch (Exception e) {
			Log.v("UserDataBase", "Exception inside delete -> " + e.toString());
		} finally {
			close();

		}
		return a;
	}

	public long updateRecordDetail(String column_id,
			HashMap<String, String> newMap) {
		long a = 0;
		try {
			open();
			values = new ContentValues();
			values.put(NAME, newMap.get(NAME));
			values.put(START_TIME, newMap.get(START_TIME));
			values.put(END_TIME, newMap.get(END_TIME));
			a = sqliteDataBase.update(TABLE, values, COLUMN_ID + " = " + column_id,
					null);
		} catch (Exception e) {
			Log.v("UserDataBase", "Exception inside update -> " + e.toString());
		} finally {
			close();
		}
		return a;
	}

	public boolean checkIfBlockNumber(String number){
		boolean blockCall = false;
		try{
			open();
			cursor = sqliteDataBase.rawQuery("select * from "+TABLE +" where "+STATUS +" = 1", null);
			if(cursor.getCount()>0){
				cursor.moveToFirst();
				do{
					String start = cursor.getString(cursor.getColumnIndex(START_TIME));
					String end = cursor.getString(cursor.getColumnIndex(END_TIME));
					String pname = cursor.getString(cursor.getColumnIndex(NAME));
					boolean profileActive;
					if(pname.equals("Default"))
						profileActive = true;
					else
						profileActive= checkCustomTime(start, end);
					if(profileActive){
						Cursor innerCursor = sqliteDataBase.rawQuery("select * from "+TABLE_CONTACT +" where ("+PHN_NUMBER
								+" like '%"+number+"' AND "+NAME+" = '"+pname+"' )", null);
						if(innerCursor.getCount()>0){
							blockCall = true;
							cursor.moveToLast();
							return blockCall;
						}
					}
				}while(cursor.moveToNext());
			}
		}catch(Exception e){

		}finally{
			close();
		}

		return blockCall;
	}
	public void checkTimeInBetween(String starttime, String endtime){
		try {
			Date time1 = null;
			try {
				time1 = new SimpleDateFormat("HH:mm", Locale.US).parse(starttime);
			} catch (java.text.ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Calendar calendar1 = Calendar.getInstance();
			calendar1.setTime(time1);

			Date time2 = null;
			try {
				time2 = new SimpleDateFormat("HH:mm", Locale.US).parse(endtime);
			} catch (java.text.ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Calendar calendar2 = Calendar.getInstance();
			calendar2.setTime(time2);
			calendar2.add(Calendar.DATE, 1);

			String currentTime = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)+":"+Calendar.getInstance().get(Calendar.MINUTE);
			Date d = null;
			try {
				d = new SimpleDateFormat("HH:mm", Locale.US).parse(currentTime);
			} catch (java.text.ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Calendar calendar3 = Calendar.getInstance();
			calendar3.setTime(d);
			calendar3.add(Calendar.DATE, 1);

			Date x = calendar3.getTime();
			if (x.after(calendar1.getTime()) && x.before(calendar2.getTime())) {
				//checkes whether the current time is between 14:49:00 and 20:11:13.
				System.out.println(true);
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	public boolean checkCustomTime(String starttime, String endtime){
		boolean pmToam = false;
		boolean timeInBetween = false;

		String arrStart[] = starttime.split(":");
		int hourStart = Integer.parseInt(arrStart[0]);
		int minStart = Integer.parseInt(arrStart[1]);

		String arrEnd[] = endtime.split(":");
		int hourEnd = Integer.parseInt(arrEnd[0]);
		int minEnd = Integer.parseInt(arrEnd[1]);

		int hourCurrent = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
		int minCurrent = Calendar.getInstance().get(Calendar.MINUTE);

		
		if(hourStart > hourEnd && hourEnd!=0){
			pmToam = true;
		}else{
			hourEnd+=24;
		}

		if(pmToam){
			hourEnd+=12;
			if(hourCurrent > hourStart){
				if(hourCurrent <= hourEnd){
					timeInBetween =true;
				}
			}else if(hourCurrent == hourStart){
				if(minCurrent < minStart){
					timeInBetween = false;
				}else{
					if(hourCurrent < hourEnd){
						timeInBetween = true;
					}else if( hourCurrent == hourEnd){
						if(minCurrent < minEnd){
							timeInBetween =true;
						}else{
							timeInBetween = false;
						}
					}
				}
			}else if(hourCurrent < hourStart){
				if(hourCurrent < hourEnd){
					timeInBetween = true;
				}else if(hourCurrent == hourEnd){
					if(minCurrent <= minEnd){
						timeInBetween = true;
					}
				}
			}

			Log.v("************** CustomTime 1: ", ""+timeInBetween);
		}else{
			if(hourCurrent > hourStart){
				if(hourCurrent < hourEnd){
					timeInBetween =true;
				}else if(hourCurrent == hourEnd){
					if(minCurrent<=minEnd){
						timeInBetween = true;
					}
				}
			}else if(hourCurrent == hourStart){
				if(minCurrent < minStart){
					timeInBetween = false;
				}else{
					if(hourCurrent < hourEnd){
						timeInBetween = true;
					}else if( hourCurrent == hourEnd){
						if(minCurrent < minEnd){
							timeInBetween =true;
						}else{
							timeInBetween = false;
						}
					}
				}
			}

			Log.v("************** CustomTime: ", ""+timeInBetween);

		}

		return timeInBetween;
	}

	public boolean checkTime(String starttime, String endtime){
		boolean profileActive = false;
		try{
			Date time11 = new SimpleDateFormat("HH:mm").parse(starttime);
			Calendar calendar1 = Calendar.getInstance();
			calendar1.setTime(time11);

			Date time22 = new SimpleDateFormat("HH:mm").parse(endtime);
			Calendar calendar2 = Calendar.getInstance();
			calendar2.setTime(time22);

			String cT = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)+":"+Calendar.getInstance().get(Calendar.MINUTE);
			Date currentTime = new SimpleDateFormat("HH:mm").parse(cT);
			Calendar startingCalendar = Calendar.getInstance();
			startingCalendar.setTime(currentTime);
			//startingCalendar.add(Calendar.DATE, 1);



			//let's say we have to check about 01:00:00
			Date d = new SimpleDateFormat("HH:mm").parse("00:00");
			Calendar calendar3 = Calendar.getInstance();
			calendar3.setTime(d);

			Log.v("Cal 1", ""+calendar1.getTime());
			Log.v("Cal 2", ""+calendar2.getTime());
			Log.v("Cal 3", ""+calendar3.getTime());
			if(startingCalendar.getTime().after(calendar1.getTime()))
			{
				calendar2.add(Calendar.DATE, 1);

				calendar3.add(Calendar.DATE, 1);

				Log.v("Cal 22", ""+calendar2.getTime());
				Log.v("Cal 33", ""+calendar3.getTime());
			}

			Date x = startingCalendar.getTime();
			Log.v("Cal start", ""+startingCalendar.getTime());

			if (x.after(calendar1.getTime()) && x.before(calendar2.getTime())) 
			{
				profileActive = true;
				System.out.println("Time is in between..");
			}
			else
			{
				profileActive = false;
				System.out.println("Time is not in between..");
			}

		} catch (ParseException e) 
		{
			e.printStackTrace();
		} catch (java.text.ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return profileActive;
	}
}