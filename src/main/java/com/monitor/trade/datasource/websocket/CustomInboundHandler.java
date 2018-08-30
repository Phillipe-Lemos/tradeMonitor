package com.monitor.trade.datasource.websocket;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.monitor.trade.model.UpdateOrder;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import java.util.Queue;

/**
 * A SimpleChannelInboundHandler to retrieve message from websocket channel and put this
 * messages into a Queue.
 * 
 * @author phillipe.lemos@gmail.com
 *
 */
public class CustomInboundHandler extends SimpleChannelInboundHandler<WebSocketFrame> {
	
	private static final Logger LOG = LoggerFactory.getLogger(CustomInboundHandler.class);
			
    private final WebSocketClientHandshaker handshaker;
	     
    private ChannelPromise handshakeFuture;

    private ChannelHandlerContext ctx;

    private final ObjectMapper objectMapper;

    private final Queue<UpdateOrder> messages;

    /**
     * Create an instance of NettyWebHandler
     * 
     * @param uri       - URI that points to Websocket server. 
     * @param messages  - Data sturcuture to retrieve information from listening
     *                    channel.
     */
    public CustomInboundHandler(final URI uri, final Queue<UpdateOrder> messages) {
        super(WebSocketFrame.class,Boolean.TRUE);
        this.messages = messages;
        this.handshaker =  
                WebSocketClientHandshakerFactory
                          .newHandshaker(uri, 
                                        WebSocketVersion.V08, 
                                        null, 
                                        false,
                                        new DefaultHttpHeaders());
        objectMapper = 
            Jackson2ObjectMapperBuilder
                    .json()
                    .annotationIntrospector(new JacksonAnnotationIntrospector())
                    .autoDetectFields(Boolean.TRUE)
                    .factory(new JsonFactory())
                    .build();
    }       
    
    /**
     * Sync the ChannelHandlerContext promisse.
     * 
     * @throws InterruptedException 
     */    
    public void sync() throws InterruptedException {
        this.handshakeFuture.sync();
    }

    /**
     * This method is call when the handler is added into Websocket client
     * object. 
     * The ChannelContextHandler is keeps to be used latter.
     * 
     * @param ctx  
     *     - Channel handler context that give access to the ChannelPipeline.
     * @throws Exception 
     */
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
            this.handshakeFuture = ctx.newPromise();
            this.ctx = ctx;
            ctx.fireChannelActive();
    }

    /**
     * This method is called after the channel is success activated.
     * 
     * @param ctx 
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx){
        handshaker.handshake(ctx.channel());
    }

    @Override
    public boolean acceptInboundMessage(Object msg) throws java.lang.Exception {
        if(!handshaker.isHandshakeComplete() && msg instanceof FullHttpResponse) {
            handshaker.finishHandshake(ctx.channel(), (FullHttpResponse)msg);
            handshakeFuture.setSuccess();
            return Boolean.FALSE;
        } 

        if(msg instanceof CloseWebSocketFrame) {
            return Boolean.TRUE;
        }

        if(msg instanceof WebSocketFrame) {
            return Boolean.TRUE;
        }

        return Boolean.FALSE;
    }


    @Override
    public void channelRead0(ChannelHandlerContext ctx, WebSocketFrame msg) throws Exception {
        if(msg instanceof TextWebSocketFrame) {
            String message = ((TextWebSocketFrame)msg).text();
            if(message.contains("sequence") && message.contains("open")) {
                final UpdateOrder mess = objectMapper.readerFor(UpdateOrder.class).readValue(message);
                messages.add(mess);
            } 
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (!handshakeFuture.isDone()) {
        	LOG.error("exceptionCaught : ", cause);
            handshakeFuture.setFailure(cause);
        }
        ctx.close();
    }
}	
