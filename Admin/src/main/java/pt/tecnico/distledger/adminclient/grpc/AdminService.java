package pt.tecnico.distledger.adminclient.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.tecnico.distledger.contract.admin.AdminServiceGrpc;
import pt.tecnico.distledger.contract.admin.AdminDistLedger.*;
import pt.tecnico.distledger.contract.user.UserServiceGrpc;

public class AdminService implements AutoCloseable {
	private ManagedChannel channel;
	private AdminServiceGrpc.AdminServiceBlockingStub stub;

    public AdminService() {
    }

    public AdminService(String host, int port) {
		this(ManagedChannelBuilder.forAddress(host, port).usePlaintext());
    }

    public AdminService(ManagedChannelBuilder<?> channelBuilder) {
        channel = channelBuilder.build();
        stub = AdminServiceGrpc.newBlockingStub(channel);
    }

    public void activate() {
        stub.activate(ActivateRequest.newBuilder().build());
    }

    public void deactivate() {
        stub.deactivate(DeactivateRequest.newBuilder().build());
    }

    public getLedgerStateResponse getLadgerState() {
        return stub.getLedgerState(getLedgerStateRequest.newBuilder().build());
    }

    public void updateServerAddress(String host, int port){
        // delete old channel and stub
        if (channel != null)
            channel.shutdown();

        // create new channel and stub
        this.channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
        this.stub = AdminServiceGrpc.newBlockingStub(channel);
    }

    // TODO: gossip method

	@Override
	public final void close() {
		channel.shutdown();
	}
}
