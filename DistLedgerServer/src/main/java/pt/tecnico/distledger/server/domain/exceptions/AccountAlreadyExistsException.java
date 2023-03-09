package pt.tecnico.distledger.server.domain.exceptions;

public class AccountAlreadyExistsException extends Exception {

    public AccountAlreadyExistsException() {
        super("Account already exists.");
    }

}
