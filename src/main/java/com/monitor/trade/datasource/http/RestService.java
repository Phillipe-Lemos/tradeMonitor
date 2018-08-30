package com.monitor.trade.datasource.http;

import java.util.List;

/**
 * Instance of this interface are able to retrieve Trades and Order book
 * from specifics urls.
 * @author phillipe.lemos@gmail.com
 * @param <T>  Represents the OrderBook class type.
 * @param <U>  Represents the Trade class type.
 */
public interface RestService<T,U> {
    
    /**
     * Given an url retrieve the OrderBook.
     *
     * @param url A valid url to load OrderBook from.
     * @return  OrderBook
     */
    T getOrderBook(final String url);
    
    /**
     * Given an url loads a collection of most recents trades.
     * @param url A valid url to load a collection of trade.
     * @return  Collection of trades.
     */
    List<U> getTrades(final String url);
}
