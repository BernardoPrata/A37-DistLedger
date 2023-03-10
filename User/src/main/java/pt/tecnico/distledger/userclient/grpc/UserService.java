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

    public void createAccount(String username) {
        stub.createAccount(CreateAccountRequest.newBuilder().
                setUserId(username).build());
    }

    public void deleteAccount(String username){
         stub.deleteAccount(DeleteAccountRequest.newBuilder().
                 setUserId(username).build());
    }


    public int getBalance(String username) {
        BalanceResponse response = stub.balance(BalanceRequest.newBuilder().
                setUserId(username).build());
        return response.getValue();
    }

    public  void transferTo(String from,String dest, int amount ){
         stub.transferTo(TransferToRequest.newBuilder().setAccountFrom(from).setAccountTo(dest).setAmount(amount).build());
    }

    @Override
    public final void close() {
        channel.shutdown();
    }
}


