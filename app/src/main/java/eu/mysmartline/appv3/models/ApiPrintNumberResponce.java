package eu.mysmartline.appv3.models;

public class ApiPrintNumberResponce {
	private String lineName;
	private String clientNumber;
	private String qrText;
	
	public String getLineName() {
		return lineName;
	}
	public void setLineName(String lineName) {
		this.lineName = lineName;
	}
	public String getClientNumber() {
		return clientNumber;
	}
	public void setClientNumber(String clientNumber) {
		this.clientNumber = clientNumber;
	}
	public String getQrText() {
		return qrText;
	}
	public void setQrText(String qrText) {
		this.qrText = qrText;
	}
}
