package com.matskiv.znamky;

import java.util.HashMap;

import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.mastkiv.znamky.R;

public class MarkDetailActivity extends SherlockFragmentActivity {
	
	public static final String EXTRA = "com.matskiv.znamky.Mark";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mark_detail_act);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		/*
		 * Work around because direct cast from getSerializableExtra() to Mark throws exception
		 */
		@SuppressWarnings("unchecked")
		HashMap<String, Object> hm = (HashMap<String, Object>) getIntent().getSerializableExtra(EXTRA);
		Mark mark = new Mark(hm); 
		
		MarkDetailFragment markDetailFragment = (MarkDetailFragment) getSupportFragmentManager().findFragmentById(R.id.mark_detail_fragment);
		markDetailFragment.setMark(mark);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				this.onBackPressed();
				break;
		}
		return super.onOptionsItemSelected(item); 
	}
	
}