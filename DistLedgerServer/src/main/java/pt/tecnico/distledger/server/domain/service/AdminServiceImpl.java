package pt.tecnico.distledger.server.domain.service;

import io.grpc.stub.StreamObserver;
import pt.tecnico.distledger.contract.DistLedgerCommonDefinitions;
import pt.tecnico.distledger.contract.admin.AdminDistLedger.*;
import pt.tecnico.distledger.contract.user.UserDistLedger;
import pt.tecnico.distledger.server.*;
import pt.tecnico.distledger.contract.admin.AdminServiceGrpc;
import pt.tecnico.distledger.server.domain.ServerState;
import pt.tecnico.distledger.server.domain.operation.Operation;

import java.util.List;

import static io.grpc.Status.INVALID_ARGUMENT;
import static io.grpc.Status.NOT_FOUND;

public class AdminServiceImpl extends AdminServiceGrpc.AdminServiceImplBase {

    private boolean toDebug;
    private ServerState serverState;

    public AdminServiceImpl(boolean toDebug, ServerState serverState) {
        this.toDebug = toDebug;
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
//
//        List<Operation> ledgerState = serverState.getLedger();
//        DistLedgerCommonDefinitions.LedgerState.Builder ledgerStateBuilder = DistLedgerCommonDefinitions.LedgerState.newBuilder();
//        for (Operation op : ledgerState) {
//        }
//
//        getLedgerStateResponse response = getLedgerStateResponse.newBuilder().setLedgerState(ledgerState).build();
//        responseObserver.onNext(response);
//        responseObserver.onCompleted();
    }

}
