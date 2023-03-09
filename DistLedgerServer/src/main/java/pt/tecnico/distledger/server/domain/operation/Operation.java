package pt.tecnico.distledger.server.domain.operation;

public abstract class Operation {
    private String account;

    public Operation(String fromAccount) {
        this.account = fromAccount;
    }

    public String getAccount() {
        return account;
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
