package com.kandy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.appcelerator.kroll.KrollFunction;
import org.appcelerator.kroll.KrollObject;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;

/**
 * Common utilities
 * 
 * @author kodeplusdev
 * 
 */
public final class Utils {

	private Utils() {
	}

	/**
	 * Check parameter and send results to callback
	 * 
	 * @param krollObject
	 * @param krollFunction
	 * @param result
	 */
	public static void checkAndSendResult(KrollObject krollObject,
			KrollFunction krollFunction, HashMap result) {
		if (krollObject != null && krollFunction != null) {
			krollFunction.call(krollObject, result);
		}
	}

	/**
	 * Send a fail result with error code and string
	 * 
	 * @param krollObject
	 * @param krollFunction
	 * @param code
	 * @param error
	 */
	public static void sendFailResult(KrollObject krollObject,
			KrollFunction krollFunction, int code, String error) {
		HashMap result = new HashMap();
		result.put("status", KandyModule.STATUS_ERROR);
		result.put("code", code);
		result.put("message", error);
		checkAndSendResult(krollObject, krollFunction, result);
	}

	/**
	 * Send a fail result with error string only
	 * 
	 * @param krollObject
	 * @param krollFunction
	 * @param error
	 */
	public static void sendFailResult(KrollObject krollObject,
			KrollFunction krollFunction, String error) {
		HashMap result = new HashMap();
		result.put("status", KandyModule.STATUS_ERROR);
		result.put("code", ""); // undefined
		result.put("message", error);
		checkAndSendResult(krollObject, krollFunction, result);
	}

	/**
	 * Send a empty success result
	 * 
	 * @param krollObject
	 * @param krollFunction
	 */
	public static void sendSuccessResult(KrollObject krollObject,
			KrollFunction krollFunction) {
		HashMap result = new HashMap();
		result.put("status", KandyModule.STATUS_SUCCESS);
		checkAndSendResult(krollObject, krollFunction, result);
	}

	/**
	 * Send a success result with data
	 * 
	 * @param krollObject
	 * @param krollFunction
	 * @param data
	 */
	public static void sendSuccessResult(KrollObject krollObject,
			KrollFunction krollFunction, Object data) {
		HashMap result = new HashMap();
		result.put("status", KandyModule.STATUS_SUCCESS);
		result.put("data", data);
		checkAndSendResult(krollObject, krollFunction, result);
	}

	/**
	 * Get id of resource
	 * 
	 * @param activity
	 * @param name
	 * @param type
	 * @return
	 */
	public static int getResource(Activity activity, String name, String type) {
		int res = -1;
		String packageName = activity.getPackageName();
		res = activity.getResources().getIdentifier(name, type, packageName);
		return res;
	}

	/**
	 * Get string resource from identifier name
	 * 
	 * @param activity
	 * @param name
	 * @return
	 */
	public static String getString(Activity activity, String name) {
		String str = "";
		int resId = getResource(activity, name, "string");
		str = activity.getString(resId);
		return str;
	}

	/**
	 * Convert from JSON object to HashMap
	 * 
	 * @param object
	 * @return
	 * @throws JSONException
	 */
	public static HashMap JSONObjectToHashMap(JSONObject object)
			throws JSONException {
		HashMap<String, Object> map = new HashMap<String, Object>();

		Iterator<String> keysItr = object.keys();
		while (keysItr.hasNext()) {
			String key = keysItr.next();
			Object value = object.get(key);

			if (value instanceof JSONArray) {
				value = JSONArrayToList((JSONArray) value);
			}

			else if (value instanceof JSONObject) {
				value = JSONObjectToHashMap((JSONObject) value);
			}
			map.put(key, value);
		}
		return map;
	}

	/**
	 * Convert from JSON array to list
	 * 
	 * @param array
	 * @return
	 * @throws JSONException
	 */
	public static List JSONArrayToList(JSONArray array) throws JSONException {
		List<Object> list = new ArrayList<Object>();

		for (int i = 0; i < array.length(); i++) {
			Object value = array.get(i);
			if (value instanceof JSONArray) {
				value = JSONArrayToList((JSONArray) value);
			}

			else if (value instanceof JSONObject) {
				value = JSONObjectToHashMap((JSONObject) value);
			}
			list.add(value);
		}
		return list;
	}
}
