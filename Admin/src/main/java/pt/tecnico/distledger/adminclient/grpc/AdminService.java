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

    public ActivateResponse activate(ActivateRequest request) {
        return stub.activate(request);
    }

    public DeactivateResponse deactivate(DeactivateRequest request) {
        return stub.deactivate(request);
    }

    public getLedgerStateResponse getLadgerState(getLedgerStateRequest request) {
        return stub.getLedgerState(request);
    }

    // TODO: gossip method

	@Override
	public final void close() {
		channel.shutdown();
	}
}
