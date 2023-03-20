package pt.tecnico.distledger.userclient;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import pt.tecnico.distledger.userclient.grpc.UserService;
import pt.tecnico.distledger.userclient.grpc.NamingServerService;

import java.util.List;
import java.util.Scanner;
public class CommandParser {

    private static final String SPACE = " ";
    private static final String OK = "OK";
    private static final String CREATE_ACCOUNT = "createAccount";
    private static final String DELETE_ACCOUNT = "deleteAccount";
    private static final String TRANSFER_TO = "transferTo";
    private static final String BALANCE = "balance";
    private static final String HELP = "help";
    private static final String EXIT = "exit";

    private final NamingServerService namingServerService;

    private final UserService userService;

    private final boolean toDebug;
    public CommandParser(NamingServerService namingServerService, boolean toDebug) {
        this.namingServerService = namingServerService;
        this.userService = new UserService();
        this.toDebug = toDebug;
    }

    public void debug(String debugMessage){
        if (this.toDebug)
            System.err.println(debugMessage);
    }

    void parseInput() {

        Scanner scanner = new Scanner(System.in);
        boolean exit = false;

        while (!exit) {
            System.out.print("> ");
            String line = scanner.nextLine().trim();
            String cmd = line.split(SPACE)[0];
            String qualifier = line.split(SPACE)[1];

            try{
                switch (cmd) {
                    case CREATE_ACCOUNT:
                        lookup(cmd, qualifier);
                        this.createAccount(line);
                        break;

                    case DELETE_ACCOUNT:
                        lookup(cmd, qualifier);
                        this.deleteAccount(line);
                        break;

                    case TRANSFER_TO:
                        lookup(cmd, qualifier);
                        this.transferTo(line);
                        break;

                    case BALANCE:
                        lookup(cmd, qualifier);
                        this.balance(line);
                        break;

                    case HELP:
                        this.printUsage();
                        break;

                    case EXIT:
                        exit = true;
                        break;

                    default:
                        System.out.println("Invalid command. Type 'help' for usage\n");
                        break;
                }
            }
            catch (Exception e){
                System.err.println(e.getMessage());
            }
        }

        scanner.close();
        this.userService.close();
        this.namingServerService.close();
    }

    private void createAccount(String line){
        String[] split = line.split(SPACE);

        if (split.length != 3){
            this.printUsage();
            return;
        }

        String server = split[1];
        String username = split[2];

        try{

            debug("Sending account creation request to server:");
            debug("   Account username: "+ username);
            this.userService.createAccount(username);
            System.out.println(OK);
            System.out.println(); // by the given example
        } catch (StatusRuntimeException e) {
            Status status = e.getStatus();
            System.out.println(status.getDescription());
        }

    }

    private void deleteAccount(String line){
        String[] split = line.split(SPACE);

        if (split.length != 3){
            this.printUsage();
            return;
        }
        String server = split[1];
        String username = split[2];
        try{
            debug("Sending account deletion request to server:");
            debug("   Account username: "+ username);
            this.userService.deleteAccount(username);
            System.out.println(OK);
            System.out.println();
        } catch (StatusRuntimeException e) {
            Status status = e.getStatus();
            System.out.println(status.getDescription());
        }

    }


    private void balance(String line){
        String[] split = line.split(SPACE);

        if (split.length != 3){
            this.printUsage();
            return;
        }
        String server = split[1];
        String username = split[2];

        try{
            debug("Sending get balance request to server:");
            debug("   Account username: "+ username);

            Integer balance = this.userService.getBalance(username);
            System.out.println(OK);
            System.out.println(balance + "\n");
        } catch (StatusRuntimeException e) {
            Status status = e.getStatus();
            System.out.println(status.getDescription());
        }


    }

    private void transferTo(String line){
        String[] split = line.split(SPACE);

        if (split.length != 5){
            this.printUsage();
            return;
        }
        String server = split[1];
        String from = split[2];
        String dest = split[3];
        Integer amount = Integer.valueOf(split[4]);

        try{
            debug("Sending transfer to request to server:");
            debug("   AccountFrom username: "+ from);
            debug("   AccountDest username: "+ dest);
            debug("   Amount: "+ amount);
            this.userService.transferTo(from, dest, amount);

            System.out.println(OK);
            System.out.println();

        } catch (StatusRuntimeException e) {
            Status status = e.getStatus();
            System.out.println(status.getDescription());
        }

    }

    private void lookup(String serviceName, String qualifier) throws StatusRuntimeException {
        try {
            // lookup server address list
            debug(String.format("lookup request sent to name server for service: " + serviceName + " and qualifier: " + qualifier));
            List<String> serverAdresses = this.namingServerService.lookup(serviceName, qualifier).getServerAddressesList();

            // choose server address from list
            String newServerAdress = serverAdresses.get(0);
            debug(String.format("lookup chosen server: " + newServerAdress));

            // update user service with server address
            String host = newServerAdress.split(":")[0];
            int port = Integer.parseInt(newServerAdress.split(":")[1]); 
            this.userService.updateServerAddress(host, port);

        } catch (StatusRuntimeException e) {
            System.out.println(e.getStatus().getDescription());
        }
    }

    private void printUsage() {
        System.out.println("Usage:\n" +
                "- createAccount <server> <username>\n" +
                "- deleteAccount <server> <username>\n" +
                "- balance <server> <username>\n" +
                "- transferTo <server> <username_from> <username_to> <amount>\n" +
                "- exit\n");
    }
}
