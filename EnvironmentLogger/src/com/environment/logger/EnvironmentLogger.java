package com.environment.logger;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Map;

public class EnvironmentLogger {
	String[] e;

	public static void main(String[] args) {
		FileWriter fstream = null;
		try {
			fstream = new FileWriter("/tmp/environment.log");
			BufferedWriter out = new BufferedWriter(fstream);
			Map<String, String> env = System.getenv();
			for (String envName : env.keySet()) {
				out.write(envName + "=" + env.get(envName) + "\n");
			}
			out.close();
			fstream.close();
		} catch (Exception e) {
			e.printStackTrace();
			
		}
	}
}