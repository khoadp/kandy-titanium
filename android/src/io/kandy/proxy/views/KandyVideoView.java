package io.kandy.proxy.views;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import com.genband.kandy.api.services.calls.IKandyCall;
import com.genband.kandy.api.services.calls.KandyView;
import io.kandy.utils.KandyUtils;

/**
 * Display call video view
 *
 * @author kodeplusdev
 * @version 1.2.0
 */
public class KandyVideoView extends Dialog {

    private KandyView kandyView;
    private RelativeLayout relativeLayout;

    private WindowManager.LayoutParams wmlp;
    private ViewGroup.LayoutParams params;

    public KandyVideoView(Context context) {
        super(context);
        setContentView(KandyUtils.getLayout("kandy_video_view"));

        Window window = getWindow();
        window.setBackgroundDrawableResource(android.R.color.transparent);
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
        window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        setCanceledOnTouchOutside(false);

        kandyView = (KandyView) findViewById(KandyUtils.getId("kandy_video_view"));
        relativeLayout = (RelativeLayout) findViewById(KandyUtils.getId("kandy_video_view_container"));

        wmlp = getWindow().getAttributes();
        wmlp.gravity = Gravity.TOP | Gravity.LEFT;

        params = relativeLayout.getLayoutParams();
    }

    public void layout(int left, int top, int width, int height) {
        wmlp.x = left;
        wmlp.y = top;
        params.width = width;
        params.height = height;

        getWindow().setAttributes(wmlp);
        relativeLayout.setLayoutParams(params);
    }

    public void setLocalVideoView(final IKandyCall call) {
        setOnShowListener(new OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                call.setLocalVideoView(kandyView);
            }
        });
    }

    public void setRemoteVideoView(final IKandyCall call) {
        setOnShowListener(new OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                call.setRemoteVideoView(kandyView);
            }
        });
    }
}