package pt.tecnico.distledger.server.domain;
import io.grpc.StatusRuntimeException;
import pt.tecnico.distledger.common.vectorclock.VectorClock;
import pt.tecnico.distledger.server.domain.operation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import pt.tecnico.distledger.server.domain.ServerState;
import pt.tecnico.distledger.server.domain.exceptions.*;
import pt.tecnico.distledger.server.grpc.DistLedgerCrossServerService;

public class ReplicaManager {

    private VectorClock valueTs;
    private VectorClock replicaTs;
    private HashMap<String, VectorClock> lastGossipTsMap;
    private ServerState serverState;

    // serverId serves as index to vectorClock
    private int serverId;

    private boolean toDebug;
    private void debug(String debugMessage) {
        if (toDebug)
            System.err.println(debugMessage);
    }

    public ReplicaManager(boolean toDebug, ServerState serverState, int serverId){
        this.valueTs = new VectorClock(serverId);
        this.replicaTs = new VectorClock(serverId);
        this.lastGossipTsMap = new HashMap<String, VectorClock>();
        this.serverState = serverState;
        this.serverId = serverId;
        this.toDebug = toDebug;

        debug("ReplicaManager: Created with serverId: " + serverId);
    }

    public List<Integer> getValueVectorClock(){
        return valueTs.getVectorClockList();
    }

    public List<Integer> getReplicaVectorClock(){
        return replicaTs.getVectorClockList();
    }

    // --------------------------------------------------------------
    // ------------------ VECTOR CLOCK OPERATIONS -------------------
    // --------------------------------------------------------------



    public synchronized void updateReplicaTsWithClientTs(){
        this.replicaTs.increment(serverId);

        debug("updateReplicaTsWithClientTs: " + replicaTs.toString());
    }

    public synchronized void updateReplicaTsWithOperationTs(VectorClock operationTs){
        this.replicaTs.mergeVectorClock(operationTs);

        debug("updateReplicaTsWithOperationTs: " + replicaTs.toString());
    }

    public synchronized void updateValueTsWithOperationTs(VectorClock operationTs){
        this.valueTs.mergeVectorClock(operationTs);

        debug("updateValueTsWithOperationTs: " + valueTs.toString());
    }

    public synchronized void addOrUpdateLastGossipTs(String replicaAddress, VectorClock gossipTs){
        if (lastGossipTsMap.containsKey(replicaAddress)) {
            lastGossipTsMap.get(replicaAddress).mergeVectorClock(gossipTs);
        } else {
            lastGossipTsMap.put(replicaAddress, gossipTs);
        }

        debug("addOrUpdateLastGossipTs: Entry for " + replicaAddress + " updated to " + lastGossipTsMap.get(replicaAddress).toString());
    }

    public VectorClock createOperationTimestamp(VectorClock clientTs){
        VectorClock operationTs = new VectorClock(clientTs);
        operationTs.setValueForServer(serverId, replicaTs.getValueForServer(serverId));
        return operationTs;
    }

    public Operation chooseStable(List<Operation> listOperationsCanBeStabilized) {
        Operation operationToStabilize = listOperationsCanBeStabilized.get(0);

        for (Operation op : listOperationsCanBeStabilized) {
            if (op.getOperationTs().compareTo(operationToStabilize.getOperationTs()) > 0) {
                operationToStabilize = op;
            }
        }

        return operationToStabilize;
    }

    // --------------------------------------------------------------
    // ------------------ STABILIZING OPERATIONS --------------------
    // --------------------------------------------------------------




    public boolean can_be_stabilized(Operation op) {
        if (op.isStable()) {
            return false;
        }

        return op.getOperationTs().can_be_stabilized(this.valueTs);
    }

    public synchronized void tryStabilizeOperation(Operation op) {
        if (can_be_stabilized(op)) {
            try {
                serverState.stabilize(op);
                updateValueTsWithOperationTs(op.getOperationTs());

                debug("tryStabilizeOperation: ValueTs changed: " + valueTs.toString());
            } catch (StabilizationFailedException e) {
                debug("tryStabilizeOperation: Exception: " + e.getMessage());
            }
        }
    }

