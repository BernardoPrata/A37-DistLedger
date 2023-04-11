package pt.tecnico.distledger.server.grpc;

import pt.tecnico.distledger.common.vectorclock.VectorClock;
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

    /* Given a ledgerState (list of operations), creates the corresponding message that is sent through the stub */
    private DistLedgerCommonDefinitions.LedgerState.Builder createLedgerStateMessage(List<Operation> ledgerState){

        DistLedgerCommonDefinitions.LedgerState.Builder ledgerStateMessage = DistLedgerCommonDefinitions.LedgerState.newBuilder();

        DistLedgerCommonDefinitions.OperationType opType;
        String userId;
        String destUserId;
        int amount;

        for (Operation op : ledgerState) {

            userId = op.getAccount();
            DistLedgerCommonDefinitions.Operation.Builder opMessage = DistLedgerCommonDefinitions.Operation.newBuilder();

            if (op.getType() == "OP_TRANSFER_TO") {
                opType = DistLedgerCommonDefinitions.OperationType.OP_TRANSFER_TO;
                destUserId = op.getDestAccount();
                amount = op.getAmount();

                opMessage.setType(opType);
                opMessage.setUserId(userId);
                opMessage.setDestUserId(destUserId);
                opMessage.setAmount(amount);
            }

            else if (op.getType() == "OP_DELETE_ACCOUNT") {
                opType = DistLedgerCommonDefinitions.OperationType.OP_DELETE_ACCOUNT;

                opMessage.setType(opType);
                opMessage.setUserId(userId);
            }

            else if (op.getType() == "OP_CREATE_ACCOUNT") {
                opType = DistLedgerCommonDefinitions.OperationType.OP_CREATE_ACCOUNT;

                opMessage.setType(opType);
                opMessage.setUserId(userId);
            }

            else {
                opType = DistLedgerCommonDefinitions.OperationType.OP_UNSPECIFIED;

                opMessage.setType(opType);
            }

            ledgerStateMessage.addLedger(opMessage.build());
        }

        return ledgerStateMessage;
    }

    public void propagateState(List<Operation> ledger, VectorClock vc) {

        // Convert the ledgerState to a valid stub message
        DistLedgerCommonDefinitions.LedgerState.Builder ledgerMessage = createLedgerStateMessage(ledger);

        stub.propagateState(PropagateStateRequest.newBuilder().setState(ledgerMessage).addAllReplicaTS(vc.getVectorClock()).build());
    }

    @Override
    public final void close() {
        channel.shutdown();
    }

}
