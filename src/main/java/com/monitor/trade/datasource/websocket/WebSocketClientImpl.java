package com.monitor.trade.datasource.websocket;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import java.net.URI;
import javax.net.ssl.SSLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;

/**
 * WebSocket client implementation to listening the server represented by
 * URI and port inject by the constructor.
 * 
 * @author phillipe.lemos@gmail.com
 */
public class WebSocketClientImpl implements Runnable {
    
	private static final Logger LOG = LoggerFactory.getLogger(WebSocketClientImpl.class);
	
    private Integer port;

    private SslContext sshContext;

    private EventLoopGroup eventLoopGroup;

    private Bootstrap bootstrap;

    private URI uri;

    private Channel channel;

    private CustomInboundHandler handler;

    public WebSocketClientImpl (final URI uri, 
                                final Integer port, 
                                final CustomInboundHandler handler)  {
        this.port = port;
        this.handler = handler;
        try {
            this.uri = uri;
            this.sshContext = SslContextBuilder
                                .forClient()
                                .trustManager(InsecureTrustManagerFactory.INSTANCE)
                                .build();
                eventLoopGroup = new NioEventLoopGroup();
        } catch(SSLException exception) {
        	LOG.error("WebSocketClientImpl creation :", exception);
            throw new RuntimeException(exception);
        }
    }
    @Override
    public void run() {
        open();
    }

    private void open() {
        bootstrap = new Bootstrap();
        bootstrap.group(this.eventLoopGroup)
                 .channel(NioSocketChannel.class)
                 .handler(new ChannelInitializer<SocketChannel>() {
               @Override
               public void initChannel(SocketChannel socketChannel){
                   ChannelPipeline channelPipeline = socketChannel.pipeline();
                   channelPipeline.addLast(sshContext.newHandler(socketChannel.alloc(),
                                                                uri.getHost(),
                                                                port));
                   channelPipeline
                      .addLast("http-codec", new HttpClientCodec())
                      .addLast("agregator",new HttpObjectAggregator(8192))
                      .addLast("handler", handler);
               }
           });
        try {
          channel = bootstrap
                     .connect(uri.getHost(), port)
                     .sync()
                     .channel();
          this.handler.sync();
          final String inscriptionDiffOrders = 
                "{\"action\":\"subscribe\", \"book\":\"btc_mxn\", \"type\":\""
                        + "diff-orders" + "\" }";
          channel.writeAndFlush(new TextWebSocketFrame(inscriptionDiffOrders));
        } catch(InterruptedException inter) {
        	LOG.error("Subscribe into diff-orders channel :", inter);
            throw new RuntimeException(inter);
        }
    }

    public void stop() {
        if(eventLoopGroup != null) {
            eventLoopGroup.shutdownGracefully();	
        } else {
            throw new IllegalStateException("Websocket not created !");
        }
    }
}
