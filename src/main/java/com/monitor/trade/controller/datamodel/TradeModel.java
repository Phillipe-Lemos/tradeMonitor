package com.monitor.trade.controller.datamodel;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 * This class represents a rendered {@linkplain com.monitor.trade.model.Trade} into a TableView component
 * @author phillipe.lemos@gmail.com 
 */
public class TradeModel {
   
    private final SimpleLongProperty tradeId;
    
    private final ObjectProperty<LocalDateTime>  created;
    
    private final ObjectProperty<BigDecimal> amount;
    
    private final ObjectProperty<BigDecimal> price;
    
    private final SimpleBooleanProperty isVirtual;

    public TradeModel(Long tradeId, 
			          LocalDateTime created, 
			          BigDecimal amount, 
			          BigDecimal price, 
			          Boolean isVirtual) {
        this.tradeId = new SimpleLongProperty(tradeId);
        this.isVirtual = new SimpleBooleanProperty(isVirtual);
        this.created = new SimpleObjectProperty<LocalDateTime>(created);
        this.amount = new SimpleObjectProperty<BigDecimal>(amount);
        this.price = new SimpleObjectProperty<BigDecimal>(price);
    }

    public Long getTradeId() {
        return tradeId.getValue();
    }
    
    public LocalDateTime getCreatedOrigim() {
        return created.getValue();
    }

    public String getCreated() {
        return created
                .getValue()
                .format(DateTimeFormatter
                          .ofPattern("dd-MM-yyyy HH:mm:ss"));
    }

    public BigDecimal getAmount() {
        return amount.getValue();
    }

    public String getPrice() {
        return NumberFormat
                  .getCurrencyInstance(Locale.getDefault())
                  .format(price.getValue().doubleValue());

    }

    public Boolean getIsVirtual() {
        return isVirtual.getValue();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + Objects.hashCode(this.tradeId);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TradeModel other = (TradeModel) obj;
        if (!Objects.equals(this.tradeId.getValue(), other.tradeId.getValue())) {
            return false;
        }
        return true;
    }

}
