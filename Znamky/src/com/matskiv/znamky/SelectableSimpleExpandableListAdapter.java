package com.matskiv.znamky;

import java.util.List;
import java.util.Map;

import com.mastkiv.znamky.R;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleExpandableListAdapter;

public class SelectableSimpleExpandableListAdapter extends SimpleExpandableListAdapter {

	private boolean selectable = true;
	private Integer currentGroup = null;
	private Integer current = null;
	
	public SelectableSimpleExpandableListAdapter(Context context,
			List<? extends Map<String, ?>> groupData, int groupLayout,
			String[] groupFrom, int[] groupTo,
			List<? extends List<? extends Map<String, ?>>> childData,
			int childLayout, String[] childFrom, int[] childTo) {
		super(context, groupData, groupLayout, groupFrom, groupTo, childData,
				childLayout, childFrom, childTo);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
		View v = super.getChildView(groupPosition, childPosition, isLastChild, convertView, parent);
		if (selectable) {
			if (currentGroup != null && current != null && currentGroup == groupPosition && current == childPosition) {
				v.setBackgroundResource(R.color.list_item_bg_selected);
			} else {
				v.setBackgroundDrawable(null);
			}
		}
		return v;
	}
	
	public void setSelected(Integer selectedGroup, Integer selected) {
		if (!(currentGroup == selectedGroup && current == selected)) {
			currentGroup = selectedGroup;
			current = selected;
			notifyDataSetChanged();
		}
	}
	
	public void setSelectable(boolean selectable) {
		this.selectable = selectable;
	}
	
}