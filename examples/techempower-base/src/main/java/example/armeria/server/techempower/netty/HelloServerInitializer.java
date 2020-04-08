package example.armeria.server.techempower.netty;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.flush.FlushConsolidationHandler;

public class HelloServerInitializer extends ChannelInitializer<SocketChannel> {
    private boolean useFlushHandler;

    public HelloServerInitializer(boolean useFlushHandler) {
        this.useFlushHandler = useFlushHandler;
    }

    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        if (useFlushHandler) {
            ch.pipeline().addLast(new FlushConsolidationHandler());
        }

        ch.pipeline()
          .addLast("encoder", new HttpResponseEncoder())
          .addLast("decoder", new HttpRequestDecoder(4096, 8192, 8192, false))
          .addLast("handler", new HelloServerHandler());
    }
}
