package com.matskiv.znamky;

import java.util.List;
import java.util.Map;

import com.mastkiv.znamky.R;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;

public class SelectableSimpleAdapter extends SimpleAdapter {
	
	private boolean selectable = true;
	private Integer current = null;
	
	public SelectableSimpleAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from,int[] to) {
		super(context, data, resource, from, to);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = super.getView(position, convertView, parent);
		if (selectable) {
			if (current != null && current == position) {
				v.setBackgroundResource(R.color.list_item_bg_selected);
			} else {
				v.setBackgroundDrawable(null);
			}
		}
		return v;
	}
	
	public void setSelected(Integer selected) {
		if (current != selected) {
			current = selected;
			notifyDataSetChanged();
		}
	}

	public void setSelectable(boolean selectable) {
		this.selectable = selectable;
	}

}