package com.matskiv.znamky;

import java.util.HashMap;

public class Subject extends HashMap<String, String> implements Comparable<Subject> {
	
	private static final long serialVersionUID = -6192410391729815989L;
	public static final String NAME = "subjname";
	public static final String TOTALWEIGHT = "totwei"; // sum of weights
	public static final String TOTALWEIGHTED = "totweited"; // sum of weighted
	public static final String AVG = "avg";
	
	public Subject() {
		super();
		put(Subject.TOTALWEIGHT, "0");
		put(Subject.TOTALWEIGHTED, "0");
	}
	
	public Subject(String name) {
		this();
		put(Subject.NAME, name);
	}
	
	public void increaseTotalW(int weight, float weighted) {
		int totalWeight = Integer.parseInt(this.get(Subject.TOTALWEIGHT)) + weight;
		float totalWeighted = Float.parseFloat(this.get(Subject.TOTALWEIGHTED)) + weighted;
		float avg = totalWeighted / ((float) totalWeight);
				
		this.put(Subject.TOTALWEIGHT, totalWeight+"");
		this.put(Subject.TOTALWEIGHTED, totalWeighted+"");
		this.put(Subject.AVG, avg+"");
		
	}
	
	@Override
	public int compareTo(Subject another) {
		return (this.get(Subject.NAME).toString().compareTo(another.get(Subject.NAME).toString()));
	}
	
}