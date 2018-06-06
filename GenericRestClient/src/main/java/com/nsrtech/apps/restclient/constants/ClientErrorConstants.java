package com.nsrtech.apps.restclient.constants;

public enum ClientErrorConstants {
	ERROR_01("01", "Logger not initialized."),
	ERROR_02("02", "Rest client Properties reading failed."),
	ERROR_03("03", "SSL Certification error while preparing the connection."),
	ERROR_04("04", "IO Exception while send/recieve the request."),
	ERROR_05("05", "Error while convert request object to JSON String."),
	ERROR_99("99","Generic Error");

	private String code;
	private String message;

	private ClientErrorConstants(String code, String message) {
		this.code = code;
		this.message = message;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
