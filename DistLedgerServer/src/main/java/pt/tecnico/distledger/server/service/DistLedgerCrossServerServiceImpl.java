package pt.tecnico.distledger.server.service;

import io.grpc.stub.StreamObserver;
import pt.tecnico.distledger.common.vectorclock.VectorClock;
import pt.tecnico.distledger.contract.DistLedgerCommonDefinitions;
import pt.tecnico.distledger.contract.distledgerserver.CrossServerDistLedger.*;
import pt.tecnico.distledger.contract.distledgerserver.DistLedgerCrossServerServiceGrpc;
import pt.tecnico.distledger.server.domain.ReplicaManager;
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

    private final ServerState serverState;
    private final ReplicaManager replicaManager;

    public DistLedgerCrossServerServiceImpl(ServerState serverState, ReplicaManager replicaManager) {
        this.serverState = serverState;
        this.replicaManager = replicaManager;
    }

    private List<Operation> messageToLedgerState(DistLedgerCommonDefinitions.LedgerState ledgerStateMessage){

        List<Operation> newLedgerState = new ArrayList<>();

        for (DistLedgerCommonDefinitions.Operation opMessage : ledgerStateMessage.getLedgerList()) {

            if (opMessage.getType() == DistLedgerCommonDefinitions.OperationType.OP_TRANSFER_TO) {
                Operation transferOp = new TransferOp(opMessage.getUserId(), opMessage.getDestUserId(), opMessage.getAmount());
                transferOp.setOperationTs(messageToVectorClock(opMessage.getPrevTSList()));
                newLedgerState.add(transferOp);
            }

            else if (opMessage.getType() == DistLedgerCommonDefinitions.OperationType.OP_DELETE_ACCOUNT) {
                Operation deleteOp = new DeleteOp(opMessage.getUserId());
                deleteOp.setOperationTs(messageToVectorClock(opMessage.getPrevTSList()));
                newLedgerState.add(deleteOp);
            }

            else if (opMessage.getType() == DistLedgerCommonDefinitions.OperationType.OP_CREATE_ACCOUNT) {
                Operation createOp = new CreateOp(opMessage.getUserId());
                createOp.setOperationTs(messageToVectorClock(opMessage.getPrevTSList()));
                newLedgerState.add(createOp);
            }

        }

        return newLedgerState;
    }

    private VectorClock messageToVectorClock(List<Integer> timestampMessage) {
        return new VectorClock(timestampMessage);
    }

    @Override
    public void propagateState(PropagateStateRequest request, StreamObserver<PropagateStateResponse> responseObserver) {

        try {
             /* Get the messages from the request */
            DistLedgerCommonDefinitions.LedgerState ledgerMessage = request.getState();
            List<Integer> timestampMessage = request.getReplicaTSList();

            /* Special exception that occurs when the server is not activated */
            // TODO: Wait for prof. answer on what to do
            if (!serverState.isActivated()) {
                responseObserver.onError(UNAVAILABLE.withDescription("Server is Unavailable").asRuntimeException());
                return;
            }

            List<Operation> ledgerState = messageToLedgerState(ledgerMessage);
            VectorClock replicaTS = messageToVectorClock(timestampMessage);
            String replicaAdress = request.getReplicaAddress();

            replicaManager.applyGossip(ledgerState, replicaTS, replicaAdress);

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
