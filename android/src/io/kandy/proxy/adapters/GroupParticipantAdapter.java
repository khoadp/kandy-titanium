package io.kandy.proxy.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.genband.kandy.api.services.groups.KandyGroupParticipant;
import io.kandy.utils.KandyUtils;

import java.util.List;

public class GroupParticipantAdapter extends ArrayAdapter<KandyGroupParticipant> {

	public GroupParticipantAdapter(Context context, int resource, List<KandyGroupParticipant> items) {
		super(context, resource, items);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ViewHolder viewHolder;

		if (convertView == null) {
			LayoutInflater inflater = LayoutInflater.from(getContext());
			convertView = inflater.inflate(KandyUtils.getLayout("participant_list_item"), parent, false);
			viewHolder = new ViewHolder();
			viewHolder.uiParticipantNameText = (TextView) convertView.findViewById(KandyUtils
					.getId("ui_participant_name_text"));
			viewHolder.uiParticipantAdminText = (TextView) convertView.findViewById(KandyUtils
					.getId("ui_participant_admin_text"));
			viewHolder.uiParticipantMutedText = (TextView) convertView.findViewById(KandyUtils
					.getId("ui_participant_muted_text"));

			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		KandyGroupParticipant participant = getItem(position);

		if (participant != null) {

			viewHolder.uiParticipantNameText.setText(participant.getParticipant().getUri());
			String isMuted = String.valueOf(participant.isMuted());
			String isAdmin = String.valueOf(participant.isAdmin());
			viewHolder.uiParticipantAdminText.setText(isAdmin);
			viewHolder.uiParticipantMutedText.setText(isMuted);

		}

		return convertView;
	}

	static class ViewHolder {
		TextView uiParticipantNameText;
		TextView uiParticipantAdminText;
		TextView uiParticipantMutedText;
	}
}
