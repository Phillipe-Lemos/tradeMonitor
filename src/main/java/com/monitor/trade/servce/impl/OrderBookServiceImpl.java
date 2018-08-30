package com.monitor.trade.servce.impl;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.monitor.trade.datasource.http.RestService;
import com.monitor.trade.datasource.websocket.CustomInboundHandler;
import com.monitor.trade.datasource.websocket.WebSocketClientImpl;
import com.monitor.trade.model.Order;
import com.monitor.trade.model.OrderBook;
import com.monitor.trade.model.Trade;
import com.monitor.trade.model.UpdateOrder;
import com.monitor.trade.servce.OrderBookService;

/**
 * Manage the {@linkplain com.monitor.trade.model.OrderBook} retrieve from REST end-point and maintain it updated applying the 
 * {@linkplain com.monitor.trade.model.UpdateOrder} that was received through web-socket channel.
 * 
 * The web-socket channel is listening in different thread to no block the application.
 *   
 * @author phillipe.lemos@gmail.com
 */
@Service
@PropertySource("classpath:application.properties")
public class OrderBookServiceImpl implements OrderBookService {

	private static final Logger LOG = LoggerFactory.getLogger(OrderBookServiceImpl.class);
	
    private static final Integer POOL_SIZE = 5;
    
    private String url;
    
    private String urlWebSocket;

    private Integer port;
    
    private final ExecutorService executor;
    
    private final Environment env;
    
    private final OrderBook orderBook;
    
    private final Queue<UpdateOrder> updateOrders;
    
    private final RestService<OrderBook, Trade> restService;
    
    private WebSocketClientImpl webSocketClient;
    
    private CustomInboundHandler handler;
    
    
    @Autowired
    public OrderBookServiceImpl(final RestService<OrderBook, Trade> restService, final Environment env) {
        this.executor = Executors.newFixedThreadPool(POOL_SIZE);
        this.restService = restService;
        this.updateOrders = new ConcurrentLinkedQueue<>();
        this.env = env;
	    this.port = Integer.valueOf(env.getProperty("num.websocket.api.port"));
	    this.url = env.getProperty("url.rest.order.book");
	    this.urlWebSocket = env.getProperty("url.websocket.api");
	    initWebsocket();
	    this.orderBook = restService.getOrderBook(url);
    }
    
    /**
     * Initiate the web-socket communication and injects the Queue responsable
     * for keeping track of the updates.
     */
    private void initWebsocket() {
        try {
            URI uri = new URI(urlWebSocket);
            this.handler = new CustomInboundHandler(uri, updateOrders);
            webSocketClient = new WebSocketClientImpl(uri, this.port, handler);
            this.executor.submit(webSocketClient);
        } catch(URISyntaxException syntaxe) {
        	LOG.error("initWebSocket :", syntaxe);
            throw new IllegalArgumentException(syntaxe);
        }
    }
    
    
    /**
     * Stop the update process and the thread pool associated.
     * 
     */
    @Override
    public void stopProcess() {
        this.webSocketClient.stop();
        executor.shutdown();
    }
    
    /**
     * Process and return the OrderBook keeping it updated with the 
     * update orders received from web-socket channel.]
     * 
     * @param limitInCollections Limit the best bids and the best asks.
     * 
     * @return OrderBook updated.
     */
    @Override
    public OrderBook processOrderBook(Integer limitInCollections) {
        this.orderBook.setLimitInCollections(limitInCollections);
    	UpdateOrder updateOrder;
    	while((updateOrder = updateOrders.poll()) != null) {
        	applyUpdateOrder(updateOrder);
        }
        return this.orderBook;
    }

    /**
     * Apply the {@linkplain com.monitor.trade.model.UpdateOrder} into 
     * {@linkplain com.monitor.trade.model.OrderBook}
     * 
     * @param updates List of {@linkplain com.monitor.trade.model.UpdateOrder}.
     */
    private void applyUpdateOrder(UpdateOrder updates) {
        if(orderBook.getSequence() < updates.getSequence()) {
        	//removing the zero amount Orders.
            updates.getUpdateOrdersDetail()
                    .stream()
                    .filter(u -> (u.getAmount()== null|| u.getAmount().equals(BigDecimal.ZERO)))
                    .forEach(uo -> {
                        final Order order = new Order(uo.getOid(), 
                                                      updates.getBook(),
                                                      uo.getRate(),
                                                      uo.getAmount() == null 
                                                        ?  BigDecimal.ZERO :
                                                           uo.getAmount());
                        if(uo.getKindOperation().equals(UpdateOrder.KindOperation.SELL)) {
                            orderBook.removeOrderFromAsk(order);
                        } else {
                            orderBook.removeOrderFromBids(order);
                        }
                    });
            // treat 
            final List<Order> sellOrders =  
                    updates.getUpdateOrdersDetail()
                        .stream()
                        .filter(u -> u.getAmount() != null && 
                                    !u.getAmount().equals(BigDecimal.ZERO))
                        .filter(uo -> uo.getKindOperation().equals(UpdateOrder.KindOperation.SELL))  
                        .map(uo ->  new Order(uo.getOid(), 
                                              updates.getBook(),
                                              uo.getRate(),
                                              uo.getAmount()))
                        .collect(Collectors.toList());
            final List<Order> buyOrders =
                    updates.getUpdateOrdersDetail()
                        .stream()
                        .filter(u -> u.getAmount() != null && 
                                     !u.getAmount().equals(BigDecimal.ZERO))
                        .filter(uo -> uo.getKindOperation().equals(UpdateOrder.KindOperation.BUY))  
                        .map(uo ->  new Order(uo.getOid(), 
                                              updates.getBook(),
                                              uo.getRate(),
                                              uo.getAmount()))
                        .collect(Collectors.toList());
            orderBook.addAks(sellOrders);
            orderBook.addBids(buyOrders);
            orderBook.setSequence(updates.getSequence());
        } else {
        	updates
        	  .getUpdateOrdersDetail()
              .forEach(updateOrderDetail -> {
            	  orderBook
            	     .removeOrderByOidAndSide(updateOrderDetail.getOid(), 
    			                              updateOrderDetail.getKindOperation().name());
               });
        }
    }
    
}
