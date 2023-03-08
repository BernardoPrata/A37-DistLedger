package pt.tecnico.distledger.server;

import pt.tecnico.distledger.contract.user.UserDistLedger.StatusCode;
import pt.tecnico.distledger.server.domain.ServerState;

public class DistLedger {

    private ServerState state;
    private Boolean isActivated;

    private void initLedger() {

        // Creates Broker account
        state.addBrokerAccount("Broker");
    }

    public DistLedger() {
        this.state = new ServerState();
        initLedger();
    }

    public ServerState getState() throws IllegalStateException {

        if (!isActivated)
            throw new IllegalStateException("UNAVAILABLE");

        return this.state;
    }

    public void setState(ServerState state) {
        this.state = state;
    }

    public Boolean activate() {
        this.isActivated = true;
        return true;
    }

    public Boolean deactivate() {
        this.isActivated = false;
        return true;
    }

    public StatusCode createAccount(String account) {
        return state.addNewActiveAccount(account);
    }

    public StatusCode deleteAccount(String account) {
        return state.removeActiveAccount(account);
    }

    public StatusCode transferTo(String from, String to, int amount) {
        return state.transferTo(from, to, amount);
    }

    public int balance(String account) {
        return state.getActiveAccountBalance(account);
    }

}
