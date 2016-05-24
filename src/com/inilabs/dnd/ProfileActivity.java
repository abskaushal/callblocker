package com.inilabs.dnd;


import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import android.app.ActionBar;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.ads.AdRequest;
import com.google.ads.AdView;
import com.inilabs.dnd.adapter.CallsLogAdapter;
import com.inilabs.dnd.adapter.ContactAdapter;
import com.inilabs.dnd.adapter.RejectListAdapter;
import com.inilabs.dnd.db.DbManager;
import com.inilabs.dnd.db.ProfileDB;
import com.inilabs.dnd.utils.Constants;
import com.inilabs.dnd.utils.ProfileEntity;
import com.inilabs.dnd.utils.Util;

public class ProfileActivity extends FragmentActivity {

	ActionBar mActionBar;
	Context mContext;
	ArrayList<ContactEntity> contactListArr, logArrayList, rejectListArray;
	ListView cList;
	ContactAdapter cAdapter;
	CallsLogAdapter callsLogAdapter;
	RejectListAdapter rejectListAdapter;
	String profileName="";
	static EditText startEt, endEt;
	ProfileDB pDb;
	String startTime ,endTime;
	int id;
	String blockedContacts="";
	int TYPE = 1;
	public static String TYPE_CONTACT= "1";
	public static String TYPE_CALL_LOG= "2";
	public static String TYPE_REJECT_LIST= "3";
	Menu menu;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_profile_list);
		mContext = ProfileActivity.this;
		AdView mAdView = (AdView) findViewById(R.id.adView);
	    AdRequest adRequest = new AdRequest();
	    mAdView.loadAd(adRequest);
		cList = (ListView)findViewById(R.id.contact_list);
		mActionBar = getActionBar();
		profileName = getIntent().getStringExtra("name");
		startTime = getIntent().getStringExtra("starttime");
		endTime = getIntent().getStringExtra("endtime");
		id = getIntent().getIntExtra("id", -1);
		mActionBar.setTitle(profileName);
		mActionBar.setDisplayUseLogoEnabled(false);
		//mActionBar.setBackgroundDrawable(new ColorDrawable(Color.GRAY));
		mActionBar.setDisplayHomeAsUpEnabled(true);
		pDb = new ProfileDB(mContext, DbManager.DATABASE_NAME, null, DbManager.DATABASE_VERSION);

		getAllContacts(getContentResolver());
		cAdapter = new ContactAdapter(mContext, R.layout.contact_row, contactListArr);
		cList.setAdapter(cAdapter);
	}


	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		if(profileName.equals("Default")){
			getMenuInflater().inflate(R.menu.profile_list_default, menu);
		}else{
			getMenuInflater().inflate(R.menu.profile_list, menu);
		}
		this.menu = menu;
		return true;
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;

		case R.id.action_add:

			if(TYPE==1)
				cAdapter.insertContactsWithProfile(profileName, TYPE_CONTACT);
			else if(TYPE==2)
				callsLogAdapter.insertContactsWithProfile(profileName, TYPE_CALL_LOG);
			//else if(TYPE==3)
			//rejectListAdapter.insertContactsWithProfile(profileName, TYPE_REJECT_LIST);
			break;

		case R.id.action_contact:

			cList.setFastScrollAlwaysVisible(true);
			cList.setFastScrollEnabled(true);
			getAllContacts(getContentResolver());

			changeMenu(true);
			cAdapter = new ContactAdapter(mContext, R.layout.contact_row, contactListArr);
			cList.setAdapter(cAdapter);
			break;

		case R.id.action_log:

			cList.setFastScrollAlwaysVisible(false);
			cList.setFastScrollEnabled(false);
			new GetCallLogAsync().execute();
			break;

		case R.id.action_reject:

			cList.setFastScrollAlwaysVisible(false);
			cList.setFastScrollEnabled(false);
			new GetRejectListAsync().execute();
			break;

		case R.id.action_edit:
			editProfile();
			break;

		case R.id.action_delete:
			showDeleteProfileDialog();
			break;

		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	//@Override
	public void changeMenu(boolean visibility) {
		MenuItem itemMenu = menu.findItem(R.id.action_add);
		itemMenu.setVisible(visibility);
	}

	public void showDeleteProfileDialog(){

		final Dialog deleteDia = new Dialog(mContext, android.R.style.Theme_Translucent_NoTitleBar);
		deleteDia.setContentView(R.layout.delete_confirm);

		((Button)deleteDia.findViewById(R.id.cancel)).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				deleteDia.dismiss();
			}
		});
		((Button)deleteDia.findViewById(R.id.delete)).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				deleteProfile();
				deleteDia.dismiss();
			}
		});

		deleteDia.show();

	}
	public void deleteProfile(){
		ProfileEntity pEntity = new ProfileEntity();
		pEntity.setId(id);
		pEntity.setProfileName(profileName);
		int res = pDb.deleteProfile(pEntity);
		if(res >=0){
			Toast.makeText(mContext, "Profile has been deleted", Toast.LENGTH_SHORT).show();
			Constants.refresh = true;
			ProfileActivity.this.finish();
		}else{
			Toast.makeText(mContext, "Profile cannot be deleted", Toast.LENGTH_SHORT).show();	
		}
	}

	public  void getAllContacts(ContentResolver cr) {

		TYPE = 1;
		blockedContacts = pDb.getBlockedContacts(profileName);
		//String blocked_contacts = mPref.getString(Constants.SELECTED_CONACTS, "");
		Cursor phones = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,null,null, null);
		//ContactEntity entity;
		contactListArr = new ArrayList<ContactEntity>(); 

		if(phones.getCount()>0)
		{
			phones.moveToFirst();
			do
			{
				ContactEntity	entity = new ContactEntity();
				String pname=phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)).trim();
				String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)).trim();
				phoneNumber = phoneNumber.replaceAll(" ", "");
				phoneNumber = phoneNumber.replaceAll("[^0-9]", "");
				if(phoneNumber.length()>10)
					phoneNumber = phoneNumber.substring(phoneNumber.length()-10);
				entity.setContactNo(phoneNumber);
				entity.setName(pname);
				entity.setProfileName(profileName);

				if(blockedContacts.contains(phoneNumber) && phoneNumber.length()>6)
					entity.setIsChecked("2");
				else
					entity.setIsChecked("0");

				entity.setType("1");
				contactListArr.add(entity);

			}while (phones.moveToNext());

		}

		phones.close();

		Collections.sort(contactListArr, new Comparator<ContactEntity>() {
			@Override
			public int compare(ContactEntity  contact1, ContactEntity  contact2)
			{

				return  contact1.getName().compareToIgnoreCase((contact2.getName()));
			}
		});
	}


	@SuppressWarnings("deprecation")
	private void getCallLog() { 
		TYPE = 2;
		int i=1;
		logArrayList = new ArrayList<ContactEntity>();
		blockedContacts = pDb.getBlockedContacts(profileName);
		ArrayList<ContactEntity> temp = new ArrayList<ContactEntity>();
		//String blocked_contacts = mPref.getString(Constants.SELECTED_CONACTS, "");
		ContactEntity entity;
		Cursor managedCursor = getContentResolver().query(CallLog.Calls.CONTENT_URI, null, null, null, CallLog.Calls.DATE + " DESC"); 
		int number = managedCursor.getColumnIndex(CallLog.Calls.NUMBER); 
		int type = managedCursor.getColumnIndex(CallLog.Calls.TYPE); 
		int date = managedCursor.getColumnIndex(CallLog.Calls.DATE);
		int duration = managedCursor.getColumnIndex(CallLog.Calls.DURATION);
		int name = managedCursor.getColumnIndex(CallLog.Calls.CACHED_NAME);

		while (managedCursor.moveToNext()) { 
			entity = new ContactEntity();
			String phNumber = managedCursor.getString(number);
			String callType = managedCursor.getString(type);
			String callDate = managedCursor.getString(date); 
			String nameStr = managedCursor.getString(name);
			phNumber = phNumber.replaceAll(" ", "");
			phNumber = phNumber.replaceAll("[^0-9]", "");
			if(phNumber.length()>10)
				phNumber = phNumber.substring(phNumber.length()-10);
			if(nameStr == null)
				nameStr = "Unknown";
			Log.v("duration", nameStr);
			Date callDayTime = new Date(Long.valueOf(callDate));
			SimpleDateFormat dfm = new SimpleDateFormat("dd-MMM-yyyy hh:mm");
			callDate = dfm.format(callDayTime);

			int dircode = Integer.parseInt(callType);
			entity.setContactNo(phNumber);
			entity.setName(nameStr);
			entity.setPriority(i);
			entity.setProfileName(profileName);
			i++;
			if(blockedContacts.contains(phNumber) && phNumber.length()>6)
				entity.setIsChecked("2");
			else
				entity.setIsChecked("0");
			entity.setType("2");

			switch (dircode) {

			case CallLog.Calls.OUTGOING_TYPE: 
				entity.setType("Outgoing: "+callDate);
				break; 

			case CallLog.Calls.INCOMING_TYPE:
				entity.setType("Incoming: "+callDate);
				break;

			case CallLog.Calls.MISSED_TYPE: 
				entity.setType("Missed: "+callDate);
				break; 
			} 
			if(temp.size()<=100)
				temp.add(entity);
		} 
		managedCursor.close();


		for(int j=temp.size(); j>0; j--)
			logArrayList.add(temp.get(j-1));

		Map<String, ContactEntity > map = new LinkedHashMap<String, ContactEntity>();
		for (ContactEntity ays : logArrayList) {
			map.put(ays.getContactNo(), ays);
		}
		logArrayList.clear();
		logArrayList.addAll(map.values());

		Collections.sort(logArrayList, new Comparator<ContactEntity>() {
			@Override
			public int compare(ContactEntity  contact1, ContactEntity  contact2)
			{

				return  (contact1.getPriority()<contact2.getPriority()?-1:1);
			}


		});
	} 



	public void getRejectList(ContentResolver cr){

		TYPE = 3;
		blockedContacts = pDb.getBlockedContacts(profileName);
		//String blocked_contacts = mPref.getString(Constants.SELECTED_CONACTS, "");
		Cursor phones = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,null,null, null);
		//ContactEntity entity;
		rejectListArray = new ArrayList<ContactEntity>(); 

		if(phones.getCount()>0)
		{
			phones.moveToFirst();
			do
			{
				ContactEntity	entity = new ContactEntity();
				String pname=phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)).trim();
				String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)).trim();
				phoneNumber = phoneNumber.replaceAll(" ", "");
				phoneNumber = phoneNumber.replaceAll("[^0-9]", "");
				if(phoneNumber.length()>10)
					phoneNumber = phoneNumber.substring(phoneNumber.length()-10);
				entity.setContactNo(phoneNumber);
				entity.setName(pname);
				entity.setType("3");
				entity.setProfileName(profileName);
				if(blockedContacts.contains(phoneNumber) && phoneNumber.length()>6){
					entity.setIsChecked("3");
					rejectListArray.add(entity);	
				}

			}while (phones.moveToNext());

		}

		phones.close();

		ArrayList<ContactEntity> temp = new ArrayList<ContactEntity>();
		//String blocked_contacts = mPref.getString(Constants.SELECTED_CONACTS, "");
		ContactEntity entity;
		Cursor managedCursor = getContentResolver().query(CallLog.Calls.CONTENT_URI, null, null, null, null); 
		int number = managedCursor.getColumnIndex(CallLog.Calls.NUMBER); 
		int type = managedCursor.getColumnIndex(CallLog.Calls.TYPE); 
		int date = managedCursor.getColumnIndex(CallLog.Calls.DATE);
		int duration = managedCursor.getColumnIndex(CallLog.Calls.DURATION);
		int name = managedCursor.getColumnIndex(CallLog.Calls.CACHED_NAME);

		while (managedCursor.moveToNext()) { 
			entity = new ContactEntity();
			String phNumber = managedCursor.getString(number);
			String callType = managedCursor.getString(type);
			String callDate = managedCursor.getString(date); 
			String nameStr = managedCursor.getString(name);
			phNumber = phNumber.replaceAll(" ", "");
			phNumber = phNumber.replaceAll("[^0-9]", "");
			if(phNumber.length()>10)
				phNumber = phNumber.substring(phNumber.length()-10);
			if(nameStr == null)
				nameStr = "Unknown";
			Log.v("duration", nameStr);
			Date callDayTime = new Date(Long.valueOf(callDate));
			SimpleDateFormat dfm = new SimpleDateFormat("dd-MMM-yyyy hh:mm");
			callDate = dfm.format(callDayTime);

			int dircode = Integer.parseInt(callType);
			entity.setContactNo(phNumber);
			entity.setName(nameStr);
			entity.setType("3");
			entity.setProfileName(profileName);
			switch (dircode) {

			case CallLog.Calls.OUTGOING_TYPE: 
				entity.setType("Outgoing: "+callDate);
				break; 

			case CallLog.Calls.INCOMING_TYPE:
				entity.setType("Incoming: "+callDate);
				break;

			case CallLog.Calls.MISSED_TYPE: 
				entity.setType("Missed: "+callDate);
				break; 
			} 
			if(blockedContacts.contains(phNumber) && phNumber.length()>6){
				entity.setIsChecked("3");
				temp.add(entity);
			}
		} 
		managedCursor.close();


		for(int i=temp.size(); i>0; i--)
			rejectListArray.add(temp.get(i-1));

		Map<String, ContactEntity > map = new LinkedHashMap<String, ContactEntity>();
		for (ContactEntity ays : rejectListArray) {
			map.put(ays.getContactNo(), ays);
		}
		rejectListArray.clear();
		rejectListArray.addAll(map.values());


		Collections.sort(rejectListArray, new Comparator<ContactEntity>() {
			@Override
			public int compare(ContactEntity  contact1, ContactEntity  contact2)
			{

				return  contact1.getName().compareToIgnoreCase((contact2.getName()));
			}
		});

	}

	public class GetCallLogAsync extends AsyncTask<Void, Void, Void>{
		@Override
		protected Void doInBackground(Void... params) {
			getCallLog();
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);

			changeMenu(true);
			callsLogAdapter = new CallsLogAdapter(mContext, R.layout.contact_row, logArrayList);
			cList.setAdapter(callsLogAdapter);
		}
	}

	public class GetRejectListAsync extends AsyncTask<Void, Void, Void>{
		@Override
		protected Void doInBackground(Void... params) {
			getRejectList(getContentResolver());
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			if(rejectListArray.size()==0){
				Toast.makeText(mContext, "No contacts blocked", Toast.LENGTH_LONG).show();
			}

			changeMenu(false);
			rejectListAdapter = new RejectListAdapter(mContext, R.layout.reject_list_row, rejectListArray);
			cList.setAdapter(rejectListAdapter);
		}
	}

	public void editProfile(){

		final Dialog mDia = new Dialog(mContext, android.R.style.Theme_Translucent_NoTitleBar);
		mDia.setContentView(R.layout.create_profile);

		TextView pName = (TextView)mDia.findViewById(R.id.profile);
		startEt = (EditText)mDia.findViewById(R.id.start_time);
		endEt = (EditText)mDia.findViewById(R.id.end_time);
		pName.setText(profileName);
		startTime = new Util().get12FmtTime(startTime);
		endTime = new Util().get12FmtTime(endTime);
		startEt.setText(startTime);
		endEt.setText(endTime);
		endEt.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				showTimerPicker(v);
			}
		});
		endEt.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if(hasFocus){
					showTimerPicker(v);
				}

			}
		});
		startEt.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				showTimerPicker(v);
			}
		});
		startEt.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if(hasFocus){
					showTimerPicker(v);
				}

			}
		});

		((Button)mDia.findViewById(R.id.create)).setText("Update");

		((Button)mDia.findViewById(R.id.create)).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				String msg = validate(mDia);
				if(msg.length()>0){
					Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
				}else{
					String profile = ((TextView)mDia.findViewById(R.id.profile)).getText().toString().trim();
					String start_time = ((TextView)mDia.findViewById(R.id.start_time)).getText().toString().trim();
					String end_time = ((TextView)mDia.findViewById(R.id.end_time)).getText().toString().trim();
					end_time = new Util().get24FmtTime(end_time);
					start_time = new Util().get24FmtTime(start_time);
					ProfileEntity pEntity = new ProfileEntity();
					pEntity.setProfileName(profile);
					pEntity.setStartTime(start_time);
					pEntity.setEndTime(end_time);
					pEntity.setStatus(1);
					pEntity.setOldProfileName(profileName);
					pEntity.setId(id);
					int i = pDb.updateProfile(pEntity);
					if(i>0){
						Toast.makeText(mContext, "Profile has been updated", Toast.LENGTH_SHORT).show();
						profileName = profile;
						reflectChange();
						mDia.dismiss();
					}
					else
						Toast.makeText(mContext, "Profile not updated", Toast.LENGTH_SHORT).show();
				}

			}
		});

		mDia.show();

	}

	public void reflectChange(){
		mActionBar.setTitle(profileName);
		Constants.refresh = true;
	}
	public String validate(Dialog mDia){
		String msg = "";
		String profile = ((TextView)mDia.findViewById(R.id.profile)).getText().toString().trim();
		String start_time = ((TextView)mDia.findViewById(R.id.start_time)).getText().toString().trim();
		String end_time = ((TextView)mDia.findViewById(R.id.end_time)).getText().toString().trim();

		if(TextUtils.isEmpty(profile) || TextUtils.isEmpty(start_time)|| TextUtils.isEmpty(end_time)){
			msg =  "Please enter all fields";
			return msg;
		}


		List<ProfileEntity> pList = new ArrayList<ProfileEntity>();
		pList = pDb.getProfiles();
		for(int i=0; i<pList.size(); i++){
			if(profile.equalsIgnoreCase(pList.get(i).getProfileName())){
				if(profile.equalsIgnoreCase(profileName)){
					msg = "";
				}else{
					msg = "Profile with same name already exists";
				}
				return msg;
			}
		}

		return msg;
	}

	public void showTimerPicker(View v){
		String tag = v.getTag().toString();
		Log.v("Tag: ", tag);
		DialogFragment tDia = new TimerPickerFragment();
		tDia.show(getSupportFragmentManager(), tag);
	}

	public static class TimerPickerFragment extends DialogFragment implements OnTimeSetListener{

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			// TODO Auto-generated method stub

			final Calendar cal = Calendar.getInstance();
			int hour = cal.get(Calendar.HOUR_OF_DAY);
			int minute = cal.get(Calendar.MINUTE);

			return new TimePickerDialog(getActivity(), this, hour, minute, false) ;
		}

		@Override
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			String tag = getTag().toString();

			String am_pm = "";
			if(hourOfDay==0){
				hourOfDay = 12;
				am_pm ="AM";
			}else if(hourOfDay == 12){
				am_pm = "PM";
			}else if(hourOfDay>12){
				am_pm = "PM";
				hourOfDay -=12;
			}else{
				am_pm = "AM";
			}
			String min = ""+minute;
			if(minute<10)
				min = "0"+minute;
			if(tag.equalsIgnoreCase("start")){
				startEt.setText(hourOfDay+":"+min+" "+am_pm);
			}else{
				endEt.setText(hourOfDay+":"+min+" "+am_pm);
			}

		}
	}

}
