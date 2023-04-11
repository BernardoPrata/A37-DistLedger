package pt.tecnico.distledger.userclient.grpc;

import pt.tecnico.distledger.contract.user.UserDistLedger.*;
import pt.tecnico.distledger.contract.user.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.tecnico.distledger.common.vectorclock.VectorClock;

import java.util.ArrayList;
import java.util.List;

public class UserService implements AutoCloseable{
    private ManagedChannel channel;
    private UserServiceGrpc.UserServiceBlockingStub stub;

    private VectorClock vc;
    public UserService() {
        vc = new VectorClock();
    }
    public UserService(ManagedChannelBuilder<?> channelBuilder) {
        channel = channelBuilder.build();
        stub = UserServiceGrpc.newBlockingStub(channel);
        vc = new VectorClock();
    }

    public void createAccount(String username) {
        CreateAccountResponse response = stub.createAccount(CreateAccountRequest.newBuilder().
                setUserId(username).addAllPrevTS(vc.getVectorClock()).build());
        vc.set(response.getTSList());
        System.out.println("CreateAccount: Recebi do server a TS:  "+ vc.toString());
    }

    public void deleteAccount(String username){
         stub.deleteAccount(DeleteAccountRequest.newBuilder().
                 setUserId(username).build());
    }


    public int getBalance(String username) {
        BalanceResponse response = stub.balance(BalanceRequest.newBuilder().
                setUserId(username).addAllPrevTS(vc.getVectorClock()).build());
        vc.set(response.getValueTSList());
        System.out.println("CreateAccount: Recebi do server a TS:  "+ vc.toString());
        return response.getValue();
    }

    public void transferTo(String from,String dest, int amount ){
        TransferToResponse response = stub.transferTo(TransferToRequest.newBuilder().setAccountFrom(from).setAccountTo(dest).setAmount(amount).addAllPrevTS(vc.getVectorClock()).build());
        vc.set(response.getTSList());
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


