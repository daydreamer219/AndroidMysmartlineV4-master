package eu.mysmartline.appv3.models;

public class ApiActivateValidateResponce {
	private boolean succesfull ;
	private String detailsAboutFailure;
	//getters and setters;
	public boolean isSuccesfull() {
		return succesfull;
	}
	public void setSuccesfull(boolean succesfull) {
		this.succesfull = succesfull;
	}
	public String getDetailsAboutFailure() {
		return detailsAboutFailure;
	}
	public void setDetailsAboutFailure(String detailsAboutFailure) {
		this.detailsAboutFailure = detailsAboutFailure;
	}
	
}
