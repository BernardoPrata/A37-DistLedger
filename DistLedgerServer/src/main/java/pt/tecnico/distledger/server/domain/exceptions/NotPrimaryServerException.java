package pt.tecnico.distledger.server.domain.exceptions;

public class NotPrimaryServerException extends DistLedgerServerException {

    public NotPrimaryServerException() {
        super("Server is not primary.");
    }

}
