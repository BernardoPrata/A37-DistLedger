package pt.tecnico.distledger.userclient.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.tecnico.distledger.contract.NamingServerServiceGrpc;
import pt.tecnico.distledger.contract.NamingServer.*;

public class NamingServerService implements AutoCloseable {
  private final ManagedChannel channel;
  private final NamingServerServiceGrpc.NamingServerServiceBlockingStub stub;

  public NamingServerService(String host, int port) {
    this.channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();

    // Create a blocking stub.
    stub = NamingServerServiceGrpc.newBlockingStub(channel);
  }

  public LookupResponse lookup(String serviceName, String qualifier) {
    return stub.lookup(LookupRequest.newBuilder().setServiceName(serviceName).setQualifier(qualifier).build());
  }

  @Override
  public final void close() {
    channel.shutdown();
  }
}
