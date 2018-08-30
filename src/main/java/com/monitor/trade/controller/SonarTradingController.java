package com.monitor.trade.controller;

import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.monitor.trade.controller.datamodel.TradeModel;
import com.monitor.trade.controller.mapper.AskBid2ObservableAskBidMapper;
import com.monitor.trade.controller.mapper.Trade2ObservableTradeModelMapper;
import com.monitor.trade.model.OrderBook;
import com.monitor.trade.model.Trade;
import com.monitor.trade.servce.OrderBookService;
import com.monitor.trade.servce.TradeService;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Duration;
import javafx.util.converter.CurrencyStringConverter;

/**
 * Controller responsible for manage the view part of this application.
 * 
 * @author phillipe.lemos@gmail.com
 */
@Component
public class SonarTradingController implements Initializable {
	
	private static final Logger LOG = LoggerFactory.getLogger(SonarTradingController.class);
    
    private static final String SERIE_BIDS_NAME = "serie.bids.name";
    
    private static final String SERIE_ASKS_NAME = "serie.asks.name";
    
    @Value("${number.most.recents.trades}")
    private Integer defaultNumberMostRecentTrades;
    
    private Integer numberMostRecentTrades;

    @Value("${number.max.consecutive.up.stick}")
    private Integer defaultMaxConsecutiveUpStick;
    
    @Value("${default.number.bids.asks}")
    private Integer defaultNumBidsAsks;

    @Value("${number.max.consecutive.down.stick}")
    private Integer defaultMaxConsecutiveDownStick; 

    private Integer maxConsecutiveUpStick;

    private Integer maxConsecutiveDownStick;
    
    private Integer numBidsAsksVisible;
    
    @FXML
    private TextField txtNumAskBid;
    
    @FXML
    private TextField txtNumTrades;
    
    @FXML
    private TextField txtUpperTickt;
    
    @FXML
    private TextField txtDownTick;
    
    @FXML
    private LineChart<Integer, BigDecimal> grAsks;
    
    @FXML
    private LineChart<Integer, BigDecimal> grBids;

    @FXML
    private NumberAxis yAxisAsks;
            
    @FXML        
    private NumberAxis xAxisAsks;
    
    @FXML
    private NumberAxis yAxisBids;
            
    @FXML
    private NumberAxis xAxisBids;
    
    @FXML
    private TableView<TradeModel> tvSell;
    
    @FXML
    private TableView<TradeModel> tvBuy;
    
    @FXML
    private TableColumn<TradeModel, Long> tcIdSell;
    
    @FXML
    private TableColumn<TradeModel, LocalDateTime> tcCreatedSell;
    
    @FXML
    private TableColumn<TradeModel, BigDecimal> tcPriceSell;
    
    @FXML
    private TableColumn<TradeModel, BigDecimal> tcAmountSell;
    
    @FXML
    private TableColumn<TradeModel, Long> tcIdBuy;
    
    @FXML
    private TableColumn<TradeModel, LocalDateTime> tcCreatedBuy;
    
    @FXML
    private TableColumn<TradeModel, BigDecimal> tcPriceBuy;
    
    @FXML
    private TableColumn<TradeModel, BigDecimal> tcAmountBuy;
    
    private ScheduledService<OrderBook> orderBookShedule;
    
    private ScheduledService<List<Trade>> tradeShedule;

    private final OrderBookService orderBookService;

    private final TradeService tradeService;
    
    private final Comparator<TradeModel> createdComparator = (r1,r2) -> {
        return r2.getCreatedOrigim().compareTo(r1.getCreatedOrigim());
    };


    @Autowired
    public SonarTradingController(final OrderBookService orderBookService, 
                                  final TradeService tradeService) {
        this.orderBookService = orderBookService;
        this.tradeService = tradeService;
    }
    
