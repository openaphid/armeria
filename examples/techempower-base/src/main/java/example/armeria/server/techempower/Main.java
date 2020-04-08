package example.armeria.server.techempower;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linecorp.armeria.server.Server;

import example.armeria.server.techempower.armeria.HelloService;
import example.armeria.server.techempower.netty.HelloWebServer;

public final class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    private static void buildArmeriaServer(int port, Map<String, String> arguments) {
        final Server server = Server.builder()
                                    .http(port)
                                    .annotatedService(new HelloService())
                                    .build();

        server.start().join();
    }

    private static void buildNettyServer(int port, Map<String, String> arguments) {
        try {
            final HelloWebServer server = new HelloWebServer(port);

            final boolean useFlushHandler = Objects.equals("1", arguments
                    .getOrDefault("--netty-flush-handler", "1"));
            server.setUseFlushHandler(useFlushHandler);
            logger.info("use FlushConsolidationHandler: {}", useFlushHandler);

            server.run();
        } catch (Exception e) {
            logger.error("Failed to run Netty server", e);
        }
    }

    public static void main(String[] args) {
        final String type = args[0];
        final int port = Integer.parseInt(args[1]);

        final Map<String, String> arguments = new HashMap<>();
        for (int i = 2; i < args.length; i += 2) {
            arguments.put(args[i], args[i + 1]);
        }


        switch (type) {
            case "armeria":
                buildArmeriaServer(port, arguments);
                break;
            case "netty":
                buildNettyServer(port, arguments);
                break;
            default:
                throw new IllegalArgumentException(String.format("unknown type: %s", type));
        }
    }
}
