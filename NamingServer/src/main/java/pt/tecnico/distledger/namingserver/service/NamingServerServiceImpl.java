package pt.tecnico.distledger.namingserver.service;

import pt.tecnico.distledger.namingserver.NamingServerState;
import pt.tecnico.distledger.contract.NamingServerServiceGrpc;
import java.util.List;
import pt.tecnico.distledger.contract.NamingServer.*;

import java.util.List;
import io.grpc.stub.StreamObserver;
import static io.grpc.Status.*;


public class NamingServerServiceImpl extends NamingServerServiceGrpc.NamingServerServiceImplBase {
    
    private final NamingServerState namingServerState;

    public NamingServerServiceImpl(NamingServerState namingServerState) {
        this.namingServerState = namingServerState;
    }

    @Override
    public void lookup(LookupRequest request, StreamObserver<LookupResponse> responseObserver) {
        String serviceName = request.getServiceName();
        String qualifier = request.getQualifier();

        List<String> hostsAdresses = namingServerState.lookup(serviceName, qualifier);
        LookupResponse response = LookupResponse.newBuilder().addAllServerAddresses(hostsAdresses).build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
    public void register(RegisterRequest request, StreamObserver<RegisterResponse> responseObserver) {
        try {
            String serviceName = request.getServiceName();
            String qualifier = request.getQualifier();
            String serverAddress = request.getServerAddress();

            namingServerState.register(serviceName, qualifier, serverAddress);
            RegisterResponse response = RegisterResponse.newBuilder().setServerId(namingServerState.generateServerId()).build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
        catch (Exception e) {
            responseObserver.onError(INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        }
    }
    @Override
    public void delete(DeleteRequest request, StreamObserver<DeleteResponse> responseObserver) {
        try {
            String serviceName = request.getServiceName();
            String serverAddress = request.getServerAddress();

            namingServerState.delete(serviceName,  serverAddress);
            DeleteResponse response = DeleteResponse.newBuilder().build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
        catch (Exception e) {
            responseObserver.onError(INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        }
    }
}
