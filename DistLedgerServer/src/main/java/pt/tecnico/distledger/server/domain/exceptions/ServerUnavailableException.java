package pt.tecnico.distledger.server.domain.exceptions;

public class ServerUnavailableException extends DistLedgerServerException {

    public ServerUnavailableException() {
        super("The server is not available.");
    }
}
