package io.kandy.proxy;

import android.app.Activity;
import android.util.Log;
import com.genband.kandy.api.Kandy;
import com.genband.kandy.api.provisioning.IKandyValidationResponse;
import com.genband.kandy.api.provisioning.KandyValidationMethoud;
import com.genband.kandy.api.provisioning.KandyValidationResponseListener;
import com.genband.kandy.api.services.common.KandyResponseListener;
import com.genband.kandy.api.services.location.IKandyAreaCode;
import com.genband.kandy.api.services.location.KandyCountryInfoResponseListener;
import io.kandy.KandyConstant;
import io.kandy.KandyModule;
import io.kandy.proxy.views.ProvisioningViewProxy;
import io.kandy.utils.KandyUtils;
import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollFunction;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.view.TiUIView;

/**
 * Provisioning service
 *
 * @author kodeplusdev
 */
@Kroll.proxy(creatableInModule = KandyModule.class)
public class ProvisioningServiceProxy extends TiViewProxy {

    public static final String LCAT = ProvisioningServiceProxy.class.getSimpleName();

    @Kroll.constant
    public static final String PROVISIONING_WIDGET_TYPE = "type";
    @Kroll.constant
    public static final String PROVISIONING_REQUEST_WIDGET = "request";
    @Kroll.constant
    public static final String PROVISIONING_VALIDATE_WIDGET = "validate";
    @Kroll.constant
    public static final String PROVISIONING_DEACTIVATE_WIDGET = "deactivate";

    private String twoLetterISOCountryCode;
    private ProvisioningViewProxy viewProxy;

    private void getTwoLetterISOCountryCode() {
        try {
            Kandy.getServices().getLocationService().getCountryInfo(new KandyCountryInfoResponseListener() {

                @Override
                public void onRequestFailed(int code, String err) {
                    Log.d(LCAT,
                            "KandyCountryInfoResponseListener->onRequestFailed() was invoked: " + String.valueOf(code)
                                    + " - " + err);
                    setTwoLetterISOCountryCode(KandyConstant.DEFAULT_TWO_LETTER_ISO_COUNTRY_CODE);
                }

                @Override
                public void onRequestSuccess(IKandyAreaCode respond) {
                    Log.d(LCAT, "KandyCountryInfoResponseListener->onRequestSuccess() was invoked.");
                    setTwoLetterISOCountryCode(respond.getCountryNameShort());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            setTwoLetterISOCountryCode(KandyConstant.DEFAULT_TWO_LETTER_ISO_COUNTRY_CODE);
        }
    }

    @Override
    public TiUIView createView(Activity activity) {
        viewProxy = new ProvisioningViewProxy(this);
        return viewProxy;
    }

    @Override
    public void handleCreationDict(KrollDict options) {
        super.handleCreationDict(options);
        if (options.containsKey("twoLetterISOCountryCode")) {
            setTwoLetterISOCountryCode(options.getString("twoLetterISOCountryCode"));
        } else {
            this.getTwoLetterISOCountryCode();
        }
    }

    private void setTwoLetterISOCountryCode(String code) {
        this.twoLetterISOCountryCode = code;
        if (viewProxy != null)
            viewProxy.setTwoLetterISOCountryCode(code);
    }

    @Kroll.method
    public void requestCode(KrollDict args) {
        final KrollFunction success = (KrollFunction) args.get("success");
        final KrollFunction error = (KrollFunction) args.get("error");

        final String phoneNumber = (String) args.get("phoneNumber");
        String twoLetterISOCountryCode = (String) args.get("countryCode");

        if (twoLetterISOCountryCode == null) {
            Log.i(LCAT, "Using default twoLetterISOCountryCode");
            twoLetterISOCountryCode = this.twoLetterISOCountryCode;
        }

        Kandy.getProvisioning().requestCode(KandyValidationMethoud.SMS, phoneNumber, twoLetterISOCountryCode, null, new KandyResponseListener() {

            @Override
            public void onRequestFailed(int code, String err) {
                Log.d(LCAT, "KandyResponseListener->onRequestFailed() was invoked: " + String.valueOf(code) + " - "
                        + err);
                KandyUtils.sendFailResult(getKrollObject(), error, code, err);
            }

            @Override
            public void onRequestSucceded() {
                Log.d(LCAT, "KandyResponseListener->onRequestSucceded() was invoked.");
                KandyUtils.sendSuccessResult(getKrollObject(), success);
            }
        });
    }

    @Kroll.method
    public void validate(KrollDict args) {
        final KrollFunction success = (KrollFunction) args.get("success");
        final KrollFunction error = (KrollFunction) args.get("error");

        String phoneNumber = (String) args.get("phoneNumber");
        String twoLetterISOCountryCode = (String) args.get("countryCode");
        String otp = (String) args.get("otp");

        if (twoLetterISOCountryCode == null) {
            Log.i(LCAT, "Using default twoLetterISOCountryCode");
            twoLetterISOCountryCode = this.twoLetterISOCountryCode;
        }

        Kandy.getProvisioning().validateAndProvision(phoneNumber, otp, twoLetterISOCountryCode,
                new KandyValidationResponseListener() {

                    @Override
                    public void onRequestFailed(int code, String err) {
                        Log.d(LCAT,
                                "KandyValidationResponseListener->onRequestFailed() was invoked: "
                                        + String.valueOf(code) + " - " + err);
                        KandyUtils.sendFailResult(getKrollObject(), error, code, err);
                    }

                    @Override
                    public void onRequestSuccess(IKandyValidationResponse reqpond) {
                        Log.d(LCAT, "KandyValidationResponseListener->onRequestSuccess() was invoked.");
                        KandyUtils.sendSuccessResult(getKrollObject(), success);
                    }
                });
    }

    @Kroll.method
    public void deactivate(KrollDict callbacks) {
        final KrollFunction success = (KrollFunction) callbacks.get("success");
        final KrollFunction error = (KrollFunction) callbacks.get("error");

        Kandy.getProvisioning().deactivate(new KandyResponseListener() {

            @Override
            public void onRequestFailed(int code, String err) {
                Log.d(LCAT, "KandyResponseListener->onRequestFailed() was invoked: " + String.valueOf(code) + " - "
                        + err);
                KandyUtils.sendFailResult(getKrollObject(), error, code, err);
            }

            @Override
            public void onRequestSucceded() {
                Log.d(LCAT, "KandyResponseListener->onRequestSucceded() was invoked.");
                KandyUtils.sendSuccessResult(getKrollObject(), success);
            }
        });
    }
}
