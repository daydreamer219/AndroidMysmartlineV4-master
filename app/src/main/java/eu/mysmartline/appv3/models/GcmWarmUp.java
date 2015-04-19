package eu.mysmartline.appv3.models;

/**
 * This is an wormUpRequest Request. Also functions as a Google Cloud Message
 * @author bogdan
 *
 */
public class GcmWarmUp {
	private String deviceShortLongId;

	public String getDeviceShortLongId() {
		return deviceShortLongId;
	}

	public void setDeviceShortLongId(String deviceShortLongId) {
		this.deviceShortLongId = deviceShortLongId;
	}
}
