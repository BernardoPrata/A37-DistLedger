package pt.tecnico.distledger.server.domain.exceptions;

public class ServerUpdatesOutdatedException extends DistLedgerServerException {

    public ServerUpdatesOutdatedException() {
        super("Server updates outdated. Try again later...");
    }
}
