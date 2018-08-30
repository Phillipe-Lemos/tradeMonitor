package com.monitor.trade.servce.impl;

import java.util.Collections;
import java.util.List;
import java.util.Comparator;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.monitor.trade.datasource.http.RestService;
import com.monitor.trade.model.Trade;
import com.monitor.trade.servce.TradeService;

/**
 * This class is a implementation from {@linkplain com.monitor.trade.servce.TradeService} interface 
 * and maintain a Set of all trades received from Bitson.com. 
 *  
 * @author phillipe.lemos@gmail.com
 */
@Service
public class TradeServiceImpl implements TradeService {
	
	private static final Logger LOG = LoggerFactory.getLogger(TradeServiceImpl.class);

    @Value("${url.rest.trade}")
    private String url;
    
    @Value("${trade.repo.limit}")
    private Integer repoLimit; 
    
    private final RestService<?, Trade> restService;
    
    private final Set<Trade> tradeRepo; 

    @Autowired
    public TradeServiceImpl(final RestService<?, Trade> restService) {
        this.restService = restService;
        this.tradeRepo = new TreeSet<>();
    }
    
    /**
     * Limit the growth of the set that represents the trade repository.
     */
    private void applyLimitToRepo() {
    	if(repoLimit != null && repoLimit > 0 && tradeRepo.size() > repoLimit) {
    		final int qtdToRemove = tradeRepo.size() - repoLimit;
    		for(int i = 0; i < qtdToRemove; i++) {
    			Optional<Trade> oldestTrade = tradeRepo
    					                         .stream()
    					                         .max(Comparator.comparing(Trade::getCreated));
    			if(oldestTrade.isPresent()) {
    				tradeRepo.remove(oldestTrade.get());
    			} else {
    				break;
    			}
    		}
    	}
    }
    
    
    /**
     * After apply the trade algorithm the most recent trades are returned.
     * 
     * @param numberMostRecentTrades Limit value of number of trades in memory.
     * @param maxConsecutiveUpStick Number of consecutive up sticks.
     * @param  maxConsecutiveDownStick Number of consecutive down sticks.
     * @return A list of most recent trades with new trade after maxConsecutiveUpStick or maxConsecutiveUpStick
     * happen.  
     */
    @Override
    public List<Trade> recentTrades(final Integer maxConsecutiveUpStick,
                                    final Integer maxConsecutiveDownStick) {
    	
    	if(maxConsecutiveUpStick != null 
		   && maxConsecutiveDownStick != null
		   && maxConsecutiveUpStick > 0 && maxConsecutiveDownStick > 0) {
	        tradeRepo.addAll(restService.getTrades(url));
	        applyLimitToRepo();
	        final List<Trade> noCalculatedTrades = tradeRepo
	        		               .stream()
	        		               .filter(trade -> !trade.getIsAlreadyCalculated())
	        		               .collect(Collectors.toList());
	        LOG.info(">>>  noCalculatedTrades : " + noCalculatedTrades);
	        if(noCalculatedTrades.isEmpty()) {
	        	return Collections.emptyList();
	        }
	        TickCalculator
	                .getInstance()
	                .executeTradeStategy(noCalculatedTrades, maxConsecutiveUpStick, maxConsecutiveDownStick);
	        return noCalculatedTrades
	                .stream()
	                .sorted()
	                .collect(Collectors.toList());
    	} else {
    		return Collections.emptyList();
    	}
    }
}

