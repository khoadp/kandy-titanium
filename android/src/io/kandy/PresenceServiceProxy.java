package io.kandy;

import android.util.Log;
import com.genband.kandy.api.Kandy;
import com.genband.kandy.api.services.calls.KandyRecord;
import com.genband.kandy.api.services.presence.IKandyPresence;
import com.genband.kandy.api.services.presence.KandyPresenceResponseListener;
import com.genband.kandy.api.utils.KandyIllegalArgumentException;
import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollFunction;
import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.annotations.Kroll;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Presence service
 * 
 * @author kodeplusdev
 *
 */
@Kroll.proxy(creatableInModule = KandyModule.class)
public class PresenceServiceProxy extends KrollProxy {

	public static final String LCAT = AccessProxy.class.getSimpleName();
	
	public PresenceServiceProxy() {
		super();
	}

	/**
	 * Start watch users
	 * 
	 * @param args
	 */
	@Kroll.method
	public void startWatch(KrollDict args) {
		final KrollFunction success = (KrollFunction) args.get("success");
		final KrollFunction error = (KrollFunction) args.get("error");

		String[] arrayList = (String[]) args.get("list");
		
		ArrayList<KandyRecord> list = new ArrayList<KandyRecord>();

        try {
        	for (String u : arrayList)
        		list.add(new KandyRecord(u));
        } catch (KandyIllegalArgumentException e) {
            e.printStackTrace();
            KandyUtils.sendFailResult(getKrollObject(), error, e.getMessage());
        }

        if (list.size() == 0) return;

		Kandy.getServices().getPresenceService()
				.retrievePresence(list, new KandyPresenceResponseListener() {

			        @Override
			        public void onRequestSucceed(ArrayList<IKandyPresence> presences, ArrayList<KandyRecord> absences) {
			            Log.d(LCAT, "KandyPresenceResponseListener->onRequestSucceeded() was invoked: presences: " + presences.size() + " and absences: " + absences.size());

			            JSONObject result = new JSONObject();

			            try {
			                JSONArray presencesList = new JSONArray();
			                for (IKandyPresence online : presences) {
			                    JSONObject obj = new JSONObject();
			                    obj.put("user", online.getUser().getUri());
			                    obj.put("lastSeen", online.getLastSeenDate().toString());
			                    presencesList.put(obj);
			                }
			                result.put("presences", presencesList);

			                JSONArray absencesList = new JSONArray();
			                for (KandyRecord offline : absences) {
			                    absencesList.put(offline.getUri());
			                }
			                result.put("absences", absencesList);

			            } catch (JSONException e) {
			                e.printStackTrace();
			            }

			            KandyUtils.sendSuccessResult(getKrollObject(), success, result);
			        }

			        @Override
			        public void onRequestFailed(int code, String err) {
			            Log.d(LCAT, "KandyPresenceResponseListener->onRequestFailed() was invoked: " + String.valueOf(code) + " - " + error);
			            KandyUtils.sendFailResult(getKrollObject(), error, code, err);
			        }
				});
	}

}
