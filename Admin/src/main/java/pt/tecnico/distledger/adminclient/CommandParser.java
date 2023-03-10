package pt.tecnico.distledger.adminclient;

import pt.tecnico.distledger.adminclient.grpc.AdminService;
import pt.tecnico.distledger.contract.DistLedgerCommonDefinitions;
import pt.tecnico.distledger.contract.DistLedgerCommonDefinitions.LedgerState;
import pt.tecnico.distledger.contract.admin.AdminDistLedger.*;

import java.util.Optional;
import java.util.Scanner;

import io.grpc.*;

public class CommandParser {

	/** Set flag to true to print debug messages. 
	 * The flag can be set using the -Ddebug command line option. */
	private final boolean DEBUG_FLAG;

	/** Helper method to print debug messages. */
	private void debug(String debugMessage) {
		if (DEBUG_FLAG)
			System.err.println(debugMessage);
	}

    private static final String SPACE = " ";
    private static final String ACTIVATE = "activate";
    private static final String DEACTIVATE = "deactivate";
    private static final String GET_LEDGER_STATE = "getLedgerState";
    private static final String GOSSIP = "gossip";
    private static final String HELP = "help";
    private static final String EXIT = "exit";

    private final AdminService adminService;
    public CommandParser(AdminService adminService, Boolean debug) {
        this.adminService = adminService;
        this.DEBUG_FLAG = debug;
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

        scanner.close();
    }

    private void activate(String line){
        String[] split = line.split(SPACE);

        if (split.length != 2){
            this.printUsage();
            return;
        }
        String server = split[1]; // TODO Phase-3

        try{
            // activate server
            ActivateRequest request = ActivateRequest.newBuilder().build();
            debug(String.format("activate server request sent to server: " + server));

            ActivateResponse response = this.adminService.activate(request);
            debug(String.format("activate server response received from server: " + server));

            System.out.println(response);
            System.out.println("OK");

        } catch (StatusRuntimeException e) {
            System.out.println("Caught exception with description: " + e.getStatus().getDescription());
        }
    }

    private void deactivate(String line){
        String[] split = line.split(SPACE);

        if (split.length != 2){
            this.printUsage();
            return;
        }
        String server = split[1]; // TODO Phase-3

        try{
            // deactivate server
            DeactivateRequest request = DeactivateRequest.newBuilder().build();
            debug(String.format("deactivate server request sent to server: " + server));

            DeactivateResponse response = this.adminService.deactivate(request);
            debug(String.format("deactivate server response received from server: " + server));

            System.out.println(response);
            System.out.println("OK");
            
        } catch (StatusRuntimeException e) {
            System.out.println("Caught exception with description: " + e.getStatus().getDescription());
        }
    }

    private void dump(String line){
        String[] split = line.split(SPACE);

        if (split.length != 2){
            this.printUsage();
            return;
        }
        String server = split[1]; // TODO Phase-3

        try{
            // deactivate server
            getLedgerStateRequest request = getLedgerStateRequest.newBuilder().build();
            debug(String.format("get ledger state request sent to server: " + server));

            getLedgerStateResponse response = this.adminService.getLadgerState(request);
            debug(String.format("get ledger state response received from server: " + server));

            LedgerState ledgerState =  response.getLedgerState();

            System.out.println("OK");
            displayLedgerState(ledgerState);
        
        } catch (StatusRuntimeException e) {
            System.out.println("Caught exception with description: " + e.getStatus().getDescription());
        }
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

    private void displayLedgerState(LedgerState ledgerState){
        Optional<DistLedgerCommonDefinitions.OperationType> opType;
        Optional<String> userId;
        Optional<String> destUserId;
        Optional<Integer> amount;

        System.out.println("ledgerState {");

        for (DistLedgerCommonDefinitions.Operation op : ledgerState.getLedgerList()) {
            System.out.println("\tledger {");
            
            opType = Optional.ofNullable(op.getType());
            userId = Optional.ofNullable(op.getUserId());
            destUserId = Optional.ofNullable(op.getDestUserId());
            amount = Optional.ofNullable(op.getAmount());

            if (opType.isPresent()) {
                System.out.print("\t\ttype: " + opType.get().toString());
            }

            if (userId.isPresent()) {
                System.out.print("\t\tuserId: " + userId.get());
            }

            if (destUserId.isPresent()) {
                System.out.print("\t\tdestUserId: " + destUserId.get());
            }

            if (amount.isPresent()) {
                System.out.print("\t\tamount: " + amount.get());
            }

            System.out.println("\t}");
        }

        System.out.println("}");
    }
}