    /**
     * Generic dialog to prompt error message.
     * 
     * @param textField  TextField where the value was introduced.
     * 
     * @param numberToReset Variable to be reset.
     * 
     * @param defaultValue Default value. 
     */
    private void showErroMessageConversion(TextField textField, 
                                           Integer numberToReset,
                                           Integer defaultValue) {
            final Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("You must inform a number greater than zero.");
            alert.setTitle("Convertion error");
            alert.showAndWait()
                    .filter(resp -> resp == ButtonType.OK)
                    .ifPresent(resp -> {
                        textField.clear();
                        textField.setText(defaultValue.toString());
                    });
            numberToReset = defaultValue;
    }
    
    @FXML
    private void handleBtnBidsAsksAction(ActionEvent event) {
        try {
            Integer validate = Integer.parseInt(txtNumAskBid.getText());
            if(validate <= 0) {
                showErroMessageConversion(this.txtNumAskBid, 
                                          this.numBidsAsksVisible, 
                                          this.defaultNumBidsAsks);
            } else {
            	this.numBidsAsksVisible = validate;
            	LOG.info(">>> New bid and ask limit :" + validate);
            	((OrderBookScheduleServer)orderBookShedule).setNumBidsAsksVisible(this.numBidsAsksVisible);
            }
        } catch(NumberFormatException numFormatException) {
            showErroMessageConversion(this.txtNumAskBid, 
                                      this.numBidsAsksVisible, 
                                      this.defaultNumBidsAsks);
        }
        
    }
    
    /**
     * Handle the click event from btnConfirm button.
     * 
     * @param event The event action.
     */
    @FXML
    private void handleBtnConfirmAction(ActionEvent event) {
        try {
        	Integer validate =  Integer.parseInt(txtNumTrades.getText());
        	if(validate <= 0) {
                showErroMessageConversion(this.txtNumTrades, 
                                          this.numberMostRecentTrades, 
                                          this.defaultNumberMostRecentTrades);
        	} else {
        		LOG.info(">>> New numberMostRecentTrades limit :" + validate);
        		this.numberMostRecentTrades = validate;
        	}
        } catch(NumberFormatException numFormatException) {
            showErroMessageConversion(this.txtNumTrades, 
                                      this.numberMostRecentTrades, 
                                      this.defaultNumberMostRecentTrades);
        }

        try {
        	Integer validate = Integer.parseInt(txtUpperTickt.getText());
        	if(validate <= 0) {
                showErroMessageConversion(this.txtUpperTickt, 
                                          this.maxConsecutiveUpStick, 
                                          this.defaultMaxConsecutiveUpStick);
        	} else {
        		LOG.info(">>> New maxConsecutiveUpStick limit :" + validate);
        		this.maxConsecutiveUpStick = validate;	
        	}
        } catch(NumberFormatException numFormatException) {
            showErroMessageConversion(this.txtUpperTickt, 
                                      this.maxConsecutiveUpStick, 
                                      this.defaultMaxConsecutiveUpStick);
        }
        
        try {
        	Integer validate = Integer.parseInt(txtUpperTickt.getText());
        	if(validate <= 0) {
                showErroMessageConversion(this.txtDownTick, 
                                          this.maxConsecutiveDownStick, 
                                          this.defaultMaxConsecutiveDownStick);
        	} else {
        		LOG.info(">>> New maxConsecutiveDownStick limit :" + validate);
        		this.maxConsecutiveDownStick = validate;	
        	}
            
        } catch(NumberFormatException numFormatException) {
            showErroMessageConversion(this.txtDownTick, 
                                      this.maxConsecutiveDownStick, 
                                      this.defaultMaxConsecutiveDownStick);
        }
        final TradeShedulerServer tradeScheduleServer = (TradeShedulerServer)tradeShedule;
        tradeScheduleServer.setMaxConsecutiveDownStick(maxConsecutiveDownStick);
        tradeScheduleServer.setMaxConsecutiveUpStick(maxConsecutiveUpStick);
        tradeScheduleServer.setNumberMostRecentTrades(numberMostRecentTrades);
    }
    
