package pt.tecnico.distledger.server.domain.exceptions;

public class BalanceNotZeroException extends Exception {

    public BalanceNotZeroException(int balance) {
        super("You can't delete an account with balance. Your balance is " + balance + ".");
    }

}
