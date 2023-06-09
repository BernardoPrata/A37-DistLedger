package pt.tecnico.distledger.server.grpc;


import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.tecnico.distledger.contract.NamingServerServiceGrpc;
import pt.tecnico.distledger.contract.NamingServer.*;

import java.util.List;

public class NameService implements AutoCloseable {

    private final ManagedChannel channel;
    private final NamingServerServiceGrpc.NamingServerServiceBlockingStub stub;

    private static final String NAMING_SERVICE_HOST = "localhost";
    private static final int NAMING_SERVICE_PORT = 5001;

    private final String serverAddress;
    private int serverId = -1;

    public NameService(String host, int port) {
        channel = ManagedChannelBuilder.forAddress(NAMING_SERVICE_HOST, NAMING_SERVICE_PORT).usePlaintext().build();
        stub = NamingServerServiceGrpc.newBlockingStub(channel);
        serverAddress = host + ":" + port;
    }

    public void register(String serviceName, String qualifier){
        RegisterRequest request = RegisterRequest.newBuilder().setServiceName(serviceName).setQualifier(qualifier).setServerAddress(serverAddress).build();
        this.serverId = stub.register(request).getServerId();
    }

    public void delete(String serviceName){
        DeleteRequest request = DeleteRequest.newBuilder().setServiceName(serviceName).setServerAddress(serverAddress).build();
        stub.delete(request);
    }

    public List<String> lookup(String serviceName, String qualifier) {
        return stub.lookup(LookupRequest.newBuilder().setServiceName(serviceName).setQualifier(qualifier).build()).getServerAddressesList();
    }

    // Looks up all servers for a given service
    public List<String> lookup(String serviceName) {
        return lookup(serviceName, "");
    }

    public int getServerId() {
        return this.serverId;
    }

    @Override
    public final void close() {
        channel.shutdown();
    }
}