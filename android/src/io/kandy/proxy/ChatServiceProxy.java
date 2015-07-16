package io.kandy.proxy;
//package io.kandy;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Queue;
//import java.util.UUID;
//import java.util.concurrent.LinkedBlockingQueue;
//
//import org.appcelerator.kroll.KrollDict;
//import org.appcelerator.kroll.KrollFunction;
//import org.appcelerator.kroll.annotations.Kroll;
//import org.appcelerator.titanium.proxy.TiViewProxy;
//import org.appcelerator.titanium.view.TiUIView;
//import org.json.JSONException;
//
//import android.app.Activity;
//import android.net.Uri;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.ListView;
//
//import com.genband.kandy.api.Kandy;
//import com.genband.kandy.api.services.calls.KandyRecord;
//import com.genband.kandy.api.services.chats.IKandyEvent;
//import com.genband.kandy.api.services.chats.IKandyMessage;
//import com.genband.kandy.api.services.chats.KandyChatServiceNotificationListener;
//import com.genband.kandy.api.services.common.KandyResponseListener;
//
///**
// * Chat service
// * 
// * @author kodpelusdev
// *
// */
//@Kroll.proxy(creatableInModule = KandyModule.class)
//public class ChatServiceProxy extends TiViewProxy {
//
//	public static String LCAT = ChatServiceProxy.class.getSimpleName();
//	
//	/**
//	 * Chat widget
//	 */
//	private class ChatWidget extends TiUIView {
//
//		private List<Message> mMessagesList;
//		private Queue<IKandyEvent> mMessagesUUIDQueue;
//		private MessagesAdapter mAdapter;
//		
//		public ChatWidget(TiViewProxy proxy) {
//			super(proxy);
//			
//			View layoutWraper;
//			
//			LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
//			layoutWraper = layoutInflater.inflate(
//					Utils.getLayout(getActivity(), "kandy_chat_widget"), null);
//			
//			ListView uiMessagesListView = (ListView)layoutWraper.findViewById(
//					Utils.getId(getActivity(), "kandy_chats_messages_list"));
//			// Set message adapter
//			mMessagesList = new ArrayList<Message>();
//			mMessagesUUIDQueue = new LinkedBlockingQueue<IKandyEvent>();
//			mAdapter = new MessagesAdapter(getActivity(), Utils.getLayout(getActivity(), "message_listview_item"), mMessagesList);
//			uiMessagesListView.setAdapter(mAdapter);
//			
//			final EditText phoneNumber = (EditText)layoutWraper.findViewById(
//					Utils.getId(getActivity(), "kandy_chats_phone_number_edit"));
//			final EditText messageEdit = (EditText)layoutWraper.findViewById(
//					Utils.getId(getActivity(), "activity_chats_message_edit"));
//			
//			Button btnSend = (Button)layoutWraper.findViewById(
//					Utils.getId(getActivity(), "kandy_chats_send_msg_button"));
//			Button btnPull = (Button)layoutWraper.findViewById(
//					Utils.getId(getActivity(), "kandy_chats_get_incoming_msgs_button"));
//			Button btnAck = (Button)layoutWraper.findViewById(
//					Utils.getId(getActivity(), "kandy_chats_ack_button"));
//			
//			btnSend.setOnClickListener(new View.OnClickListener() {
//				
//				public void onClick(View v) {
//					ChatServiceProxy.this.send(phoneNumber.getText().toString(), messageEdit.getText().toString());
//				}
//			});
//			
//			btnPull.setOnClickListener(new View.OnClickListener() {
//				
//				public void onClick(View v) {
//					KrollDict args = Utils.getKrollDictFromCallbacks(callbacks, "pull");
//					ChatServiceProxy.this.pullEvents(args);
//				}
//			});
//			
//			btnAck.setOnClickListener(new View.OnClickListener() {
//				
//				public void onClick(View v) {
//					IKandyEvent event = (mMessagesUUIDQueue.poll());
//					if(event == null) return;
//					if(getAckFormMessageByUUID(event.getUUID())) {
//						mAdapter.notifyDataSetChanged();
//						markMsgAsReceived(event);
//					}
//				}
//			});
//			
//			setNativeView(layoutWraper);
//		}
//		
//		private boolean getAckFormMessageByUUID(UUID pUUID) {
//			for(int i = 0; i<mMessagesList.size(); i++) {
//				Message msg = mMessagesList.get(i);
//
//				if(pUUID.equals(msg.getMessage().getUUID()) && 
//						msg.getMessage().isIncoming() &&
//						!msg.isAcked()) {
//					mMessagesList.get(i).setAck(true);
//					return true;
//				}
//			}
//			
//			return false;
//		}
//		
//		public void setMessageDeliveredByUUID(UUID pUUID) {
//			for(int i = 0; i<mMessagesList.size(); i++) {
//				Message msg = mMessagesList.get(i);
//
//				if(!msg.getMessage().isIncoming() && 
//						pUUID.equals(msg.getMessage().getUUID()) &&
//						!msg.isMsgDelivered) {
//					mMessagesList.get(i).setDelivered(true);
//					return;
//				}
//			}
//		}
//
//		public void markMsgAsReceived(final IKandyEvent pSelection) {
//			
//			Kandy.getServices().getChatService().markAsReceived(pSelection, new KandyResponseListener() {
//				@Override
//				public void onRequestFailed(int responseCode, String err) {
//				}
//
//				@Override
//				public void onRequestSucceded() {
//				}
//			});
//		}
//
//		public void handleIncomingMessageOnUIThread(final IKandyMessage message) {
//			getActivity().runOnUiThread(new Runnable() {
//
//				public void run() {
//					handleIncomingMessage(message);
//				}
//			});
//		}
//		
//		private void handleIncomingMessage(IKandyMessage message) {
//			for(Message msg : mMessagesList) {
//				if(msg.getMessage().isIncoming() && msg.getMessage().getUUID().equals(message.getUUID()))
//					return;
//			}
//
//			mMessagesList.add(new Message(message));
//			mAdapter.notifyDataSetChanged();
//		}
//		
//		private void handleDeliveredgMessage(IKandyEvent pEvent) {
//			if(pEvent == null || pEvent.getUUID() == null) return;
//			setMessageDeliveredByUUID(pEvent.getUUID());
//			mAdapter.notifyDataSetChanged();
//		}
//
//		public void handleDeliveredgMessageOnUIThread(final IKandyEvent pEvent) {
//			getActivity().runOnUiThread(new Runnable() {
//
//				public void run() {
//					handleDeliveredgMessage(pEvent);
//				}
//			});
//		}
//	}
//	
//	private KrollDict callbacks = null;
//	
//	private ChatWidget chatWidget;
//	private KandyChatServiceNotificationListener _kandyChatServiceNotificationListener = null;
//
//	public ChatServiceProxy() {
//		super();
//	}
//
//	/**
//	 * {@inheritDoc}
//	 */
//	@Override
//	public TiUIView createView(Activity activity) {
//		chatWidget = new ChatWidget(this);
//		return chatWidget;
//	}
//	
//	/**
//	 * Set callbacks for chat widget
//	 * 
//	 * @param callbacks
//	 */
//	@Kroll.setProperty
//	@Kroll.method
//	public void setCallbacks(KrollDict callbacks){
//		this.callbacks = callbacks;
//	}
//	
//	/**
//	 * Send message
//	 * 
//	 * @param recipient
//	 * @param message
//	 */
//	public void send(String recipient, String message){
//		KrollDict args = Utils.getKrollDictFromCallbacks(callbacks, "send");
//		
//		args.put("recipient", recipient);
//		args.put("message", message);
//		
//		send(args);
//	}
//	
//	/**
//	 * Send message
//	 * 
//	 * @param args
//	 */
//	@Kroll.method
//	public void send(KrollDict args) {
//		final KrollFunction success = (KrollFunction) args.get("success");
//		final KrollFunction error = (KrollFunction) args.get("error");
//
//		String user = (String) args.get("recipient");
//		String text = (String) args.get("message");
//
//		KandyRecord recipient;
//		try {
//			recipient = new KandyRecord(user);
//		} catch (IllegalArgumentException ex) {
//			Utils.sendFailResult(getKrollObject(), error,
//					"kandy_chat_phone_number_verification_text");
//			return;
//		}
//
//		final KandyMessage message = new KandyMessage(recipient, text);
//
//		Kandy.getServices().getChatService()
//				.send(message, new KandyResponseListener() {
//
//					@Override
//					public void onRequestFailed(int code, String err) {
//						Utils.sendFailResult(getKrollObject(), error, code, err);
//					}
//
//					@Override
//					public void onRequestSucceded() {
//						chatWidget.handleIncomingMessageOnUIThread(message);
//						Utils.sendSuccessResult(getKrollObject(), success);
//					}
//				});
//
//	}
//
//	/**
//	 * Send message received signal
//	 * 
//	 * @param args
//	 */
//	@Kroll.method
//	public void markAsReceived(KrollDict args) {
//		final KrollFunction success = (KrollFunction) args.get("success");
//		final KrollFunction error = (KrollFunction) args.get("error");
//
//		String[] list = (String[]) args.get("list");
//		ArrayList<IKandyEvent> events = new ArrayList<IKandyEvent>();
//		for (String i : list) {
//			KandyMessage event = new KandyMessage(new KandyRecord(
//					"dummy@domain.com"), "dummy");
//			event.setUUID(UUID.fromString(i));
//			events.add(event);
//		}
//
//		Kandy.getServices().getChatService()
//				.markAsReceived(events, new KandyResponseListener() {
//
//					@Override
//					public void onRequestFailed(int code, String err) {
//						Utils.sendFailResult(getKrollObject(), error, code, err);
//					}
//
//					@Override
//					public void onRequestSucceded() {
//						Utils.sendSuccessResult(getKrollObject(), success);
//					}
//				});
//	}
//
//	/**
//	 * Pull pending envents
//	 */
//	@Kroll.method
//	public void pullEvents() {
//		Kandy.getServices().getChatService().pullEvents();
//	}
//
//	/**
//	 * Pull pending events with callback
//	 * 
//	 * @param args
//	 */
//	@Kroll.method
//	public void pullEvents(KrollDict args) {
//		final KrollFunction success = (KrollFunction) args.get("success");
//		final KrollFunction error = (KrollFunction) args.get("error");
//
//		Kandy.getServices().getChatService()
//				.pullEvents(new KandyResponseListener() {
//
//					@Override
//					public void onRequestFailed(int code, String err) {
//						Utils.sendFailResult(getKrollObject(), error, code, err);
//					}
//
//					@Override
//					public void onRequestSucceded() {
//						Utils.sendSuccessResult(getKrollObject(), success);
//					}
//				});
//	}
//	
//	public class Message {
//		private IKandyMessage mmMesage;
//		private boolean isMsgDelivered;
//		private boolean isMsgAcked;
//
//		public Message(IKandyMessage pMsg) {
//			mmMesage = pMsg;
//			isMsgAcked = false;
//			isMsgDelivered = false;
//		}
//
//		public synchronized void setDelivered(boolean isDelivered) {
//			isMsgDelivered = isDelivered;
//		}
//
//		public synchronized void setAck(boolean isAcked) {
//			isMsgAcked = isAcked;
//		}
//
//		public synchronized boolean isMessageDelviered() {
//			return isMsgDelivered;
//		}
//
//		public synchronized boolean isAcked() {
//			return isMsgAcked;
//		}
//
//		public IKandyMessage getMessage() {
//			return mmMesage;
//		}
//	}
//}
