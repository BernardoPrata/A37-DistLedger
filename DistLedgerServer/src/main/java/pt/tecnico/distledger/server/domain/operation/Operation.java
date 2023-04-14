package pt.tecnico.distledger.server.domain.operation;

import pt.tecnico.distledger.common.vectorclock.VectorClock;

import java.util.List;

public abstract class Operation {
    private static final boolean STABLE = true;
    private static final boolean UNSTABLE = false;

    private String account;
    private boolean stability;
    private VectorClock OperationTs;

    public Operation(String fromAccount) {
        this.account = fromAccount;
        this.stability = UNSTABLE;
        this.OperationTs = new VectorClock();
    }

    public Operation(String fromAccount, boolean stable, VectorClock OperationTs) {
        this.account = fromAccount;
        this.stability = UNSTABLE;
        this.OperationTs = OperationTs;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public void setStable() {
        stability = STABLE;
    }

    public boolean isStable() {
        return stability;
    }

    public boolean isUnstable() {
        return !stability;
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

    public void setOperationTs(VectorClock OperationTs) {
        this.OperationTs = OperationTs;
    }

    public VectorClock getOperationTs() {
        return OperationTs;
    }
}
