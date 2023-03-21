package pt.tecnico.distledger.server.domain.grpc;


import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.tecnico.distledger.contract.NamingServer;
import pt.tecnico.distledger.contract.NamingServerServiceGrpc;
import pt.tecnico.distledger.contract.NamingServer.*;

public class NameService implements AutoCloseable {

    private final ManagedChannel channel;
    private final NamingServerServiceGrpc.NamingServerServiceBlockingStub stub;

    private final String namingServiceHost = "localhost";
    private final int namingServicePort = 5001;

    private final String serverAddress;

    public NameService(String host, int port) {
        channel = ManagedChannelBuilder.forAddress(namingServiceHost, namingServicePort).usePlaintext().build();
        stub = NamingServerServiceGrpc.newBlockingStub(channel);
        serverAddress = host + ":" + port;
        System.out.println("Server address: " + serverAddress);
    }

    public void register(String serviceName, String qualifier){
        RegisterRequest request = RegisterRequest.newBuilder().setServiceName(serviceName).setQualifier(qualifier).setServerAddress(serverAddress).build();
        stub.register(request);
    }


    @Override
    public final void close() {
        channel.shutdown();
    }
}