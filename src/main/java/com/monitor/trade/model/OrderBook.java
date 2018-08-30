package com.monitor.trade.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
/**
 *
 * @author phillipe.lemos@gmail.com
 */
public class OrderBook  {
	
	private static final int DEFAULT_LIMIT_IN_COLLECTION = 50; 
	
    @JsonProperty("sequence")
    private Long sequence;

	@JsonProperty("updated_at")
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss+SS:SS")
    private LocalDateTime lastUdate;
    
    @JsonIgnore
    private final Set<BigDecimal> bestBid;
    
    @JsonIgnore
    private final Set<BigDecimal> bestAsk;
    
    @JsonIgnore
    private final Map<String, Order> bidsOrder;
    
    @JsonIgnore
    private final Map<String, Order> asksOrder;
    
    private Integer limitInCollections;
    
    @JsonCreator     
    public OrderBook() {
        super();
        this.limitInCollections = DEFAULT_LIMIT_IN_COLLECTION;
        Comparator<BigDecimal> comparator = Comparator.naturalOrder();
        this.bestAsk = new TreeSet<>(comparator.reversed());
        this.bestBid = new TreeSet<>();
        this.bidsOrder = new HashMap<>();
        this.asksOrder = new HashMap<>();
    }

    public Long getSequence() {
        return sequence;
    }

    public Collection<BigDecimal> getBestBid() {
        return bestBid;
    }

    public Collection<BigDecimal> getBestAsk() {
        return bestAsk;
    }
    
    public void setSequence(Long sequence) {
		this.sequence = sequence;
	}

	public void setLastUdate(LocalDateTime lastUdate) {
		this.lastUdate = lastUdate;
	}
    
    @JsonProperty("bids")
    public void addBids(Collection<Order> orders) {
    	orders.forEach(order -> bidsOrder.put(order.getOid(), order));
    	updateBestBids(orders);
    }

    @JsonProperty("asks")
    public void addAks(Collection<Order> orders) {
        orders.forEach(order -> asksOrder.put(order.getOid(), order));
        updateBestAsks(orders);
    }
    /**
     * Retrieve the max price from the best ask list. Using the orders list received, build a list with all prices 
     * that is less than the max value and update the best ask list.   
     * For each price in the temporary list, add it into a best ask list and if the size superseed the limitInCollections
     * than removes the max price. 
     * 
     * @param ordes Order list received from update. 
     * 
     */
    private void updateBestAsks(final Collection<Order> orders) {
    	final BigDecimal maxPrice = this.bestAsk
    			                    .stream()
    			                    .max(Comparator.naturalOrder())
    			                    .orElse(BigDecimal.ZERO);
    	
    	final List<BigDecimal> bestNewAsks;
    	if(!maxPrice.equals(BigDecimal.ZERO)) {
	    	bestNewAsks = orders
		                    .stream()
							.filter(order -> order.getPrice().compareTo(maxPrice) < 0)
					    	.map(Order::getPrice)
					    	.collect(Collectors.toList());
    	} else {
    		bestNewAsks = orders
                    .stream()
			    	.map(Order::getPrice)
			    	.collect(Collectors.toList());
    	}
    	
    	removeMaxUntilLimit(bestAsk,limitInCollections);
    	
    	bestNewAsks
		    .forEach(price -> {
		    	bestAsk.add(price);
		    	removeMaxUntilLimit(bestAsk,limitInCollections);
		    });
    }

