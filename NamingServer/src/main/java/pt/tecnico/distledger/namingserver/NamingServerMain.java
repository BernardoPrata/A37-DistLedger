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
        if (args.length > 1) {
            System.err.println("A Naming Server doesn't need arguments other than `-debug`!");
            System.err.println("Usage: mvn exec:java <-debug>");
            return;
        }

        int port = 5001;
        toDebug = (args.length == 1 && args[0].equals("-debug")) ? true : false;

        // Creates the ServerState and the services
        namingServerState = new NamingServerState(toDebug);
        final BindableService namingServerImpl = new NamingServerServiceImpl(namingServerState);

        Server server = null;

        try{
            // Create a new server to listen on port
            server = ServerBuilder.forPort(port).addService(namingServerImpl).build();

            // Start the server
            server.start();

            // Server threads are running in the background.
            System.out.println("Server started");

            // Wait until Enter is pressed.
            System.out.println("Press enter to shutdown");
            System.in.read();
            // Shutdown server
            server.shutdown();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (server != null)
                server.shutdown();
        }

    }

}
