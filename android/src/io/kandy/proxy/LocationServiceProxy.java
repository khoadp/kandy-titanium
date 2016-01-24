package io.kandy.proxy;

import android.location.Location;
import android.util.Log;
import com.genband.kandy.api.Kandy;
import com.genband.kandy.api.services.location.IKandyAreaCode;
import com.genband.kandy.api.services.location.KandyCountryInfoResponseListener;
import com.genband.kandy.api.services.location.KandyCurrentLocationListener;
import com.genband.kandy.api.utils.KandyIllegalArgumentException;
import io.kandy.KandyModule;
import io.kandy.utils.KandyUtils;
import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollFunction;
import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.annotations.Kroll;

/**
 * Location service
 *
 * @author kodeplusdev
 */
@Kroll.proxy(creatableInModule = KandyModule.class)
public class LocationServiceProxy extends KrollProxy {

    private static final String LCAT = LocationServiceProxy.class.getSimpleName();

    /**
     * Get country info
     *
     * @param args
     */
    @Kroll.method
    public void getCountryInfo(KrollDict args) {
        final KrollFunction success = (KrollFunction) args.get("success");
        final KrollFunction error = (KrollFunction) args.get("error");

        Kandy.getServices().getLocationService().getCountryInfo(new KandyCountryInfoResponseListener() {

            @Override
            public void onRequestFailed(int code, String err) {
                Log.d(LCAT, "KandyCountryInfoResponseListener->onRequestFailed() was invoked: " + String.valueOf(code)
                        + " - " + err);
                KandyUtils.sendFailResult(getKrollObject(), error, code, err);
            }

            @Override
            public void onRequestSuccess(IKandyAreaCode response) {
                Log.d(LCAT, "KandyCountryInfoResponseListener->onRequestSuccess() was invoked.");
                KrollDict data = new KrollDict();
                data.put("code", response.getCountryCode());
                data.put("nameLong", response.getCountryNameLong());
                data.put("nameShort", response.getCountryNameShort());
                KandyUtils.sendSuccessResult(getKrollObject(), success, data);
            }
        });
    }

    @Kroll.method
    public void getCurrentLocation(KrollDict args) {
        final KrollFunction success = (KrollFunction) args.get("success");
        final KrollFunction error = (KrollFunction) args.get("error");

        try {
            Kandy.getServices().getLocationService().getCurrentLocation(new KandyCurrentLocationListener() {

                public void onCurrentLocationReceived(Location location) {
                    Log.d(LCAT, "KandyCurrentLocationListener->onCurrentLocationReceived() was invoked.");

                    KrollDict result = new KrollDict();

                    result.put("provider", location.getProvider());
                    result.put("time", location.getTime());
                    result.put("latitude", location.getLatitude());
                    result.put("longitude", location.getLongitude());
                    result.put("hasAltitude", location.hasAltitude());
                    result.put("altitude", location.getAltitude());
                    result.put("hasSpeed", location.hasSpeed());
                    result.put("speed", location.getSpeed());
                    result.put("hasBearing", location.hasBearing());
                    result.put("bearing", location.getBearing());
                    result.put("hasAccuracy", location.hasAccuracy());
                    result.put("accuracy", location.getAccuracy());

                    KandyUtils.sendSuccessResult(getKrollObject(), success, result);
                }

                public void onCurrentLocationFailed(int code, String err) {
                    Log.d(LCAT,
                            "KandyCurrentLocationListener->onCurrentLocationFailed() was invoked: "
                                    + String.valueOf(code) + " - " + err);
                    KandyUtils.sendFailResult(getKrollObject(), error, code, err);
                }
            });
        } catch (KandyIllegalArgumentException e) {
            e.printStackTrace();
            KandyUtils.sendFailResult(getKrollObject(), error, e.getMessage());
        }
    }

}
