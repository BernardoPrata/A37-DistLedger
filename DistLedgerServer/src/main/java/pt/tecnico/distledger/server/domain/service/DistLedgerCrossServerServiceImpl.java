package pt.tecnico.distledger.server.domain.service;

import io.grpc.stub.StreamObserver;
import pt.tecnico.distledger.contract.DistLedgerCommonDefinitions;
import pt.tecnico.distledger.contract.distledgerserver.CrossServerDistLedger.*;
import pt.tecnico.distledger.contract.distledgerserver.DistLedgerCrossServerServiceGrpc;
import pt.tecnico.distledger.server.domain.ServerState;
import pt.tecnico.distledger.server.domain.operation.CreateOp;
import pt.tecnico.distledger.server.domain.operation.DeleteOp;
import pt.tecnico.distledger.server.domain.operation.Operation;
import pt.tecnico.distledger.server.domain.operation.TransferOp;

import java.util.ArrayList;
import java.util.List;

import static io.grpc.Status.*;

public class DistLedgerCrossServerServiceImpl extends DistLedgerCrossServerServiceGrpc.DistLedgerCrossServerServiceImplBase {

    private ServerState serverState;

    public DistLedgerCrossServerServiceImpl(ServerState serverState) {
        this.serverState = serverState;
    }

    private List<Operation> messageToLedgerState(DistLedgerCommonDefinitions.LedgerState ledgerStateMessage){

        List<Operation> newLedgerState = new ArrayList<>();

        for (DistLedgerCommonDefinitions.Operation opMessage : ledgerStateMessage.getLedgerList()) {

            if (opMessage.getType() == DistLedgerCommonDefinitions.OperationType.OP_TRANSFER_TO) {
                Operation transferOp = new TransferOp(opMessage.getUserId(), opMessage.getDestUserId(), opMessage.getAmount());
                newLedgerState.add(transferOp);
            }

            else if (opMessage.getType() == DistLedgerCommonDefinitions.OperationType.OP_DELETE_ACCOUNT) {
                Operation deleteOp = new DeleteOp(opMessage.getUserId());
                newLedgerState.add(deleteOp);
            }

            else if (opMessage.getType() == DistLedgerCommonDefinitions.OperationType.OP_CREATE_ACCOUNT) {
                Operation createOp = new CreateOp(opMessage.getUserId());
                newLedgerState.add(createOp);
            }

        }

        return newLedgerState;
    }

    @Override
    public void propagateState(PropagateStateRequest request, StreamObserver<PropagateStateResponse> responseObserver) {

        try {
            DistLedgerCommonDefinitions.LedgerState ledgerStateMessage = request.getState();

            List<Operation> newLedgerState = messageToLedgerState(ledgerStateMessage);

            serverState.setLedger(newLedgerState);

            PropagateStateResponse response = PropagateStateResponse.newBuilder().build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
        /* General exception is sent because the destiny server isn't responding */
        catch (Exception e) {
            responseObserver.onError(ABORTED.withDescription(e.getMessage()).asRuntimeException());
        }
    }
}
