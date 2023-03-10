package pt.tecnico.distledger.server.domain.service;

import io.grpc.stub.StreamObserver;
import pt.tecnico.distledger.contract.DistLedgerCommonDefinitions;
import pt.tecnico.distledger.contract.admin.AdminDistLedger.*;
import pt.tecnico.distledger.contract.admin.AdminServiceGrpc;
import pt.tecnico.distledger.server.domain.ServerState;
import pt.tecnico.distledger.server.domain.operation.Operation;

import java.util.List;

public class AdminServiceImpl extends AdminServiceGrpc.AdminServiceImplBase {

    private final ServerState serverState;

    public AdminServiceImpl(ServerState serverState) {
        this.serverState = serverState;
    }

    @Override
    public void activate(ActivateRequest request, StreamObserver<ActivateResponse> responseObserver) {
        serverState.activate();
    }

    @Override
    public void deactivate(DeactivateRequest request, StreamObserver<DeactivateResponse> responseObserver) {
        serverState.deactivate();
    }

    @Override
    public void gossip(GossipRequest request, StreamObserver<GossipResponse> responseObserver) {
        // 3rd part of the project
        return;
    }

    @Override
    public void getLedgerState(getLedgerStateRequest request, StreamObserver<getLedgerStateResponse> responseObserver) {

        List<Operation> ledgerState = serverState.getLedger();
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
                userId = op.getAccount();
                destUserId = op.getDestAccount();
                amount = op.getAmount();

                opMessage.setType(opType);
                opMessage.setUserId(userId);
                opMessage.setDestUserId(destUserId);
                opMessage.setAmount(amount);

                ledgerStateMessage.addLedger(opMessage.build());
            }

            else if (op.getType() == "OP_DELETE_ACCOUNT") {
                opType = DistLedgerCommonDefinitions.OperationType.OP_DELETE_ACCOUNT;

                opMessage.setType(opType);
                opMessage.setUserId(userId);

                ledgerStateMessage.addLedger(opMessage.build());
            }

            else if (op.getType() == "OP_CREATE_ACCOUNT") {
                opType = DistLedgerCommonDefinitions.OperationType.OP_CREATE_ACCOUNT;

                opMessage.setType(opType);
                opMessage.setUserId(userId);

                ledgerStateMessage.addLedger(opMessage.build());
            }

            else {
                opType = DistLedgerCommonDefinitions.OperationType.OP_UNSPECIFIED;

                opMessage.setType(opType);
                ledgerStateMessage.addLedger(opMessage.build());
            }
        }

        getLedgerStateResponse response = getLedgerStateResponse.newBuilder().setLedgerState(ledgerStateMessage.build()).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

}