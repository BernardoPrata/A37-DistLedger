package pt.tecnico.distledger.server.domain.operation;

public class CreateOp extends Operation {

    public CreateOp(String account) {
        super(account);
    }

    @Override
    public String getType() {
        return "OP_CREATE_ACCOUNT";
    }

    @Override
    public String toString() {
        return "ledger {\n" +
                "  type: " + getType() + "\n" +
                "  userId: " + getAccount() + "\n" +
                "}";
    }
}
