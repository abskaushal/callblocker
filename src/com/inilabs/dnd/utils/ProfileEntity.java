package com.inilabs.dnd.utils;

public class ProfileEntity {

	private String profileName, startTime,endTime, oldProfileName;
	
	public String getOldProfileName() {
		return oldProfileName;
	}

	public void setOldProfileName(String oldProfileName) {
		this.oldProfileName = oldProfileName;
	}

	private int id, status;
	
	

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getProfileName() {
		return profileName;
	}

	public void setProfileName(String profileName) {
		this.profileName = profileName;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}
	
	
	
}
