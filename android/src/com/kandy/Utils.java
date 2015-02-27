package com.kandy;

import java.util.HashMap;

import org.appcelerator.kroll.KrollFunction;
import org.appcelerator.kroll.KrollObject;

import android.app.Activity;

public final class Utils {

	private Utils() {
	}

	public static void checkAndSendResult(KrollObject krollObject,
			KrollFunction krollFunction, HashMap result) {
		if (krollObject != null && krollFunction != null) {
			krollFunction.call(krollObject, result);
		}
	}

	public static void sendFailResult(KrollObject krollObject,
			KrollFunction krollFunction, int code, String error) {
		HashMap result = new HashMap();
		result.put("status", KandyModule.STATUS_ERROR);
		result.put("code", code);
		result.put("message", error);
		checkAndSendResult(krollObject, krollFunction, result);
	}

	public static void sendFailResult(KrollObject krollObject,
			KrollFunction krollFunction, String error) {
		HashMap result = new HashMap();
		result.put("status", KandyModule.STATUS_ERROR);
		result.put("code", ""); // undefined
		result.put("message", error);
		checkAndSendResult(krollObject, krollFunction, result);
	}

	public static void sendSuccessResult(KrollObject krollObject,
			KrollFunction krollFunction) {
		HashMap result = new HashMap();
		result.put("status", KandyModule.STATUS_SUCCESS);
		checkAndSendResult(krollObject, krollFunction, result);
	}

	public static void sendSuccessResult(KrollObject krollObject,
			KrollFunction krollFunction, Object data) {
		HashMap result = new HashMap();
		result.put("status", KandyModule.STATUS_SUCCESS);
		result.put("data", data);
		checkAndSendResult(krollObject, krollFunction, result);
	}

	public static int getResource(Activity activity, String name, String type) {
		int res = -1;
		String packageName = activity.getPackageName();
		res = activity.getResources().getIdentifier(name, type, packageName);
		return res;
	}

	public static String getString(Activity activity, String name) {
		String str = "";
		int resId = getResource(activity, name, "string");
		str = activity.getString(resId);
		return str;
	}
}
