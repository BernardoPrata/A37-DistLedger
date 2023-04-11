package pt.tecnico.distledger.userclient.grpc;

import pt.tecnico.distledger.contract.user.UserDistLedger.*;
import pt.tecnico.distledger.contract.user.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.ArrayList;
import java.util.List;

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
        List<Integer> list = new ArrayList<>(2);
        list.add(0);
        list.add(0);
        stub.createAccount(CreateAccountRequest.newBuilder().
                setUserId(username).addAllPrevTS(list).build());
    }

    public void deleteAccount(String username){
         stub.deleteAccount(DeleteAccountRequest.newBuilder().
                 setUserId(username).build());
    }


    public int getBalance(String username) {
        //BalanceResponse response = stub.balance(BalanceRequest.newBuilder().
        //        setUserId(username).build());
        List<Integer> list = new ArrayList<>(2);
        list.add(0);
        list.add(2);
        BalanceResponse response = stub.balance(BalanceRequest.newBuilder().
                setUserId(username).addAllPrevTS(list).build());

        return response.getValue();
    }

    public void transferTo(String from,String dest, int amount ){
        List<Integer> list = new ArrayList<>(2);
        list.add(0);
        list.add(0);
         stub.transferTo(TransferToRequest.newBuilder().setAccountFrom(from).setAccountTo(dest).setAmount(amount).addAllPrevTS(list).build());
    }

    public void updateServerAddress(String host, int port){
        if (channel != null)
        {
            // if the new address is the same as the old one, do nothing
            if (channel.authority().equals(host + ":" + port))
                return;

            // delete old channel
            channel.shutdown();
        }

        // create new channel and stub
        this.channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
        this.stub = UserServiceGrpc.newBlockingStub(channel);
    }   

    @Override
    public final void close() {
        channel.shutdown();
    }
}


