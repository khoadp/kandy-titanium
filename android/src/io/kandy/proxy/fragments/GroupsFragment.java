package io.kandy.proxy.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import com.genband.kandy.api.Kandy;
import com.genband.kandy.api.services.groups.*;
import io.kandy.proxy.adapters.GroupAdapter;
import io.kandy.utils.KandyUtils;
import io.kandy.utils.UIUtils;

import java.util.ArrayList;
import java.util.List;

public class GroupsFragment extends Fragment implements OnItemClickListener, KandyGroupServiceNotificationListener {

	private static final String LCAT = GroupsFragment.class.getSimpleName();

	private List<KandyGroup> mGroups;
	private GroupAdapter mGroupAdapter;
	private EditText uiGroupNameEdit;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getListOfGroupsOnUiThread();
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
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(KandyUtils.getLayout("kandy_groups_fragment"), container, false);

		mGroups = new ArrayList<KandyGroup>();
		mGroupAdapter = new GroupAdapter(getActivity(), KandyUtils.getLayout("group_list_item"), mGroups);
		uiGroupNameEdit = (EditText) view.findViewById(KandyUtils.getId("ui_activity_groups_list_group_name_edit"));

		((ListView) view.findViewById(KandyUtils.getId("activity_groups_list"))).setAdapter(mGroupAdapter);
		((ListView) view.findViewById(KandyUtils.getId("activity_groups_list"))).setOnItemClickListener(this);

		((Button) view.findViewById(KandyUtils.getId("ui_activity_groups_list_group_create_btn")))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						createGroup();
					}

				});

		return view;
	}

	private void createGroup() {
		UIUtils.showProgressDialogWithMessage(getActivity(),
				KandyUtils.getString("groups_list_activity_create_group_msg"));
		KandyGroupParams params = new KandyGroupParams();
		String name = uiGroupNameEdit.getText().toString();
		params.setGroupName(name);

		Kandy.getServices().getGroupService().createGroup(params, new KandyGroupResponseListener() {

			@Override
			public void onRequestFailed(int responseCode, String error) {
				UIUtils.handleResultOnUiThread(getActivity(), true, error);
			}

			@Override
			public void onRequestSucceded(KandyGroup kandyGroup) {
				mGroups.add(kandyGroup);
				updateUI();
				UIUtils.handleResultOnUiThread(getActivity(), false,
						KandyUtils.getString("groups_list_activity_group_created"));
			}
		});
	}

	private void getListOfGroupsOnUiThread() {
		getActivity().runOnUiThread(new Runnable() {

			@Override
			public void run() {
				getListOfGroups();
			}
		});
	}

	private void getListOfGroups() {

		UIUtils.showProgressDialogWithMessage(getActivity(),
				KandyUtils.getString("groups_list_activity_get_groups_msg"));
		Kandy.getServices().getGroupService().getMyGroups(new KandyGroupsResponseListener() {

			@Override
			public void onRequestFailed(int responseCode, String error) {
				UIUtils.handleResultOnUiThread(getActivity(), true, error);
			}

			@Override
			public void onRequestSucceded(List<KandyGroup> groupList) {
				mGroups.clear();
				mGroups.addAll(groupList);
				updateUI();
				UIUtils.dismissProgressDialog();
			}
		});
	}

	private void updateUI() {
		getActivity().runOnUiThread(new Runnable() {

			@Override
			public void run() {
				mGroupAdapter.notifyDataSetChanged();
			}
		});
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		KandyGroup group = (KandyGroup) mGroupAdapter.getItem(position);
		String groupId = group.getGroupId().getUri();
		// Intent intent = new Intent(KandyGroupsActivity.this ,
		// GroupDetailsActivity.class);
		// intent.putExtra(GroupDetailsActivity.GROUP_ID, groupId);
		// startActivity(intent);

	}

	@Override
	public void onGroupDestroyed(IKandyGroupDestroyed message) {
		getListOfGroupsOnUiThread();
	}

	@Override
	public void onGroupUpdated(IKandyGroupUpdated message) {
		getListOfGroupsOnUiThread();
	}

	@Override
	public void onParticipantJoined(IKandyGroupParticipantJoined message) {
		getListOfGroupsOnUiThread();
	}

	@Override
	public void onParticipantKicked(IKandyGroupParticipantKicked message) {
		getListOfGroupsOnUiThread();
	}

	@Override
	public void onParticipantLeft(IKandyGroupParticipantLeft message) {
		getListOfGroupsOnUiThread();
	}
}
