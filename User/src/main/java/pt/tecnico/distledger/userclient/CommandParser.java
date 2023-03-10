package pt.tecnico.distledger.userclient;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import pt.tecnico.distledger.userclient.grpc.UserService;
import pt.tecnico.distledger.contract.user.UserDistLedger.*;

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

    private final UserService userService;

    public CommandParser(UserService userService) {
        this.userService = userService;
    }

    void parseInput() {

        Scanner scanner = new Scanner(System.in);
        boolean exit = false;

        while (!exit) {
            System.out.print("> ");
            String line = scanner.nextLine().trim();
            String cmd = line.split(SPACE)[0];

            try{
                switch (cmd) {
                    case CREATE_ACCOUNT:
                        this.createAccount(line);
                        break;

                    case DELETE_ACCOUNT:
                        this.deleteAccount(line);
                        break;

                    case TRANSFER_TO:
                        this.transferTo(line);
                        break;

                    case BALANCE:
                        this.balance(line);
                        break;

                    case HELP:
                        this.printUsage();
                        break;

                    case EXIT:
                        exit = true;
                        break;

                    default:
                        break;
                }
            }
            catch (Exception e){
                System.err.println(e.getMessage());
            }
        }
        userService.close();
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
            CreateAccountResponse response = userService.
                    createAccount(CreateAccountRequest.newBuilder().
                            setUserId(username).build());
            // TODO: debug - "create account request sent to server: "

            System.out.println(OK);
            System.out.println(response);
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
            DeleteAccountResponse response = userService.
                    deleteAccount(DeleteAccountRequest.newBuilder().
                            setUserId(username).build());
            // TODO: debug - "delete account request sent to server: "

            System.out.println(OK);
            System.out.println(response);
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
            BalanceResponse response = userService.
                    getBalance(BalanceRequest.newBuilder().
                            setUserId(username).build());
            // TODO: debug - "get balance request sent to server: "

            Integer balance = response.getValue();

            System.out.println(OK);
            System.out.println("balance:"+balance);
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
            TransferToResponse response = userService.
                    transferTo(TransferToRequest.newBuilder().setAccountFrom(from).setAccountTo(dest).setAmount(amount).build());
            // TODO: debug - "transfer to request sent to server: "

            System.out.println(OK);
            System.out.println(response);
        } catch (StatusRuntimeException e) {
            Status status = e.getStatus();
            System.out.println(status.getDescription());
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
