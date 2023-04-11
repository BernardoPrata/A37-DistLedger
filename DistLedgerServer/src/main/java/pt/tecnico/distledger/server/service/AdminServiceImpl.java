package pt.tecnico.distledger.server.service;

import io.grpc.stub.StreamObserver;
import pt.tecnico.distledger.contract.DistLedgerCommonDefinitions;
import pt.tecnico.distledger.contract.admin.AdminDistLedger.*;
import pt.tecnico.distledger.contract.admin.AdminServiceGrpc;
import pt.tecnico.distledger.server.domain.ReplicaManager;
import pt.tecnico.distledger.server.domain.ServerState;
import pt.tecnico.distledger.server.domain.operation.Operation;

import java.util.List;

public class AdminServiceImpl extends AdminServiceGrpc.AdminServiceImplBase {

    private final ServerState serverState;
    private final ReplicaManager replicaManager;

    public AdminServiceImpl(ServerState serverState, ReplicaManager replicaManager) {
        this.serverState = serverState;
        this.replicaManager = replicaManager;
    }

    @Override
    public void activate(ActivateRequest request, StreamObserver<ActivateResponse> responseObserver) {
        serverState.activate();

        ActivateResponse response = ActivateResponse.newBuilder().build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void deactivate(DeactivateRequest request, StreamObserver<DeactivateResponse> responseObserver) {
        serverState.deactivate();

        DeactivateResponse response = DeactivateResponse.newBuilder().build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void gossip(GossipRequest request, StreamObserver<GossipResponse> responseObserver) {
        replicaManager.findServersAndGossip();

        GossipResponse response = GossipResponse.newBuilder().build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getLedgerState(getLedgerStateRequest request, StreamObserver<getLedgerStateResponse> responseObserver) {

        List<Operation> ledgerState = serverState.getLedger();
        System.out.println("Ledger state: " + ledgerState);
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

                // TODO: throw exception ???

                opMessage.setType(opType);
            }
        
            ledgerStateMessage.addLedger(opMessage.build());
        }

        getLedgerStateResponse response = getLedgerStateResponse.newBuilder().setLedgerState(ledgerStateMessage.build()).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

}