package io.kandy;

import android.content.Context;
import android.util.Log;
import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollFunction;
import org.appcelerator.kroll.KrollObject;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public final class KandyUtils {

	private static Context _context;

	private KandyUtils() {
	}

	public static void initialize(Context context) {
		_context = context;
	}

	public static int getResource(String name, String type) {
		int res;
		String packageName = _context.getPackageName();
		res = _context.getResources().getIdentifier(name, type, packageName);
		return res;
	}

	public static String getString(String name) {
		String str;
		int resId = getResource(name, "string");
		str = _context.getString(resId);
		return str;
	}

	public static int getLayout(String name) {
		return getResource(name, "layout");
	}

	public static int getId(String name) {
		return getResource(name, "id");
	}

	public static void checkAndSendResult(KrollObject krollObject,
			KrollFunction krollFunction, HashMap result) {
		if (krollObject != null && krollFunction != null) {
			krollFunction.call(krollObject, result);
		}
	}

	public static void checkAndSendResult(KrollObject krollObject,
			KrollFunction krollFunction, Object... results) {
		if (krollObject != null && krollFunction != null) {
			krollFunction.call(krollObject, results);
		}
	}

	public static void sendFailResult(KrollObject krollObject,
			KrollFunction krollFunction) {
		KrollDict result = new KrollDict();
		result.put("status", KandyModule.STATUS_ERROR);
		checkAndSendResult(krollObject, krollFunction, result);
	}

	public static void sendFailResult(KrollObject krollObject,
			KrollFunction krollFunction, int code, String error) {
		String result = String.format(getString("kandy_error_message"), code,
				error);
		checkAndSendResult(krollObject, krollFunction, result);
	}

	public static void sendFailResult(KrollObject krollObject,
			KrollFunction krollFunction, String error) {
		checkAndSendResult(krollObject, krollFunction, error);
	}

	public static void sendSuccessResult(KrollObject krollObject,
			KrollFunction krollFunction) {
		KrollDict result = new KrollDict();
		result.put("status", KandyModule.STATUS_SUCCESS);
		checkAndSendResult(krollObject, krollFunction, result);
	}

	public static void sendSuccessResult(KrollObject krollObject,
			KrollFunction krollFunction, Object result) {
		checkAndSendResult(krollObject, krollFunction, result);
	}

	@SuppressWarnings("unchecked")
	public static KrollDict JSONObjectToKrollDict(JSONObject object)
			throws JSONException {
		KrollDict map = new KrollDict();

		Iterator<String> keysItr = object.keys();
		while (keysItr.hasNext()) {
			String key = keysItr.next();
			Object value = object.get(key);

			if (value instanceof JSONArray) {
				value = JSONArrayToList((JSONArray) value);
			}

			else if (value instanceof JSONObject) {
				value = JSONObjectToKrollDict((JSONObject) value);
			}
			map.put(key, value);
		}
		return map;
	}

	public static List<Object> JSONArrayToList(JSONArray array)
			throws JSONException {
		List<Object> list = new ArrayList<Object>();

		for (int i = 0; i < array.length(); i++) {
			Object value = array.get(i);
			if (value instanceof JSONArray) {
				value = JSONArrayToList((JSONArray) value);
			}

			else if (value instanceof JSONObject) {
				value = JSONObjectToKrollDict((JSONObject) value);
			}
			list.add(value);
		}
		return list;
	}

	public static KrollDict getKrollDictFromCallbacks(KrollDict callbacks,
			String key) {
		KrollDict result = null;
		if (callbacks != null) {
			result = callbacks.getKrollDict(key);
		}

		if (result == null) {
			Log.i(KandyUtils.class.getSimpleName(), key + " is not defined");
			result = new KrollDict();
		}

		return result;
	}

	public static KrollFunction joinKrollFunctions(
			final KrollFunction... functions) {
		return new KrollFunction() {

			@Override
			public void callAsync(KrollObject arg0, Object[] arg1) {
				for (KrollFunction krollFunction : functions) {
					if (krollFunction != null)
						krollFunction.callAsync(arg0, arg1);
				}
			}

			@Override
			public void callAsync(KrollObject arg0, HashMap arg1) {
				for (KrollFunction krollFunction : functions) {
					if (krollFunction != null)
						krollFunction.callAsync(arg0, arg1);
				}
			}

			@Override
			public Object call(KrollObject arg0, Object[] arg1) {
				HashMap<KrollFunction, Object> obj = new HashMap<KrollFunction, Object>();

				for (KrollFunction krollFunction : functions) {
					if (krollFunction != null)
						obj.put(krollFunction, krollFunction.call(arg0, arg1));
				}

				return obj;
			}

			@Override
			public Object call(KrollObject arg0, HashMap arg1) {
				HashMap<KrollFunction, Object> obj = new HashMap<KrollFunction, Object>();

				for (KrollFunction krollFunction : functions) {
					if (krollFunction != null)
						obj.put(krollFunction, krollFunction.call(arg0, arg1));
				}

				return obj;
			}
		};
	}
}
