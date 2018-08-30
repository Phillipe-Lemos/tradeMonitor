package com.monitor.trade.datasource.http.contract;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.monitor.trade.model.OrderBook;

/**
 * This class represents the response from the end-point 
 * {@link https://api.bitso.com/v3/order_book/?book=btc_mxn&aggregate=false} to 
 * get the initial OrderBook. 
 * 
 * @author phillipe.lemos@gmail.com
 *
 */
public class OrderBookResponse {

	@JsonProperty("success") 
	private Boolean status;
	
	@JsonProperty("payload") 
	private OrderBook orderBook;

	public Boolean getStatus() {
		return status;
	}

	public OrderBook getOrderBook() {
		return orderBook;
	}

	public OrderBookResponse() {
		super();
	}
	
}
