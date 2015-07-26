package io.kandy.utils;

import android.content.Context;
import android.location.Location;
import android.util.Log;
import com.genband.kandy.api.services.calls.IKandyCall;
import com.genband.kandy.api.services.calls.KandyRecord;
import io.kandy.KandyModule;
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

	public static int getDrawable(String name) {
		return getResource(name, "drawable");
	}

	public static void checkAndSendResult(KrollObject krollObject, KrollFunction krollFunction, HashMap result) {
		if (krollObject != null && krollFunction != null) {
			krollFunction.call(krollObject, result);
		}
	}

	public static void checkAndSendResult(KrollObject krollObject, KrollFunction krollFunction, Object... results) {
		if (krollObject != null && krollFunction != null) {
			krollFunction.call(krollObject, results);
		}
	}

	public static void sendFailResult(KrollObject krollObject, KrollFunction krollFunction) {
		checkAndSendResult(krollObject, krollFunction, KandyModule.STATUS_ERROR);
	}

	public static void sendFailResult(KrollObject krollObject, KrollFunction krollFunction, int code, String error) {
		String result = String.format(getString("kandy_error_message"), code, error);
		checkAndSendResult(krollObject, krollFunction, result);
	}

	public static void sendFailResult(KrollObject krollObject, KrollFunction krollFunction, String error) {
		checkAndSendResult(krollObject, krollFunction, error);
	}

	public static void sendSuccessResult(KrollObject krollObject, KrollFunction krollFunction) {
		checkAndSendResult(krollObject, krollFunction, KandyModule.STATUS_SUCCESS);
	}

	public static void sendSuccessResult(KrollObject krollObject, KrollFunction krollFunction, Object result) {
		checkAndSendResult(krollObject, krollFunction, result);
	}

	public static KrollDict getKrollDictFromKandyCall(IKandyCall call) {
		KrollDict obj = new KrollDict();
		obj.put("callId", call.getCallId());
        obj.put("callee", getKrollDictFromKandyRecord(call.getCallee()));
        obj.put("via", call.getVia());
        obj.put("type", call.getCallType().name());
        obj.put("state", call.getCallState().name());
        obj.put("startTime", call.getStartTime());
        obj.put("endTime", call.getEndTime());
        obj.put("duration", call.getDurationString());
        obj.put("cameraForVideo", call.getCameraForVideo().name());
        obj.put("isCallStartedWithVideo", call.isCallStartedWithVideo());
        obj.put("isIncomingCall", call.isIncomingCall());
        obj.put("isMute", call.isMute());
        obj.put("isOnHold", call.isOnHold());
        obj.put("isOtherParticipantOnHold", call.isOtherParticipantOnHold());
        obj.put("isReceivingVideo", call.isReceivingVideo());
        obj.put("isSendingVideo", call.isSendingVideo());
        return obj;
    }
	
	public static KrollDict getKrollDictFromKandyRecord(KandyRecord record) {
		KrollDict obj = new KrollDict();
		obj.put("uri", record.getUri());
        obj.put("type", record.getType().toString());
        obj.put("domain", record.getDomain());
        obj.put("username", record.getUserName());
        return obj;
    }
	
	@SuppressWarnings("unchecked")
	public static KrollDict JSONObjectToKrollDict(JSONObject object) throws JSONException {
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

	public static List<Object> JSONArrayToList(JSONArray array) throws JSONException {
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

	public static KrollDict getKrollDictFromCallbacks(KrollDict callbacks, String key) {
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

	public static KrollFunction joinKrollFunctions(final KrollFunction... functions) {
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

	public static Location getLocationFromJson(JSONObject obj) {
		Location location = new Location("Kandy");

		location.setAccuracy((Float) getObjectValueFromJson(obj, "accuracy", 0.0f));
		location.setAltitude((Double) getObjectValueFromJson(obj, "altitude", 0.0));
		location.setBearing((Float) getObjectValueFromJson(obj, "bearing", 0.0f));
		location.setLatitude((Double) getObjectValueFromJson(obj, "latitude", 0.0));
		location.setLongitude((Double) getObjectValueFromJson(obj, "longitude", 0.0));
		location.setProvider((String) getObjectValueFromJson(obj, "provider", "Kandy"));
		location.setTime((Long) getObjectValueFromJson(obj, "time", 0));
		location.setSpeed((Float) getObjectValueFromJson(obj, "speed", 0.0f));

		return location;
	}

	public static Object getObjectValueFromJson(JSONObject obj, String key, Object def) {
		try {
			return obj.get(key);
		} catch (JSONException e) {
			// e.printStackTrace();
		}
		return def;
	}
}
