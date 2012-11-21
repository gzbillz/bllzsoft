package org.jboss.bpm.console.server.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.sql.Timestamp;

public class GsonFactory {
	public static Gson createInstance() {
		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss")
				.registerTypeAdapter(Timestamp.class, new SQLDateTypeAdapter())
				.create();

		return gson;
	}
}