    /**
     * Configure the chart and corresponding axis. 
     * 
     * @param rb     ResourceBundle 
     * @param xAxis  X axis 
     * @param yAxis  Y axis
     * @param serieName Serie name
     * @param grid    Line chart
     */
    private void configureGrd(ResourceBundle rb, 
                              NumberAxis xAxis, 
                              NumberAxis yAxis,
                              String serieName,
                              String color,
                              LineChart<Integer, BigDecimal> grid) {
        yAxis.setForceZeroInRange(Boolean.FALSE);
        yAxis.setMinorTickVisible(Boolean.FALSE);
        yAxis.setAutoRanging(Boolean.TRUE);
        yAxis.setTickLabelFormatter(new CurrencyStringConverter(Locale.getDefault()));
        
        xAxis.setForceZeroInRange(Boolean.FALSE);
        xAxis.setAutoRanging(Boolean.TRUE);
        xAxis.setMinorTickVisible(Boolean.TRUE);

        grid.setCreateSymbols(Boolean.FALSE);
        grid.setLegendVisible(Boolean.FALSE);
        grid.setAnimated(Boolean.FALSE);
        final XYChart.Series<Integer, BigDecimal> serie 
             = new XYChart.Series<Integer, BigDecimal>(rb.getString(serieName), FXCollections.observableArrayList());
        grid.getData().add(serie);
        final Node line = serie.getNode().lookup(".chart-series-line");
        line.setStyle("-fx-stroke: " + color);
    }
    
    /**
     * Configure each TableColumn from the TableView, setting his cellValueFactory.
     * 
     * @param tableView TableView to be configured.
     * @param columnIds 
     */
    private void configureTables(TableView<TradeModel> tableView, 
                                 Map<String, String> columnIds) {
        columnIds.forEach((id, name) -> {
            tableView.getColumns()
                    .stream()
                    .filter(column -> column.getId().equals(id))
                    .findFirst()
                    .ifPresent(column ->  {
                        column.setCellValueFactory(new PropertyValueFactory(name));
                        column.setCellFactory(c -> {
                            return new TableCell() {
                                @Override
                                protected void updateItem(Object item, boolean empty) {
                                    if(item == null || empty) {
                                        setText("");
                                        setStyle(""); 
                                    } else {
                                        if(item instanceof SimpleObjectProperty) {
                                        	SimpleObjectProperty itemB = (SimpleObjectProperty)item;
                                        	if(itemB.getValue() != null && itemB.getValue().equals(BigDecimal.ONE) ) {
                                                getTableRow().setStyle("-fx-background-color:lightcoral");
                                            } 
                                        }
                                        setText(item.toString());
                                    }
                                }
                            };
                        });
                    });
        });
        tableView.setSortPolicy(t -> {
            FXCollections.sort(tableView.getItems(), createdComparator);
            return Boolean.TRUE;
        });
    }

    /**
     * This method is called by FXMLLoader after its root element has been 
     * completely processed.
     * 
     * @param location the url from the resource.
     * @param rb ResoourceBundle used to translate the titles in UI.
     * 
     */
    @Override
    public void initialize(URL location, ResourceBundle rb) {
        this.numBidsAsksVisible = this.defaultNumBidsAsks;
        this.numberMostRecentTrades = this.defaultNumberMostRecentTrades;
        this.maxConsecutiveDownStick = this.defaultMaxConsecutiveDownStick;
        this.maxConsecutiveUpStick  = this.defaultMaxConsecutiveUpStick;
        
        configureGrd(rb, 
                     this.xAxisAsks, 
                     this.yAxisAsks, 
                     SERIE_ASKS_NAME,
                     "#db4848",
                     grAsks);
        configureGrd(rb, 
                     this.xAxisBids, 
                     this.yAxisBids, 
                     SERIE_BIDS_NAME,
                     "#529e68",
                     grBids);
        
        final Map<String, String> mapColumnsSell = new HashMap<>();
        mapColumnsSell.put(tcIdSell.getId(), "tradeId");
        mapColumnsSell.put(tcCreatedSell.getId(), "created");
        mapColumnsSell.put(tcPriceSell.getId(), "price");
        mapColumnsSell.put(tcAmountSell.getId(), "amount");
        configureTables(tvSell, mapColumnsSell);
        
        final Map<String, String> mapColumnsBuy = new HashMap<>();
        mapColumnsBuy.put(tcIdBuy.getId(), "tradeId");
        mapColumnsBuy.put(tcCreatedBuy.getId(), "created");
        mapColumnsBuy.put(tcPriceBuy.getId(), "price");
        mapColumnsBuy.put(tcAmountBuy.getId(), "amount");
        configureTables(tvBuy,mapColumnsBuy);

        configureSheduleOrderBook();
        configureSheduleTrades();
    }
    
