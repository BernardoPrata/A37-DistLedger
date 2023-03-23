package pt.tecnico.distledger.adminclient;

import pt.tecnico.distledger.adminclient.grpc.AdminService;
import pt.tecnico.distledger.adminclient.grpc.NamingServerService;
import pt.tecnico.distledger.contract.admin.AdminDistLedger.*;

import java.util.List;
import java.util.Scanner;

import io.grpc.StatusRuntimeException;

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
    private static final String SERVICE_NAME = "DistLedger";
    private final NamingServerService namingServerService;

    private final AdminService adminService;

    public CommandParser(NamingServerService namingServerService, Boolean debug) {
        this.namingServerService = namingServerService;
        this.adminService = new AdminService();
        this.DEBUG_FLAG = debug;
    }

    void parseInput() {

        Scanner scanner = new Scanner(System.in);
        boolean exit = false;

        while (!exit) {
            System.out.print("> ");
            String line = scanner.nextLine().trim();
            String cmd = line.split(SPACE)[0];
            String qualifier = "";
            if (!cmd.equals("exit") && !cmd.equals("help")) {
                qualifier = line.split(SPACE)[1];
            }

            switch (cmd) {
                case ACTIVATE:
                    lookup(SERVICE_NAME, qualifier);
                    this.activate(line);
                    break;

                case DEACTIVATE:
                    lookup(SERVICE_NAME, qualifier);
                    this.deactivate(line);
                    break;

                case GET_LEDGER_STATE:
                    lookup(SERVICE_NAME, qualifier);
                    this.dump(line);
                    break;

                case GOSSIP:
                    lookup(SERVICE_NAME, qualifier);
                    this.gossip(line);
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

        scanner.close();
        this.adminService.close();
        this.namingServerService.close();
    }

    private void activate(String line){
        String[] split = line.split(SPACE);

        if (split.length != 2){
            this.printUsage();
            return;
        }
        String qualifier = split[1]; // TODO Phase-3

        try{
            // activate server
            debug(String.format("activate server request sent to server: " + qualifier));
            this.adminService.activate();
            debug(String.format("activate server response received from server: " + qualifier));

            System.out.println("OK");
            System.out.println();

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
        String qualifier = split[1]; // TODO Phase-3

        try{
            // deactivate server
            debug(String.format("deactivate server request sent to server: " + qualifier));
            this.adminService.deactivate();
            debug(String.format("deactivate server response received from server: " + qualifier));

            System.out.println("OK");
            System.out.println();
            
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
        String qualifier = split[1]; // TODO Phase-3

        try{
            // deactivate server
            debug(String.format("get ledger state request sent to server: " + qualifier));
            getLedgerStateResponse response = this.adminService.getLadgerState();
            debug(String.format("get ledger state response received from server: " + qualifier));

            System.out.println("OK");
            System.out.println(response);
        
        } catch (StatusRuntimeException e) {
            System.out.println("Caught exception with description: " + e.getStatus().getDescription());
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
            this.adminService.updateServerAddress(host, port);

        } catch (StatusRuntimeException e) {
            System.out.println(e.getStatus().getDescription());
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
}
