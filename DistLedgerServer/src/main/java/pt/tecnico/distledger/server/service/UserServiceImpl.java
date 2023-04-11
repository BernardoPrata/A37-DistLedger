package pt.tecnico.distledger.server.service;

import io.grpc.stub.StreamObserver;
import pt.tecnico.distledger.contract.user.UserDistLedger.*;
import pt.tecnico.distledger.contract.user.UserServiceGrpc;
import pt.tecnico.distledger.server.domain.ReplicaManager;
import pt.tecnico.distledger.server.domain.exceptions.DistLedgerServerException;

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
            BalanceResponse response = BalanceResponse.newBuilder().setValue(balance).addAllValueTS(replicaManager.updateVectorClock(request.getPrevTSList())).build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
        catch (DistLedgerServerException e) {
            responseObserver.onError(NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
        }

    }

    @Override
    public void createAccount(CreateAccountRequest request, StreamObserver<CreateAccountResponse> responseObserver) {
        try {
           // List<Integer> tS = replicaManager.createAccount(request.getUserId(),request.getPrevTSList());
            CreateAccountResponse response = CreateAccountResponse.newBuilder().addAllTS(replicaManager.createAccount(request.getUserId(),request.getPrevTSList())).build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
        catch (DistLedgerServerException e) {
            responseObserver.onError(INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        }
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
        try {
            String from = request.getAccountFrom(), to = request.getAccountTo();
            int value = request.getAmount();

            //List<Integer> tS = replicaManager.transferTo(from, to, value,request.getPrevTSList());
            TransferToResponse response = TransferToResponse.newBuilder().addAllTS(replicaManager.transferTo(from, to, value,request.getPrevTSList())).build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
        catch (DistLedgerServerException e) {
            responseObserver.onError(INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        }
    }

}