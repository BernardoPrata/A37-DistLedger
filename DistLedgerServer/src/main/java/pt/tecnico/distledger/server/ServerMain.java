package pt.tecnico.distledger.server;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.StatusRuntimeException;
import pt.tecnico.distledger.server.domain.ReplicaManager;
import pt.tecnico.distledger.server.domain.ServerState;
import pt.tecnico.distledger.server.grpc.NameService;
import pt.tecnico.distledger.server.service.AdminServiceImpl;
import pt.tecnico.distledger.server.service.DistLedgerCrossServerServiceImpl;
import pt.tecnico.distledger.server.service.UserServiceImpl;

import java.io.IOException;

public class ServerMain {

    private final static String SERVICE_NAME = "DistLedger";
    private final static String LOCALHOST = "localhost";
    public static void main(String[] args) throws IOException, InterruptedException {

        boolean toDebug = false;
        ServerState serverState;
        ReplicaManager replicaManager;

        System.out.println(ServerMain.class.getSimpleName());

        // Receive and print arguments
        System.out.printf("Received %d arguments%n", args.length);
        for (int i = 0; i < args.length; i++) {
            System.out.printf("arg[%d] = %s%n", i, args[i]);
        }

        // Checks arguments
        if (args.length < 1) {
            System.err.println("Argument(s) missing!");
            System.err.printf("Usage: java %s port%n", ServerMain.class.getName());
            return;
        }

        // Converts the arguments
        final int port = Integer.parseInt(args[0]);
        final String qualifier = args[1];
        if (args.length > 2 && args[2].equals("-debug")) {
            toDebug = true;
        }

        // Creates the NameService, ServerState and the services
        NameService nameService = new NameService(LOCALHOST,port);
        try{
            nameService.register(SERVICE_NAME, qualifier);
        } catch (StatusRuntimeException e) {
            System.err.println(e.getStatus().getDescription());
        }
        serverState = new ServerState(toDebug, LOCALHOST + ":" + port, nameService);

        if (qualifier.equals("A")) {
            replicaManager = new ReplicaManager(serverState, 0,toDebug);
        }
        else {
            replicaManager = new ReplicaManager(serverState, 1,toDebug);
        }
        final BindableService userImpl = new UserServiceImpl(replicaManager);
        final BindableService adminImpl = new AdminServiceImpl(serverState, replicaManager);

        // Create a new server to listen on port
        ServerBuilder<?> serverBuilder = ServerBuilder.forPort(port).addService(userImpl).addService(adminImpl);

        // If the server is secondary, adds the cross server service implementation
        final BindableService crossServerImpl = new DistLedgerCrossServerServiceImpl(serverState, replicaManager);
            serverBuilder.addService(crossServerImpl);


        Server server = null;

        try{
            // Builds and starts the server
            server = serverBuilder.build();
            server.start();

            // Server threads are running in the background.
            System.out.println("Server started");

            // Wait until Enter is pressed.
            System.out.println("Press enter to shutdown");
            System.in.read();
            // Remove server from Naming Server
            nameService.delete(SERVICE_NAME);
            // Close channel to Naming Server
            nameService.close();
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
