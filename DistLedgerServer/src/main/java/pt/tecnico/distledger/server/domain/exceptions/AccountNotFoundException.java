package pt.tecnico.distledger.server.domain.exceptions;

public class AccountNotFoundException extends Exception {

        public AccountNotFoundException() {
            super("Account not found.");
        }
}
