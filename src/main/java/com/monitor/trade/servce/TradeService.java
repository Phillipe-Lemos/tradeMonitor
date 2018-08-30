package com.monitor.trade.servce;

import java.util.List;

import com.monitor.trade.model.Trade;

/**
 * Manage the trades and delegate to {@linkplain com.monitor.trade.servce.impl.TickCalculator} 
 * to apply the trade strategy.
 * 
 * @author phillipe.lemos@gmail.com
 */
public interface TradeService {
    
	/**
	 * Retrieves the last trades from Bitso.com with trade strategy applied.
	 * The trades that result from strategy are identified by the 
	 * {@linkplain com.monitor.trade.model.Trade.Origin} enumerator. Trades identified by
	 * Origin.REAL are ones that comes from  Bitso.com, and Origin.VIRTUAL identifies 
	 * trades that represent an operation of buy or sell.
	 * 
	 * @param maxConsecutiveUpStick  Maximum consecutive upsticks.
	 * @param maxConsecutiveDownStick Maximum consecutive downstick.
	 * @return Recent trade list with with  
	 * 
	 */
    List<Trade> recentTrades(final Integer maxConsecutiveUpStick,
                             final Integer maxConsecutiveDownStick); 
    
}