    /**
     * Configure the ScheduleServer for the trades
     */
    @SuppressWarnings("unchecked")
	private void configureSheduleTrades() {
        tradeShedule = new TradeShedulerServer(tradeService,
                                               this.maxConsecutiveUpStick,
                                               this.maxConsecutiveDownStick);
        
        tradeShedule.setOnFailed((WorkerStateEvent  event) -> {
            if(event.getEventType().equals(WorkerStateEvent.WORKER_STATE_FAILED)){
            	LOG.error("TradeShedulerServer ", event.getSource().getException());
            }
        });
        
        tradeShedule.setOnSucceeded((WorkerStateEvent  event) -> {
            if(event.getEventType().equals(WorkerStateEvent.WORKER_STATE_SUCCEEDED)){
                Object obj = event.getSource().getValue();
                if(obj != null) {
                   final List<Trade> recentTrades = (List<Trade>)obj;
                   Trade2ObservableTradeModelMapper.from(recentTrades, 
                                                         tvSell.getItems(),
                                                         numberMostRecentTrades,
                                                         Trade.MarketSide.SELL,
                                                         createdComparator);
                   Trade2ObservableTradeModelMapper.from(recentTrades, 
                                                          tvBuy.getItems(),
                                                          numberMostRecentTrades,
                                                          Trade.MarketSide.BUY,
                                                          createdComparator);
                   //recentTrades.clear();
                }
            }
        });
        tradeShedule.start();
    }
    
    /**
     * Configure the schedule server for the OrderBook.
     */
    private void configureSheduleOrderBook() {
        orderBookShedule = new OrderBookScheduleServer(orderBookService, this.defaultNumBidsAsks);

        orderBookShedule.setOnFailed((WorkerStateEvent  event) -> {
            if(event.getEventType().equals(WorkerStateEvent.WORKER_STATE_FAILED)){
                LOG.error("OrderBookScheduler ", event.getSource().getException());
            }            
        });
        
        orderBookShedule.setOnSucceeded((WorkerStateEvent  event) -> {
            if(event.getEventType().equals(WorkerStateEvent.WORKER_STATE_SUCCEEDED)){
                Object obj = event.getSource().getValue();
                if(obj != null) {
                    final OrderBook orderBook = (OrderBook)obj;
                    //defining a series
                    AskBid2ObservableAskBidMapper.from(grBids.getData().get(0), 
                                                       orderBook.getBestBid(),
                                                       numBidsAsksVisible);
                    AskBid2ObservableAskBidMapper.from(grAsks.getData().get(0), 
                                                       orderBook.getBestAsk(),
                                                       numBidsAsksVisible);
                }
            }
        });
        orderBookShedule.start();
    }
    
    /** 
     * This scheduler is responsible for create 
     * {@linkplain com.monitor.trade.controller.SonarTradingController.OrderBookTask} and pull 
     * {@linkplain com.monitor.trade.model.UpdateOrder} from web-socket channel. 
     *  
     * @author phillipe.lemos@gmail.com.lemos@gmail.com
     *
     */
    private static class OrderBookScheduleServer extends ScheduledService<OrderBook> {

        private final OrderBookService orderBookService;
        
        private Integer numBidsAsksVisible;
        
        public OrderBookScheduleServer(final OrderBookService orderBookService, Integer numBidsAsksVisible) {
            this.orderBookService = orderBookService;
            this.setPeriod(Duration.seconds(1D));
            this.numBidsAsksVisible = numBidsAsksVisible;
        }
        
