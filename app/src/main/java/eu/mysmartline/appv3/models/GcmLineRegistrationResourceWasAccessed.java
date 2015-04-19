package eu.mysmartline.appv3.models;

public class GcmLineRegistrationResourceWasAccessed {
	private String deviceId;

	public static GcmLineRegistrationResourceWasAccessed build() {
		GcmLineRegistrationResourceWasAccessed gcmLineRegistrationResourceWasAccessed = new GcmLineRegistrationResourceWasAccessed();
		gcmLineRegistrationResourceWasAccessed
				.setDeviceId("not implemented yet");
		return gcmLineRegistrationResourceWasAccessed;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

}
