package pt.tecnico.distledger.server.domain.exceptions;

public class OtherServerUnavailableException extends DistLedgerServerException {

    public OtherServerUnavailableException() {
        super("One of the other servers is unavailable.");
    }
}
