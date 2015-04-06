package com.hricigor.timerapplication;

public class TimerUtil {

	private static long lSeconds;
	private static long lMinutes;
	private static long lHours;

	private static long mColonInterval;

	private static String sSeconds;
	private static String sMinutes;
	private static String sHours;

	public static String convertTime(long time) {

		lSeconds = (long) (time / 1000);
		lMinutes = (long) (lSeconds / 60);
		lHours = (long) (lMinutes / 60);

		// Formating seconds
		lSeconds = lSeconds % 60;
		sSeconds = String.valueOf(lSeconds);
		if (lSeconds == 0) {
			sSeconds = "00";
		} else if (lSeconds < 10 && lSeconds > 0) {
			sSeconds = "0" + sSeconds;
		}

		// Formating minutes
		lMinutes = lMinutes % 60;
		sMinutes = String.valueOf(lMinutes);
		if (lMinutes == 0) {
			sMinutes = "00";
		} else if (lMinutes < 10 && lMinutes > 0) {
			sMinutes = "0" + sMinutes;
		}

		// Formating hours
		sHours = String.valueOf(lHours);
		if (lHours == 0) {
			sHours = "00";
		} else if (lHours < 10 && lHours > 0) {
			sHours = "0" + sHours;
		}

		return (sHours + " : " + sMinutes + " : " + sSeconds);

	}

	// Setting timer text and simulating blinking colon
	public static String updateTimer(long time) {
		mColonInterval = (long) (time / 300);
		convertTime(time);

		if (mColonInterval % 2 == 0) {
			return (sHours + " : " + sMinutes + " : " + sSeconds);
		} else {
			return (sHours + "   " + sMinutes + "   " + sSeconds);
		}
	}

}
