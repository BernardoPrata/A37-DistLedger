package pt.tecnico.distledger.userclient;


import pt.tecnico.distledger.userclient.grpc.NamingServerService;

public class UserClientMain {
    public static void main(String[] args) {

        System.out.println(UserClientMain.class.getSimpleName());

        // receive and print arguments
        System.out.printf("Received %d arguments%n", args.length);
        for (int i = 0; i < args.length; i++) {
            System.out.printf("arg[%d] = %s%n", i, args[i]);
        }

        // check arguments
        if (args.length > 1) {
            System.err.println("A client doesn't need arguments other than `-debug`!");
            System.err.println("Usage: mvn exec:java <-debug>");
            return;
        }

        // the host and port to connect to the Naming Server
        final String host = "localhost";
        final int port = 5001;
        final boolean toDebug = (args.length == 1 && args[0].equals("-debug")) ? true : false;
        CommandParser parser = new CommandParser(new NamingServerService(host,port),toDebug);
        parser.parseInput();
    }
}
