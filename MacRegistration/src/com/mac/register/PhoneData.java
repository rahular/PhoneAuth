package com.mac.register;

import java.io.Serializable;

public class PhoneData implements Serializable {
	private static final long serialVersionUID = 1L;
	byte[] imei, mac;
	
	public byte[] getImei() {
		return imei;
	}
	public void setImei(byte[] imei) {
		this.imei = imei;
	}
	public byte[] getMac() {
		return mac;
	}
	public void setMac(byte[] mac) {
		this.mac = mac;
	}
}
