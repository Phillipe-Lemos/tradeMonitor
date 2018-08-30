package com.monitor.trade.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * Represents a bid or a ask
 * @author phillipe.lemos@gmail.com
 */
public class Order  {

    @JsonProperty("oid")
    private String oid;

    @JsonProperty("book")
    private String book;

    @JsonProperty("price")
    private BigDecimal price;
    
    @JsonProperty("amount")
    private BigDecimal amount;
    
    /**
     * Constructor is required by the Jackson. 
     */
    public Order() {}

    public Order(String oid, 
                  String book, 
                  BigDecimal price, 
                  BigDecimal amount) {
        this.oid = oid;
        this.book = book;
        this.price = price;
        this.amount = amount;
    }
    
    public String getOid() {
        return oid;
    }

    public String getBook() {
        return book;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public BigDecimal getAmount() {
        return amount;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.oid);
    }

    @Override
    public boolean equals(Object obj) {
        if(!super.equals(obj)){
            return false;
        }
        
        if (this == obj) {
            return true;
        }
        
        if (obj == null) {
            return false;
        }
        
        if (getClass() != obj.getClass()) {
            return false;
        }
        
        final Order other = (Order) obj;
        
        if (!Objects.equals(this.oid, other.oid)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Order{" + "oid=" + oid + ", book=" + book + ", price=" + price + ", amount=" + amount + '}';
    }


    
}
