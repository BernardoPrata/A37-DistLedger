package pt.tecnico.distledger.adminclient;

import pt.tecnico.distledger.adminclient.grpc.AdminService;

public class AdminClientMain {
    public static void main(String[] args) {

        System.out.println(AdminClientMain.class.getSimpleName());

        // receive and print arguments
        System.out.printf("Received %d arguments%n", args.length);
        for (int i = 0; i < args.length; i++) {
            System.out.printf("arg[%d] = %s%n", i, args[i]);
        }

        // check arguments
        if (args.length != 2) {
            System.err.println("Argument(s) missing!");
            System.err.println("Usage: mvn exec:java -Dexec.args=<host> <port> (-debug)");
            return;
        }

        final String host = args[0];
        final int port = Integer.parseInt(args[1]);
        final boolean debugFlag = Boolean.parseBoolean(args[2]);

        // TODO: add debug flag to CommandParser or AdminService
        CommandParser parser = new CommandParser(new AdminService(host, port));
        parser.parseInput();

    }
}
