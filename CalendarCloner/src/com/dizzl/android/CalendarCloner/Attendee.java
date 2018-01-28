package com.dizzl.android.CalendarCloner;

public interface Attendee {
	public long getId();
	public String getName();
	public String getEmail();
	public int getRelationship();
	public int getType();
	public int getStatus();
}
