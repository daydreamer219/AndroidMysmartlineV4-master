package eu.mysmartline.appv3.models;

public class GcmMessage {
	private String type;
	private String jsonObject;
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getJsonObject() {
		return jsonObject;
	}
	public void setJsonObject(String jsonObject) {
		this.jsonObject = jsonObject;
	}
}
