package com.matskiv.znamky;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockFragment;

public class MarkListFragment extends SherlockFragment {

	private ListView listView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		listView = new ListView(inflater.getContext());
		listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		return listView;
	}
	
	public void setAdapter(ListAdapter la) {
		listView.setAdapter(la);
	}
	
	public void setOnItemClickListener (OnItemClickListener oicl) {
		listView.setOnItemClickListener(oicl);
	}

	public ListView getListView() {
		return listView;
	}
	
}
