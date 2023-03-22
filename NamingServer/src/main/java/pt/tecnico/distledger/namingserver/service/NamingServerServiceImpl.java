package pt.tecnico.distledger.namingserver.service;

import pt.tecnico.distledger.namingserver.NamingServerState;
import pt.tecnico.distledger.contract.NamingServerServiceGrpc;
import pt.tecnico.distledger.contract.ClientNamingServer.*;

import java.util.List;

import io.grpc.stub.StreamObserver;

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

}
