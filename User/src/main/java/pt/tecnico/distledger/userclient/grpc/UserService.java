package pt.tecnico.distledger.userclient.grpc;

import pt.tecnico.distledger.contract.user.UserDistLedger.*;
import pt.tecnico.distledger.contract.user.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class UserService implements AutoCloseable{
    private ManagedChannel channel;
    private UserServiceGrpc.UserServiceBlockingStub stub;

    public UserService() {
    }

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

    public void transferTo(String from,String dest, int amount ){
         stub.transferTo(TransferToRequest.newBuilder().setAccountFrom(from).setAccountTo(dest).setAmount(amount).build());
    }

    public void updateServerAddress(String host, int port){
        // delete old channel and stub
        if (channel != null)
            channel.shutdown();

        // create new channel and stub
        this.channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
        this.stub = UserServiceGrpc.newBlockingStub(channel);
    }   

    @Override
    public final void close() {
        channel.shutdown();
    }
}


