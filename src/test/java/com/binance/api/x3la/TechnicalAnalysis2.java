package com.binance.api.x3la;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.List;

import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.Num;

public class TechnicalAnalysis2 {

	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm");
	// private static final DateTimeFormatter DATE_FORMAT =
	// DateTimeFormatter.ofPattern("yyyy-MM-dd");
	public static String dateFormat = "dd-MM-yyyy hh:mm";
	private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat);

	public static String ConvertMilliSecondsToFormattedDate(String milliSeconds) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(Long.parseLong(milliSeconds));
		return simpleDateFormat.format(calendar.getTime());
	}

	public static BarSeries loadSeries(List<double[]> lst) {

		BarSeries series = new BaseBarSeries("apple_bars");

		for (double[] line : lst) {
			ZonedDateTime date = LocalDate.parse(ConvertMilliSecondsToFormattedDate(String.format("%.0f", line[0])), DATE_FORMAT).atStartOfDay(ZoneId.systemDefault());
			// ZonedDateTime date = LocalDate.parse(String.format("%.0f", line[0]),
			// DATE_FORMAT).atStartOfDay(ZoneId.systemDefault());
			double open = line[1];
			double high = line[2];
			double low = line[3];
			double close = line[4];
			double volume = line[5];

			series.addBar(date, open, high, low, close, volume);
		}

		return series;
	}

}
