package com.matskiv.znamky;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.mastkiv.znamky.R;

import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.AdapterView.OnItemClickListener;
 
public class MainPagerAdapter extends PagerAdapter {
	
	private static String[] titles;
	
	private Context cxt;
	private List<Mark> marks;
	private List<Subject> groupData;
	private List<List<Mark>> childData;
	
	private View viewA;
	private MarkListFragment markList;
	private MarkDetailFragment markDetailA;
	private SelectableSimpleAdapter markListAdapter;
	
	private View viewB;
	private SubjectListFragment subjectList;
	private MarkDetailFragment markDetailB;
	private SelectableSimpleExpandableListAdapter subjectListAdapter;

	public MainPagerAdapter(Context cxt, FragmentManager fragmentManager) {
		this.cxt = cxt;
		titles = cxt.getResources().getStringArray(R.array.main_titles);
		
		marks = new ArrayList<Mark>();
		groupData = new ArrayList<Subject>();
		childData = new ArrayList<List<Mark>>();
		
		LayoutInflater inflater = (LayoutInflater) cxt.getSystemService(Context.LAYOUT_INFLATER_SERVICE );
		
		initializeA(fragmentManager, inflater);
		initializeB(fragmentManager, inflater);
	}
	
	private void initializeA(FragmentManager fragmentManager, LayoutInflater inflater) {
		viewA = inflater.inflate(R.layout.main_a, null);
		markList = (MarkListFragment) fragmentManager.findFragmentById(R.id.mark_list_fragment);
		
		loadMarks();
		
		markDetailA = (MarkDetailFragment) fragmentManager.findFragmentById(R.id.mark_detail_fragment_a);
		
		OnItemClickListener onItemClickListener;
		if (markDetailA == null || !markDetailA.isInLayout()) {
			markListAdapter.setSelectable(false);
			onItemClickListener = new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					Intent intent = new Intent(MainPagerAdapter.this.cxt, MarkDetailActivity.class);
					Mark mark = marks.get(position);
					intent.putExtra(MarkDetailActivity.EXTRA, mark);
					MainPagerAdapter.this.cxt.startActivity(intent);
				}
			};
		} else {
			markListAdapter.setSelectable(true);
			onItemClickListener = new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					markListAdapter.setSelected(position);
					markDetailA.setMark(marks.get(position));
				}
			};
			selectFirst();
		}
		markList.setOnItemClickListener(onItemClickListener);
	}
	
	private void initializeB(FragmentManager fragmentManager, LayoutInflater inflater) {
		viewB = inflater.inflate(R.layout.main_b, null);
		subjectList = (SubjectListFragment) fragmentManager.findFragmentById(R.id.subject_list_fragment);

		loadSubjects();
		
		markDetailB = (MarkDetailFragment) fragmentManager.findFragmentById(R.id.mark_detail_fragment_b);
		
		OnChildClickListener onChildClickListener;
		if (markDetailB == null || !markDetailB.isInLayout()) {
			subjectListAdapter.setSelectable(false);
			onChildClickListener = new OnChildClickListener() {
				@Override
				public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
					Intent intent = new Intent(MainPagerAdapter.this.cxt, MarkDetailActivity.class);
					Mark mark = childData.get(groupPosition).get(childPosition);
					intent.putExtra(MarkDetailActivity.EXTRA, mark);
					MainPagerAdapter.this.cxt.startActivity(intent);
					return true;
				}
			};
		} else {
			subjectListAdapter.setSelectable(true);
			onChildClickListener = new OnChildClickListener() {
				@Override
				public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
					subjectListAdapter.setSelected(groupPosition, childPosition);
					markDetailB.setMark(childData.get(groupPosition).get(childPosition));
					return true;
				}
			};
		}
		subjectList.setOnChildClickListener(onChildClickListener);
	}
	
	public void refresh() {
		loadMarks();
		loadSubjects();
		selectFirst();
	}
	
	private void selectFirst() {
		if (markDetailA != null  && markDetailA.isInLayout() && marks.size() > 0) {
			markListAdapter.setSelected(0);
			markDetailA.setMark(marks.get(0));
		}
	}
	
	private void loadMarks() {
		marks.clear();

		SQLiteHelper dbb = new SQLiteHelper(cxt);
		dbb.open();
		marks = dbb.getAllMarks();
		dbb.close();
		
		/*
		 * new adapter because markListAdapter.notifyDataSetChanged() won't refresh view
		 */
		createMarkListAdapter();
	    markList.setAdapter(markListAdapter);
	}
	
	private void loadSubjects() {
		groupData = new ArrayList<Subject>();
		childData = new ArrayList<List<Mark>>();      
		
		for (int i = 0; i < marks.size(); i++) {
			Mark m = marks.get(i);
			String name = m.get(SQLiteHelper.COLUMN_SUBJECT).toString();
			int subjIndex = 0;
			Boolean contName = false;
			
			for(int k = 0; k < groupData.size(); k++) {
				if (groupData.get(k).get(Subject.NAME).equals(name)) {
					contName = true;
					subjIndex = k;
					break;
				}
			}
			
			if (!contName) {
				subjIndex = groupData.size();
				Subject s = new Subject(name);
				s.put("index", subjIndex+"");
				groupData.add(s);
				childData.add(new ArrayList<Mark>());
			}
			if (Mark.valueFlo((String) m.get(SQLiteHelper.COLUMN_VALUE)) > 0){
				groupData.get(subjIndex).increaseTotalW(Integer.parseInt(m.get(SQLiteHelper.COLUMN_WEIGHT).toString()), Float.parseFloat(m.get(SQLiteHelper.COLUMN_WEIGHTED).toString())); 
			}
			
			childData.get(subjIndex).add(m);
		}
		
		Collections.sort(groupData);
		
		ArrayList<List<Mark>> newChildData = new ArrayList<List<Mark>>(childData.size());
		for(int i = 0; i < groupData.size(); i++) {
			int oldIndex = Integer.parseInt(groupData.get(i).get("index"));
			if (groupData.get(i).containsKey(Subject.AVG)) {
				DecimalFormat df = new DecimalFormat("#.##");
				groupData.get(i).put(Subject.AVG, MainPagerAdapter.fixNumLen(df.format(Double.parseDouble(groupData.get(i).get(Subject.AVG)))));
			} else {
				groupData.get(i).put(Subject.AVG, "-");
			}
			newChildData.add(i, childData.get(oldIndex));
		}
		childData = newChildData;
		
		createSubjectListAdapter();
		subjectList.setAdapter(subjectListAdapter);
	}
	
	private void createMarkListAdapter() {
		markListAdapter =  new SelectableSimpleAdapter(	
			cxt,
			marks,
			R.layout.list_item,
			new String[]{Mark.DateFixed, Mark.SubjectFixed, Mark.ValueWeight, SQLiteHelper.COLUMN_NOTE},
			new int[]{R.id.date, R.id.subj, R.id.mark, R.id.note}
		);
	}
	
	private void createSubjectListAdapter() {
		subjectListAdapter = new SelectableSimpleExpandableListAdapter(	
			cxt,
			groupData,
			R.layout.list_group,
			new String[]{Subject.NAME, Subject.AVG},
			new int[]{R.id.subj, R.id.avg},
			childData,
			R.layout.list_child_item,
			new String[]{Mark.DateFixed, Mark.ValueWeight, SQLiteHelper.COLUMN_NOTE},
			new int[]{R.id.date, R.id.mark, R.id.note}
		);
	}
	
	private static String fixNumLen(String s) {
		if (s.length() == 1) s += ",";
		while (s.length() < 4) {
			s += "0";
		}
		return s;
	}
	
	@Override
	public Object instantiateItem( View pager, int position ) {
		switch (position) {
			case 0:
				((ViewPager) pager).addView(viewA);
				return viewA;
				
			case 1:
				((ViewPager) pager).addView(viewB);
				return viewB;
		}
		return null;
	}

	@Override
	public String getPageTitle(int position) {
		return titles[position];
	}
	
	@Override
	public int getCount() {
		return titles.length;
	}
	
	@Override
	public void destroyItem( View pager, int position, Object view ) {
		((ViewPager)pager).removeView( (View)view );
	}
	
	@Override
	public boolean isViewFromObject( View view, Object object ) { return view.equals(object); }
	
	@Override
	public void finishUpdate( View view ){}
	
	@Override
	public void restoreState( Parcelable p, ClassLoader c ){}
	
	@Override
	public Parcelable saveState() { return null; }
	
	@Override
	public void startUpdate( View view ){}
	
}