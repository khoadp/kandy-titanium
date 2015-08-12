package io.kandy.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.widget.Toast;

/**
 * Handles notification on UI thread from other threads
 */
public class UIUtils {

	private static ProgressDialog mProgressDialog;

	public static void showProgressDialogOnUiThread(final Activity activity, final String message) {
		activity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				dismissProgressDialog();
				mProgressDialog = ProgressDialog.show(activity, null, message);
			}
		});
	}

	public static ProgressDialog showProgressDialogOnUiThreadWithProgress(final Activity activity, final String message) {
		activity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				dismissProgressDialog();
				mProgressDialog = new ProgressDialog(activity);
				mProgressDialog.setMessage(message);
				mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
				mProgressDialog.setProgressNumberFormat(null);
				mProgressDialog.show();
			}
		});
		return mProgressDialog;
	}

	public static ProgressDialog showCancelableProgressDialogOnUiThreadWithProgress(final Activity activity,
			final String message, final OnCancelListener listener) {
		activity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				dismissProgressDialog();
				mProgressDialog = new ProgressDialog(activity);
				mProgressDialog.setCancelable(true);
				mProgressDialog.setOnCancelListener(listener);
				mProgressDialog.setMessage(message);
				mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
				mProgressDialog.setProgressNumberFormat(null);
				mProgressDialog.show();
			}
		});
		return mProgressDialog;
	}

	public static ProgressDialog showCancelableProgressDialogOnUiThreadWithProgressAndButton(final Activity activity,
			final String message, final OnCancelListener listener, final DialogInterface.OnClickListener clickListener) {
		activity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				dismissProgressDialog();
				mProgressDialog = new ProgressDialog(activity);
				mProgressDialog.setCancelable(true);
				mProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE,
						activity.getString(KandyUtils.getId("ui_dialog_cancel_button_label")), clickListener);
				mProgressDialog.setOnCancelListener(listener);
				mProgressDialog.setMessage(message);
				mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
				mProgressDialog.setProgressNumberFormat(null);
				mProgressDialog.show();
			}
		});
		return mProgressDialog;
	}

	public static void showProgressInDialogOnUiThread(final int progress) {
		if (mProgressDialog != null && mProgressDialog.isShowing()) {
			mProgressDialog.setProgress(progress);
		}
	}

	public static void showProgressDialogWithMessage(Context context, String pMessage) {
		dismissProgressDialog();
		mProgressDialog = ProgressDialog.show(context, null, pMessage);
	}

	public static void dismissProgressDialog() {
		if (mProgressDialog != null && mProgressDialog.isShowing()) {
			mProgressDialog.dismiss();
			mProgressDialog = null;
		}
	}

	/**
	 * Show toast with message. Must be called from UI Thread
	 * 
	 * @param context
	 * @param pMessage
	 *            message to be shown
	 */
	public static void showToastWithMessage(Context context, String pMessage) {
		dismissProgressDialog();
		Toast.makeText(context, pMessage, Toast.LENGTH_SHORT).show();
	}

	/**
	 * Show dialog with message. Must be called from UI Thread
	 * 
	 * @param context
	 * @param pErrorMessage
	 *            message to be shown
	 */
	public static void showDialogWithErrorMessage(Context context, String pErrorMessage) {
		dismissProgressDialog();

		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle("Error");
		builder.setMessage(pErrorMessage);
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});

		builder.create().show();
	}

	/**
	 * Show titled dialog with message. Must be called from UI Thread
	 * 
	 * @param context
	 * @param pTitle
	 *            title of the dialog
	 * @param pMessage
	 *            message to be shown
	 */
	public static void showDialogWithTitledMessage(Context context, String pTitle, String pMessage) {
		dismissProgressDialog();

		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(pTitle);
		builder.setMessage(pMessage);
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});

		builder.create().show();
	}

	/**
	 * Handles the result of requests on UI thread(Hides the progress dialog,
	 * shows the toast/dialog)
	 * 
	 * @param isFail
	 *            result of request - succeed<tt>(false)</tt>/failed
	 *            <tt>(true)</tt>
	 * @param pMessage
	 *            response to show
	 */
	public static void handleResultOnUiThread(final Activity pActivity, final boolean isFail, final String pMessage) {
		pActivity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				dismissProgressDialog();

				if (isFail) {
					showDialogWithErrorMessage(pActivity, pMessage);
				} else {
					showToastWithMessage(pActivity, pMessage);
				}
			}
		});
	}

	public static void showDialogOnUiThread(final Activity pActivity, final String pTitle, final String pMessage) {
		pActivity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				dismissProgressDialog();
				showDialogWithTitledMessage(pActivity, pTitle, pMessage);
			}
		});
	}
}
