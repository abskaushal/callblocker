package com.inilabs.dnd.utils;

public class Util {


	public String get12FmtTime(String tm){
		String time ="";
		tm = tm.trim();
		String arr[] = tm.split(":");
		int hour = Integer.parseInt(arr[0]);
		int min = Integer.parseInt(arr[1]);
		String min_str = ""+min;
		if(min<10)
			min_str = "0"+min;
		if(hour == 12){
			time = hour+":"+min_str+ " PM";
		}else if(hour==0){
			hour = 12;
			time = hour+":"+min_str+ " AM";
		}else if(hour>12){
			hour = hour-12;
			time = hour+":"+min_str+ " PM";
		}else{
			time = hour+":"+min_str+ " AM";
		}
		return time;
	}

	public String get24FmtTime(String time){
		String time24 = "";
		if(time.contains("AM")){
			time =time.replace("AM", "");
			time =time.trim();
			String arr[] = time.split(":");
			int hour = Integer.parseInt(arr[0]);
			int min = Integer.parseInt(arr[1]);

			if(hour==12){
				hour = 0;
				time24 = hour+":"+min;
			}else{
				time24 = hour+":"+min;
			}
		}else{
			time =time.replace("PM", "");
			time =time.trim();
			String arr[] = time.split(":");
			int hour = Integer.parseInt(arr[0]);
			int min = Integer.parseInt(arr[1]);
			if(hour!=12)
				hour+=12;
			time24 = hour+":"+min;

		}
		return time24;
	}
}
