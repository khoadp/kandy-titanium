package io.kandy.proxy.adapters;

import io.kandy.proxy.CallServiceProxy;
import io.kandy.utils.KandyUtils;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class CalleeAdapter extends ArrayAdapter<String> {

	public CalleeAdapter(Context context, int resource, List<String> objects) {
		super(context, resource, objects);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;

		if (convertView == null) {
			LayoutInflater inflater = LayoutInflater.from(getContext());
			convertView = inflater.inflate(KandyUtils.getLayout("call_listview_item"), parent, false);
			viewHolder = new ViewHolder();

			viewHolder.text1 = (TextView) convertView.findViewById(KandyUtils.getId("line_one"));
			viewHolder.text2 = (TextView) convertView.findViewById(KandyUtils.getId("line_three"));

			convertView.setTag(viewHolder);

		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		String callee = getItem(position);

		viewHolder.text1.setText(callee);
		viewHolder.text2.setText(CallServiceProxy.getKandyCall(callee).getCallState().name().toLowerCase());

		return convertView;
	}

	static class ViewHolder {
		TextView text1;
		TextView text2;
	}

}
