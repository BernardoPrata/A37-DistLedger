package pt.tecnico.distledger.server.domain;

import pt.tecnico.distledger.contract.user.UserDistLedger.StatusCode;
import pt.tecnico.distledger.server.domain.operation.Operation;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ServerState {

    // The ledger is a list of operations that have been executed
    private List<Operation> ledger;

    // The active accounts have the current balance of each account
    private HashMap<String, Integer> activeAccounts;

    public ServerState() {
        this.ledger = new ArrayList<>();
    }

    public List<Operation> getLedger() {
        return ledger;
    }

    public HashMap<String, Integer> getActiveAccounts() {
        return activeAccounts;
    }

    public Boolean isAccountActive(String account) {
        return getActiveAccounts().containsKey(account);
    }

    public void setActiveAccounts(HashMap<String, Integer> activeAccounts) {
        this.activeAccounts = activeAccounts;
    }

    public StatusCode addBrokerAccount(String account) {
        this.activeAccounts.put(account, 1000);
        return StatusCode.OK;
    }

    public StatusCode addNewActiveAccount(String account) {

        // verifies if account already exists
        if (this.activeAccounts.containsKey(account)) {
            return StatusCode.ACC_ALREADY_EXISTS;
        }

        this.activeAccounts.put(account, 0);
        return StatusCode.OK;
    }


    public StatusCode removeActiveAccount(String account) {

        // if balance is not 0, throw exception
        int currentBalance = this.activeAccounts.get(account);
        if (currentBalance != 0) {
            return StatusCode.BALANCE_NOT_0;
        }

        this.activeAccounts.remove(account);
        return StatusCode.OK;
    }


    public void updateActiveAccount(String account, int deltaBalance) {
        int currentBalance = this.activeAccounts.get(account);
        this.activeAccounts.put(account, currentBalance + deltaBalance);
    }

    public int getActiveAccountBalance(String account) {
        return this.activeAccounts.get(account);
    }

    public StatusCode transferTo(String from, String to, int amount) {

        // Verifies if accounts exist
        if (!isAccountActive(from) || !isAccountActive(to)) {
            return StatusCode.ACCOUNT_NOT_FOUND;
        }

        int fromBalance = getActiveAccountBalance(from);
        int toBalance   = getActiveAccountBalance(to);

        // Verifies if there is enough balance
        if (fromBalance < amount) {
            return StatusCode.INSUFFICIENT_BALANCE;
        }

        // Updates the balance of the accounts
        updateActiveAccount(from, -amount);
        updateActiveAccount(to, amount);
        return StatusCode.OK;
    }

    public void setLedger(List<Operation> ledger) {
        this.ledger = ledger;
    }

    public void addOperation(Operation op) {
        this.ledger.add(op);
    }


}
