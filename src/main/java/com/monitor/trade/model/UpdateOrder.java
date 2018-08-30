package com.monitor.trade.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @author phillipe.lemos@gmail.com
 */
public class UpdateOrder {
    
    public enum KindOperation {
        BUY(0), //buy - bid 
        SELL(1); // sell - ask
        
        private final Integer kindOperation;
        
        private KindOperation(Integer kindOperation) {
            this.kindOperation = kindOperation;
        }
        
        public Integer getKinInteger() {
            return this.kindOperation;
        }
    }
    
    public static class UpdateOrderDetail {
        
        @JsonProperty("d")
        private Long timestamp;
        
        @JsonProperty("r")
        private BigDecimal rate;

        @JsonProperty("t")        
        private KindOperation kindOperation;
        
        @JsonProperty("a")
        private BigDecimal amount;
        
        @JsonProperty("v")
        private BigDecimal value;
        
        @JsonProperty("o")
        private String oid;
        
        public static UpdateOrderDetail createUpdateOrderDetail(Long timestamp, 
        		                                                BigDecimal rate, 
        		                                                KindOperation kind, 
        		                                                BigDecimal amount, 
        		                                                BigDecimal value, String oid) {
        	UpdateOrderDetail detail = new UpdateOrderDetail();
        	detail.amount = amount;
        	detail.kindOperation = kind;
        	detail.oid  = oid;
        	detail.timestamp = timestamp;
        	detail.value = value;
        	detail.rate = rate;
        	return detail;
        }
        
        public UpdateOrderDetail() {
            
        }

        public Long getTimestamp() {
            return timestamp;
        }

        public BigDecimal getRate() {
            return rate;
        }

        public KindOperation getKindOperation() {
            return kindOperation;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public BigDecimal getValue() {
            return value;
        }

        public String getOid() {
            return oid;
        }

        @Override
        public String toString() {
            return "UpdateOrderDetail{" + "timestamp=" + timestamp + 
                                      ", rate=" + rate + 
                                      ", kindOperation=" + kindOperation + 
                                      ", amount=" + amount + 
                                      ", value=" + value + 
                                      ", oid=" + oid + '}';
        }
    }
    
    @JsonProperty("type")
    private String type;
    
    @JsonProperty("book")
    private String book;
    
    @JsonProperty("sequence")
    private Long sequence;
    
    @JsonProperty("payload")
    private final Collection<UpdateOrderDetail> updateOrdersDetail;

    public String getType() {
        return type;
    }

    public String getBook() {
        return book;
    }

    public Long getSequence() {
        return sequence;
    }

    public Collection<UpdateOrderDetail> getUpdateOrdersDetail() {
        return updateOrdersDetail;
    }

    public UpdateOrder() {
        updateOrdersDetail = new ArrayList<>();
    }
    
    public static UpdateOrder createUpdateOrder(Long sequence, String book, String type, Collection<UpdateOrderDetail> details) {
    	UpdateOrder updateOrder = new UpdateOrder();
    	updateOrder.book = book;
    	updateOrder.sequence = sequence;
    	updateOrder.type = type;
    	updateOrder.updateOrdersDetail.addAll(details); 
    	return updateOrder;
    }

    @Override
    public String toString() {
        return "UpdateOrder{" + "type=" + type + ", book=" + book + ", sequence=" + sequence + ", updateOrdersDetail=" + updateOrdersDetail + '}';
    }
    
}
