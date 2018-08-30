package com.monitor.trade.servce;

import com.monitor.trade.model.OrderBook;

/**
 * Provides service to process the {@linkplain com.monitor.trade.model.OrderBook}
 * @author phillipe.lemos@gmail.com
 */
public interface OrderBookService {
    
	/**
	 * Update the {@linkplain com.monitor.trade.model.OrderBook} with the {@linkplain com.monitor.trade.model.UpdateOrder} queue.  
	 * 
	 * @param limitInCollections  
	 * 
	 * @return
	 */
    OrderBook processOrderBook(Integer limitInCollections);
    
    /**
     * Stops the web-socket client.
     */
    void stopProcess();
}
