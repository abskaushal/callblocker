package com.inilabs.dnd;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.ShareActionProvider;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.ads.AdRequest;
import com.google.ads.AdView;
import com.inilabs.dnd.adapter.ProfileAdapter;
import com.inilabs.dnd.db.DbManager;
import com.inilabs.dnd.db.ProfileDB;
import com.inilabs.dnd.utils.Constants;
import com.inilabs.dnd.utils.ProfileEntity;
import com.inilabs.dnd.utils.Util;

public class DNDActivity extends FragmentActivity {

	ArrayList<ContactEntity> contactListArr, logArrayList;
	Context mContext;
	SharedPreferences mPref;
	Button iconImage;
	//TextView statusTv;
	LinearLayout noActiveLl;
	Button noActiveBtn;
	ListView profileLv;
	Animation anim;
	ProfileDB pDb;
	List<ProfileEntity> pList = new ArrayList<ProfileEntity>();
	static EditText startEt, endEt;
	ProfileAdapter pAdapter;
	private ShareActionProvider mShareActionProvider;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_dnd);
		AdView mAdView = (AdView) findViewById(R.id.adView);
		AdRequest adRequest = new AdRequest();
		mAdView.loadAd(adRequest);
		mContext = DNDActivity.this;
		mPref = mContext.getSharedPreferences(Constants.PREF_NAME, 0);
		pDb = new ProfileDB(mContext, DbManager.DATABASE_NAME, null, DbManager.DATABASE_VERSION);
		setViewobjects();
		setListener();

		anim = AnimationUtils.loadAnimation(mContext, R.anim.set);
		int appInstall = mPref.getInt(Constants.APP_INSTALLED, 0);
		if(appInstall==0){
			Editor edit = mPref.edit();
			edit.putInt(Constants.APP_INSTALLED, 1);
			edit.putInt(Constants.STATUS, 1);
			edit.commit();
			showHelpDialog();
		}


		//getAllContacts(getContentResolver());
		//getCallLog();
		setProfileList();



	}

	public void setProfileList(){
		pList = pDb.getProfiles();
		if(pList.size()>0){
			noActiveLl.setVisibility(View.GONE);
			profileLv.setVisibility(View.VISIBLE);
			pAdapter = new ProfileAdapter(mContext, R.layout.profile_row, pList);
			profileLv.setAdapter(pAdapter);
		}else{
			noActiveLl.setVisibility(View.VISIBLE);
			profileLv.setVisibility(View.GONE);
		}
	}


	public void setViewobjects(){
		profileLv = (ListView) findViewById(R.id.pList);
		noActiveLl = (LinearLayout) findViewById(R.id.no_active_linear);
		noActiveBtn = (Button) findViewById(R.id.no_active_btn);
		iconImage = (Button) findViewById(R.id.icon);
		iconImage.setSelected(true);
	}

	public void setValue(){
		int status = mPref.getInt(Constants.STATUS, 1);
		if(status == 0){
			iconImage.setSelected(true);
		}else{
			iconImage.setSelected(false);
		}
	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		setValue();
		if(Constants.refresh){
			Constants.refresh = false;
			setProfileList();
		}

	}

	public void setListener(){
		/*iconImage.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				runOnUiThread(new Runnable() {

					@Override
					public void run() {

						iconImage.startAnimation(anim);
						anim.setAnimationListener(new AnimationListener() {  

							@Override
							public void onAnimationEnd(Animation animation) {
								if(Constants.power == 0){
									Constants.power = 1;
									iconImage.setImageResource(R.drawable.app_on);

								}
								else{
									Constants.power = 0;
									iconImage.setImageResource(R.drawable.app_off);

								}
								showAppPowerDialog();

							}

							@Override
							public void onAnimationRepeat(Animation animation) {


							}

							@Override
							public void onAnimationStart(Animation animation) {


							}
						});
					}
				});


			}
		});*/

		iconImage.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				mPref = mContext.getSharedPreferences(Constants.PREF_NAME, 0);
				int status =mPref.getInt(Constants.STATUS, 1);
				if(status == 0){
					status = 1;
					iconImage.setSelected(false);
				}else{
					status = 0;
					iconImage.setSelected(true);
				}

				Editor edit =mPref.edit();
				edit.putInt(Constants.STATUS, status);
				edit.commit();
				showAppPowerDialog(status);
			}
		});

		noActiveBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				createDefaultProfile();

			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.dnd, menu);

		return true;
	}

	private void share( ) {
		Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
		sharingIntent.setType("text/plain");
		String shareBody = "Checkout the DND call blocker app here: https://play.google.com/store/apps/details?id=com.inilabs.dnd";
		sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "DND");
		sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
		startActivity(Intent.createChooser(sharingIntent, "Share via"));
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		case R.id.action_profile:

			createProfile();
			break;

		case R.id.action_help:
			showHelpDialog();
			break;

		case R.id.action_aboutus:
			showAboutUsDialog();
			break;
			
		case R.id.menu_item_share:
			share();
			break;

		default:
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	@SuppressWarnings("deprecation")



	public void showAboutUsDialog(){
		final Dialog aboutDia = new Dialog(mContext, android.R.style.Theme_Translucent_NoTitleBar);
		aboutDia.setContentView(R.layout.about_us);

		((Button)aboutDia.findViewById(R.id.ok)).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				aboutDia.dismiss();
			}
		});

		aboutDia.show();
	}

	public void showAppPowerDialog(int status){
		final Dialog aboutDia = new Dialog(mContext, android.R.style.Theme_Translucent_NoTitleBar);
		aboutDia.setContentView(R.layout.power_status_dialog);
		String message = "";

		if(status == 0)
			message = "The app is turned OFF now. All profiles are in deactivated state.";
		else
			message = "The app is turned ON now. All profiles are in their respective state.";
		((TextView)aboutDia.findViewById(R.id.text)).setText(message);
		((Button)aboutDia.findViewById(R.id.ok)).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				aboutDia.dismiss();
			}
		});

		aboutDia.show();
	}

	public void showDefaultProfileDialog(){
		final Dialog aboutDia = new Dialog(mContext, android.R.style.Theme_Translucent_NoTitleBar);
		aboutDia.setContentView(R.layout.default_profile_created);

		((Button)aboutDia.findViewById(R.id.ok)).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				aboutDia.dismiss();
			}
		});

		aboutDia.show();
	}

	public void showProfileCreatedDialog(String from, String to){
		final Dialog aboutDia = new Dialog(mContext, android.R.style.Theme_Translucent_NoTitleBar);
		aboutDia.setContentView(R.layout.profile_created);

		((TextView)aboutDia.findViewById(R.id.text)).setText("This profile will block calls from "+from+" to "+to+".");
		((Button)aboutDia.findViewById(R.id.ok)).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				aboutDia.dismiss();
			}
		});

		aboutDia.show();
	}

	public void showHelpDialog(){
		final Dialog aboutDia = new Dialog(mContext, android.R.style.Theme_Translucent_NoTitleBar);
		aboutDia.setContentView(R.layout.help_dialog);

		((Button)aboutDia.findViewById(R.id.ok)).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				aboutDia.dismiss();
			}
		});

		aboutDia.show();
	}

	public void createDefaultProfile(){
		ProfileEntity pEntity = new ProfileEntity();
		pEntity.setProfileName("Default");
		pEntity.setStartTime("00:00");
		pEntity.setEndTime("24:00");
		pEntity.setStatus(1);
		long i = pDb.createProfile(pEntity);
		if(i>=0){
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					showDefaultProfileDialog();
				}
			}, 1000);
			pList.add(pEntity);
			if(pList.size()==1){
				noActiveLl.setVisibility(View.GONE);
				profileLv.setVisibility(View.VISIBLE);
				pAdapter = new ProfileAdapter(mContext, R.layout.profile_row, pList);
				profileLv.setAdapter(pAdapter);
			}else{
				pAdapter.notifyDataSetChanged();
			}
		}
	}
	public void createProfile(){
		final Dialog mDia = new Dialog(mContext, android.R.style.Theme_Translucent_NoTitleBar);
		mDia.setContentView(R.layout.create_profile);

		startEt = (EditText)mDia.findViewById(R.id.start_time);
		endEt = (EditText)mDia.findViewById(R.id.end_time);
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
					Util objUtil = new Util();
					start_time = objUtil.get24FmtTime(start_time);
					end_time = objUtil.get24FmtTime(end_time);
					ProfileEntity pEntity = new ProfileEntity();
					pEntity.setProfileName(profile);
					pEntity.setStartTime(start_time);
					pEntity.setEndTime(end_time);
					pEntity.setStatus(1);
					long i = pDb.createProfile(pEntity);
					if(i>=0){
						setProfileList();
						mDia.dismiss();
						final String from = objUtil.get12FmtTime(start_time);
						final String to = objUtil.get12FmtTime(end_time);
						new Handler().postDelayed(new Runnable() {

							@Override
							public void run() {
								showProfileCreatedDialog(from, to);
							}
						}, 1000);
						/*
						Toast.makeText(mContext, "Profile has been created", Toast.LENGTH_SHORT).show();
						pList.add(pEntity);
						if(pList.size()==1){
							noActiveLl.setVisibility(View.GONE);
							profileLv.setVisibility(View.VISIBLE);
							pAdapter = new ProfileAdapter(mContext, R.layout.profile_row, pList);
							profileLv.setAdapter(pAdapter);
						}else{
							pAdapter.notifyDataSetChanged();
						}

						mDia.dismiss();
						 */}
					else
						Toast.makeText(mContext, "Profile not created", Toast.LENGTH_SHORT).show();
				}

			}
		});

		mDia.show();
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

		if(pList.size()==5){
			msg = "You can create maximum 5 profiles";
			return msg;
		}
		for(int i=0; i<pList.size(); i++){
			if(profile.equalsIgnoreCase(pList.get(i).getProfileName())){
				msg = "Profile with same name already exists";
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


