package eu.mysmartline.appv3.models;

public class DeviceRegistrationResponceModel {
	private boolean validMessage;
	private boolean databaseError;
	private boolean isRecordedInDatabase;
	private boolean isActive;
	private String shortId;
	private String userFrendlyName;
	
	public boolean isRecordedInDatabase() {
		return isRecordedInDatabase;
	}
	public void setRecordedInDatabase(boolean isRecordedInDatabase) {
		this.isRecordedInDatabase = isRecordedInDatabase;
	}
	public boolean isActive() {
		return isActive;
	}
	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}
	public String getUserFrendlyName() {
		return userFrendlyName;
	}
	public void setUserFrendlyName(String userFrendlyName) {
		this.userFrendlyName = userFrendlyName;
	}
	public boolean isDatabaseError() {
		return databaseError;
	}
	public void setDatabaseError(boolean databaseError) {
		this.databaseError = databaseError;
	}
	public String getShortId() {
		return shortId;
	}
	public void setShortId(String shortId) {
		this.shortId = shortId;
	}
	public boolean isValidMessage() {
		return validMessage;
	}
	public void setValidMessage(boolean validMessage) {
		this.validMessage = validMessage;
	}
	
}