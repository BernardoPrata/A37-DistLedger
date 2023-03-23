package pt.tecnico.distledger.server.domain.service;

import io.grpc.stub.StreamObserver;
import pt.tecnico.distledger.contract.DistLedgerCommonDefinitions;
import pt.tecnico.distledger.contract.distledgerserver.CrossServerDistLedger.*;
import pt.tecnico.distledger.contract.distledgerserver.DistLedgerCrossServerServiceGrpc;
import pt.tecnico.distledger.server.domain.ServerState;
import pt.tecnico.distledger.server.domain.exceptions.ServerUnavailableException;
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

    private Operation messageToOperation(DistLedgerCommonDefinitions.Operation opMessage){

        if (opMessage.getType() == DistLedgerCommonDefinitions.OperationType.OP_TRANSFER_TO) {
            return new TransferOp(opMessage.getUserId(), opMessage.getDestUserId(), opMessage.getAmount());
        }

        else if (opMessage.getType() == DistLedgerCommonDefinitions.OperationType.OP_DELETE_ACCOUNT) {
            return new DeleteOp(opMessage.getUserId());
        }

        else if (opMessage.getType() == DistLedgerCommonDefinitions.OperationType.OP_CREATE_ACCOUNT) {
            return new CreateOp(opMessage.getUserId());
        }

        // The Server never sends an operation that is not one of the above
        throw new RuntimeException("Operation type not supported");
    }

    @Override
    public void propagateState(PropagateStateRequest request, StreamObserver<PropagateStateResponse> responseObserver) {

        try {
            DistLedgerCommonDefinitions.Operation opMessage = request.getOp();

            /* Special exception that occurs when the server is not activated */
            if (!serverState.isActivated()) {
                responseObserver.onError(UNAVAILABLE.withDescription("Server is Unavailable").asRuntimeException());
                return;
            }

            Operation op = messageToOperation(opMessage);
            serverState.performOperation(op);

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
