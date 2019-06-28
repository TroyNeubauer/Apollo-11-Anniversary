package com.troy.apollo11anni;

import static com.troy.apollo11anni.Constants.*;

import org.joda.time.*;

public class MissionEvent {
	private String name, comments;
	private DateTime time;

	public MissionEvent(String name, double gmtDate, double gmtTime, String comments) {
		this.name = name;

		double hours = gmtTime * 24.0 + 0.0000001;

		double minuites = (hours - ((int) hours));
		hours -= minuites;
		minuites *= 60.0;

		double seconds = (minuites - ((int) minuites));
		minuites -= seconds;
		seconds *= 60.0;

		this.time = Constants.EXCEL_DAY_ZERO.plusDays((int) gmtDate).toDateTime(new LocalTime((int) hours, (int) minuites, (int) seconds), DateTimeZone.UTC).withZone(DateTimeZone.getDefault());
		this.comments = comments;
		//System.out.println(this);
	}

	public String getName() {
		return name;
	}

	public DateTime getRealTime() {
		return time;
	}
	

	@Override
	public String toString() {
		return "MissionEvent [name=" + name + ", comments=" + comments + ", time=" + time + "]";
	}

	public DateTime getTime(DateTime launchTime) {
		Duration timeFromLaunch = new Duration(APOLLO_11_LAUNCH_TIME, time);
		return launchTime.plus(timeFromLaunch);
	}

}
