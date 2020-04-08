package example.armeria.server.techempower;

import com.linecorp.armeria.server.Server;

import example.armeria.server.techempower.armeria.HelloService;
import example.armeria.server.techempower.netty.HelloWebServer;

public class Main {
    private static void buildArmeriaServer(int port) {
        final Server server = Server.builder()
                                    .http(port)
                                    .annotatedService(new HelloService())
                                    .build();

        server.start().join();
    }

    private static void buildNettyServer(int port) {
        try {
            new HelloWebServer(port).run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        final String type = args[0];
        final int port = Integer.parseInt(args[1]);

        switch (type) {
            case "armeria":
                buildArmeriaServer(port);
                break;
            case "netty":
                buildNettyServer(port);
                break;
            default:
                throw new IllegalArgumentException(String.format("unknown type: %s", type));
        }
    }
}