        @Override
        public Task<OrderBook> createTask() {
            return new OrderBookTask(orderBookService, numBidsAsksVisible);
        }

		public void setNumBidsAsksVisible(Integer numBidsAsksVisible) {
			this.numBidsAsksVisible = numBidsAsksVisible;
		}
        
    }
    
    /**
     * Consume the {@linkplain com.monitor.trade.model.UpdateOrder} from web-socket chain and update 
     * {@linkplain com.monitor.trade.model.OrderBook}.
     *   
     * @author phillipe.lemos@gmail.com
     *
     */
    private static class OrderBookTask extends Task<OrderBook> {

        private final OrderBookService orderBookSerive;
        
        private final Integer numBidsAsksVisible;

        public OrderBookTask(final OrderBookService orderBookSerive, final Integer numBidsAsksVisible) {
            this.orderBookSerive = orderBookSerive;
            this.numBidsAsksVisible = numBidsAsksVisible;
        }

        @Override
        public OrderBook call() throws Exception {
            return this.orderBookSerive.processOrderBook(numBidsAsksVisible);
        }   
    }
    
    /**
     * This scheduler is responsible for create 
     * {@linkplain com.monitor.trade.controller.SonarTradingController.TradeTask } and pull 
     * trade information from {@link https://api.bitso.com/v3/trades/?book=btc_mxn}. 
     *  
     * @author phillipe.lemos@gmail.com.lemos@gmail.com
     *
     */
    private static class TradeShedulerServer extends ScheduledService<List<Trade>> {
        
        private final TradeService tradeServie;

        private Integer numberMostRecentTrades;
        
        private Integer maxConsecutiveUpStick;
        
        private Integer maxConsecutiveDownStick; 
        
        public TradeShedulerServer(final TradeService tradeServie,
                                   Integer maxConsecutiveUpStick,
                                   Integer maxConsecutiveDownStick) {
            this.tradeServie = tradeServie;
            this.maxConsecutiveDownStick = maxConsecutiveDownStick;
            this.maxConsecutiveUpStick = maxConsecutiveUpStick;
            this.setPeriod(Duration.seconds(2D));
        }
        
        public void setNumberMostRecentTrades(Integer numberMostRecentTrades) {
            this.numberMostRecentTrades = numberMostRecentTrades;
        }

        public void setMaxConsecutiveUpStick(Integer maxConsecutiveUpStick) {
            this.maxConsecutiveUpStick = maxConsecutiveUpStick;
        }

        public void setMaxConsecutiveDownStick(Integer maxConsecutiveDownStick) {
            this.maxConsecutiveDownStick = maxConsecutiveDownStick;
        }
        
        @Override
        public Task<List<Trade>> createTask() {
            return new TradeTask(this.tradeServie,
                                 this.maxConsecutiveUpStick,
                                 this.maxConsecutiveDownStick);
        }
    }
    
    /**
     * Retrieves trades from {@link https://api.bitso.com/v3/trades/?book=btc_mxn} and execute 
     * a trade strategy describe in {@linkplain com.monitor.trade.servce.impl.TickCalculator}.
     * This strategy do not trade, instated create a virtual trade that could be shown in table
     * below the charts.
     * 
     * @author phillipe.lemos@gmail.com
     *
     */
    private static class TradeTask extends Task<List<Trade>> {
        
        private final TradeService tradeService;
        
        private final Integer maxConsecutiveUpStick;
        
        private final Integer maxConsecutiveDownStick; 
        
        public TradeTask(final TradeService tradeService,
                         final Integer maxConsecutiveUpStick,
                         final Integer maxConsecutiveDownStick) {
            this.tradeService = tradeService;
            this.maxConsecutiveUpStick = maxConsecutiveUpStick;
            this.maxConsecutiveDownStick = maxConsecutiveDownStick;
        }
        @Override
        public List<Trade> call() throws Exception {
            return this.tradeService.recentTrades(this.maxConsecutiveUpStick, 
            		                              this.maxConsecutiveDownStick);
        }
        
    }
    
}
