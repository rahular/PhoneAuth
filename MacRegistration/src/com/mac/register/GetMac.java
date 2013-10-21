package com.mac.register;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;

public class GetMac {
	static byte[] hash;
	public static void main(String[] args) {
		try {
			FileInputStream fis = null;
			fis = new FileInputStream("/tmp/." + getUserName() + ".dat");
			ObjectInputStream ois = new ObjectInputStream(fis);
			hash = (byte[]) ois.readObject();
		} catch (Exception e) {
			e.printStackTrace();
		}
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
}