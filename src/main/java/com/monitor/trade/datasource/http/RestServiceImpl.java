package com.monitor.trade.datasource.http;

import com.monitor.trade.datasource.http.contract.OrderBookResponse;
import com.monitor.trade.datasource.http.contract.TradeResponse;
import com.monitor.trade.model.OrderBook;
import com.monitor.trade.model.Trade;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Helper class that offers http service to retrieve {@linkplain com.sonar.model.Trade} and 
 * {@linkplain com.sonar.model.OrderBook}
 * 
 * @author phillipe.lemos@gmail.com
 */
@Service
public class RestServiceImpl implements RestService<OrderBook, Trade> {

    private final RestTemplateBuilder restTemplateBuilder;
    
    private final RestTemplate restTemplate;
    
    private final HttpHeaders headers;
    
    private final HttpEntity<String> entity;
    
    @Autowired
    public RestServiceImpl(final RestTemplateBuilder restTemplateBuilder) {
        this.restTemplateBuilder = restTemplateBuilder;
        this.restTemplate = restTemplateBuilder.build();
        final List<HttpMessageConverter<?>> messageConverters 
                = new ArrayList<>(1);        
        final MappingJackson2HttpMessageConverter converter = 
                new MappingJackson2HttpMessageConverter();
        converter.setSupportedMediaTypes(Arrays.asList(MediaType.APPLICATION_JSON_UTF8));
        messageConverters.add(converter);  
        this.restTemplate.setMessageConverters(messageConverters); 
        
        this.headers = new HttpHeaders();
        this.headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON_UTF8));
        this.headers.add("user-agent", 
                "Mozilla/5.0 (X11; Ubuntu; Linu…) Gecko/20100101 Firefox/61.0");
        this.entity = new HttpEntity<>("parameters", headers);
    }
    
    @Override
    public OrderBook getOrderBook(final String url) {
        ResponseEntity<OrderBookResponse> response = 
                restTemplate.exchange(url, HttpMethod.GET, entity, OrderBookResponse.class);
       return response.getBody().getOrderBook();
    }

    @Override
    public List<Trade> getTrades(final String url) {
        ResponseEntity<TradeResponse> response = 
                restTemplate.exchange(url, HttpMethod.GET, entity, TradeResponse.class);
        return response.getBody().getTrades();
    }
    
}
