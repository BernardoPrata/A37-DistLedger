package pt.tecnico.distledger.userclient.grpc;

import pt.tecnico.distledger.contract.user.UserDistLedger.*;
import pt.tecnico.distledger.contract.user.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.tecnico.distledger.common.vectorclock.VectorClock;

public class UserService implements AutoCloseable{
    private ManagedChannel channel;
    private UserServiceGrpc.UserServiceBlockingStub stub;
    private boolean toDebug;
    private VectorClock vc;
    public UserService(boolean toDebug) {
        vc = new VectorClock();
        this.toDebug = toDebug;
    }

    public void createAccount(String username) {
        debug("CreateAccount - vc: "+ vc.toString());
        CreateAccountResponse response = stub.createAccount(CreateAccountRequest.newBuilder().
                setUserId(username).addAllPrevTS(vc.getVectorClockList()).build());
        vc.mergeVectorClock(new VectorClock(response.getTSList()));
        debug("CreateAccount - updated vc: "+ vc.toString());
    }

    public void deleteAccount(String username){
         stub.deleteAccount(DeleteAccountRequest.newBuilder().
                 setUserId(username).build());
    }


    public int getBalance(String username) {
        debug("getBalance - vc: "+ vc.toString());
        BalanceResponse response = stub.balance(BalanceRequest.newBuilder().
                setUserId(username).addAllPrevTS(vc.getVectorClockList()).build());
        vc.mergeVectorClock(new VectorClock(response.getValueTSList()));
        debug("getBalance - updated vc: "+ vc.toString());
        return response.getValue();
    }

    public void transferTo(String from,String dest, int amount ){
        debug("transferTo - vc: "+ vc.toString());
        TransferToResponse response = stub.transferTo(TransferToRequest.newBuilder().setAccountFrom(from).setAccountTo(dest).setAmount(amount).addAllPrevTS(vc.getVectorClockList()).build());
        vc.mergeVectorClock(new VectorClock(response.getTSList()));
        debug("transferTo - updated vc: "+ vc.toString());

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

    public void debug(String debugMessage){
        if (this.toDebug)
            System.err.println(debugMessage);
    }
}


