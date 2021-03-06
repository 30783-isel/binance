package com.binance.api.x3la;

import com.binance.api.client.BinanceApiAsyncRestClient;
import com.binance.api.client.BinanceApiCallback;
import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiWebSocketClient;
import com.binance.api.client.domain.market.Candlestick;
import com.binance.api.client.domain.market.CandlestickInterval;
import com.binance.api.client.impl.BinanceApiAsyncRestClientImpl;
import com.binance.api.client.impl.BinanceApiService;

import weka.core.DenseInstance;
import weka.core.Instances;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Market data stream endpoints examples.
 *
 * It illustrates how to create a stream to obtain updates on market data such
 * as executed trades.
 */
public class MarketDataStreamX3la {

	static Future<List<Candlestick>> listaCandleStick = null;

	public static void main(String[] args) throws InterruptedException, IOException {

		BinanceApiCallback<List<Candlestick>> callback = null;

		getHistoricalData("ETHBTC", CandlestickInterval.HOURLY, 10000, 1461711600000L, 1587942000000L, response -> {

			try {
				WekaImplAux.prepareInstancesForWeka(response);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		});
	}

	public static void getHistoricalData(String symbol, CandlestickInterval interval, Integer limit, Long startTime, Long endTime, BinanceApiCallback<List<Candlestick>> callback) {

		BinanceApiAsyncRestClient cliente = BinanceApiClientFactory.newInstance().newAsyncRestClient();

		cliente.getCandlestickBars(symbol, interval, limit, startTime, endTime, callback);

	}
	
	public void getCandleStick() {
		BinanceApiWebSocketClient client = BinanceApiClientFactory.newInstance().newWebSocketClient();

		// Obtain 1m candlesticks in real-time for ETH/BTC
		client.onCandlestickEvent("ethbtc", CandlestickInterval.ONE_MINUTE, response -> {
			System.out.println(response);
			String open = response.getOpen();
			String close = response.getClose();
			String high = response.getHigh();
			String low = response.getLow();
			String volume = response.getVolume();
		});
	}
}
