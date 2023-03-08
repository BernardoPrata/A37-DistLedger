package pt.tecnico.distledger.server.domain.service;

import io.grpc.stub.StreamObserver;
import pt.tecnico.distledger.contract.admin.AdminDistLedger;
import pt.tecnico.distledger.server.*;
import pt.tecnico.distledger.contract.admin.AdminServiceGrpc;
import static io.grpc.Status.INVALID_ARGUMENT;

public class AdminServiceImpl extends AdminServiceGrpc.AdminServiceImplBase {

    @Override
    public void activate(AdminDistLedger.ActivateRequest request, StreamObserver<AdminDistLedger.ActivateResponse> responseObserver) {
        // TODO
    }

    @Override
    public void deactivate(AdminDistLedger.DeactivateRequest request, StreamObserver<AdminDistLedger.DeactivateResponse> responseObserver) {
        // TODO
    }

    @Override
    public void gossip(AdminDistLedger.GossipRequest request, StreamObserver<AdminDistLedger.GossipResponse> responseObserver) {
        // 3rd part of the project
        return;
    }

    @Override
    public void getLedgerState(AdminDistLedger.getLedgerStateRequest request, StreamObserver<AdminDistLedger.getLedgerStateResponse> responseObserver) {
        // TODO
    }

}
