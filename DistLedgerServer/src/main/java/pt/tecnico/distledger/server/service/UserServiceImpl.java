package pt.tecnico.distledger.server.service;

import io.grpc.stub.StreamObserver;
import pt.tecnico.distledger.contract.user.UserDistLedger.*;
import pt.tecnico.distledger.contract.user.UserServiceGrpc;
import pt.tecnico.distledger.server.domain.ReplicaManager;
import pt.tecnico.distledger.server.domain.exceptions.DistLedgerServerException;
import pt.tecnico.distledger.server.domain.operation.CreateOp;
import pt.tecnico.distledger.server.domain.operation.Operation;
import pt.tecnico.distledger.server.domain.operation.TransferOp;

import java.util.List;

import static io.grpc.Status.*;

public class UserServiceImpl extends UserServiceGrpc.UserServiceImplBase {

    private final ReplicaManager replicaManager ;
    public UserServiceImpl(ReplicaManager replicaManager) {
        this.replicaManager = replicaManager;
    }
    @Override
    public void balance(BalanceRequest request, StreamObserver<BalanceResponse> responseObserver) {
        try {
            int balance = replicaManager.balance(request.getUserId(),request.getPrevTSList());
            BalanceResponse response = BalanceResponse.newBuilder().setValue(balance).addAllValueTS(replicaManager.getValueVectorClock()).build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
        catch (DistLedgerServerException e) {
            responseObserver.onError(NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
        }

    }

    @Override
    public void createAccount(CreateAccountRequest request, StreamObserver<CreateAccountResponse> responseObserver) {
        Operation clientOperation = new CreateOp(request.getUserId());
        responseObserver.onNext(CreateAccountResponse.newBuilder().addAllTS(replicaManager.addClientOperation(clientOperation, request.getPrevTSList())).build());
        responseObserver.onCompleted();
    }

    @Override
    public void deleteAccount(DeleteAccountRequest request, StreamObserver<DeleteAccountResponse> responseObserver) {
        try {
            replicaManager.deleteAccount(request.getUserId());
            DeleteAccountResponse response = DeleteAccountResponse.newBuilder().build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
        catch (DistLedgerServerException e) {
            responseObserver.onError(NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void transferTo(TransferToRequest request, StreamObserver<TransferToResponse> responseObserver) {
            Operation clientOperation = new TransferOp(request.getAccountFrom(), request.getAccountTo(), request.getAmount());
            responseObserver.onNext(TransferToResponse.newBuilder().addAllTS(replicaManager.addClientOperation(clientOperation, request.getPrevTSList())).build());
            responseObserver.onCompleted();
    }
}