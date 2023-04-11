package pt.tecnico.distledger.server.domain;
import io.grpc.StatusRuntimeException;
import pt.tecnico.distledger.common.vectorclock.VectorClock;
import pt.tecnico.distledger.server.domain.operation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import pt.tecnico.distledger.server.domain.ServerState;
import pt.tecnico.distledger.server.domain.exceptions.*;
import pt.tecnico.distledger.server.grpc.DistLedgerCrossServerService;

public class ReplicaManager {

    private static final boolean STABLE = true;
    private static final boolean UNSTABLE = false;
    private boolean toDebug;

    private VectorClock valueTs;
    private ServerState serverState;

    // serverId serves as index to vectorClock
    private int serverId;
    public ReplicaManager(ServerState serverState, int serverId,boolean toDebug){
        this.valueTs = new VectorClock();
        this.serverState = serverState;
        this.serverId = serverId;
        this.toDebug = toDebug;
    }

    public List<Integer> getReplicaVectorClock(){
        return valueTs.getVectorClock();
    }



    public int balance(String id, List<Integer> prevTSList) throws AccountNotFoundException, ServerUnavailableException {
        VectorClock prevTs = new VectorClock(prevTSList);
        // STABLE ?  EXECUTE : THROW EXCEPTION
        if (valueTs.compareTo(prevTs) < 0) {
            throw new ServerUnavailableException();
        }
        debug("balance: ServerTimeStamp: " + valueTs.toString() );
        return serverState.balance(id);

    }
    public List<Integer> createAccount(String id,List<Integer> prevTsList) throws AccountAlreadyExistsException, ServerUnavailableException, OtherServerUnavailableException, NotPrimaryServerException, BalanceNotZeroException, InsufficientBalanceException, InvalidBalanceException, AccountNotFoundException {
        VectorClock prevTs = new VectorClock(new ArrayList<>(prevTsList));
        // STABLE ?  EXECUTE + ADD TO LEDGER WITH STABLE : ADD TO LEDGER WITH UNSTABLE


        // TABLE WILL STORE THE VECTORCLOCK OF CLIENT BUT WITH THIS SERVER UPDATED
        valueTs.increment(serverId); // UPDATE CURRENT SERVER VECTORCLOCK
        prevTs.setValueForServer(serverId,valueTs.getVectorClock().get(serverId)); // UPDATE CLIENT VECTORCLOCK

        if (valueTs.compareTo(prevTs) < 0) {
            serverState.addOperation(new CreateOp(id,UNSTABLE,prevTs));
            debug("createAccount: UNSTABLE");
        }
        else{
            CreateOp createOp = new CreateOp(id,STABLE,prevTs);
            serverState.performOperation(createOp);
            serverState.addOperation(createOp);
            debug("createAccount: STABLE");
        }

        debug("createAccount: ServerTimeStamp: " + valueTs.toString() );
        return valueTs.getVectorClock();
    }

    public List<Integer> transferTo(String from, String to, int value, List<Integer> prevTsList) throws AccountNotFoundException, InsufficientBalanceException, ServerUnavailableException, OtherServerUnavailableException, InvalidBalanceException, NotPrimaryServerException, BalanceNotZeroException, AccountAlreadyExistsException {
        VectorClock prevTs = new VectorClock(new ArrayList<>(prevTsList));
        // STABLE ?  EXECUTE + ADD TO LEDGER WITH STABLE : ADD TO LEDGER WITH UNSTABLE

        // TABLE WILL STORE THE VECTORCLOCK OF CLIENT BUT WITH THIS SERVER UPDATED
        valueTs.increment(serverId); // UPDATE CURRENT SERVER VECTORCLOCK
        prevTs.setValueForServer(serverId,valueTs.getVectorClock().get(serverId)); // UPDATE CLIENT VECTORCLOCK

        if (valueTs.compareTo(prevTs) < 0) {
            serverState.addOperation(new TransferOp(from,to,value,UNSTABLE,prevTs));
            debug("transferTo: UNSTABLE");
        }
        else {
            TransferOp transferOp = new TransferOp(from, to, value, STABLE,prevTs);
            serverState.performOperation(transferOp);
            serverState.addOperation(transferOp);
            debug("transferTo: STABLE");
        }
        debug("transferTo: ServerTimeStamp: " + valueTs.toString() );
        return valueTs.getVectorClock();
    }
    public void deleteAccount(String id) throws AccountNotFoundException, ServerUnavailableException, OtherServerUnavailableException, NotPrimaryServerException,BalanceNotZeroException {
        // simply call serverState as this is not part of phase 3 implementation
        serverState.deleteAccount(id);
    }

    private void debug(String debugMessage) {
        if (toDebug)
            System.err.println(debugMessage);
    }

    public synchronized void findServersAndGossip() throws StatusRuntimeException {

        List<String> addresses = serverState.getNameService().lookup("DistLedger");

        for (String adr : addresses) {

            // Skip self
            if (adr.equals(serverState.getAddress()))
                continue;

            String hostname = adr.split(":")[0];
            int port = Integer.parseInt(adr.split(":")[1]);

            DistLedgerCrossServerService otherServer = new DistLedgerCrossServerService(hostname, port);
            debug("findServersAndGossip: Sending to " + hostname + ":" + port);
            try {
                otherServer.propagateState(serverState.getLedger(), valueTs);
                //close connection with secondary server
                otherServer.close();
            } catch (StatusRuntimeException e) {
                System.err.println("Runtime Exception: " + e.getMessage());
            }
            // Receive gossip response from each server
            // TODO: Awaiting teacher's answer
        }

    }

    public synchronized void applyGossip(List<Operation> gossipersLedger, VectorClock replicaTS) {
        // FIXME: @PedromcaMartins pls fix, ty Pedrocas
        System.err.println("applyGossip> Applying gossip");
        assert false;
    }
}
