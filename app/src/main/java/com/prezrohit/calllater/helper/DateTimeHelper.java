package com.prezrohit.calllater.helper;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateTimeHelper {
	public static String formatDate(String stringDate) {
		DateFormat originalFormat = new SimpleDateFormat("dd MM yyyy", Locale.ENGLISH);
		DateFormat targetFormat = new SimpleDateFormat("dd MMM YYYY", Locale.ENGLISH);
		Date date = null;
		try {
			date = originalFormat.parse(stringDate);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return targetFormat.format(date);
	}

	public static Date getDateFromString(String dateTimeAsString) {
		String dateAsString = dateTimeAsString.split(" - ")[0];
		String timeAsString = dateTimeAsString.split(" - ")[1];

		try {
			DateFormat sourceFormat = new SimpleDateFormat("dd MMM yyyy hh:mm a");
			return sourceFormat.parse(dateAsString + " " + timeAsString);

		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
	}
}