    public synchronized void tryStabilizeAllOperations() {
        debug("tryStabilizeAllOperations: ValueTs: " + valueTs.toString());
        List<Operation> listOperationsCanBeStabilized = serverState.getListOperationsCanBeStabilized(this.valueTs);
        Operation operationToStabilize;

        while (listOperationsCanBeStabilized.size() != 0) {
            debug("tryStabilizeAllOperations: ListOperationsCanBeStabilized: " + listOperationsCanBeStabilized.toString());

            operationToStabilize = chooseStable(listOperationsCanBeStabilized);

            tryStabilizeOperation(operationToStabilize);

            listOperationsCanBeStabilized = serverState.getListOperationsCanBeStabilized(this.valueTs);
        }

        debug("tryStabilizeAllOperations: ValueTs changed: " + valueTs.toString());
    }

    // --------------------------------------------------------------
    // ----------------------- OPERATIONS ---------------------------
    // --------------------------------------------------------------





    public int balance(String id, List<Integer> prevTsList) throws AccountNotFoundException, ServerUnavailableException, ServerUpdatesOutdatedException {
        // if client has a more recent timestamp than server, throw exception
        VectorClock prevTs = new VectorClock(prevTsList);
        if (valueTs.givenVectorClockIsGreaterThanThis(prevTs)) {
            debug("balance: Server Updates Outdated\tServerValueTimeStamp: " + valueTs.toString() );
            throw new ServerUpdatesOutdatedException();
        }
        debug("balance\tServerValueTimeStamp: " + valueTs.toString() );
        return serverState.balance(id);
    }

    public void deleteAccount(String id) throws AccountNotFoundException, ServerUnavailableException, OtherServerUnavailableException, NotPrimaryServerException,BalanceNotZeroException {
        // simply call serverState as this is not part of phase 3 implementation
        serverState.deleteAccount(id);
    }

    public synchronized List<Integer> addClientOperation(Operation clientOperation, List<Integer> prevTsList) {
        VectorClock prevTs = new VectorClock(prevTsList);
        updateReplicaTsWithClientTs();

        clientOperation.setOperationTs(createOperationTimestamp(prevTs));

        serverState.addOperation(clientOperation);
        tryStabilizeAllOperations();

        debug("addClientOperation: Operation: " + clientOperation.toString() );
        return clientOperation.getOperationTs().getVectorClockList();
    }

    public synchronized void addReplicOperations(List<Operation> replicOperations) {
        for (Operation replicOperation: replicOperations)
        {
            if (!serverState.isOperationDuplicated(replicOperation))
            {
                replicOperation.setUnstable();
                updateReplicaTsWithOperationTs(replicOperation.getOperationTs());
                serverState.addOperation(replicOperation);
            }
        }

        tryStabilizeAllOperations();
    }

    public synchronized void findServersAndGossip() throws StatusRuntimeException {

        List<String> addresses = serverState.getNameService().lookup("DistLedger");

        for (String replicaAddress : addresses) {

            // Skip self
            if (replicaAddress.equals(serverState.getAddress()))
                continue;

            String hostname = replicaAddress.split(":")[0];
            int port = Integer.parseInt(replicaAddress.split(":")[1]);

            DistLedgerCrossServerService otherServer = new DistLedgerCrossServerService(hostname, port);
            debug("findServersAndGossip: Sending to " + hostname + ":" + port);
            try {
                otherServer.propagateState(getListOperationsToPropagateToReplic(replicaAddress), replicaTs, replicaAddress);
                //close connection with secondary server
                otherServer.close();
            } catch (StatusRuntimeException e) {
                System.err.println("Runtime Exception: " + e.getMessage());
            }
        }
    }

    public synchronized List<Operation> getListOperationsToPropagateToReplic(String replicaAddress) {
        // get from map the last gossip timestamp
        if (lastGossipTsMap.containsKey(replicaAddress)) {
            VectorClock lastGossipTs = lastGossipTsMap.get(replicaAddress);
            debug("getListOperationsToPropagateToReplic: Last gossip timestamp for " + replicaAddress + " is " + lastGossipTs.toString());
            return serverState.getListOperationsToPropagateToReplic(lastGossipTs);
        } else {
            debug("getListOperationsToPropagateToReplic: No gossip timestamp for " + replicaAddress + " found");
            return serverState.getListOperationsToPropagateToReplic(new VectorClock(serverId));
        }
    }
    
    public synchronized void applyGossip(List<Operation> gossipersLedger, VectorClock replicaTs, String replicaAddress) {
        debug("applyGossip> Applying gossip");
        
        addOrUpdateLastGossipTs(replicaAddress, replicaTs);
        addReplicOperations(gossipersLedger);

        debug("applyGossip> Gossip applied");
    }
}
