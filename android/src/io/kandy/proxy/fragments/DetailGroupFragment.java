package io.kandy.proxy.fragments;

import android.app.*;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView.OnEditorActionListener;
import com.genband.kandy.api.Kandy;
import com.genband.kandy.api.services.calls.KandyRecord;
import com.genband.kandy.api.services.chats.IKandyImageItem;
import com.genband.kandy.api.services.chats.IKandyTransferProgress;
import com.genband.kandy.api.services.chats.KandyThumbnailSize;
import com.genband.kandy.api.services.common.KandyGroupUploadProgressListener;
import com.genband.kandy.api.services.common.KandyResponseListener;
import com.genband.kandy.api.services.common.KandyResponseProgressListener;
import com.genband.kandy.api.services.groups.*;
import com.genband.kandy.api.utils.KandyIllegalArgumentException;
import io.kandy.proxy.adapters.GroupParticipantAdapter;
import io.kandy.proxy.views.GroupViewProxy;
import io.kandy.utils.KandyUtils;
import io.kandy.utils.UIUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class DetailGroupFragment extends Fragment implements KandyGroupServiceNotificationListener {

	public static final String GROUP_ID = "group_id";
	private static final int PICK_IMAGE = 10000;
	private List<KandyGroupParticipant> mParticipants;
	private GroupParticipantAdapter mAdapter;

	private KandyGroup mKandyGroup;

	private String mGroupId = null;
	private SimpleDateFormat mDateFormat;

	private EditText uiGroupNameEdit;
	private EditText uiGroupParticipantEdit;
	private TextView uiGroupCreatedAt;
	private TextView uiGroupMuted;
	private ImageView uiThumbnailImage;

	private GroupViewProxy groupViewProxy;

	public DetailGroupFragment(GroupViewProxy groupViewProxy, String groupId) {
		this.groupViewProxy = groupViewProxy;
		this.mGroupId = groupId;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (mGroupId != null) {
			getSelectedGroup();
		}

		mDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	}

	@Override
	public void onResume() {
		super.onResume();
		Kandy.getServices().getGroupService().registerNotificationListener(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		Kandy.getServices().getGroupService().unregisterNotificationListener(this);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {

		case PICK_IMAGE:
			if (resultCode == Activity.RESULT_OK) {
				Uri uri = data.getData();

				if (uri != null) {
					uploadSelectedImage(uri);
				}
			}
			break;
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		getActivity().getMenuInflater().inflate(KandyUtils.getResource("group_settings", "menu"), menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == KandyUtils.getId("action_settings_leave")) {
			leaveGroup();
			return true;
		} else if (id == KandyUtils.getId("action_settings_destroy")) {
			destroyGroup();
			return true;
		} else if (id == KandyUtils.getId("action_settings_mute")) {
			muteGroup(getKandyGroupInstance().isGroupMuted());
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(KandyUtils.getLayout("kandy_group_detail_fragment"), container, false);

		uiGroupCreatedAt = (TextView) view
				.findViewById(KandyUtils.getId("ui_activity_group_settings_group_created_at"));
		uiGroupMuted = (TextView) view.findViewById(KandyUtils.getId("ui_activity_group_settings_group_muted"));
		uiGroupNameEdit = (EditText) view.findViewById(KandyUtils.getId("ui_activity_group_settings_group_name_edit"));
		uiGroupParticipantEdit = (EditText) view.findViewById(KandyUtils
				.getId("ui_activity_group_settings_add_participant_edit"));
		uiThumbnailImage = (ImageView) view.findViewById(KandyUtils.getId("ui_group_thumbnail_img"));

		uiGroupNameEdit.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				boolean handled = false;
				if (actionId == EditorInfo.IME_ACTION_SEND) {
					updateGroupName(uiGroupNameEdit.getText().toString());
					handled = true;
				}
				return handled;
			}
		});

		uiThumbnailImage.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				createImageActionsDialog().show();
			}
		});

		if (mParticipants == null) {
			mParticipants = new ArrayList<KandyGroupParticipant>();
			mAdapter = new GroupParticipantAdapter(getActivity(), KandyUtils.getLayout("participant_list_item"),
					mParticipants);
		}

		((Button) view.findViewById(KandyUtils.getId("ui_activity_group_settings_chat_button")))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						FragmentManager fragmentManager = getFragmentManager();
						FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
						fragmentTransaction.hide(DetailGroupFragment.this);
						fragmentTransaction.add(KandyUtils.getId("kandy_group_fragment_container"),
								new DetailGroupFragment(groupViewProxy, getKandyGroupInstance().getGroupId().getUri()))
								.addToBackStack(null);
						fragmentTransaction.commit();
					}
				});

		((Button) view.findViewById(KandyUtils.getId("ui_activity_group_settings_add_participant_button")))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						String participant = uiGroupParticipantEdit.getText().toString();
						addParticipant(participant);
					}
				});

		((Button) view.findViewById(KandyUtils.getId("ui_activity_groups_back_button")))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						FragmentManager fragmentManager = getFragmentManager();
						FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
						fragmentTransaction.remove(DetailGroupFragment.this);
						fragmentTransaction.replace(KandyUtils.getId("kandy_group_fragment_container"),
								new GroupsFragment(groupViewProxy)).addToBackStack(null);
						fragmentTransaction.commit();
					}
				});

		ListView list = (ListView) view.findViewById(KandyUtils.getId("ui_activity_group_settings_participants_list"));
		list.setAdapter(mAdapter);
		list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				final KandyGroupParticipant participant = mAdapter.getItem(position);
				createParticipantActionsDialog(participant).show();
			}
		});

		return view;
	}

	private void pickImage() {

		Intent pickerIntent = new Intent(Intent.ACTION_PICK,
				android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		pickerIntent.setType("image/*");

		startActivityForResult(pickerIntent, PICK_IMAGE);
	}

	private void updateGroupName(String string) {
		UIUtils.showProgressDialogWithMessage(getActivity(),
				KandyUtils.getString("groups_settings_activity_action_updating_group_name_"));
		Kandy.getServices().getGroupService()
				.updateGroupName(getKandyGroupInstance().getGroupId(), string, new KandyGroupResponseListener() {

					@Override
					public void onRequestFailed(int responseCode, String err) {
						UIUtils.handleResultOnUiThread(getActivity(), true, err);
					}

					@Override
					public void onRequestSucceded(KandyGroup kandyGroup) {
						setKandyGroupInstance(kandyGroup);
						updateUI();
						UIUtils.handleResultOnUiThread(getActivity(), false,
								KandyUtils.getString("groups_settings_activity_action_group_name_changed"));
					}
				});
	}

	private void uploadSelectedImage(Uri uri) {
		UIUtils.showProgressDialogWithMessage(getActivity(),
				KandyUtils.getString("groups_settings_activity_action_updating_group_image_"));
		Kandy.getServices().getGroupService()
				.updateGroupImage(getKandyGroupInstance().getGroupId(), uri, new KandyGroupUploadProgressListener() {

					@Override
					public void onRequestFailed(int responseCode, String err) {
						UIUtils.handleResultOnUiThread(getActivity(), true, err);

					}

					@Override
					public void onRequestSucceded(KandyGroup kandyGroup) {
						setKandyGroupInstance(kandyGroup);
						updateImageUI();
					}

					@Override
					public void onProgressUpdate(IKandyTransferProgress progress) {
						Log.d("UPLOAD_IMAGE", "uploading image, progress: " + progress.getProgress());
					}
				});
	}

	private AlertDialog createImageActionsDialog() {

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(KandyUtils.getString("groups_settings_activity_action_image_action_msg"));

		builder.setItems(new String[] { "Remove Image", "Update Image" },
				new android.content.DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

						switch (which) {
						case 0:
							removeImage();
							break;

						case 1:
							pickImage();
							break;
						}

						dialog.dismiss();
					}
				});

		return builder.create();
	}

	private AlertDialog createParticipantActionsDialog(final KandyGroupParticipant participant) {

		final List<KandyRecord> participants = new ArrayList<KandyRecord>();
		participants.add(participant.getParticipant());

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(KandyUtils.getString("groups_settings_activity_action_participant_action_msg"));
		String muteState = (participant.isMuted()) ? "Unmute Participant" : "Mute Participant";

		builder.setItems(new String[] { "Remove participant", muteState },
				new android.content.DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

						switch (which) {
						case 0:
							removeParticipants(participants);
							break;

						case 1:
							muteParticipants(participant.isMuted(), participants);
							break;
						}

						dialog.dismiss();
						UIUtils.showProgressDialogWithMessage(getActivity(),
								KandyUtils.getString("groups_settings_activity_action_settings_applying_msg"));
					}
				});

		return builder.create();
	}

	private void removeImage() {
		Kandy.getServices().getGroupService()
				.removeGroupImage(getKandyGroupInstance().getGroupId(), new KandyGroupResponseListener() {

					@Override
					public void onRequestFailed(int responseCode, String err) {
						UIUtils.handleResultOnUiThread(getActivity(), true, err);
					}

					@Override
					public void onRequestSucceded(KandyGroup kandyGroup) {
						setKandyGroupInstance(kandyGroup);
						UIUtils.handleResultOnUiThread(getActivity(), false,
								KandyUtils.getString("groups_settings_activity_action_image_has_been_removed"));
						updateImageUI();
					}
				});
	}

	private void downloadImageThumbnail() {
		Kandy.getServices()
				.getGroupService()
				.downloadGroupImageThumbnail(getKandyGroupInstance().getGroupId(), KandyThumbnailSize.MEDIUM,
						new KandyResponseProgressListener() {

							@Override
							public void onRequestFailed(int responseCode, String err) {
								UIUtils.handleResultOnUiThread(getActivity(), true, err);
							}

							@Override
							public void onRequestSucceded(final Uri fileUri) {
								getActivity().runOnUiThread(new Runnable() {

									@Override
									public void run() {
										uiThumbnailImage.setImageURI(fileUri);
										UIUtils.handleResultOnUiThread(getActivity(), false, KandyUtils
												.getString("groups_settings_activity_action_image_has_been_changed"));
									}
								});
							}

							@Override
							public void onProgressUpdate(IKandyTransferProgress progress) {
								Log.d("DOWNLOAD_IMAGE", "download Image thumbnail, progress: " + progress.getProgress());
							}
						});
	}

	private void muteGroup(boolean groupMuted) {
		UIUtils.showProgressDialogWithMessage(getActivity(),
				KandyUtils.getString("groups_settings_activity_action_settings_applying_msg"));
		if (groupMuted) {
			Kandy.getServices().getGroupService()
					.unmuteGroup(getKandyGroupInstance().getGroupId(), new KandyGroupResponseListener() {

						@Override
						public void onRequestFailed(int responseCode, String err) {
							UIUtils.handleResultOnUiThread(getActivity(), true, err);
						}

						@Override
						public void onRequestSucceded(KandyGroup kandyGroup) {
							setKandyGroupInstance(kandyGroup);
							updateUI();
							UIUtils.handleResultOnUiThread(getActivity(), false,
									KandyUtils.getString("groups_settings_activity_action_group_unmuted"));
						}
					});
		} else {
			Kandy.getServices().getGroupService()
					.muteGroup(getKandyGroupInstance().getGroupId(), new KandyGroupResponseListener() {

						@Override
						public void onRequestFailed(int responseCode, String err) {
							UIUtils.handleResultOnUiThread(getActivity(), true, err);
						}

						@Override
						public void onRequestSucceded(KandyGroup kandyGroup) {
							setKandyGroupInstance(kandyGroup);
							updateUI();
							UIUtils.handleResultOnUiThread(getActivity(), false,
									KandyUtils.getString("groups_settings_activity_action_group_muted"));
						}
					});
		}
	}

	private void destroyGroup() {
		UIUtils.showProgressDialogWithMessage(getActivity(),
				KandyUtils.getString("groups_settings_activity_action_settings_applying_msg"));
		Kandy.getServices().getGroupService()
				.destroyGroup(getKandyGroupInstance().getGroupId(), new KandyResponseListener() {

					@Override
					public void onRequestFailed(int responseCode, String err) {
						UIUtils.handleResultOnUiThread(getActivity(), true, err);
					}

					@Override
					public void onRequestSucceded() {
						UIUtils.handleResultOnUiThread(getActivity(), false,
								KandyUtils.getString("groups_settings_activity_action_group_destroyed"));
						getActivity().finish();
					}
				});
	}

	private void leaveGroup() {
		UIUtils.showProgressDialogWithMessage(getActivity(),
				KandyUtils.getString("groups_settings_activity_action_settings_applying_msg"));
		Kandy.getServices().getGroupService()
				.leaveGroup(getKandyGroupInstance().getGroupId(), new KandyResponseListener() {

					@Override
					public void onRequestFailed(int responseCode, String err) {
						UIUtils.handleResultOnUiThread(getActivity(), true, err);
					}

					@Override
					public void onRequestSucceded() {
						UIUtils.handleResultOnUiThread(getActivity(), false,
								KandyUtils.getString("groups_settings_activity_action_group_left"));
					}
				});
	}

	private void removeParticipants(List<KandyRecord> participants) {
		Kandy.getServices()
				.getGroupService()
				.removeParticipants(getKandyGroupInstance().getGroupId(), participants,
						new KandyGroupResponseListener() {

							@Override
							public void onRequestFailed(int responseCode, String err) {
								UIUtils.handleResultOnUiThread(getActivity(), true, err);
							}

							@Override
							public void onRequestSucceded(KandyGroup kandyGroup) {
								setKandyGroupInstance(kandyGroup);
								updateUI();
								UIUtils.handleResultOnUiThread(getActivity(), false,
										KandyUtils.getString("groups_settings_activity_action_participant_removed"));
							}
						});
	}

	private void muteParticipants(boolean mute, List<KandyRecord> participants) {
		if (mute) {

			Kandy.getServices()
					.getGroupService()
					.unmuteParticipants(getKandyGroupInstance().getGroupId(), participants,
							new KandyGroupResponseListener() {

								@Override
								public void onRequestFailed(int responseCode, String err) {
									UIUtils.handleResultOnUiThread(getActivity(), true, err);
								}

								@Override
								public void onRequestSucceded(KandyGroup kandyGroup) {
									setKandyGroupInstance(kandyGroup);
									updateUI();
									UIUtils.handleResultOnUiThread(getActivity(), false,
											KandyUtils.getString("groups_settings_activity_action_participant_unmuted"));
								}
							});

		} else {
			Kandy.getServices()
					.getGroupService()
					.muteParticipants(getKandyGroupInstance().getGroupId(), participants,
							new KandyGroupResponseListener() {

								@Override
								public void onRequestFailed(int responseCode, String err) {
									UIUtils.handleResultOnUiThread(getActivity(), true, err);
								}

								@Override
								public void onRequestSucceded(KandyGroup kandyGroup) {
									setKandyGroupInstance(kandyGroup);
									updateUI();
									UIUtils.handleResultOnUiThread(getActivity(), false,
											KandyUtils.getString("groups_settings_activity_action_participant_muted"));
								}
							});
		}
	}

	private void addParticipant(String participant) {

		List<KandyRecord> newParticipants = new ArrayList<KandyRecord>();

		try {
			KandyRecord newParticipant = new KandyRecord(participant);
			newParticipants.add(newParticipant);
		} catch (KandyIllegalArgumentException e) {
			UIUtils.showDialogWithErrorMessage(getActivity(), e.getMessage());
			return;
		}

		Kandy.getServices()
				.getGroupService()
				.addParticipants(getKandyGroupInstance().getGroupId(), newParticipants,
						new KandyGroupResponseListener() {

							@Override
							public void onRequestFailed(int responseCode, String err) {
								UIUtils.handleResultOnUiThread(getActivity(), true, err);
							}

							@Override
							public void onRequestSucceded(KandyGroup kandyGroup) {
								setKandyGroupInstance(kandyGroup);
								updateUI();
								UIUtils.handleResultOnUiThread(getActivity(), false,
										KandyUtils.getString("groups_list_activity_participant_added_msg"));
							}
						});

	}

	private synchronized void setKandyGroupInstance(KandyGroup kandyGroup) {
		mKandyGroup = kandyGroup;
	}

	private synchronized KandyGroup getKandyGroupInstance() {
		return mKandyGroup;
	}

	private void getSelectedGroup() {
		getActivity().runOnUiThread(new Runnable() {

			@Override
			public void run() {
				UIUtils.showProgressDialogWithMessage(getActivity(),
						KandyUtils.getString("groups_settings_activity_action_getting_group_data"));
				try {
					Kandy.getServices().getGroupService()
							.getGroupById(new KandyRecord(mGroupId), new KandyGroupResponseListener() {

								@Override
								public void onRequestFailed(int responseCode, String error) {
									UIUtils.handleResultOnUiThread(getActivity(), true, error);
								}

								@Override
								public void onRequestSucceded(KandyGroup kandyGroup) {
									setKandyGroupInstance(kandyGroup);
									updateUI();
									updateImageUI();
								}
							});
				} catch (KandyIllegalArgumentException e) {
					UIUtils.showDialogWithErrorMessage(getActivity(), e.getMessage());
				}

			}
		});
	}

	private void updateUI() {
		getActivity().runOnUiThread(new Runnable() {

			@Override
			public void run() {
				String dateString = mDateFormat.format(getKandyGroupInstance().getCreationDate());
				uiGroupCreatedAt.setText("created at: " + dateString);
				uiGroupMuted.setText("is muted: " + Boolean.toString(getKandyGroupInstance().isGroupMuted()));
				uiGroupNameEdit.setText(getKandyGroupInstance().getGroupName());

				updateImageUI();
				mParticipants.clear();
				mParticipants.addAll(getKandyGroupInstance().getGroupParticipants());
				mAdapter.notifyDataSetChanged();
			}
		});
	}

	private void updateImageUI() {
		getActivity().runOnUiThread(new Runnable() {

			@Override
			public void run() {
				UIUtils.dismissProgressDialog();
				IKandyImageItem image = getKandyGroupInstance().getImage();
				if (image == null) {
					uiThumbnailImage.setImageResource(KandyUtils.getDrawable("icon_photo"));
				} else {
					if (image.getLocalThumbnailUri() == null) {
						downloadImageThumbnail();
					} else {
						uiThumbnailImage.setImageURI(image.getLocalThumbnailUri());
					}
				}
			}
		});
	}

	@Override
	public void onGroupDestroyed(IKandyGroupDestroyed message) {
		((KandyGroupServiceNotificationListener) groupViewProxy).onGroupDestroyed(message);
		if (message.getGroupId().getUri().equals(getKandyGroupInstance().getGroupId().getUri())) {
			UIUtils.handleResultOnUiThread(getActivity(), false,
					KandyUtils.getString("groups_settings_activity_action_group_destroyed"));
			getActivity().finish();
		}
	}

	@Override
	public void onGroupUpdated(IKandyGroupUpdated message) {
		((KandyGroupServiceNotificationListener) groupViewProxy).onGroupUpdated(message);
		if (message.getGroupId().getUri().equals(getKandyGroupInstance().getGroupId().getUri())) {
			boolean isNameUpdated = message.getGroupParams().getGroupName() != null;
			if (isNameUpdated) {
				getKandyGroupInstance().setGroupName(message.getGroupParams().getGroupName());
				updateUI();
				UIUtils.handleResultOnUiThread(getActivity(), false,
						KandyUtils.getString("groups_settings_activity_action_group_name_changed"));
			}

			boolean isImageUpdated = message.getGroupParams().getGroupImage() != null;
			if (isImageUpdated) {
				// is image updated?
				getKandyGroupInstance().setGroupImage(message.getGroupParams().getGroupImage());
				updateImageUI();
			} else {
				getKandyGroupInstance().setGroupImage(message.getGroupParams().getGroupImage());
				boolean isImageRemoved = KandyGroupParams.REMOVE_IMAGE.equals(message.getGroupParams().getImageUri());
				// is image removed?
				if (isImageRemoved) {
					getActivity().runOnUiThread(new Runnable() {

						@Override
						public void run() {
							uiThumbnailImage.setImageResource(KandyUtils.getDrawable("icon_photo"));
						}
					});
				}
			}
		}
	}

	@Override
	public void onParticipantJoined(IKandyGroupParticipantJoined message) {
		((KandyGroupServiceNotificationListener) groupViewProxy).onParticipantJoined(message);
		if (message.getGroupId().getUri().equals(getKandyGroupInstance().getGroupId().getUri())) {
			getSelectedGroup();
		}
	}

	@Override
	public void onParticipantKicked(IKandyGroupParticipantKicked message) {
		((KandyGroupServiceNotificationListener) groupViewProxy).onParticipantKicked(message);
		if (message.getGroupId().getUri().equals(getKandyGroupInstance().getGroupId().getUri())) {
			List<KandyRecord> booted = message.getBooted();
			boolean isMeKicked = false;
			for (KandyRecord record : booted) {
				String me = Kandy.getSession().getKandyUser().getUserId();
				if (record.getUri().equals(me)) {
					UIUtils.handleResultOnUiThread(getActivity(), false,
							KandyUtils.getString("groups_settings_activity_action_group_kicked"));
					getActivity().finish();
					isMeKicked = true;
				}
			}

			if (!isMeKicked) {
				getSelectedGroup();
			}
		}

	}

	@Override
	public void onParticipantLeft(IKandyGroupParticipantLeft message) {
		((KandyGroupServiceNotificationListener) groupViewProxy).onParticipantLeft(message);
		if (message.getGroupId().getUri().equals(getKandyGroupInstance().getGroupId().getUri())) {
			getSelectedGroup();
		}

	}
}
