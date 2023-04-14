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
    private VectorClock lastGossipTs;
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
        this.lastGossipTs = new VectorClock(serverId);
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
        List<Operation> listOperationsCanBeStabilized = serverState.getListOperationsCanBeStabilized(this.valueTs);
        Operation operationToStabilize;

        while (listOperationsCanBeStabilized.size() != 0) {
            operationToStabilize = chooseStable(listOperationsCanBeStabilized);

            tryStabilizeOperation(operationToStabilize);

            listOperationsCanBeStabilized = serverState.getListOperationsCanBeStabilized(this.valueTs);
        }
    }

    // --------------------------------------------------------------
    // ----------------------- OPERATIONS ---------------------------
    // --------------------------------------------------------------





    public int balance(String id, List<Integer> prevTsList) throws AccountNotFoundException, ServerUnavailableException, ServerUpdatesOutdatedException {
        // if client has a more recent timestamp than server, throw exception
        VectorClock prevTs = new VectorClock(prevTsList);
        if (valueTs.givenVectorClockIsGreaterThanThis(prevTs)) {
            debug("balance: Server Updates Outdated\tServerTimeStamp: " + valueTs.toString() );
            throw new ServerUpdatesOutdatedException();
        }
        debug("balance\tServerTimeStamp: " + valueTs.toString() );
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

    /*
    - For each Operation in List
        - Se a Operation não for duplicada (OperationTS ≠ OperationTS todas Operations LedgerState)
            - Update ReplicaTS
                - `replicaTS = max(replicaTS, operationTS)`
            - Adiciona ao ladgerState objeto Operation
    - Enquanto houver operações instáveis que podem ser estabilizadas
        - Obtém a lista de operações que podem ser estáveis - `can_be_stabilized()`
        - Se a lista for vazia
            - `break;`
        - Escolhe uma operação da lista, de modo deterministico - `chooseStable()`
        - estabiliza a - executa `stabilize()`
    */
    // public synchronized List<Integer> addReplicOperations(List<Operation> replicOperations) {
    //     for (Operation replicOperation: replicOperations)
    //     {
    //         if (!serverState.isOperationDuplicated(replicOperation))
    //         {
    //             replicOperation.setUnstable();
    //             updateReplicaTsWithOperationTs(replicOperation.getOperationTs());
    //             serverState.addOperation(replicOperation);
    //         }
    //     }

    //     tryStabilizeAllOperations();

    //     return this.replicaTs.getVectorClockList();
    // }

    public synchronized void findServersAndGossip() throws StatusRuntimeException { // TODO fixme propagar estado

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
                otherServer.propagateState(serverState.getLedger(), replicaTs);
                //close connection with secondary server
                otherServer.close();
            } catch (StatusRuntimeException e) {
                System.err.println("Runtime Exception: " + e.getMessage());
            }
            // Receive gossip response from each server
            // TODO: Awaiting teacher's answer
        }

    }

    public synchronized void applyGossip(List<Operation> gossipersLedger, VectorClock replicaTs) {
        // FIXME: @PedromcaMartins pls fix, ty Pedrocas
        System.err.println("applyGossip> Applying gossip");
        assert false;
    }
}
