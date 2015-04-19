package eu.mysmartline.appv3.models;

public class DeviceStatus {
	private String deviceGcmId;
	private String shortId;
	private boolean isInTheDatabase;
	private boolean isActive;

	public String getDeviceGcmId() {
		return deviceGcmId;
	}

	public void setDeviceGcmId(String deviceGcmId) {
		this.deviceGcmId = deviceGcmId;
	}

	public boolean isInTheDatabase() {
		return isInTheDatabase;
	}

	public void setInTheDatabase(boolean isInTheDatabase) {
		this.isInTheDatabase = isInTheDatabase;
	}

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	public String getShortId() {
		return shortId;
	}

	public void setShortId(String shortId) {
		this.shortId = shortId;
	}

}
