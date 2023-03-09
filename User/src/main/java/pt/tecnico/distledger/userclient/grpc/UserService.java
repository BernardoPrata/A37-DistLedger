package pt.tecnico.distledger.userclient.grpc;

import pt.tecnico.distledger.contract.user.UserDistLedger.*;
import pt.tecnico.distledger.contract.user.*;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;


public class UserService implements AutoCloseable{
    private final ManagedChannel channel;
    private final UserServiceGrpc.UserServiceBlockingStub stub;

    public UserService(String host, int port) {
        this(ManagedChannelBuilder.forAddress(host, port).usePlaintext());
    }

    public UserService(ManagedChannelBuilder<?> channelBuilder) {
        channel = channelBuilder.build();
        stub = UserServiceGrpc.newBlockingStub(channel);
    }

    public CreateAccountResponse createAccount(CreateAccountRequest request) {
        return stub.createAccount(request);
    }

    public DeleteAccountResponse deleteAccount(DeleteAccountRequest request){
        return stub.deleteAccount(request);
    }


    public BalanceResponse getBalance(BalanceRequest request) {
        return stub.balance(request);
    }

    public  TransferToResponse transferTo(TransferToRequest request ){
        return stub.transferTo(request);
    }

    @Override
    public final void close() {
        channel.shutdown();
    }
}


