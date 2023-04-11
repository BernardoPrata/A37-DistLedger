package pt.tecnico.distledger.server.domain.operation;

import pt.tecnico.distledger.common.vectorclock.VectorClock;

import java.util.List;

public class CreateOp extends Operation {

    public CreateOp(String account, boolean stable, VectorClock vectorClock){
        super(account,stable,vectorClock);
    }
    public CreateOp(String account) {
        super(account);
    }

    @Override
    public String getType() {
        return "OP_CREATE_ACCOUNT";
    }

    @Override
    public String toString() {
        return "CreateOp";
    }
}
