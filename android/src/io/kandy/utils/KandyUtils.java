package io.kandy.utils;

import android.content.Context;
import android.location.Location;
import android.util.Log;
import com.genband.kandy.api.services.billing.IKandyBillingPackage;
import com.genband.kandy.api.services.billing.IKandyBillingPackageProperty;
import com.genband.kandy.api.services.calls.IKandyCall;
import com.genband.kandy.api.services.calls.KandyRecord;
import com.genband.kandy.api.services.chats.IKandyMediaItem;
import com.genband.kandy.api.services.chats.IKandyMessage;
import com.genband.kandy.api.services.chats.KandyDeliveryAck;
import com.genband.kandy.api.services.groups.KandyGroup;
import com.genband.kandy.api.services.groups.KandyGroupParticipant;
import io.kandy.KandyModule;
import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollFunction;
import org.appcelerator.kroll.KrollObject;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Common utils for Kandy module.
 *
 * @author kodeplusdev
 */
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

    public static void checkAndSendResult(KrollObject krollObject, KrollFunction krollFunction, KrollDict result) {
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

    public static KrollDict getKrollDictFromKandyGroup(KandyGroup group) {
        KrollDict obj = new KrollDict();

        obj.put("id", getKrollDictFromKandyRecord(group.getGroupId()));
        obj.put("name", group.getGroupName());
        obj.put("creationDate", group.getCreationDate().getTime());
        obj.put("maxParticipantsNumber", group.getMaxParticipantsNumber());
        obj.put("selfParticipant", getKrollDictFromKandyGroupParticipant(group.getSelfParticipant()));
        obj.put("isGroupMuted", group.isGroupMuted());
        ArrayList<KrollDict> list = new ArrayList<KrollDict>();
        if (group.getGroupParticipants() != null) {
            for (KandyGroupParticipant participant : group.getGroupParticipants())
                list.add(getKrollDictFromKandyGroupParticipant(participant));
        }
        obj.put("participants", list);

        return obj;
    }

    public static KrollDict getKrollDictFromKandyGroupParticipant(KandyGroupParticipant participant) {
        KrollDict obj = getKrollDictFromKandyRecord(participant.getParticipant());

        obj.put("isAdmin", participant.isAdmin());
        obj.put("isMuted", participant.isMuted());

        return obj;
    }

    public static KrollDict getKrollDictFromKandyCall(IKandyCall call) {
        KrollDict obj = new KrollDict();
        obj.put("callId", call.getCallId());
        obj.put("callee", getKrollDictFromKandyRecord(call.getCallee()));
        obj.put("via", call.getVia());
        obj.put("type", call.getCallType().name());
        obj.put("state", call.getCallState().name());
//		obj.put("startTime", call.getStartTime());
//		obj.put("endTime", call.getEndTime());
//		obj.put("duration", call.getDurationString());
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

    public static KrollDict JSONObjectToKrollDict(JSONObject object) throws JSONException {
        KrollDict map = new KrollDict();

        Iterator<String> keysItr = object.keys();
        while (keysItr.hasNext()) {
            String key = keysItr.next();
            Object value = object.get(key);

            if (value instanceof JSONArray) {
                value = JSONArrayToList((JSONArray) value);
            } else if (value instanceof JSONObject) {
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
            } else if (value instanceof JSONObject) {
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

    public static Location getLocationFromJson(JSONObject obj) {
        Location location = new Location("Kandy");

        location.setAccuracy(getFloatValueFromJson(obj, "accuracy", 0.0f));
        location.setAltitude(getDoubleValueFromJson(obj, "altitude", 0.0));
        location.setBearing(getFloatValueFromJson(obj, "bearing", 0.0f));
        location.setLatitude(getDoubleValueFromJson(obj, "latitude", 0.0));
        location.setLongitude(getDoubleValueFromJson(obj, "longitude", 0.0));
        location.setProvider(getStringValueFromJson(obj, "provider", "Kandy"));
        location.setTime(getLongValueFromJson(obj, "time", 0));
        location.setSpeed(getFloatValueFromJson(obj, "speed", 0.0f));

        return location;
    }

    public static boolean getBoolValueFromJson(JSONObject obj, String key, boolean def) {
        try {
            return obj.getBoolean(key);
        } catch (JSONException e) {
            //e.printStackTrace();
        }
        return def;
    }

    public static String getStringValueFromJson(JSONObject obj, String key, String def) {
        try {
            return obj.getString(key);
        } catch (JSONException e) {
            //e.printStackTrace();
        }
        return def;
    }

    public static int getIntValueFromJson(JSONObject obj, String key, int def) {
        try {
            return obj.getInt(key);
        } catch (JSONException e) {
            //e.printStackTrace();
        }
        return def;
    }

    public static long getLongValueFromJson(JSONObject obj, String key, long def) {
        try {
            return obj.getLong(key);
        } catch (JSONException e) {
            //e.printStackTrace();
        }
        return def;
    }

    public static float getFloatValueFromJson(JSONObject obj, String key, float def) {
        return (float) getDoubleValueFromJson(obj, key, def);
    }

    public static double getDoubleValueFromJson(JSONObject obj, String key, double def) {
        try {
            return obj.getDouble(key);
        } catch (JSONException e) {
            //e.printStackTrace();
        }
        return def;
    }

    public static KrollDict getKrollDictFromKandyPackagesCredit(IKandyBillingPackage billingPackage) {
        KrollDict obj = new KrollDict();
        obj.put("currency", billingPackage.getCurrency());
        obj.put("balance", billingPackage.getBalance());
        obj.put("exiparyDate", billingPackage.getExiparyDate().toString());
        obj.put("packageId", billingPackage.getPackageId());
        obj.put("remainingTime", billingPackage.getRemainingTime());
        obj.put("startDate", billingPackage.getStartDate());
        obj.put("packageName", billingPackage.getPackageName());

        ArrayList<KrollDict> properties = new ArrayList<KrollDict>();
        if (billingPackage.getProperties().size() > 0) {
            ArrayList<IKandyBillingPackageProperty> billingPackageProperties = billingPackage.getProperties();
            for (IKandyBillingPackageProperty p : billingPackageProperties)
                properties.add(getKrollDictFromKandyBillingPackageProperty(p));
        }

        obj.put("properties", properties.toArray());
        return obj;
    }

    private static KrollDict getKrollDictFromKandyBillingPackageProperty(IKandyBillingPackageProperty property) {
        KrollDict obj = new KrollDict();
        obj.put("packageName", property.getPackageName());
        obj.put("quotaUnits", property.getQuotaUnits());
        obj.put("remainingQuota", property.getRemainingQuota());
        return obj;
    }

    public static KrollDict getKrollDictFromKandyMessage(IKandyMessage message) {
        KrollDict obj = new KrollDict();

        obj.put("messageType", message.getMessageType().name());
        obj.put("recipient", getKrollDictFromKandyRecord(message.getRecipient()));
        obj.put("sender", getKrollDictFromKandyRecord(message.getSender()));
        obj.put("timestamp", message.getTimestamp());
        obj.put("uuid", message.getUUID().toString());
        obj.put("eventType", message.getEventType().name());
        obj.put("mediaItem", getKrollDictFromKandyMediaItem(message.getMediaItem()));

        return obj;
    }

    private static KrollDict getKrollDictFromKandyMediaItem(IKandyMediaItem item) {
        KrollDict obj = new KrollDict();

        obj.put("mediaItemType", item.getMediaItemType().name());
        obj.put("message", item.getMessage());
        obj.put("mimeType", item.getMimeType());

        return obj;
    }

    public static KrollDict getKrollDictFromKandyDeliveryAck(KandyDeliveryAck ack) {
        KrollDict obj = new KrollDict();

        obj.put("uuid", ack.getUUID().toString());
        obj.put("eventType", ack.getEventType().name());
        obj.put("timestamp", ack.getTimestamp());

        return obj;
    }
}
