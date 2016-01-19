package io.kandy.proxy;

import io.kandy.KandyModule;
import io.kandy.utils.KandyUtils;

import java.util.ArrayList;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollFunction;
import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.annotations.Kroll;

import android.util.Log;

import com.genband.kandy.api.Kandy;
import com.genband.kandy.api.services.calls.KandyRecord;
import com.genband.kandy.api.services.presence.IKandyPresence;
import com.genband.kandy.api.services.presence.KandyPresenceResponseListener;
import com.genband.kandy.api.utils.KandyIllegalArgumentException;

/**
 * Presence service
 * 
 * @author kodeplusdev
 * 
 */
@Kroll.proxy(creatableInModule = KandyModule.class)
public class PresenceServiceProxy extends KrollProxy {

	public static final String LCAT = PresenceServiceProxy.class.getSimpleName();

	/**
	 * Start watch users
	 * 
	 * @param args
	 */
	@Kroll.method
	public void startWatch(KrollDict args) {
		final KrollFunction success = (KrollFunction) args.get("success");
		final KrollFunction error = (KrollFunction) args.get("error");

		String[] arrayList = args.getStringArray("list");

		ArrayList<KandyRecord> list = new ArrayList<KandyRecord>();

		try {
			for (String u : arrayList)
				list.add(new KandyRecord(u));
		} catch (KandyIllegalArgumentException e) {
			e.printStackTrace();
			KandyUtils.sendFailResult(getKrollObject(), error, e.getMessage());
		}

		if (list.size() == 0) {
			KandyUtils.sendFailResult(getKrollObject(), error, "The user list is empty!");
			return;
		}

		Kandy.getServices().getPresenceService().retrievePresence(list, new KandyPresenceResponseListener() {

			@Override
			public void onRequestSucceed(ArrayList<IKandyPresence> presences, ArrayList<KandyRecord> absences) {
				Log.d(LCAT,
						"KandyPresenceResponseListener->onRequestSucceeded() was invoked: presences: "
								+ presences.size() + " and absences: " + absences.size());

				KrollDict result = new KrollDict();

				ArrayList<KrollDict> presencesList = new ArrayList<KrollDict>();
				for (IKandyPresence online : presences) {
					KrollDict obj = new KrollDict();
					obj.put("user", online.getUser().getUri());
					obj.put("lastSeen", online.getLastSeenDate().toString());
					presencesList.add(obj);
				}
				result.put("presences", presencesList.toArray());

				ArrayList<String> absencesList = new ArrayList<String>();
				for (KandyRecord offline : absences) {
					absencesList.add(offline.getUri());
				}
				result.put("absences", absencesList.toArray());

				KandyUtils.sendSuccessResult(getKrollObject(), success, result);
			}

			@Override
			public void onRequestFailed(int code, String err) {
				Log.d(LCAT, "KandyPresenceResponseListener->onRequestFailed() was invoked: " + String.valueOf(code)
						+ " - " + err);
				KandyUtils.sendFailResult(getKrollObject(), error, code, err);
			}
		});
	}

}
