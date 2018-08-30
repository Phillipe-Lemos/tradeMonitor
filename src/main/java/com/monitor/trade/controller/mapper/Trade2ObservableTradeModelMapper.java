package com.monitor.trade.controller.mapper;

import com.monitor.trade.controller.datamodel.TradeModel;
import com.monitor.trade.model.Trade;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Maps Trades to TradeModel
 * @author phillipe.lemos@gmail.com
 */
public class Trade2ObservableTradeModelMapper {
    
    /**
     * Maps {@linkplain com.monitor.trade.model.Trade} to his equivalent data 
     * model to be rendered in TabelView 
     * {@linkplain com.monitor.trade.controller.datamodel.TradeModel}
     * 
     * @param source Trade instance.
     * @return TradeModel instance.
     */  
    private static TradeModel fromTrade2TradeModel(Trade source) {
        return new TradeModel(source.getTradeId(),
                              source.getCreated(),
                              source.getAmount(),
                              source.getPrice(),
                              source.getOrigin() == Trade.Origin.VIRTUAL);
    }
    
    
    /**
     * Map a trade list to an trade mode observable list.
     * 
     * @param trades List of {@link com.monitor.trade.model.Trade} objects
     * @param to TableView that shows only sell Trades.
     * @param numberMostRecentTrades Number of most recent trades. This limit 
     *    the number of trades into each one of TableView.
     * @param side  Indicates if a trade have side buy or side sell.
     * @param createdComparator Comparator that helps to organize the collection 
     *     before the 
     *     
     */
    public static void from(List<Trade> trades,  
                            ObservableList<TradeModel> to, 
                            Integer numberMostRecentTrades,
                            Trade.MarketSide side,
                            Comparator<TradeModel> createdComparator) {
    	if(trades != null && to != null && createdComparator != null) {
	        final List<TradeModel> tradeModelDif 
	                = trades
	                      .stream()
	                      .filter(trade -> trade.getMarketSide().equals(side))
	                      .map(Trade2ObservableTradeModelMapper::fromTrade2TradeModel)
	                      .filter(tradeModel  -> !to.contains(tradeModel))
	                      .collect(Collectors.toList());
	        if(!tradeModelDif.isEmpty()) {
	            to.addAll(tradeModelDif);
		        FXCollections.sort(to, createdComparator);
	        }
            if(to.size() > numberMostRecentTrades) {
                int cuttLimit = to.size() - numberMostRecentTrades; 
                to.remove(to.size() - cuttLimit, to.size());
            }
    	}
    }
}
