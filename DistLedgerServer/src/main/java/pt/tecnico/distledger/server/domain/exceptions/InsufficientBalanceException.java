package pt.tecnico.distledger.server.domain.exceptions;

public class InsufficientBalanceException extends DistLedgerServerException {

    public InsufficientBalanceException(int balance) {
        super("You don't have sufficient balance. Your balance is " + balance + ".");
    }

}
