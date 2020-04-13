package com.binance.api.x3la;

import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiWebSocketClient;
import com.binance.api.client.domain.market.CandlestickInterval;

import java.io.IOException;

/**
 * Market data stream endpoints examples.
 *
 * It illustrates how to create a stream to obtain updates on market data such
 * as executed trades.
 */
public class MarketDataStreamX3la {

	public static void main(String[] args) throws InterruptedException, IOException {
		BinanceApiWebSocketClient client = BinanceApiClientFactory.newInstance().newWebSocketClient();

		// Listen for aggregated trade events for ETH/BTC
		// client.onAggTradeEvent("ethbtc", response -> System.out.println(response));

		// Listen for changes in the order book in ETH/BTC
		// client.onDepthEvent("ethbtc", response -> System.out.println(response));

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
