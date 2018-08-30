#Solution Description


Information flow
---
The Spring context is responsible for manage the JavaFX controllers. After the context is initialized and all beans was created, a web-socket connection is made and the UpdateOrder queue is populated. This process happens in different thread to not block the application. 
	
The initialize method from SonarTradingController is responsible for configure the TableView, LineChart components  and schedulers. The schedulers are started after being initialized.

Class model explanation 
---
Order - represent a bid or an ask in OrderBook.

OrderBook - represents a electronic book with a map of bids and a map of asks.

UpdateOrder - represents an update order to be applied on OrderBook.

Trade - represent a trade.

How do I maintain the Trades and OrderBook states
---
Schedulers are responsible for maintain the order book updated and periodic retrieve trade information from Bitso. 

SonarTradingController.OrderBookScheduleServer is a ScheduledService  responsible for create the tasks that process the queue with UpdateOrders. 
	
This queue is populated by the messages that SimpleChannelInboundHandler receives from web-socket connection into diff-orders channel in wss://ws.bitso.com. The web-socket connection is managed by WebSocketClientImpl class.

SonarTradingController.TradeShedulerServer is a ScheduledService that is responsible for create the tasks that retrieve trade information and apply trade strategy.
	
How to build and run the application
---
##buid
	mvn clean build

##run
	mvn spring-boot:run

Third parties API and framework - motivation
---
Spring boot : Easiest way to create an configuration and testable Java application. The usage of Spring dependency inject to control the JavaFX controllers.

netty-all : From all web-socket implementations that I research, only this one was capable to handle web-socket V08 communications.

Jackson : Easy handle json and  is shipped with Spring.s

JUnit/Mockito - For test purpose and because it comes with Spring boot.


# Checklist

| Feature                  | File name                  | Class name           | Method name
|--------------------------|----------------------------|----------------------|------------------
| Schedule the polling of  |SonarTradingController.java |   inner cllass       |  createTask()
| tradesover REST.	   |                            | TradeShedulerServer  |
|                          |                            |                      |
| Request a book snapshot  | RestServiceImpl.java       | RestServiceImpl      | getOrderBook()
| over REST.               |                            |                      |  
|--------------------------|----------------------------|----------------------|------------------
| Listen for diff-orders   | CustomInBoundHandler.java  | CustomInBoundHandler | channelRead0  
| over websocket.          |                            |                      | 
|--------------------------|----------------------------|----------------------|------------------
| Replay diff-orders.      | WebSocketClientImpl.java   | WebSocketClientImpl  | open
|--------------------------|----------------------------|----------------------|------------------
| Use config option X to   | TradeServiceImpl.java      | TradeServiceImpl     | recentTrades  
| request recent trades.   |                            |                      |
---------------------------|----------------------------|----------------------|------------------
| Use config option X to   | OrderBookServiceImpl.java  | OrderBookService     | processOrderBook
| limit number of ASKs     |                            |                      |
| displayed in UI.         |                            |                      |
|--------------------------|----------------------------|----------------------|------------------
| The loop that causes the | TradeServiceImpl.java(loop)| TradeServiceImpl     | recentTrades
| trading algorithm        | TickCalculator.java        | TickCalculator       | executeTradeStategy 
| to reevaluate.           | (algorithm)                |                      |
|--------------------------|----------------------------|----------------------|------------------
