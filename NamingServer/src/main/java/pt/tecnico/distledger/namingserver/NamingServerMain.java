package pt.tecnico.distledger.namingserver;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import pt.tecnico.distledger.namingserver.NamingServerState;
import pt.tecnico.distledger.namingserver.service.NamingServerServiceImpl;

import java.io.IOException;

public class NamingServerMain {

    public static void main(String[] args) throws IOException, InterruptedException {

        boolean toDebug = false;
        NamingServerState namingServerState;

        System.out.println(NamingServerMain.class.getSimpleName());


        // Receive and print arguments
        System.out.printf("Received %d arguments%n", args.length);
        for (int i = 0; i < args.length; i++) {
            System.out.printf("arg[%d] = %s%n", i, args[i]);
        }

        // Checks arguments
        if (args.length < 1) {
            System.err.println("Argument(s) missing!");
            System.err.printf("Usage: java %s port%n", NamingServerMain.class.getName());
            return;
        }

        // Converts the arguments
        final int port = Integer.parseInt(args[0]);
        if (args.length > 1 && args[1].equals("-debug")) {
            toDebug = true;
        }

        // Creates the ServerState and the services
        namingServerState = new NamingServerState(toDebug);
        final BindableService namingServerImpl = new NamingServerServiceImpl(namingServerState);

        // Create a new server to listen on port
        Server server = ServerBuilder.forPort(port).addService(namingServerImpl).build();

        // Start the server
        server.start();

        // Server threads are running in the background.
        System.out.println("Server started");

        // Do not exit the main thread. Wait until server is terminated.
        server.awaitTermination();

    }

}
