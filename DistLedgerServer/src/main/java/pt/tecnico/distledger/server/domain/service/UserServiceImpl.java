package pt.tecnico.distledger.server.domain.service;

import io.grpc.stub.StreamObserver;
import pt.tecnico.distledger.contract.user.UserDistLedger.*;
import pt.tecnico.distledger.contract.user.UserServiceGrpc;
import pt.tecnico.distledger.server.domain.ServerState;
import pt.tecnico.distledger.server.domain.exceptions.DistLedgerServerException;

import static io.grpc.Status.*;

public class UserServiceImpl extends UserServiceGrpc.UserServiceImplBase {

    private final ServerState serverState;

    public UserServiceImpl(ServerState serverState) {
        this.serverState = serverState;
    }
    @Override
    public void balance(BalanceRequest request, StreamObserver<BalanceResponse> responseObserver) {

        try {
            int balance = serverState.balance(request.getUserId());
            BalanceResponse response = BalanceResponse.newBuilder().setValue(balance).build();
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
            serverState.createAccount(request.getUserId());
            CreateAccountResponse response = CreateAccountResponse.newBuilder().build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
        catch (DistLedgerServerException e) {
            responseObserver.onError(ALREADY_EXISTS.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void deleteAccount(DeleteAccountRequest request, StreamObserver<DeleteAccountResponse> responseObserver) {
        try {
            serverState.deleteAccount(request.getUserId());
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

            serverState.transferTo(from, to, value);
            TransferToResponse response = TransferToResponse.newBuilder().build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
        catch (DistLedgerServerException e) {
            responseObserver.onError(ABORTED.withDescription(e.getMessage()).asRuntimeException());
        }
    }

}