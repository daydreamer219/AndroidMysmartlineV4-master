package eu.mysmartline.appv3.models;

public class ApiActivateValidateRequest {
	private String email;
	private String gcmRegId;
	private String shortAndLongId;
	//getters and setters;
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getGcmRegId() {
		return gcmRegId;
	}
	public void setGcmRegId(String gcmRegId) {
		this.gcmRegId = gcmRegId;
	}
	public String getShortAndLongId() {
		return shortAndLongId;
	}
	public void setShortAndLongId(String shortAndLongId) {
		this.shortAndLongId = shortAndLongId;
	}
}
