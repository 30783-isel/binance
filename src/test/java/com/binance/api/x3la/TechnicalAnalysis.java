package com.binance.api.x3la;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ta4j.core.*;
import org.ta4j.core.analysis.criteria.AverageProfitableTradesCriterion;
import org.ta4j.core.analysis.criteria.RewardRiskRatioCriterion;
import org.ta4j.core.analysis.criteria.TotalProfitCriterion;
import org.ta4j.core.analysis.criteria.VersusBuyAndHoldCriterion;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.Num;
import org.ta4j.core.trading.rules.CrossedDownIndicatorRule;
import org.ta4j.core.trading.rules.CrossedUpIndicatorRule;
import org.ta4j.core.trading.rules.StopGainRule;
import org.ta4j.core.trading.rules.StopLossRule;


// List<String[]> allElements =
// TechnicalAnalysis.prepareListForTa4j(listAttributes);
// TechnicalAnalysis.executeTechnicalAnalysis(allElements);

public class TechnicalAnalysis {

	public static void executeTechnicalAnalysis(List<String[]> allElements) {

		// Getting a bar series (from any provider: CSV, web service, etc.)
		BarSeries series = loadBitstampSeries(allElements);

		// Getting the close price of the bars
		Num firstClosePrice = series.getBar(0).getClosePrice();
		System.out.println("First close price: " + firstClosePrice.doubleValue());
		// Or within an indicator:
		ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
		// Here is the same close price:
		System.out.println(firstClosePrice.isEqual(closePrice.getValue(0))); // equal to firstClosePrice

		// Getting the simple moving average (SMA) of the close price over the last 5
		// bars
		SMAIndicator shortSma = new SMAIndicator(closePrice, 5);
		// Here is the 5-bars-SMA value at the 42nd index
		System.out.println("5-bars-SMA value at the 42nd index: " + shortSma.getValue(42).doubleValue());

		// Getting a longer SMA (e.g. over the 30 last bars)
		SMAIndicator longSma = new SMAIndicator(closePrice, 30);

		// Ok, now let's building our trading rules!

		// Buying rules
		// We want to buy:
		// - if the 5-bars SMA crosses over 30-bars SMA
		// - or if the price goes below a defined price (e.g $800.00)
		Rule buyingRule = new CrossedUpIndicatorRule(shortSma, longSma).or(new CrossedDownIndicatorRule(closePrice, 800));

		// Selling rules
		// We want to sell:
		// - if the 5-bars SMA crosses under 30-bars SMA
		// - or if the price loses more than 3%
		// - or if the price earns more than 2%
		Rule sellingRule = new CrossedDownIndicatorRule(shortSma, longSma).or(new StopLossRule(closePrice, series.numOf(3))).or(new StopGainRule(closePrice, series.numOf(2)));

		// Running our juicy trading strategy...
		BarSeriesManager seriesManager = new BarSeriesManager(series);
		TradingRecord tradingRecord = seriesManager.run(new BaseStrategy(buyingRule, sellingRule));
		System.out.println("Number of trades for our strategy: " + tradingRecord.getTradeCount());

		// Analysis

		// Getting the profitable trades ratio
		AnalysisCriterion profitTradesRatio = new AverageProfitableTradesCriterion();
		System.out.println("Profitable trades ratio: " + profitTradesRatio.calculate(series, tradingRecord));
		// Getting the reward-risk ratio
		AnalysisCriterion rewardRiskRatio = new RewardRiskRatioCriterion();
		System.out.println("Reward-risk ratio: " + rewardRiskRatio.calculate(series, tradingRecord));

		// Total profit of our strategy
		// vs total profit of a buy-and-hold strategy
		AnalysisCriterion vsBuyAndHold = new VersusBuyAndHoldCriterion(new TotalProfitCriterion());
		System.out.println("Our profit vs buy-and-hold profit: " + vsBuyAndHold.calculate(series, tradingRecord));

		// Your turn!
	}

	/**
	 * @return the bar series from Bitstamp (bitcoin exchange) trades
	 */

	public static String[] createArrayOfStrings(double... dble) {
		String[] strings = new String[3];

		String timestamp = String.format("%.0f", dble[0]);
		strings[0] = timestamp;
		strings[1] = Double.toString(dble[1]);
		strings[2] = Double.toString(dble[2]);

		return strings;
	}

	public static List<String[]> prepareListForTa4j(List<double[]> listAttributes) {

		List<String[]> listStr = new ArrayList<String[]>();

		for (double[] arrDoubles : listAttributes) {
			listStr.add(createArrayOfStrings(arrDoubles[0], arrDoubles[4], arrDoubles[5]));
		}

		return listStr;
	}

	public static BarSeries loadBitstampSeries(List<String[]> lines) {

		lines.remove(0); // Removing header line

		BarSeries series = new BaseBarSeries();
		if ((lines != null) && !lines.isEmpty()) {

			// Getting the first and last trades timestamps
			ZonedDateTime beginTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(lines.get(0)[0]) * 1000), ZoneId.systemDefault());
			ZonedDateTime endTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(lines.get(lines.size() - 1)[0]) * 1000), ZoneId.systemDefault());
			if (beginTime.isAfter(endTime)) {
				Instant beginInstant = beginTime.toInstant();
				Instant endInstant = endTime.toInstant();
				beginTime = ZonedDateTime.ofInstant(endInstant, ZoneId.systemDefault());
				endTime = ZonedDateTime.ofInstant(beginInstant, ZoneId.systemDefault());
				// Since the CSV file has the most recent trades at the top of the file, we'll
				// reverse the list to feed
				// the List<Bar> correctly.
				Collections.reverse(lines);
			}
			// build the list of populated bars
			buildSeries(series, beginTime, endTime, 300, lines);
		}

		return series;
	}

	/**
	 * Builds a list of populated bars from csv data.
	 *
	 * @param beginTime the begin time of the whole period
	 * @param endTime   the end time of the whole period
	 * @param duration  the bar duration (in seconds)
	 * @param lines     the csv data returned by CSVReader.readAll()
	 */
	@SuppressWarnings("deprecation")
	private static void buildSeries(BarSeries series, ZonedDateTime beginTime, ZonedDateTime endTime, int duration, List<String[]> lines) {

		Duration barDuration = Duration.ofSeconds(duration);
		ZonedDateTime barEndTime = beginTime;
		// line number of trade data
		int i = 0;
		do {
			// build a bar
			barEndTime = barEndTime.plus(barDuration);
			Bar bar = new BaseBar(barDuration, barEndTime, series.function());
			do {
				// get a trade
				String[] tradeLine = lines.get(i);
				ZonedDateTime tradeTimeStamp = ZonedDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(tradeLine[0]) * 1000), ZoneId.systemDefault());
				// if the trade happened during the bar
				if (bar.inPeriod(tradeTimeStamp)) {
					// add the trade to the bar
					double tradePrice = Double.parseDouble(tradeLine[1]);
					double tradeVolume = Double.parseDouble(tradeLine[2]);
					bar.addTrade(tradeVolume, tradePrice, series.function());
				} else {
					// the trade happened after the end of the bar
					// go to the next bar but stay with the same trade (don't increment i)
					// this break will drop us after the inner "while", skipping the increment
					break;
				}
				i++;
			} while (i < lines.size());
			// if the bar has any trades add it to the bars list
			// this is where the break drops to
			if (bar.getTrades() > 0) {
				series.addBar(bar);
			}
		} while (barEndTime.isBefore(endTime));
	}

}
