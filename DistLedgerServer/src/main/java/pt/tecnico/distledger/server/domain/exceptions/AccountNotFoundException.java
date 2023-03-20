package pt.tecnico.distledger.server.domain.exceptions;

public class AccountNotFoundException extends DistLedgerServerException {

        public AccountNotFoundException() {
            super("Account not found.");
        }
}
