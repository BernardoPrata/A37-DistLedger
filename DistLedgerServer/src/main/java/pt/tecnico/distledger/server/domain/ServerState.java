package pt.tecnico.distledger.server.domain;



import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import pt.tecnico.distledger.server.domain.exceptions.*;
import pt.tecnico.distledger.server.grpc.NameService;
import pt.tecnico.distledger.server.grpc.DistLedgerCrossServerService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import pt.tecnico.distledger.server.domain.operation.*;

public class ServerState {

    // The ledger is a list of operations
    private List<Operation> ledger;

    // The active accounts have the current balance of each account
    private final ConcurrentHashMap<String, Integer> activeAccounts;

    private Boolean isActivated;
    private String address;
    private boolean toDebug;
    private NameService nameService;

    public ServerState(boolean toDebug, String address, NameService nameService) {
        this.ledger = new ArrayList<>();
        this.activeAccounts = new ConcurrentHashMap<>();
        this.isActivated = true;
        this.toDebug = toDebug;
        this.address = address;
        this.nameService = nameService;

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
        debug("activate> Activating server\n");
        isActivated = true;
    }

    public synchronized void deactivate() {
        debug("deactivate> Deactivating server\n");
        isActivated = false;
    }

    public void verifyServerAvailability() throws ServerUnavailableException {
        debug("verifyServerAvailability> Server is activated? " + isActivated());
        if (!isActivated()) {
            debug("verifyServerAvailability> Server is not activated. Throwing exception\n");
            throw new ServerUnavailableException();
        }
    }

    public void setLedger(List<Operation> ledger) {
        this.ledger = ledger;
    }

    public List<Operation> getLedger() {
        debug("getLedgerState> Ledger state: " + ledger.toString() + "\n");
        return ledger;
    }

    public NameService getNameService() {
        return nameService;
    }

    public String getAddress() {
        return address;
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

    public synchronized void createAccount(String account) throws AccountAlreadyExistsException, ServerUnavailableException, OtherServerUnavailableException, NotPrimaryServerException {

        debug("createAccount> Creating account `" + account + "`");

        verifyServerAvailability();

        addAccount(account);

        debug("createAccount> Account created");

        debug("createAccount> propagating State");
        try {
            //findServersAndPropagate(op);
        }
        catch (StatusRuntimeException e) {
            Status status = e.getStatus();
            /* If any of the destiny servers was unavailable, an expection was thrown */
            /* This means that the operation must be reverted */
            if (status.getCode() == Status.Code.UNAVAILABLE) {
                try {
                    debug("createAccount> One of the destiny servers was unavailable. Deleting account");
                    removeAccount(account);
                    debug("createAccount> Operation reverted. Throwing exception to inform client\n");
                    throw new OtherServerUnavailableException();
                }
                catch (BalanceNotZeroException | AccountNotFoundException e1) {
                    /* These exceptions never happen because the account was just created */
                    e1.printStackTrace();
                }
            }
            return;
        }

        debug("createAccount> Adding new AddAccountOp to ledger");
        debug("createAccount> State propagated\n");
    }


    public synchronized void deleteAccount(String account) throws BalanceNotZeroException, ServerUnavailableException, OtherServerUnavailableException, AccountNotFoundException, NotPrimaryServerException {

        debug("deleteAccount> Removing account `" + account + "`");

        verifyServerAvailability();

        removeAccount(account);

        debug("deleteAccount> Account removed");

        debug("deleteAccount> propagating State");
        try {
        //    findServersAndPropagate(op);
        }
        catch (StatusRuntimeException e) {
            Status status = e.getStatus();
            if (status.getCode() == Status.Code.UNAVAILABLE) {
                try {
                    debug("deleteAccount> One of the destiny servers was unavailable. Creating account");
                    addAccount(account);
                    debug("deleteAccount> Operation reverted. Throwing exception to inform client\n");
                    throw new OtherServerUnavailableException();
                }
                catch (AccountAlreadyExistsException e1) {
                    /* This exception never happens because the account was just deleted */
                    e1.printStackTrace();
                }
            }
            return;
        }

        debug("deleteAccount> Adding new RemoveAccountOp to ledger");

        debug("deleteAccount> State propagated\n");
    }


    public synchronized int balance(String account) throws AccountNotFoundException, ServerUnavailableException {

        debug("balance> Getting balance of account `" + account + "`");

        verifyServerAvailability();

        int balance = getBalance(account);
        debug("balance> Returning balance\n");
        return balance;
    }


    public synchronized void transferTo(String from, String to, int amount) throws AccountNotFoundException, InsufficientBalanceException, ServerUnavailableException, OtherServerUnavailableException, InvalidBalanceException, NotPrimaryServerException {

        debug("transferTo> Transferring `" + amount + "` from `" + from + "` to `" + to + "`");

        verifyServerAvailability();

        transferBetweenAccounts(from, to, amount);

        debug("transferTo> Transfer successful");

        debug("transferTo> propagating State");
        try {
            //findServersAndPropagate(op);
        }
        catch (StatusRuntimeException e) {
            Status status = e.getStatus();
            if (status.getCode() == Status.Code.UNAVAILABLE) {
                debug("transferTo> One of the destiny servers was unavailable. Reverting transfer");
                transferBetweenAccounts(to, from, amount);
                debug("transferTo> Operation reverted. Throwing exception to inform client\n");
                throw new OtherServerUnavailableException();
            }
            return;
        }
        debug("transferTo> Adding new TransferOp to ledger");
        debug("transferTo> State propagated\n");
    }

    // --------------------------------------------------------------
    // ------------------- PROPAGATION OPERATIONS -------------------
    // --------------------------------------------------------------




    public synchronized void performOperation(Operation op) throws BalanceNotZeroException, AccountNotFoundException, InsufficientBalanceException, InvalidBalanceException, AccountAlreadyExistsException {

        debug("performOperation> Performing an operation");

        if (op instanceof CreateOp) {
            CreateOp createOp = (CreateOp) op;
            debug("performOperation> Operation is a CreateOp with account `" + createOp.getAccount() + "`");
            /* The operation has been propagated from the Primary Server, which means that it has been successful. */
            /* Therefore, there is no need to care about Exceptions. */
            addAccount(createOp.getAccount());

        }
        else if (op instanceof DeleteOp) {
            DeleteOp deleteOp = (DeleteOp) op;
            debug("performOperation> Operation is a DeleteOp with account `" + deleteOp.getAccount() + "`");
            removeAccount(deleteOp.getAccount());
        }
        else if (op instanceof TransferOp) {
            TransferOp transferOp = (TransferOp) op;
            debug("performOperation> Operation is a TransferOp with account `" + transferOp.getAccount() + "`, destAccount `" + transferOp.getDestAccount() + "` and amount `" + transferOp.getAmount() + "`");
            transferBetweenAccounts(transferOp.getAccount(), transferOp.getDestAccount(), transferOp.getAmount());

        }

        debug("performOperation> Operation successfully performed\n");
    }
}