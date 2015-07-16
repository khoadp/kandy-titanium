package io.kandy.proxy;
//package io.kandy;
//
//import java.util.List;
//
//import android.content.Context;
//import android.graphics.Color;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ArrayAdapter;
//import android.widget.TextView;
//
//import com.kandy.ChatServiceProxy.Message;
//
//public class MessagesAdapter extends ArrayAdapter<Message> {
//
//	public MessagesAdapter(Context context, int textViewResourceId) {
//		super(context, textViewResourceId);
//	}
//
//	public MessagesAdapter(Context context, int resource, List<Message> items) {
//		super(context, resource, items);
//	}
//
//	@Override
//	public View getView(int position, View convertView, ViewGroup parent) {
//
//		ViewHolder viewHolder;
//
//		if(convertView == null) {			
//			LayoutInflater inflater = LayoutInflater.from(getContext());
//			convertView = inflater.inflate(Utils.getLayout(getContext(), "message_listview_item"), null);
//			viewHolder = new ViewHolder();
//			viewHolder.uiMessageText = (TextView)convertView.findViewById(Utils.getId(getContext(), "ui_message_body_text"));
//			viewHolder.uiNumberText = (TextView)convertView.findViewById(Utils.getId(getContext(), "ui_message_sender_text"));
//			convertView.setTag(viewHolder);
//		} else {
//			viewHolder = (ViewHolder)convertView.getTag();
//		}
//
//		Message message = getItem(position);
//		if(message != null) {
//
//			String toFrom = (message.getMessage().isIncoming())? Utils.getString(getContext(), "ui_message_listview_item_sender") : 
//				Utils.getString(getContext(), "ui_message_listview_item_recipient");
//			String recipientSender = (message.getMessage().isIncoming()? message.getMessage().getSender().getUri(): message.getMessage().getRecipient().getUri());
//
//			String formattedString = String.format("%s %s", toFrom, recipientSender);
//
//			if(viewHolder.uiNumberText != null)
//				viewHolder.uiNumberText.setText(formattedString);
//
//			if(viewHolder.uiMessageText != null)
//				viewHolder.uiMessageText.setText(message.getMessage().getData().getMessage());
//			
//			viewHolder.uiMessageText.setTag(message.getMessage().getUUID());
//			viewHolder.uiNumberText.setTag(message.getMessage().getUUID());
//			
//			convertView.setBackgroundColor(Color.WHITE);
//			
//			if(message.isAcked())//if was sent ACK for received message
//				convertView.setBackgroundColor(Color.GRAY);
//			
//			if(message.isMessageDelviered()) { //if got ACK for sent message
//				convertView.setBackgroundColor(Color.argb(0xaa, 0x00, 0xaa, 0x00));
//			}
//		}
//
//		return convertView;
//	}
//
//	static class ViewHolder {
//		TextView uiNumberText;
//		TextView uiMessageText;
//	}
//}
