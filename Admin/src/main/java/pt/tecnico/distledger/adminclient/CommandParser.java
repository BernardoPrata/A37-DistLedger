package pt.tecnico.distledger.adminclient;

import pt.tecnico.distledger.adminclient.grpc.AdminService;
import pt.tecnico.distledger.contract.DistLedgerCommonDefinitions;
import pt.tecnico.distledger.contract.DistLedgerCommonDefinitions.LedgerState;
import pt.tecnico.distledger.contract.admin.AdminDistLedger.*;

import java.util.Scanner;

import com.google.longrunning.Operation;

public class CommandParser {

    private static final String SPACE = " ";
    private static final String ACTIVATE = "activate";
    private static final String DEACTIVATE = "deactivate";
    private static final String GET_LEDGER_STATE = "getLedgerState";
    private static final String GOSSIP = "gossip";
    private static final String HELP = "help";
    private static final String EXIT = "exit";

    private final AdminService adminService;
    public CommandParser(AdminService adminService) {
        this.adminService = adminService;
    }
    void parseInput() {

        Scanner scanner = new Scanner(System.in);
        boolean exit = false;

        while (!exit) {
            System.out.print("> ");
            String line = scanner.nextLine().trim();
            String cmd = line.split(SPACE)[0];

            switch (cmd) {
                case ACTIVATE:
                    this.activate(line);
                    break;

                case DEACTIVATE:
                    this.deactivate(line);
                    break;

                case GET_LEDGER_STATE:
                    this.dump(line);
                    break;

                case GOSSIP:
                    this.gossip(line);
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
    }

    private void activate(String line){
        String[] split = line.split(SPACE);

        if (split.length != 2){
            this.printUsage();
            return;
        }
        String server = split[1]; // TODO Phase-3

        // activate server
        ActivateRequest request = ActivateRequest.getDefaultInstance();
        // TODO: debug - "activate server request sent to server: "

        this.adminService.activate(request);

        // TODO: Catch exception
        System.out.println("OK");
    }

    private void deactivate(String line){
        String[] split = line.split(SPACE);

        if (split.length != 2){
            this.printUsage();
            return;
        }
        String server = split[1]; // TODO Phase-3

        // deactivate server
        DeactivateRequest request = DeactivateRequest.getDefaultInstance();
        // TODO: debug - "deactivate server request sent to server: "

        this.adminService.deactivate(request);

        // TODO: Catch exception
        System.out.println("OK");
    }

    private void dump(String line){
        String[] split = line.split(SPACE);

        if (split.length != 2){
            this.printUsage();
            return;
        }
        String server = split[1]; // TODO Phase-3

        // deactivate server
        getLedgerStateRequest request = getLedgerStateRequest.getDefaultInstance();
        // TODO: debug - "get ledger state request sent to server: "

        getLedgerStateResponse response = this.adminService.getLadgerState(request);

        LedgerState ledgerState =  response.getLedgerState();

        // TODO: Apresentar no terminal o ledger state
        //System.out.println(ledgerState);
        // displayLedgerState(ledgerState);
        // TODO: Catch exception
    }

    @SuppressWarnings("unused")
    private void gossip(String line){
        /* TODO Phase-3 */
        System.out.println("TODO: implement gossip command (only for Phase-3)");
    }
    private void printUsage() {
        System.out.println("Usage:\n" +
                "- activate <server>\n" +
                "- deactivate <server>\n" +
                "- getLedgerState <server>\n" +
                "- gossip <server>\n" +
                "- exit\n");
    }

}

/** Helper method to display ledger state as text. */
private static void displayLedgerState(LedgerState ledgerState) {
    System.out.println("Ledger state:");
    for (DistLedgerCommonDefinitions.Operation operation : ledgerState.getLedgerList()) {
        // TODO: acabar de implementar
    }
}
