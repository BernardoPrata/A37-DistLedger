package pt.tecnico.distledger.server.domain.service;

import io.grpc.stub.StreamObserver;
import pt.tecnico.distledger.contract.DistLedgerCommonDefinitions;
import pt.tecnico.distledger.contract.distledgerserver.CrossServerDistLedger;
import pt.tecnico.distledger.contract.distledgerserver.DistLedgerCrossServerServiceGrpc;


public class NamingServerServiceImpl extends DistLedgerCrossServerServiceGrpc.DistLedgerCrossServerServiceImplBase {

    @Override
    public void register(CrossServerDistLedger.RegistrationRequest request, StreamObserver<CrossServerDistLedger.RegistrationResponse> responseObserver) {

        // read from the request
        String serviceName = request.getServiceName();
        String host = request.getHost();
        int port = request.getPort();
    }
}