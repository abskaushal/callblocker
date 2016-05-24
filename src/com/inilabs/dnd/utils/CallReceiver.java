package com.inilabs.dnd.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ParseException;
import android.os.Binder;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.android.internal.telephony.ITelephony;
import com.inilabs.dnd.db.DbManager;
import com.inilabs.dnd.db.ProfileDB;

public class CallReceiver extends BroadcastReceiver {

	private TelephonyManager telephonyManager;
	private ITelephony telephonyService;
	private PhoneStateListener mPhoneStateListener;
	@SuppressWarnings("deprecation")
	int events = PhoneStateListener.LISTEN_DATA_ACTIVITY
	| PhoneStateListener.LISTEN_CELL_LOCATION
	| PhoneStateListener.LISTEN_CALL_STATE
	| PhoneStateListener.LISTEN_CALL_FORWARDING_INDICATOR
	| PhoneStateListener.LISTEN_DATA_CONNECTION_STATE
	| PhoneStateListener.LISTEN_MESSAGE_WAITING_INDICATOR
	| PhoneStateListener.LISTEN_SERVICE_STATE;

	SharedPreferences mPref;
	String blocked_contacts="", log_contacts="";
	int status=0;
	ProfileDB profileDB;
	Context mContext;
	@Override
	public void onReceive(Context context, Intent intent) {

		telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		mContext = context;
		profileDB = new ProfileDB(context, DbManager.DATABASE_NAME, null, DbManager.DATABASE_VERSION);
		connectToTelephonyService();
		if(!intent.getAction().equals("android.intent.action.PHONE_STATE"))
			return;


		String phoneState = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
		if(phoneState.equalsIgnoreCase(TelephonyManager.EXTRA_STATE_RINGING)){
			String phoneNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
			telephonyManager.listen(mPhoneStateListener, events);
		}


	}



	@SuppressWarnings("unchecked")
	private void connectToTelephonyService() {
		try {



			Class c = Class.forName(telephonyManager.getClass().getName());
			Method m = c.getDeclaredMethod("getITelephony");
			m.setAccessible(true);
			telephonyService = (ITelephony) m.invoke(telephonyManager);


			mPhoneStateListener = new PhoneStateListener(){
				public void onCallStateChanged(int state, String incomingNumber) {
					super.onCallStateChanged(state, incomingNumber);
					incomingNumber = processIncomingNumber(incomingNumber);
					Log.v("R", "R"+incomingNumber);
					switch (state) {
					case TelephonyManager.CALL_STATE_RINGING:
						boolean blockCall = profileDB.checkIfBlockNumber(incomingNumber);
						mPref = mContext.getSharedPreferences(Constants.PREF_NAME, 0);
						status = mPref.getInt(Constants.STATUS, 1);
						if(blockCall && (status==1)){
							endCall();
						}

						break;

					default:
						break;
					}
				}
			};


		} catch (Exception e) {

			Log.v("call prompt", "Exception object: "+ e);

		}
	}

	public void endCall(){
		// required permission <uses-permission android:name="android.permission.CALL_PHONE"/>
		try {
			//String serviceManagerName = "android.os.IServiceManager";
			String serviceManagerName = "android.os.ServiceManager";
			String serviceManagerNativeName = "android.os.ServiceManagerNative";
			String telephonyName = "com.android.internal.telephony.ITelephony";

			Class telephonyClass;
			Class telephonyStubClass;
			Class serviceManagerClass;
			Class serviceManagerStubClass;
			Class serviceManagerNativeClass;
			Class serviceManagerNativeStubClass;

			Method telephonyCall;
			Method telephonyEndCall;
			Method telephonyAnswerCall;
			Method getDefault;

			Method[] temps;
			Constructor[] serviceManagerConstructor;

			// Method getService;
			Object telephonyObject;
			Object serviceManagerObject;

			telephonyClass = Class.forName(telephonyName);
			telephonyStubClass = telephonyClass.getClasses()[0];
			serviceManagerClass = Class.forName(serviceManagerName);
			serviceManagerNativeClass = Class.forName(serviceManagerNativeName);

			Method getService = serviceManagerClass.getMethod("getService", String.class);

			Method tempInterfaceMethod = serviceManagerNativeClass.getMethod(
					"asInterface", IBinder.class);

			Binder tmpBinder = new Binder();
			tmpBinder.attachInterface(null, "fake");

			serviceManagerObject = tempInterfaceMethod.invoke(null, tmpBinder);
			IBinder retbinder = (IBinder) getService.invoke(serviceManagerObject, "phone");
			Method serviceMethod = telephonyStubClass.getMethod("asInterface", IBinder.class);

			telephonyObject = serviceMethod.invoke(null, retbinder);

			telephonyEndCall = telephonyClass.getMethod("endCall");

			telephonyEndCall.invoke(telephonyObject);


			//telephonyAnswerCall = telephonyClass.getMethod("answerRingingCall");

			//telephonyCall = telephonyClass.getMethod("call", String.class);



		} catch (Exception e) {
			e.printStackTrace();

		}
	}

	public String processIncomingNumber(String number){
		String num = "";
		if(number.length()>10)
			num = number.substring(number.length()-10, number.length());

		return num;
	}

	public void checkTimeInBetween(){
		try {
			String string1 = "20:11:13";
			Date time1 = null;
			try {
				time1 = new SimpleDateFormat("HH:mm:ss").parse(string1);
			} catch (java.text.ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Calendar calendar1 = Calendar.getInstance();
			calendar1.setTime(time1);

			String string2 = "14:49:00";
			Date time2 = null;
			try {
				time2 = new SimpleDateFormat("HH:mm:ss").parse(string2);
			} catch (java.text.ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Calendar calendar2 = Calendar.getInstance();
			calendar2.setTime(time2);
			calendar2.add(Calendar.DATE, 1);

			String someRandomTime = "01:00:00";
			Date d = null;
			try {
				d = new SimpleDateFormat("HH:mm:ss").parse(someRandomTime);
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

}
