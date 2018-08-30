package com.monitor.trade.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal; 
import java.time.LocalDateTime;
import java.util.Objects;
import javax.validation.constraints.NotNull;

/**
 * Represents a trade that is received by a rest client from Bitson.com public API.
 * The Origin enumerator indicates if the trade is real or a calculated(virtual).
 * 
 * @author phillipe.lemos@gmail.com
 */
public class Trade implements Comparable<Trade> {

    public static enum MarketSide {
        SELL("sell"),
        BUY("buy");
        
        private final String marketSide;
        
        MarketSide(String marketSide) {
            this.marketSide = marketSide;
        }

        public String getMarketSide() {
            return marketSide;
        }
        
        @JsonCreator
        public static MarketSide fromString(String value) {
            for(MarketSide marketSide : MarketSide.values()) {
                if(marketSide.getMarketSide().equals(value)) {
                    return marketSide;
                }
            }
            return null;
        }
        
    }
    
    public static enum Origin {
        REAL,
        VIRTUAL;
    }
    
    @JsonProperty("tid")
    @NotNull
    private Long tradeId;
    
    @JsonProperty("book")
    @NotNull
    private String book;
    
    @JsonProperty("created_at")
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss+SSSS")
    @NotNull
    private LocalDateTime created;
    
    @JsonProperty("amount")
    @NotNull
    private BigDecimal amount;
    
    @JsonProperty("price")
    @NotNull
    private BigDecimal price;
    
    @JsonProperty("maker_side")
    @NotNull
    private MarketSide marketSide;
    
    private Boolean isAlreadyCalculated;
    
    private final Origin origin;

    public Trade() {
        this.origin = Origin.REAL;
        this.isAlreadyCalculated = Boolean.FALSE;
    }

    public static Trade createRealTrade(Long tradeId, 
                                        String book, 
                                        LocalDateTime created, 
                                        BigDecimal amount, 
                                        BigDecimal price, 
                                        MarketSide marketSide) {
        return new Trade(tradeId, 
                         book,
                         created, 
                         amount, 
                         price, 
                         marketSide,
                         Origin.REAL,
                         Boolean.FALSE);
    }
    
    public static Trade createVirtualTrade(Long tradeId, 
                                           String book, 
                                           LocalDateTime created, 
                                           BigDecimal amount, 
                                           BigDecimal price, 
                                           MarketSide marketSide) {
        return new Trade(tradeId, 
                         book,
                         created, 
                         amount, 
                         price, 
                         marketSide,
                         Origin.VIRTUAL,
                         Boolean.TRUE);
    }
    
    private Trade(Long tradeId, 
                  String book, 
                  LocalDateTime created, 
                  BigDecimal amount, 
                  BigDecimal price, 
                  MarketSide marketSide,
                  Origin origin,
                  Boolean isAlreadyCalculated) {
        this.tradeId = tradeId;
        this.book = book;
        this.created = created;
        this.amount = amount;
        this.price = price;
        this.marketSide = marketSide;
        this.origin = origin;
        this.isAlreadyCalculated = isAlreadyCalculated;
    }
    
    

    public Boolean getIsAlreadyCalculated() {
		return isAlreadyCalculated;
	}

	public void setIsAlreadyCalculated(Boolean isAlreadyCalculated) {
		this.isAlreadyCalculated = isAlreadyCalculated;
	}

	public Long getTradeId() {
        return tradeId;
    }

    public String getBook() {
        return book;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public MarketSide getMarketSide() {
        return marketSide;
    }

    public void setMarketSide(MarketSide marketSide) {
        this.marketSide = marketSide;
    }

    public Origin getOrigin() {
        return origin;
    }
    
    @Override
    public int compareTo(Trade o) {
        return o.getCreated().compareTo(this.getCreated());
    }
    
        @Override
    public int hashCode() {
        int hash = 3;
        hash = 23 * hash + Objects.hashCode(this.tradeId);
        hash = 23 * hash + Objects.hashCode(this.created);
        hash = 23 * hash + Objects.hashCode(this.marketSide);
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
        final Trade other = (Trade) obj;
        if (!Objects.equals(this.tradeId, other.tradeId)) {
            return false;
        }
        if (!Objects.equals(this.created, other.created)) {
            return false;
        }
        if (this.marketSide != other.marketSide) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Trade{" + "tradeId=" + tradeId + 
                      ", book=" + book + 
                      ", created=" + created + 
                      ", amount=" + amount + 
                      ", price=" + price + 
                      ", marketSide=" + marketSide + 
                      ", isAlreadyCalculated=" + isAlreadyCalculated +
                      ", origin=" + origin + "}";
    }
}
