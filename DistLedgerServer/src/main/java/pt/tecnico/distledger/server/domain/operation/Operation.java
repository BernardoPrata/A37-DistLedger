package pt.tecnico.distledger.server.domain.operation;

public abstract class Operation {
    private String account;

    private boolean Stable;
    public Operation(String fromAccount) {
        this.account = fromAccount;
    }
    public Operation(String fromAccount, boolean stable) {
        this.account = fromAccount;
        this.Stable = stable;
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
