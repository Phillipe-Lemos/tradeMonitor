package com.monitor.trade.service.mock;

import static com.monitor.trade.model.UpdateOrder.createUpdateOrder;
import static com.monitor.trade.model.UpdateOrder.UpdateOrderDetail.createUpdateOrderDetail;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

import com.monitor.trade.model.UpdateOrder;
import com.monitor.trade.model.UpdateOrder.KindOperation;
import com.monitor.trade.model.UpdateOrder.UpdateOrderDetail;

public class UpdateOrderMock {
	
	private static String TYPE = "diff-orders";
	
	public static Queue<UpdateOrder> buildUpdateOrderQueue(final String book) {
		final Random price   = new Random(Integer.MAX_VALUE);
		final Random amount  = new Random(Integer.MAX_VALUE);
		final Queue<UpdateOrder> result = new LinkedList<>(); 
		
		
		Collection<UpdateOrderDetail> details 
		   = Arrays.asList(
				   createUpdateOrderDetail(System.currentTimeMillis(),
		                                   BigDecimal.valueOf(price.nextDouble()),
				                           KindOperation.BUY,
				                           BigDecimal.valueOf(amount.nextDouble()),
				                           BigDecimal.valueOf(price.nextDouble()),
				                           "1"),
				   createUpdateOrderDetail(System.currentTimeMillis(),
				                           BigDecimal.valueOf(price.nextDouble()),
				                           KindOperation.BUY,
				                           BigDecimal.valueOf(amount.nextDouble()),
				                           BigDecimal.valueOf(price.nextDouble()),
				                           "2"),
				   
				   createUpdateOrderDetail(System.currentTimeMillis(),
				                           BigDecimal.valueOf(price.nextDouble()),
				                           KindOperation.BUY,
				                           BigDecimal.valueOf(amount.nextDouble()),
				                           BigDecimal.valueOf(price.nextDouble()),
				                           "3"),
							   
				   createUpdateOrderDetail(System.currentTimeMillis(),
				                           BigDecimal.valueOf(price.nextDouble()),
				                           KindOperation.SELL,
				                           BigDecimal.valueOf(amount.nextDouble()),
				                           BigDecimal.valueOf(price.nextDouble()),
				                           "4"),
				   
				   createUpdateOrderDetail(System.currentTimeMillis(),
				                           BigDecimal.valueOf(price.nextDouble()),
				                           KindOperation.SELL,
				                           BigDecimal.valueOf(amount.nextDouble()),
				                           BigDecimal.valueOf(price.nextDouble()),
				                           "5"),
				   createUpdateOrderDetail(System.currentTimeMillis(),
				                           BigDecimal.valueOf(price.nextDouble()),
				                           KindOperation.SELL,
				                           BigDecimal.valueOf(amount.nextDouble()),
				                           BigDecimal.valueOf(price.nextDouble()),
				                           "6")
				   );
		result.add(createUpdateOrder(2L,book, TYPE, details));		
        return result; 
	}

}
