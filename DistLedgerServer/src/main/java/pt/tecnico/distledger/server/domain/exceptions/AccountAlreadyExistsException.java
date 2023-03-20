package pt.tecnico.distledger.server.domain.exceptions;

public class AccountAlreadyExistsException extends DistLedgerServerException {

    public AccountAlreadyExistsException() {
        super("Account already exists.");
    }

}
