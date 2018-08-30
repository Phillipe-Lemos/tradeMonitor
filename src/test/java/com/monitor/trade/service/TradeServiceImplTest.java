/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.monitor.trade.service;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import com.monitor.trade.datasource.http.RestServiceImpl;
import com.monitor.trade.model.Trade;
import com.monitor.trade.servce.impl.TradeServiceImpl;
import com.monitor.trade.service.mock.TradeMock;

/**
 *
 * @author phillipe.lemos@gmail.com
 */
@RunWith(MockitoJUnitRunner.class)
@ContextConfiguration(classes = {TradeServiceImpl.class, RestServiceImpl.class})
@TestPropertySource(properties = {
    "url.rest.trade=http://rest.trade",
})
public class TradeServiceImplTest {
    
    @Value("${url.rest.trade}")
    private String url;
    
    @InjectMocks
    private TradeServiceImpl tradeService;
    
    @Mock
    private RestServiceImpl restService;
    
    
    @Test
    public void shouldCreateVirtualTradeToBuy() {
        when(restService.getTrades(url)).thenReturn(TradeMock.buildTradeList(5, Trade.MarketSide.BUY));
        final List<Trade> result = tradeService.recentTrades(3,3);
        assertThat(result, notNullValue());
        assertThat(result, not(empty()));
        Long qtdVirtual = result
                    .stream()
                    .filter(trade -> trade.getOrigin() == Trade.Origin.VIRTUAL)
                    .count();
        assertThat(qtdVirtual, equalTo(1L));
    }
    
    @Test
    public void shouldCreateVirtualTradeToSell() {
        when(restService.getTrades(url)).thenReturn(TradeMock.buildTradeList(5, Trade.MarketSide.SELL));
        final List<Trade> result = tradeService.recentTrades(3,3);
        assertThat(result, notNullValue());
        assertThat(result, not(empty()));
        Long qtdVirtual = result
                    .stream()
                    .filter(trade -> trade.getOrigin() == Trade.Origin.VIRTUAL)
                    .count();
        assertThat(qtdVirtual, equalTo(1L));
    }
    
    @Test
    public void shouldHaveatLeastOnVirtualTradeToSell() {
    	when(restService.getTrades(url)).thenReturn(TradeMock.buildTradeList(10, Trade.MarketSide.SELL));
        final List<Trade> result = tradeService.recentTrades(2,2);
        assertThat(result, notNullValue());
        assertThat(result, not(empty()));
        Long qtdVirtual = result
                    .stream()
                    .filter(trade -> trade.getOrigin() == Trade.Origin.VIRTUAL)
                    .count();
        assertThat(qtdVirtual, greaterThanOrEqualTo(1L));
    }

    @Test
    public void shouldHaveatLeastOnVirtualTradeToBuy() {
    	when(restService.getTrades(url)).thenReturn(TradeMock.buildTradeList(10, Trade.MarketSide.BUY));
        final List<Trade> result = tradeService.recentTrades(2,2);
        assertThat(result, notNullValue());
        assertThat(result, not(empty()));
        Long qtdVirtual = result
                    .stream()
                    .filter(trade -> trade.getOrigin() == Trade.Origin.VIRTUAL)
                    .count();
        assertThat(qtdVirtual, greaterThanOrEqualTo(1L));
    }    
    
    
    @Test
    public void shouldNotReturnTradeListDuParameterNullOrZero() {
        List<Trade> result = tradeService.recentTrades(0,3);
        assertThat(result, notNullValue());
        assertThat(result, empty());
        result = tradeService.recentTrades(3,0);
        assertThat(result, notNullValue());
        assertThat(result, empty());
        result = tradeService.recentTrades(0,0);
        assertThat(result, notNullValue());
        assertThat(result, empty());
        result = tradeService.recentTrades(null,0);
        assertThat(result, notNullValue());
        assertThat(result, empty());
        result = tradeService.recentTrades(0,null);
        assertThat(result, notNullValue());
        assertThat(result, empty());
        result = tradeService.recentTrades(null,null);
        assertThat(result, notNullValue());
        assertThat(result, empty());
    }
    

     
} 
