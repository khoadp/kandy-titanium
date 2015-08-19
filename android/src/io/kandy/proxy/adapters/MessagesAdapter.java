package io.kandy.proxy.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.genband.kandy.api.Kandy;
import com.genband.kandy.api.services.chats.*;
import com.genband.kandy.api.utils.KandyMediaUtils;
import io.kandy.proxy.fragments.ChatFragment.Message;
import io.kandy.utils.KandyUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class MessagesAdapter extends ArrayAdapter<Message> {

	private static final String TAG = MessagesAdapter.class.getSimpleName();

	private SimpleDateFormat mDateFormat;

	public MessagesAdapter(Context context, int resource, List<Message> items) {
		super(context, resource, items);
		mDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ViewHolder viewHolder;

		if (convertView == null) {
			LayoutInflater inflater = LayoutInflater.from(getContext());
			convertView = inflater.inflate(KandyUtils.getLayout("message_listview_item"), parent, false);
			viewHolder = new ViewHolder();
			viewHolder.uiMessageText = (TextView) convertView.findViewById(KandyUtils.getId("ui_message_body_text"));
			viewHolder.uiMessageDate = (TextView) convertView.findViewById(KandyUtils.getId("ui_message_body_time"));
			viewHolder.uiNumberText = (TextView) convertView.findViewById(KandyUtils.getId("ui_message_sender_text"));
			viewHolder.uiThumbnail = (ImageView) convertView.findViewById(KandyUtils.getId("ui_thumbnail"));
			viewHolder.uiMsgDirection = (ImageView) convertView.findViewById(KandyUtils.getId("ui_message_direction"));
			viewHolder.uiThumbnailVideoIndicator = (ImageView) convertView.findViewById(KandyUtils
					.getId("ui_thumbnail_video_indicator"));
			viewHolder.uiThumbnailVideoDuration = (TextView) convertView.findViewById(KandyUtils
					.getId("ui_thumbnail_video_duration"));

			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		Message message = getItem(position);
		if (message != null) {

			setMessageDisplayName(viewHolder, message);
			setMessageStatus(convertView, message);

			boolean isIncoming = message.kandyMessage.isIncoming();
			setMessageDirection(viewHolder, isIncoming);

			String text = message.kandyMessage.getMediaItem().getMessage();
			setMessageText(viewHolder, text);

			long time = message.kandyMessage.getTimestamp() + Kandy.getSession().getUTCTimestampCorrection();
			Date data = new Date(time);
			String dateString = mDateFormat.format(data);
			setMessageDate(viewHolder, dateString);

			// This function is time consuming operation , in commercial
			// application, consider handle it on non UI thread
			setMessageThumbnail(viewHolder, message.kandyMessage);

		}

		return convertView;
	}

	private void setMessageDirection(ViewHolder viewHolder, boolean isIncoming) {
		if (isIncoming) {
			viewHolder.uiMsgDirection.setImageResource(KandyUtils.getDrawable("ic_incoming"));
		} else {
			viewHolder.uiMsgDirection.setImageResource(KandyUtils.getDrawable("ic_outgoing"));
		}
	}

	private void setMessageText(ViewHolder viewHolder, String text) {
		viewHolder.uiMessageText.setText(text);
	}

	private void setMessageDate(ViewHolder viewHolder, String text) {
		viewHolder.uiMessageDate.setText(text);
	}

	private void setMessageDisplayName(ViewHolder viewHolder, Message message) {
		boolean isIncoming = message.kandyMessage.isIncoming();
		String displayName;
		if (isIncoming) {
			displayName = message.kandyMessage.getSender().getUserName();
		} else {
			displayName = message.kandyMessage.getRecipient().getUserName();
		}

		viewHolder.uiNumberText.setText(displayName);
	}

	private void setMessageStatus(View convertView, Message message) {
		convertView.setBackgroundColor(Color.WHITE);

		if (message.isMsgAcked)// if was sent ACK for received message
			convertView.setBackgroundColor(Color.GRAY);

		if (message.isMsgDelivered) { // if got ACK for sent message
			convertView.setBackgroundColor(Color.argb(0xaa, 0x00, 0xaa, 0x00));
		}
	}

	private void setMessageThumbnail(ViewHolder viewHolder, IKandyMessage message) {
		KandyMessageMediaItemType mediaType = message.getMediaItem().getMediaItemType();
		KandyMessageType messageType = message.getMessageType();

		Uri thumbnailUri;
		Bitmap bitmap = null;

		viewHolder.uiThumbnailVideoIndicator.setVisibility(View.INVISIBLE);
		viewHolder.uiThumbnailVideoDuration.setVisibility(View.INVISIBLE);

		switch (mediaType) {

		case AUDIO:
			viewHolder.uiThumbnail.setImageResource(KandyUtils.getDrawable("ic_action_volume_on"));
			break;

		case LOCATION:
			viewHolder.uiThumbnail.setImageResource(KandyUtils.getDrawable("ic_action_place"));
			break;

		case TEXT:
			if (KandyMessageType.SMS.equals(messageType)) {
				viewHolder.uiThumbnail.setImageResource(android.R.drawable.sym_action_email);
			} else {
				viewHolder.uiThumbnail.setImageResource(KandyUtils.getDrawable("ic_action_chat"));
			}
			break;

		case FILE:
			viewHolder.uiThumbnail.setImageResource(KandyUtils.getDrawable("ic_action_attachment"));
			break;

		case IMAGE:
			IKandyImageItem kandyImage = (IKandyImageItem) message.getMediaItem();
			Log.i(TAG, "IMAGE: viewHolder.uiThumbnail.setImageBitmap(bitmap) called");

			if (kandyImage.getLocalThumbnailUri() == null) {
				thumbnailUri = kandyImage.getLocalDataUri();
			} else {
				thumbnailUri = kandyImage.getLocalThumbnailUri();
			}

			if (thumbnailUri != null) {
				bitmap = KandyMediaUtils.getImageThumbnail(thumbnailUri);
			}

			if (bitmap != null) {
				viewHolder.uiThumbnail.setImageBitmap(bitmap);
			} else {
				viewHolder.uiThumbnail.setImageResource(KandyUtils.getDrawable("ic_action_picture"));
			}

			break;

		case CONTACT:
			IKandyContactItem kandyContact = (IKandyContactItem) message.getMediaItem();

			Bitmap bmp = kandyContact.getContactImage();

			if (bmp != null) {
				viewHolder.uiThumbnail.setImageBitmap(bmp);
			} else {
				viewHolder.uiThumbnail.setImageResource(KandyUtils.getDrawable("ic_action_person"));
			}

			break;

		case VIDEO:
			IKandyVideoItem kandyVideo = (IKandyVideoItem) message.getMediaItem();

			if (kandyVideo.getLocalThumbnailUri() == null) {

				thumbnailUri = kandyVideo.getLocalDataUri();

			} else {
				thumbnailUri = kandyVideo.getLocalThumbnailUri();
			}

			bitmap = KandyMediaUtils.getImageThumbnail(thumbnailUri);

			if (bitmap != null) {
				viewHolder.uiThumbnailVideoIndicator.setVisibility(View.VISIBLE);

				viewHolder.uiThumbnail.setImageBitmap(bitmap);
			} else {
				viewHolder.uiThumbnail.setImageResource(KandyUtils.getDrawable("ic_action_video"));
			}

			long duration = kandyVideo.getDuration() / 1000;
			Log.d(TAG, "setMessageThumbnail: " + " Video duration: " + duration);

			if (duration > 0) {
				String durationString = String.format("%d:%02d:%02d", duration / 3600, (duration % 3600) / 60,
						(duration % 60));
				viewHolder.uiThumbnailVideoDuration.setText(durationString);
				viewHolder.uiThumbnailVideoDuration.setVisibility(View.VISIBLE);
			} else {
				viewHolder.uiThumbnailVideoDuration.setVisibility(View.INVISIBLE);
			}

			break;

		default:
			viewHolder.uiThumbnail.setVisibility(View.GONE);
			break;

		}
	}

	public void addUniqe(Message myMessage) {
		IKandyMessage kandyMsgToAdd = myMessage.kandyMessage;
		for (int i = 0; i < getCount(); i++) {

			IKandyMessage kandyMessage = (IKandyMessage) getItem(i).kandyMessage;

			if (kandyMessage.isIncoming() && kandyMessage.getUUID().equals(kandyMsgToAdd.getUUID())) {
				return;
			}
		}

		add(myMessage);
		notifyDataSetChanged();
	}

	public boolean markAsReceived(IKandyMessage message) {

		for (int i = 0; i < this.getCount(); i++) {
			Message msg = getItem(i);

			if (msg.kandyMessage.isIncoming() && message.getUUID().equals(msg.kandyMessage.getUUID())
					&& !msg.isMsgAcked) {

				getItem(i).isMsgAcked = true;

				return true;
			}
		}

		return false;
	}

	public void markAsDelivered(KandyDeliveryAck message) {
		UUID msgUUId = message.getUUID();

		for (int i = 0; i < this.getCount(); i++) {
			Message msg = getItem(i);

			if (!msg.kandyMessage.isIncoming() && msgUUId.equals(msg.kandyMessage.getUUID()) && !msg.isMsgDelivered) {
				getItem(i).isMsgDelivered = true;
				notifyDataSetChanged();
				return;
			}
		}
	}

	static class ViewHolder {
		TextView uiNumberText;
		TextView uiMessageText;
		TextView uiMessageDate;
		ImageView uiThumbnail;
		ImageView uiMsgDirection;
		ImageView uiThumbnailVideoIndicator;
		TextView uiThumbnailVideoDuration;
	}

}
