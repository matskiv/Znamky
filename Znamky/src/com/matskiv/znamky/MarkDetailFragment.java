package com.matskiv.znamky;

import com.actionbarsherlock.app.SherlockFragment;
import com.mastkiv.znamky.R;

import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class MarkDetailFragment extends SherlockFragment {
	
	private static final String[] markKeys = {SQLiteHelper.COLUMN_DATE, SQLiteHelper.COLUMN_SUBJECT,
		SQLiteHelper.COLUMN_VALUE, SQLiteHelper.COLUMN_EXAMTYPE, SQLiteHelper.COLUMN_TEACHER, SQLiteHelper.COLUMN_NOTE};
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.mark_detail, container);
	}

	public void setMark(Mark mark) {
		TableLayout tab = (TableLayout) getView().findViewById(R.id.detail_table);
		for (int i = 0; i < tab.getChildCount(); i++) {
			TableRow row = (TableRow) tab.getChildAt(i);
			TextView tv = (TextView) row.getChildAt(1);
			tv.setText(Html.fromHtml(mark.get(MarkDetailFragment.markKeys[i]).toString()));
		}
	}
	
}