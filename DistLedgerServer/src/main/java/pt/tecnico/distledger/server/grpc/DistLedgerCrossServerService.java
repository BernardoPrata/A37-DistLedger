package pt.tecnico.distledger.server.grpc;

import pt.tecnico.distledger.contract.DistLedgerCommonDefinitions;
import pt.tecnico.distledger.contract.distledgerserver.CrossServerDistLedger.*;
import pt.tecnico.distledger.contract.distledgerserver.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.tecnico.distledger.server.domain.operation.CreateOp;
import pt.tecnico.distledger.server.domain.operation.DeleteOp;
import pt.tecnico.distledger.server.domain.operation.Operation;
import pt.tecnico.distledger.server.domain.operation.TransferOp;

import java.util.List;

public class DistLedgerCrossServerService implements AutoCloseable {

    private final ManagedChannel channel;
    private final DistLedgerCrossServerServiceGrpc.DistLedgerCrossServerServiceBlockingStub stub;

    public DistLedgerCrossServerService(String host, int port) {
        this(ManagedChannelBuilder.forAddress(host, port).usePlaintext());
    }

    public DistLedgerCrossServerService(ManagedChannelBuilder<?> channelBuilder) {
        channel = channelBuilder.build();
        stub = DistLedgerCrossServerServiceGrpc.newBlockingStub(channel);
    }

    /* Given an Operation, creates the corresponding message that is sent through the stub */
    private DistLedgerCommonDefinitions.Operation.Builder createOperationMessage(Operation op) {

        DistLedgerCommonDefinitions.Operation.Builder opMessage = DistLedgerCommonDefinitions.Operation.newBuilder();

        DistLedgerCommonDefinitions.OperationType opType;
        String destUserId;
        int amount;

        opMessage.setUserId(op.getAccount());

        if (op instanceof TransferOp) {
            opType = DistLedgerCommonDefinitions.OperationType.OP_TRANSFER_TO;
            destUserId = op.getDestAccount();
            amount = op.getAmount();

            opMessage.setType(opType);
            opMessage.setDestUserId(destUserId);
            opMessage.setAmount(amount);
        }

        else if (op instanceof DeleteOp) {
            opType = DistLedgerCommonDefinitions.OperationType.OP_DELETE_ACCOUNT;

            opMessage.setType(opType);
        }

        else if (op instanceof CreateOp) {
            opType = DistLedgerCommonDefinitions.OperationType.OP_CREATE_ACCOUNT;

            opMessage.setType(opType);
        }

        else {
            opType = DistLedgerCommonDefinitions.OperationType.OP_UNSPECIFIED;

            opMessage.setType(opType);
        }

        return opMessage;
    }


    public void propagateState(Operation op) {

        DistLedgerCommonDefinitions.Operation.Builder opMessage = createOperationMessage(op);

        stub.propagateState(PropagateStateRequest.newBuilder().setOp(opMessage).build());
    }


    @Override
    public final void close() {
        channel.shutdown();
    }

}
