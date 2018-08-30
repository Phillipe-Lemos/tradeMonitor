package com.monitor.trade.service.mock;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.monitor.trade.model.Trade;

public class TradeMock {

    public static List<Trade> buildTradeList(Integer numOfTrades, Trade.MarketSide marketSide) {
    	final Random r = new Random(Integer.MAX_VALUE);
    	final List<Trade> trades = IntStream.range(1, numOfTrades + 1)
                 .mapToObj(i -> {
                	 BigDecimal price;
                	 if(marketSide == Trade.MarketSide.BUY) {
                		 price = new BigDecimal(i);
                	 } else {
                		 price = new BigDecimal(numOfTrades - i + 1);
                	 }
                	 return Trade.createRealTrade(r.nextLong(), 
                                                      "BTCMXN", 
                                                      LocalDateTime.now().plusSeconds(Integer.valueOf(i).longValue()), 
                                                      new BigDecimal(12), 
                                                      price, 
                                                      marketSide);
                 })
                 .collect(Collectors.toList());
    	return trades;
    }
    
    public static List<Trade> buildRandomPriceTradeList(Integer numOfTrades, Trade.MarketSide marketSide) {
    	final Random r = new Random(Integer.MAX_VALUE);
    	final Random randomPrice = new Random(numOfTrades * 10);
    	final List<Trade> trades = IntStream.range(1, numOfTrades + 1)
                 .mapToObj(i -> {
                	 BigDecimal price;
                	 return Trade.createRealTrade(r.nextLong(), 
                                                      "BTCMXN", 
                                                      LocalDateTime.now().plusSeconds(Integer.valueOf(i).longValue()), 
                                                      new BigDecimal(12), 
                                                      BigDecimal.valueOf(randomPrice.nextDouble()), 
                                                      marketSide);
                 })
                 .collect(Collectors.toList());
    	return trades;
    	
    }
	
}
