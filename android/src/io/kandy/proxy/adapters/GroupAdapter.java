package io.kandy.proxy.adapters;

import io.kandy.utils.KandyUtils;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.genband.kandy.api.Kandy;
import com.genband.kandy.api.services.groups.KandyGroup;
import com.genband.kandy.api.services.groups.KandyGroupParticipant;

public class GroupAdapter extends ArrayAdapter<KandyGroup> {

	public GroupAdapter(Context context, int resource, List<KandyGroup> items) {
		super(context, resource, items);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ViewHolder viewHolder;

		if (convertView == null) {
			LayoutInflater inflater = LayoutInflater.from(getContext());
			convertView = inflater.inflate(KandyUtils.getLayout("group_list_item"), parent, false);
			viewHolder = new ViewHolder();
			viewHolder.uiGroupNameText = (TextView) convertView.findViewById(KandyUtils.getId("ui_group_list_name"));
			viewHolder.uiGroupParticipantsText = (TextView) convertView.findViewById(KandyUtils
					.getId("ui_group_list_participants_lbl"));
			viewHolder.uiGroupParticipantsNumber = (TextView) convertView.findViewById(KandyUtils
					.getId("ui_group_list_participants_number"));
			viewHolder.uiGroupCreatedAt = (TextView) convertView.findViewById(KandyUtils
					.getId("ui_group_list_created_at"));
			viewHolder.uiGroupMuted = (TextView) convertView.findViewById(KandyUtils.getId("ui_group_list_muted"));
			viewHolder.uiGroupPermissions = (TextView) convertView.findViewById(KandyUtils
					.getId("ui_group_list_permissions"));

			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		KandyGroup kandyGroup = getItem(position);

		if (kandyGroup != null) {
			viewHolder.uiGroupNameText.setText(kandyGroup.getGroupName());

			int number = kandyGroup.getGroupParticipants().size();
			viewHolder.uiGroupParticipantsNumber.setText(String.valueOf(number));

			viewHolder.uiGroupCreatedAt.setText(kandyGroup.getCreationDate().toString());

			viewHolder.uiGroupMuted.setText(Boolean.toString(kandyGroup.isGroupMuted()));

			String permissions = "read only";
			String me = Kandy.getSession().getKandyUser().getUserId();
			List<KandyGroupParticipant> participants = kandyGroup.getGroupParticipants();
			for (KandyGroupParticipant participant : participants) {
				if (participant.getParticipant().getUri().equals(me) && participant.isAdmin()) {
					permissions = "can edit";
				}
			}
			viewHolder.uiGroupPermissions.setText(permissions);
		}

		return convertView;
	}

	static class ViewHolder {
		TextView uiGroupNameText;
		TextView uiGroupParticipantsText;
		TextView uiGroupParticipantsNumber;
		TextView uiGroupCreatedAt;
		TextView uiGroupMuted;
		TextView uiGroupPermissions;
	}
}
