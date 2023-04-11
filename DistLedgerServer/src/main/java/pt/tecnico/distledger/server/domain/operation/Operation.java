package pt.tecnico.distledger.server.domain.operation;

import pt.tecnico.distledger.common.vectorclock.VectorClock;

import java.util.List;

public abstract class Operation {
    private String account;

    private boolean Stable;

    private VectorClock vectorClock;
    public Operation(String fromAccount) {
        this.account = fromAccount;
    }

    public Operation(String fromAccount, boolean stable, VectorClock vectorClock) {
        this.account = fromAccount;
        this.Stable = stable;
        this.vectorClock = new VectorClock(vectorClock.getVectorClock());
    }
    public String getAccount() {
        return account;
    }

    public boolean isStable() {
        return Stable;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getDestAccount() {
        return account;
    }

    public int getAmount() {
        return 0;
    }

    public String getType() {
        return "OP_UNSPECIFIED";
    }
}
