package com.mac.register;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

public class MainModule {
	static String mac;
	static String imei;
	static String actualMac = "";
	static String userName;
	static String type;
	static byte[] macHash, imeiHash;

	public static void main(String[] args) {
		if (args.length < 3 || args.length > 3) {
			System.out
					.println("Usage : Pass exactly 2 arguements. MAC address and IMEI number.");
			return;
		}

		type = args[0];
		mac = args[1];
		imei = args[2];

		if (!validateMac(mac)) {
			System.out.println("Wrong MAC address format.");
		}
		if (!validateImei(imei)) {
			System.out.println("Wrong IMEI number.");
		}

		String[] chunks = mac.split(":");
		for (int i = 0; i < chunks.length; i++)
			actualMac += chunks[i];

		macHash = Encryptor.encrypt(actualMac);
		imeiHash = Encryptor.encrypt(imei);
		userName = getUserName();

		storeData(userName, macHash, imeiHash);
	}

	static String getUserName() {
		try {
			String line;
			Process p = Runtime.getRuntime().exec("whoami");

			BufferedReader in = new BufferedReader(new InputStreamReader(
					p.getInputStream()));
			if ((line = in.readLine()) != null) {
				return line.toString();
			}
			in.close();
			return null;
		} catch (Exception e) {
			return null;
		}
	}

	static boolean validateMac(String address) {
		return address.matches("^([0-9A-F]{2}[:-]){5}([0-9A-F]{2})$");

	}

	static boolean validateImei(String imei) {
		return imei.matches("[0-9]{15,17}");
	}

	@SuppressWarnings("unchecked")
	static void storeData(String userName, byte[] mac, byte[] imei) {
		if (type.equals("user")) {
			try {
				FileOutputStream fos = null;
				fos = new FileOutputStream("/home/" + getUserName() + "/."
						+ getUserName() + ".dat");
				ObjectOutputStream oos = new ObjectOutputStream(fos);
				oos.writeObject(mac);
				oos.writeObject(imei);
				fos.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (type.equals("sys")) {
			ArrayList<PhoneData> data = null;
			try {
				FileInputStream fis = null;
				fis = new FileInputStream("/usr/local/bin/.AuthData.dat");
				ObjectInputStream ois = new ObjectInputStream(fis);
				data = (ArrayList<PhoneData>) ois.readObject();
				ois.close();
			} catch (Exception e) {
				// e.printStackTrace();
			}

			try {
				ObjectOutputStream oos = new ObjectOutputStream(
						new FileOutputStream("/usr/local/bin/.AuthData.dat"));
				
				if(data == null) data = new ArrayList<PhoneData>();
				
				PhoneData temp = new PhoneData();
				temp.mac = mac;
				temp.imei = imei;
				data.add(temp);
				
				oos.writeObject(data);
				oos.close();

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}