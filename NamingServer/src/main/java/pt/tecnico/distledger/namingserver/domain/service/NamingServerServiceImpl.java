package pt.tecnico.distledger.namingserver.domain.service;

import pt.tecnico.distledger.namingserver.domain.NamingServerState;

import io.grpc.stub.StreamObserver;

public class NamingServerServiceImpl { // extends NamingServerServiceGrpc.AdminServiceImplBase { //TODO: descomentar dps de implementar o servico
    
    private final NamingServerState namingServerState;

    public NamingServerServiceImpl(NamingServerState namingServerState) {
        this.namingServerState = namingServerState;
    }

}
