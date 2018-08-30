package com.monitor.trade.datasource.http.contract;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.monitor.trade.model.Trade;

import java.util.Collections;
import java.util.List;

/**
 * This class represents the response from the end-point 
 * {@link https://api.bitso.com/v3/trades/} to keep track on trades.
 * 
 * @author phillipe.lemos@gmail.com
 */
public class TradeResponse {
    
    @JsonProperty("success")
    private Boolean status; 
    
    @JsonProperty("payload")
    private List<Trade> trades;

    public TradeResponse() {
        super();
    }

    public Boolean getStatus() {
        return status;
    }

    public List<Trade> getTrades() {
        if(this.status) {
            return this.trades;
        }
        return Collections.emptyList();
    }
    
    @Override
    public String toString() {
        return "TradeResponse{" + "status=" + status + ", trades=" + trades + '}';
    }
    
}
