package com.hricigor.timerapplication;

public class Timer {

	private String timerName;
	private long timerValue;

	public Timer(String name, long value) {
		timerName = name;
		timerValue = value;
	}

	public String getTimerName() {
		return timerName;
	}

	public long getTimerValue() {
		return timerValue;
	}

	public void setTimerValue(long timerValue) {
		this.timerValue = timerValue;
	}

}
