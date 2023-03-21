package pt.tecnico.distledger.namingserver.exceptions;

public class NotPossibleToRemoveServerException extends Exception {

    public NotPossibleToRemoveServerException() {
        super("Not possible to remove the server.");
    }
}