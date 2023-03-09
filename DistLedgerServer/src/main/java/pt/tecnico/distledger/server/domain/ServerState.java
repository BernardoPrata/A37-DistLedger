package pt.tecnico.distledger.server.domain;

import pt.tecnico.distledger.server.domain.exceptions.*;
import pt.tecnico.distledger.server.domain.operation.Operation;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ServerState {

    // The ledger is a list of operations that have been executed
    private List<Operation> ledger;

    // The active accounts have the current balance of each account
    private ConcurrentHashMap<String, Integer> activeAccounts;

    private Boolean isActivated;

    private boolean toDebug = false;

    public ServerState(boolean toDebug) {
        this.ledger = new ArrayList<>();
        this.activeAccounts = new ConcurrentHashMap<>();
        this.isActivated = true;
        this.toDebug = toDebug;

        addBrokerAccount("broker");
    }

    private void debug(String debugMessage) {
        if (toDebug)
            System.err.println(debugMessage);
    }

    public Boolean isActivated() {
        return isActivated;
    }

    public void activate() {
        debug("activate> Activating server");
        this.isActivated = true;
    }

    public void deactivate() {
        debug("deactivate> Deactivating server");
        this.isActivated = false;
    }

    public void verifyServerAvailability() throws ServerUnavailableException {
        debug("verifyServerAvailability> Server is activated? " + isActivated());
        if (!isActivated()) {
            throw new ServerUnavailableException();
        }
    }

    public List<Operation> getLedger() {
        return ledger;
    }

    public void setLedger(List<Operation> ledger) {
        this.ledger = ledger;
    }

    public void addOperation(Operation op) {
        this.ledger.add(op);
    }

    public ConcurrentHashMap<String, Integer> getActiveAccounts() {
        return activeAccounts;
    }

    public Boolean isAccountActive(String account) throws ServerUnavailableException {
        verifyServerAvailability();
        return getActiveAccounts().containsKey(account);
    }

    public void setActiveAccounts(ConcurrentHashMap<String, Integer> activeAccounts) throws ServerUnavailableException {
        verifyServerAvailability();
        this.activeAccounts = activeAccounts;
    }

    public void addBrokerAccount(String account) {
        this.activeAccounts.put(account, 1000);
    }

    public synchronized void addAccount(String account) throws AccountAlreadyExistsException, ServerUnavailableException {

        verifyServerAvailability();

        // verifies if account already exists
        if (isAccountActive(account)) {
            throw new AccountAlreadyExistsException();
        }

        this.activeAccounts.put(account, 0);
    }

    public synchronized void removeAccount(String account) throws BalanceNotZeroException, ServerUnavailableException, AccountNotFoundException {

        verifyServerAvailability();

        // if balance is not 0, throw exception
        int currentBalance = this.activeAccounts.get(account);

        // verifies if the account exists
        if (!isAccountActive(account)) {
            throw new AccountNotFoundException();
        }

        if (currentBalance != 0) {
            throw new BalanceNotZeroException(currentBalance);
        }

        this.activeAccounts.remove(account);
    }

    public synchronized int getBalance(String account) throws AccountNotFoundException, ServerUnavailableException {


        verifyServerAvailability();

        // verifies if the account exists
        if (!isAccountActive(account)) {
            throw new AccountNotFoundException();
        }

        return this.activeAccounts.get(account);
    }

    public synchronized void updateAccount(String account, int deltaBalance) throws AccountNotFoundException, ServerUnavailableException {

        debug("updateAccount> Updating account " + account + " with delta " + deltaBalance);
        int currentBalance = this.getBalance(account);
        this.activeAccounts.put(account, currentBalance + deltaBalance);
        debug("updateAccount> Account updated");
    }

    public synchronized void transferTo(String from, String to, int amount) throws AccountNotFoundException, InsufficientBalanceException, ServerUnavailableException, InvalidBalanceException {


        int fromBalance = getBalance(from);
        isAccountActive(to); // Verifies if the account exists

        debug("transferTo> amountToTransfer: " + amount + " fromBalance: " + fromBalance);
        // Verifies if the amount is valid
        if (amount <= 0) {
            throw new InvalidBalanceException(amount);
        }

        // Verifies if there is enough balance
        if (fromBalance < amount) {
            throw new InsufficientBalanceException(fromBalance);
        }

        debug("transferTo> No exceptions thrown. Updating accounts");
        // Updates the balance of the accounts
        updateAccount(from, -amount);
        updateAccount(to, amount);

        debug("transferTo> Accounts updated");
    }

//    public String toString() {
//        StringBuilder sb = new StringBuilder();
//        sb.append("ledger {\n");
//        for (Operation op : ledger) {
//            sb.append("  ").append(op.toString());
//        }
//        sb.append("}");
//        return sb.toString();
//    }
}
