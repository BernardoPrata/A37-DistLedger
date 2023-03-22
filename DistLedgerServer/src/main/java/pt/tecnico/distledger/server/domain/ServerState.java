package pt.tecnico.distledger.server.domain;

import pt.tecnico.distledger.server.domain.exceptions.*;
import pt.tecnico.distledger.server.domain.operation.*;
import pt.tecnico.distledger.server.grpc.DistLedgerCrossServerService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ServerState {

    // The ledger is a list of operations that have been executed
    private List<Operation> ledger;

    // The active accounts have the current balance of each account
    private final ConcurrentHashMap<String, Integer> activeAccounts;

    private Boolean isActivated;
    private Boolean isPrimary;

    private boolean toDebug;

    public ServerState(boolean toDebug, boolean isPrimary) {
        this.ledger = new ArrayList<>();
        this.activeAccounts = new ConcurrentHashMap<>();
        this.isActivated = true;
        this.toDebug = toDebug;
        this.isPrimary = isPrimary;

        addBrokerAccount("broker");
    }

    private void debug(String debugMessage) {
        if (toDebug)
            System.err.println(debugMessage);
    }

    public synchronized Boolean isActivated() {
        return isActivated;
    }

    public synchronized void activate() {
        debug("activate> Activating server");
        isActivated = true;
    }

    public synchronized void deactivate() {
        debug("deactivate> Deactivating server");
        isActivated = false;
    }

    public void verifyServerAvailability() throws ServerUnavailableException {
        debug("verifyServerAvailability> Server is activated? " + isActivated());
        if (!isActivated()) {
            debug("verifyServerAvailability> Server is not activated. Throwing exception\n");
            throw new ServerUnavailableException();
        }
    }

    public void verifyIfPrimaryServer() throws NotPrimaryServerException {
        debug("verifyIfPrimaryServer> Server is primary? " + isPrimary);
        if (!isPrimary) {
            debug("verifyIfPrimaryServer> Server is not primary. Throwing exception\n");
            throw new NotPrimaryServerException();
        }
    }

    public void setLedger(List<Operation> ledger) {
        this.ledger = ledger;
    }

    public List<Operation> getLedger() {
        debug("getLedgerState> Ledger state: " + ledger.toString());
        return ledger;
    }

    public void addOperation(Operation op) {
        ledger.add(op);
    }

    public ConcurrentHashMap<String, Integer> getActiveAccounts() {
        return activeAccounts;
    }

    public Boolean isAccountActive(String account) {
        return getActiveAccounts().containsKey(account);
    }

    public synchronized int getBalance(String account) throws AccountNotFoundException {

        // verifies if the account exists
        if (!isAccountActive(account)) {
            debug("getBalance> Account does not exist. Throwing exception\n");
            throw new AccountNotFoundException();
        }

        int balance = activeAccounts.get(account);
        debug("getBalance> Account `" + account + "` has balance `" + balance + "`");
        return balance;
    }

    public synchronized void updateAccount(String account, int deltaBalance) throws AccountNotFoundException {

        debug("updateAccount> Updating account `" + account + "` with delta `" + deltaBalance + "`");
        int currentBalance = getBalance(account);
        activeAccounts.put(account, currentBalance + deltaBalance);
        debug("updateAccount> Account updated");
    }

    public synchronized void addAccount(String account) throws AccountAlreadyExistsException {

        // verifies if account already exists
        if (isAccountActive(account)) {
            debug("addAccount> Account already exists. Throwing exception\n");
            throw new AccountAlreadyExistsException();
        }

        activeAccounts.put(account, 0);
    }

    public synchronized void removeAccount(String account) throws BalanceNotZeroException, AccountNotFoundException {

        // if balance is not 0, throw exception
        int currentBalance = getBalance(account);

        if (currentBalance != 0) {
            debug("removeAccount> Balance is not 0. Throwing exception\n");
            throw new BalanceNotZeroException(currentBalance);
        }

        // Removes the account
        activeAccounts.remove(account);
    }

    public void addBrokerAccount(String account) {
        activeAccounts.put(account, 1000);
    }

    public synchronized void transferBetweenAccounts(String from, String to, int amount) throws AccountNotFoundException, InsufficientBalanceException, InvalidBalanceException {

        int fromBalance = getBalance(from);
        boolean isToActive = isAccountActive(to); // Verifies if the account exists

        debug("transferBetweenAccounts> amount to transfer: `" + amount + "`. balance from origin account: `" + fromBalance + "`");

        // Verifies if the destiny account exists
        if (!isToActive) {
            debug("transferBetweenAccounts> Destiny account does not exist. Throwing exception\n");
            throw new AccountNotFoundException();
        }

        // Verifies if the amount is valid
        if (amount <= 0) {
            debug("transferBetweenAccounts> Invalid amount. Throwing exception\n");
            throw new InvalidBalanceException(amount);
        }

        // Verifies if there is enough balance
        if (fromBalance < amount) {
            debug("transferBetweenAccounts> Insufficient balance. Throwing exception\n");
            throw new InsufficientBalanceException(fromBalance);
        }

        debug("transferBetweenAccounts> No exceptions thrown. Updating accounts");
        // Updates the balance of the accounts
        updateAccount(from, -amount);
        updateAccount(to, amount);

        debug("transferBetweenAccounts> Accounts updated");
    }

    // --------------------------------------------------------------
    // ----------------------- USER OPERATIONS ----------------------
    // --------------------------------------------------------------

    public synchronized void createAccount(String account) throws AccountAlreadyExistsException, ServerUnavailableException, NotPrimaryServerException {

        debug("createAccount> Creating account `" + account + "`");

        verifyServerAvailability();
        verifyIfPrimaryServer();

        addAccount(account);

        debug("createAccount> Account created");
        debug("createAccount> Adding new AddAccountOp to ledger");
        CreateOp op = new CreateOp(account);
        addOperation(op);

        debug("createAccount> propagating State");// TODO: use lookup() to get the server's host and port
        DistLedgerCrossServerService otherServer = new DistLedgerCrossServerService("localhost", 1337);
        otherServer.propagateState(op);
        debug("createAccount> State propagated\n");
    }


    public synchronized void deleteAccount(String account) throws BalanceNotZeroException, ServerUnavailableException, AccountNotFoundException, NotPrimaryServerException {

        debug("deleteAccount> Removing account `" + account + "`");

        verifyServerAvailability();
        verifyIfPrimaryServer();

        removeAccount(account);

        debug("deleteAccount> Account removed");
        debug("deleteAccount> Adding new RemoveAccountOp to ledger");
        DeleteOp op = new DeleteOp(account);
        addOperation(op);

        debug("deleteAccount> propagating State");
        // TODO: use lookup() to get the server's host and port
        DistLedgerCrossServerService otherServer = new DistLedgerCrossServerService("localhost", 1337);
        otherServer.propagateState(op);
        debug("deleteAccount> State propagated\n");
    }


    public synchronized int balance(String account) throws AccountNotFoundException, ServerUnavailableException {

        debug("balance> Getting balance of account `" + account + "`");

        verifyServerAvailability();

        int balance = getBalance(account);
        debug("balance> Returning balance\n");
        return balance;
    }


    public synchronized void transferTo(String from, String to, int amount) throws AccountNotFoundException, InsufficientBalanceException, ServerUnavailableException, InvalidBalanceException, NotPrimaryServerException {

        debug("transferTo> Transferring `" + amount + "` from `" + from + "` to `" + to + "`");

        verifyServerAvailability();
        verifyIfPrimaryServer();

        transferBetweenAccounts(from, to, amount);

        debug("transferTo> Transfer successful");
        debug("transferTo> Adding new TransferOp to ledger");
        TransferOp op = new TransferOp(from, to, amount);
        addOperation(op);

        debug("transferTo> propagating State");
        // TODO: use lookup() to get the server's host and port
        DistLedgerCrossServerService otherServer = new DistLedgerCrossServerService("localhost", 1337);
        otherServer.propagateState(op);
        debug("transferTo> State propagated\n");
    }

    // --------------------------------------------------------------
    // ------------------- PROPAGATION OPERATIONS -------------------
    // --------------------------------------------------------------

    public synchronized void performOperation(Operation op){

        debug("performOperation> Performing operation: " + op.toString());

        if (op instanceof CreateOp) {
            debug("performOperation> Operation is a CreateOp");
            CreateOp createOp = (CreateOp) op;
            /* The operation has been propagated from the Primary Server, which means that it has been successful. */
            /* Therefore, there is no need to care about Exceptions. */
            try {
                addAccount(createOp.getAccount());
            } catch (AccountAlreadyExistsException e) {
                e.printStackTrace();
            }
            debug("performOperation> Adding new CreateOp to ledger");
            addOperation(createOp);
        }
        else if (op instanceof DeleteOp) {
            debug("performOperation> Operation is a DeleteOp");
            DeleteOp deleteOp = (DeleteOp) op;
            try {
                removeAccount(deleteOp.getAccount());
            } catch (BalanceNotZeroException e) {
                e.printStackTrace();
            } catch (AccountNotFoundException e) {
                e.printStackTrace();
            }
            debug("performOperation> Adding new DeleteOp to ledger");
            addOperation(deleteOp);
        }
        else if (op instanceof TransferOp) {
            debug("performOperation> Operation is a TransferOp");
            TransferOp transferOp = (TransferOp) op;
            try {
                transferBetweenAccounts(transferOp.getAccount(), transferOp.getDestAccount(), transferOp.getAmount());
            } catch (AccountNotFoundException e) {
                e.printStackTrace();
            } catch (InsufficientBalanceException e) {
                e.printStackTrace();
            } catch (InvalidBalanceException e) {
                e.printStackTrace();
            }
            debug("performOperation> Adding new TransferOp to ledger");
            addOperation(transferOp);
        }

        debug("performOperation> Operation successfully performed");
    }
}