package com.monitor.trade.service;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutorService;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.FieldSetter;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextConfiguration;

import com.monitor.trade.datasource.http.RestService;
import com.monitor.trade.datasource.http.RestServiceImpl;
import com.monitor.trade.datasource.websocket.CustomInboundHandler;
import com.monitor.trade.datasource.websocket.WebSocketClientImpl;
import com.monitor.trade.model.OrderBook;
import com.monitor.trade.model.Trade;
import com.monitor.trade.model.UpdateOrder;
import com.monitor.trade.servce.impl.OrderBookServiceImpl;
import com.monitor.trade.service.mock.OrderBookMock;
import com.monitor.trade.service.mock.UpdateOrderMock;

@RunWith(MockitoJUnitRunner.class)
@ContextConfiguration(classes = {OrderBookServiceImpl.class, RestServiceImpl.class})
public class OrderBookServiceImplTest {

	private static String BOOK = "btc_mxn";
	
	@Mock
	private WebSocketClientImpl webSocketClient;
	
	@Mock
	private CustomInboundHandler handler;
	
	@Mock
	private ExecutorService executor;
	
	@Mock
	private RestService<OrderBook, Trade> restService;
	
	@Mock
	private Environment env;
	
	@Mock
	private Queue<UpdateOrder> updateOrders;
	
	private String port = "443";
	
	private String urlWebSocket = "wss://ws.bitso.com";
	
	private String url = "https://api.bitso.com/v3/order_book/?book=btc_mxn&aggregate=false";
	
	private OrderBookServiceImpl orderBookServiceImpl;
	
	@InjectMocks
	private OrderBook orderBook;
	
	@Before
	public void setUpdate() {
		when(env.getProperty("num.websocket.api.port")).thenReturn(port);
		when(env.getProperty("url.websocket.api")).thenReturn(urlWebSocket);
		when(env.getProperty("url.rest.order.book")).thenReturn(url);
	}
	
	private void comparation(Collection<BigDecimal> prices, Boolean compartionOrder)  {
		final List<BigDecimal> orderListPrices = new ArrayList<>(prices);
		for(int i = 0; i < orderListPrices.size()-1; i++) {
			BigDecimal previous =  orderListPrices.get(i);
			BigDecimal next =  orderListPrices.get(i+1);
			if(compartionOrder) {
				assertThat(next, greaterThan(previous));
			} else {
				assertThat(previous, greaterThan(next));
			}
		}
	}
	
	@Test
	public void processOrderBookSuccess() throws NoSuchFieldException {
		Integer numberOfOrders = 20;
		orderBook  = OrderBookMock.buildOrderBook(numberOfOrders, BOOK);
		when(restService.getOrderBook(url)).thenReturn(orderBook);
		updateOrders = UpdateOrderMock.buildUpdateOrderQueue(BOOK);

		orderBookServiceImpl = new OrderBookServiceImpl(restService, env);
		FieldSetter.setField(orderBookServiceImpl, 
				             orderBookServiceImpl.getClass().getDeclaredField("updateOrders"), 
				             updateOrders);
		OrderBook result = orderBookServiceImpl.processOrderBook(numberOfOrders);
		assertThat(result, notNullValue());
		assertThat(result.getBestAsk(), notNullValue());
		assertThat(result.getBestBid(), notNullValue());
		assertThat(result.getBestAsk(), not(empty()));
		assertThat(result.getBestBid(), not(empty()));
		assertThat(result.getBestBid(), not(empty()));
		assertThat(result.getBestBid().size(),  equalTo(numberOfOrders));
		assertThat(result.getBestAsk().size(),  equalTo(numberOfOrders));
		comparation(result.getBestBid(), Boolean.TRUE);
		comparation(result.getBestAsk(), Boolean.FALSE);
	}

	@Test(expected= IllegalArgumentException.class)
	public void shouldNotAcceptNegativeValuelimitInCollections() {
		Integer numberOfOrders = 20;
		orderBook  = OrderBookMock.buildOrderBook(numberOfOrders, BOOK);
		orderBook.setLimitInCollections(-1);
	}
	
	@Test(expected= IllegalArgumentException.class)
	public void shouldNotAcceptNullValuelimitInCollections() {
		orderBook  = OrderBookMock.buildOrderBook(20, BOOK);
		orderBook.setLimitInCollections(null);
	}

	@Test(expected= IllegalArgumentException.class)
	public void shouldNotAcceptZeroValuelimitInCollections() {
		orderBook  = OrderBookMock.buildOrderBook(20, BOOK);
		orderBook.setLimitInCollections(0);
	}
	
	@Test
	public void shouldChangeTheLimitFromBidsAndAsks() throws NoSuchFieldException {
		Integer numberOfOrders = 200;
		orderBook  = OrderBookMock.buildOrderBook(numberOfOrders, BOOK);
		when(restService.getOrderBook(url)).thenReturn(orderBook);
		orderBookServiceImpl = new OrderBookServiceImpl(restService, env);
		updateOrders = UpdateOrderMock.buildUpdateOrderQueue(BOOK);
		FieldSetter.setField(orderBookServiceImpl, 
	                         orderBookServiceImpl.getClass().getDeclaredField("updateOrders"), 
	                         updateOrders);
		orderBookServiceImpl = new OrderBookServiceImpl(restService, env);
		OrderBook result = orderBookServiceImpl.processOrderBook(numberOfOrders);
		assertThat(result, notNullValue());
		assertThat(result.getBestAsk(), notNullValue());
		assertThat(result.getBestBid(), notNullValue());
		assertThat(result.getBestAsk(), not(empty()));
		assertThat(result.getBestBid(), not(empty()));
		assertThat(result.getBestBid(), not(empty()));
		assertThat(result.getBestBid().size(),  equalTo(numberOfOrders));
		assertThat(result.getBestAsk().size(),  equalTo(numberOfOrders));
		comparation(result.getBestBid(), Boolean.TRUE);
		comparation(result.getBestAsk(), Boolean.FALSE);
		orderBookServiceImpl = new OrderBookServiceImpl(restService, env);
		updateOrders = UpdateOrderMock.buildUpdateOrderQueue(BOOK);
		FieldSetter.setField(orderBookServiceImpl, 
                             orderBookServiceImpl.getClass().getDeclaredField("updateOrders"), 
                             updateOrders);
		numberOfOrders = 20;
		result = orderBookServiceImpl.processOrderBook(numberOfOrders);
		assertThat(result, notNullValue());
		assertThat(result.getBestAsk(), notNullValue());
		assertThat(result.getBestBid(), notNullValue());
		assertThat(result.getBestAsk(), not(empty()));
		assertThat(result.getBestBid(), not(empty()));
		assertThat(result.getBestBid(), not(empty()));
		assertThat(result.getBestBid().size(),  equalTo(numberOfOrders));
		assertThat(result.getBestAsk().size(),  equalTo(numberOfOrders));
		comparation(result.getBestBid(), Boolean.TRUE);
		comparation(result.getBestAsk(), Boolean.FALSE);
	}

}
