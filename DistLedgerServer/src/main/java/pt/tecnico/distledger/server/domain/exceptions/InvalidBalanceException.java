package pt.tecnico.distledger.server.domain.exceptions;

public class InvalidBalanceException extends DistLedgerServerException {

    public InvalidBalanceException(int balance) {
        super("Invalid balance: " + balance + ".");
    }
}
