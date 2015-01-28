package com.matskiv.znamky;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;

import com.actionbarsherlock.app.SherlockFragment;

public class SubjectListFragment extends SherlockFragment {
	
	private ExpandableListView listView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		listView = new ExpandableListView(inflater.getContext());
		return listView;
	}
	
	public void setAdapter(ExpandableListAdapter la) {
		listView.setAdapter(la);
	}
	
	public void setOnChildClickListener (ExpandableListView.OnChildClickListener occl) {
		listView.setOnChildClickListener(occl);
	}

	public ExpandableListView getListView() {
		return listView;
	}
}