	/**
     * Update the best bids list with limitInCollections reducing the list by removing the minimum price
     * 
     *  @param orders A list of new orders that arrive from UpdateOrders.
     */
    private void updateBestBids(final Collection<Order> orders) {
    	final BigDecimal minPrice = this.bestBid
						    	     .stream()
						    	     .min(Comparator.naturalOrder())
						    	     .orElse(BigDecimal.ZERO);
    	
    	final List<BigDecimal> bestNewBids;
    	if(!minPrice.equals(BigDecimal.ZERO)) {
    		bestNewBids = orders
		                   .stream()
				           .filter(order -> order.getPrice().compareTo(minPrice) > 0)
				           .map(Order::getPrice)
				           .collect(Collectors.toList());
    	} else {
    		bestNewBids = orders
	                       .stream()
			               .map(Order::getPrice)
			               .collect(Collectors.toList());
    	}
    	
    	removeMinUntilLimit(bestBid, limitInCollections);
    	
    	bestNewBids
    	    .forEach(price -> {
    	    	bestBid.add(price);
    	    	removeMinUntilLimit(bestBid, limitInCollections);
    	    });
    }
    
    /**
     * Remove from a list until a limit always removing the minimum value.
     * 
     * @param prices price list to be reduced.
     * @param limit Limit value. 
     */
    private void removeMinUntilLimit(final Set<BigDecimal> prices,final Integer limit) {
		if(prices.size() > limit) {
			int numToRemove = prices.size() - limit;
			for(int i = 0; i < numToRemove; i++) {
	        	final BigDecimal max = prices
							    	     .stream()
							    	     .sorted()
							    	     .min(Comparator.naturalOrder())
							    	     .orElse(BigDecimal.ZERO);

	        	 prices.remove(max);
			}
		}
	}
    
    /**
     * Remove from a list until a limit always removing the maximum value.
     * 
     * @param prices price list to be reduced.
     * @param limit Limit value. 
     */
    private void removeMaxUntilLimit(final Set<BigDecimal> prices,final Integer limit) {
		if(prices.size() > limit) {
			int numToRemove = prices.size() - limit;
			for(int i = 0; i < numToRemove; i++) {
	        	final BigDecimal max = prices
							    	     .stream()
							    	     .sorted()
							    	     .max(Comparator.naturalOrder())
							    	     .orElse(BigDecimal.ZERO);

	        	 prices.remove(max);
			}
		}
	}

    
    /**
     * Remove the order from a specific map and corresponding price 
     * @param mapToRemove
     * @param verifiedCollection
     * @param oid
     */
    private Boolean removeFromMapAndCollection(final Map<String, Order> mapToRemove, 
                                               final Collection<BigDecimal> verifiedCollection, 
                                               final String oid) {
        Order orderToRemove = mapToRemove.remove(oid);
        if (orderToRemove != null) {
           if (verifiedCollection.contains(orderToRemove.getPrice())) {
               verifiedCollection.remove(orderToRemove.getPrice());
               return Boolean.TRUE;
           }
        }
        return Boolean.FALSE;
    }
    
    public void removeOrderFromAsk(final Order order){
        if(order.getAmount().equals(BigDecimal.ZERO)) {
        	if(removeFromMapAndCollection(this.asksOrder, bestAsk, order.getOid())) {
        		updateBestAsks(Arrays.asList(order));
        	}
        }
    } 
    
    public void removeOrderFromBids(final Order order){
        if(order.getAmount().equals(BigDecimal.ZERO)) {
        	if(removeFromMapAndCollection(this.bidsOrder, bestBid, order.getOid())) {
        		updateBestBids(Arrays.asList(order));
        	}
        }
    }
    
    public void removeOrderByOidAndSide(final String oid,final String side) {
    	if("BUY".equals(side)) {
    		removeFromMapAndCollection(bidsOrder, bestBid, oid);
    	} else {
    		removeFromMapAndCollection(asksOrder, bestAsk, oid);
    	}
    }

    @Override
    public String toString() {
        return "OrderBook{" + "sequence=" + sequence + ", bidsOrder=" + bidsOrder + ", asksOrder=" + asksOrder + ", limitInCollections=" + limitInCollections + '}';
    }

	public void setLimitInCollections(Integer limitInCollections) {
		if(limitInCollections == null || limitInCollections <= 0 ) {
			throw new IllegalArgumentException("The value of limitInCollections should be greater than 0 :" + limitInCollections);
		}
		this.limitInCollections = limitInCollections;
	}
 
    
    
}
