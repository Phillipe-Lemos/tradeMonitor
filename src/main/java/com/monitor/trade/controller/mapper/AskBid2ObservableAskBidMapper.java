package com.monitor.trade.controller.mapper;

import java.math.BigDecimal;
import java.util.Collection;

import javafx.scene.chart.XYChart;

/**
 * Maps a {@linkplain java.util.Set<BigDecimal>} to 
 * {@linkplain javafx.collections.ObservableList<XYChart.Data<Integer, BigDecimal>>}
 * 
 * @author phillipe.lemos@gmail.com
 */
public class AskBid2ObservableAskBidMapper {
    
    public static void from(XYChart.Series<Integer, BigDecimal> serie,
                            Collection<BigDecimal> sourceSet,
                            Integer numberOfItemsAxisX) {
    	if(serie != null && serie.getData() != null && sourceSet != null) {
	    	if(serie.getData().size() > numberOfItemsAxisX) {
	    		final int toRemove = serie.getData().size() - numberOfItemsAxisX + 1;
	    		serie.getData().remove(0, toRemove);
	    	}
	    	int index = serie.getData().size();
	    	for(BigDecimal bestPrice : sourceSet) {
	    		final XYChart.Data<Integer, BigDecimal> value = new XYChart.Data<>(index, bestPrice);
	    		value.setExtraValue(bestPrice);
		    	serie.getData().add(value);
		    	index +=1;
		    	if(serie.getData().size() > numberOfItemsAxisX) {
		    		serie.getData().remove(0);
		    	}
	    	}
    	}
    }
}
