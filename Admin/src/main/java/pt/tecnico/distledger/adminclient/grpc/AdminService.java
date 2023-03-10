package pt.tecnico.distledger.adminclient.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.tecnico.distledger.contract.admin.AdminServiceGrpc;
import pt.tecnico.distledger.contract.admin.AdminDistLedger.*;

public class AdminService implements AutoCloseable {
	private final ManagedChannel channel;
	private final AdminServiceGrpc.AdminServiceBlockingStub stub;
    
    public AdminService(String host, int port) {
		this.channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();

		// Create a blocking stub.
		stub = AdminServiceGrpc.newBlockingStub(channel);
    }

    public void activate(String server) {
        stub.activate(ActivateRequest.newBuilder().build());
    }

    public void deactivate(String server) {
        stub.deactivate(DeactivateRequest.newBuilder().build());
    }

    public getLedgerStateResponse getLadgerState(String server) {
        return stub.getLedgerState(getLedgerStateRequest.newBuilder().build());
    }

    // TODO: gossip method

	@Override
	public final void close() {
		channel.shutdown();
	}
}
