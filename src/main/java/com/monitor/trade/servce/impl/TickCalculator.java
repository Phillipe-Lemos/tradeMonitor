package com.monitor.trade.servce.impl;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.monitor.trade.model.Trade;
import com.monitor.trade.model.Trade.MarketSide;

/**
 * Represents the trade strategy that buy or sell a virtual trade after a number of downsticks or
 * upsticks.
 *  
 * @author phillipe.lemos@gmail.com
 */
public class TickCalculator {
	
	private static final Logger LOG = LoggerFactory.getLogger(TickCalculator.class);
    
    private static TickCalculator instance;
    
    private BigDecimal lastSellPrice;
    
    private BigDecimal lastBuyPrice;
    
    private Integer countUpStick;
    
    private Integer countDownStick;
    
    private Integer maxConsecutiveUpStick;
    
    private Integer maxConsecutiveDownStick;
    
    private Random random;
    
    private TickCalculator() {
        this.maxConsecutiveUpStick = 0;
        this.maxConsecutiveDownStick = 0;
        this.lastSellPrice = BigDecimal.ZERO;
        this.lastBuyPrice = BigDecimal.ZERO;
        this.countUpStick = 0;
        this.countDownStick = 0;
        random = new Random(Integer.MAX_VALUE);
    }
    
    public static TickCalculator getInstance() {
        if (instance == null) {
            instance = new TickCalculator();
        }
        return instance;
    }
    
    public void executeTradeStategy(List<Trade> trades, Integer maxConsecutiveUpStick, Integer maxConsecutiveDownStick) {
    	this.maxConsecutiveDownStick = maxConsecutiveDownStick;
    	this.maxConsecutiveUpStick = maxConsecutiveUpStick;
        Collections.sort(trades);
        trades.stream()
            .filter(trade -> trade.getMarketSide().equals(MarketSide.BUY))
            .filter(trade -> !trade.getIsAlreadyCalculated() )
            .findFirst()
            .ifPresent(trade -> lastBuyPrice = trade.getPrice());
        trades.stream()
	        .filter(trade -> trade.getMarketSide().equals( MarketSide.SELL))
	        .filter(trade -> !trade.getIsAlreadyCalculated())
	        .findFirst()
	        .ifPresent(trade -> lastSellPrice = trade.getPrice());
        LOG.info(">>> Initial sell price " + lastSellPrice + " initial buy price : " + lastBuyPrice);
        final List<Trade> virtualTrades = trades
						                       .stream()
						                       .map(trad -> checkOccurenceOfStick(trad))
						                       .filter(t -> t != null)
						                       .collect(Collectors.toList());
        if(!virtualTrades.isEmpty()) {
            trades.addAll(virtualTrades);
            Collections.sort(trades);
        }
    }
    
    private Trade checkOccurenceOfStick(Trade currenttrade) {
        int comp;
        Trade virtualbTrade = null;
        if(currenttrade.getMarketSide().equals(Trade.MarketSide.BUY)) {
            comp =  lastBuyPrice.compareTo(currenttrade.getPrice());
            if (comp > 0){
                countDownStick++;
                if(countDownStick > maxConsecutiveDownStick) {
                    LOG.info(">>> comp BUY : " + comp + " lastBuyPrice : " 
                            + lastBuyPrice + " currenttrade : " + currenttrade.getPrice() 
                            + " countDownStick " + countDownStick 
                            + " maxConsecutiveDownStick : " + maxConsecutiveDownStick
                            + " trade id " + currenttrade.getTradeId());
                    this.countDownStick = 0;
                    virtualbTrade 
                             = Trade.createVirtualTrade (random.nextLong(),
                            		                     currenttrade.getBook(),
                            		                     currenttrade.getCreated().plusNanos(1000L),
                                                         new BigDecimal(1),
                                                         currenttrade.getPrice(),
                                                         Trade.MarketSide.BUY);
                }
            } else if(comp < 0) {
                countDownStick = 0;
            }
            lastBuyPrice = currenttrade.getPrice();
        } else {
            comp = currenttrade.getPrice().compareTo(lastSellPrice);
            if(comp > 0) {
               countUpStick++; 
               if (countUpStick > maxConsecutiveUpStick) {
                   LOG.info(">>> comp SELL : " + comp + " lastSellPrice : " 
                           + lastSellPrice + " currenttrade : " + currenttrade.getPrice() 
                           + " countUpStick " + countUpStick 
                           + " maxConsecutiveUpStick : " + maxConsecutiveUpStick
                           + " trade id " + currenttrade.getTradeId());
                   this.countUpStick = 0;
                   virtualbTrade 
                           = Trade.createVirtualTrade(random.nextLong(),
                        		                      currenttrade.getBook(),
                        		                      currenttrade.getCreated().plusNanos(1000L),
                                                      new BigDecimal(1),
                                                      currenttrade.getPrice(),
                                                      Trade.MarketSide.SELL);
               }
            } else if(comp < 0) {
                countUpStick = 0;
            }
            lastSellPrice = currenttrade.getPrice();
        }
        currenttrade.setIsAlreadyCalculated(Boolean.TRUE);
        return virtualbTrade;
    }
}
