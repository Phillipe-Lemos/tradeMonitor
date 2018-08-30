package com.monitor.trade.service.mock;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.monitor.trade.model.Order;
import com.monitor.trade.model.OrderBook;

public class OrderBookMock {

	private static Collection<Order> buildOrder(int qtd, String book) {
		final Random price   = new Random(Integer.MAX_VALUE);
		final Random amount  = new Random(Integer.MAX_VALUE);
		return IntStream.range(0, qtd)
				         .mapToObj(i -> new Order(Integer.toString(i), 
				        		                  book, 
				        		                  BigDecimal.valueOf(price.nextDouble()), 
				        		                  BigDecimal.valueOf(amount.nextDouble())))
				         .collect(Collectors.toList());
	}
	
	public static  OrderBook buildOrderBook(int numberOfOrders, String book) {
		final Collection<Order> bids = buildOrder(numberOfOrders, book);  
		Collection<Order> asks = buildOrder(numberOfOrders, book);
		OrderBook orderBook = new OrderBook();
		orderBook.setLimitInCollections(numberOfOrders);
		orderBook.addAks(asks);
		orderBook.addBids(bids);
		orderBook.setSequence(1L);
		orderBook.setLastUdate(LocalDateTime.now());
		return orderBook;
	}
	
	
	
}
