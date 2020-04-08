package example.armeria.server.techempower.netty;

import java.net.InetSocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linecorp.armeria.common.util.SystemInfo;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.kqueue.KQueue;
import io.netty.channel.kqueue.KQueueEventLoopGroup;
import io.netty.channel.kqueue.KQueueServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.ResourceLeakDetector;
import io.netty.util.ResourceLeakDetector.Level;

public class HelloWebServer {
    private static final Logger logger = LoggerFactory.getLogger(HelloWebServer.class);

    static {
        ResourceLeakDetector.setLevel(Level.DISABLED);
    }

    private final int port;

    public HelloWebServer(int port) {
        this.port = port;
    }

    public void run() throws Exception {
        // Configure the server.
        IoMultiplexer multiplexer = IoMultiplexer.JDK;
        switch (SystemInfo.osType()) {
            case LINUX:
                if (Epoll.isAvailable()) {
                    multiplexer = IoMultiplexer.EPOLL;
                }
                break;
            case MAC:
                if (KQueue.isAvailable()) {
                    multiplexer = IoMultiplexer.KQUEUE;
                }
                break;
            default:
                break;
        }

        logger.info("using io multiplexer: {}", multiplexer);

        switch (multiplexer) {
            case EPOLL:
                doRun(new EpollEventLoopGroup(), EpollServerSocketChannel.class, IoMultiplexer.EPOLL);
                break;
            case KQUEUE:
                doRun(new KQueueEventLoopGroup(), KQueueServerSocketChannel.class, IoMultiplexer.KQUEUE);
                break;
            case JDK:
                doRun(new NioEventLoopGroup(), NioServerSocketChannel.class, IoMultiplexer.JDK);
                break;
            default:
                break;
        }
    }

    private void doRun(EventLoopGroup loupGroup, Class<? extends ServerChannel> serverChannelClass,
                       IoMultiplexer multiplexer) throws InterruptedException {
        try {
            final InetSocketAddress inet = new InetSocketAddress(port);

            final ServerBootstrap b = new ServerBootstrap();

            if (multiplexer == IoMultiplexer.EPOLL) {
                b.option(EpollChannelOption.SO_REUSEPORT, true);
            }

            b.option(ChannelOption.SO_BACKLOG, 8192);
            b.option(ChannelOption.SO_REUSEADDR, true);
            b.group(loupGroup).channel(serverChannelClass).childHandler(
                    new HelloServerInitializer(loupGroup.next()));
            b.childOption(ChannelOption.SO_REUSEADDR, true);

            final Channel ch = b.bind(inet).sync().channel();

            logger.info("Httpd started. Listening on: {}", inet);

            ch.closeFuture().sync();
        } finally {
            loupGroup.shutdownGracefully().sync();
        }
    }
}
