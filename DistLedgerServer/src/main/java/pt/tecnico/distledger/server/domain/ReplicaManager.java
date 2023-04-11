package pt.tecnico.distledger.server.domain;
import pt.tecnico.distledger.common.vectorclock.VectorClock;
import pt.tecnico.distledger.server.domain.operation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import pt.tecnico.distledger.server.domain.ServerState;
import pt.tecnico.distledger.server.domain.exceptions.*;

public class ReplicaManager {

    private VectorClock valueTs;
    private ServerState serverState;

    // serverId serves as index to vectorClock
    private int serverId;
    public ReplicaManager(ServerState serverState, int serverId){
        this.valueTs = new VectorClock();
        this.serverState = serverState;
        this.serverId = serverId;
    }

    // Method defined to be called by UserServiceImpl to update the vector clock for balance request
    public List<Integer> updateVectorClock(List<Integer> vecToUpdate){
        VectorClock prevTs = new VectorClock(vecToUpdate);
        return valueTs.mergeVectorClocks(prevTs);
    }

    public int balance(String id, List<Integer> prevTSList) throws AccountNotFoundException, ServerUnavailableException {
        VectorClock prevTs = new VectorClock(prevTSList);
        // STABLE ?  EXECUTE : THROW EXCEPTION
        if (valueTs.compareTo(prevTs) < 0) {
            System.out.println("Valor do server: "+ valueTs.toString() +"Valor do prevTs: "+ prevTs.toString());
            throw new ServerUnavailableException();
        }
        return serverState.balance(id);

    }
    public List<Integer> createAccount(String id,List<Integer> prevTsList)  throws AccountAlreadyExistsException, ServerUnavailableException, OtherServerUnavailableException, NotPrimaryServerException {
        VectorClock prevTs = new VectorClock(new ArrayList<>(prevTsList));
        // STABLE ?  EXECUTE + ADD TO LEDGER WITH STABLE : ADD TO LEDGER WITH UNSTABLE
        if (valueTs.compareTo(prevTs) < 0) {
            serverState.addOperation(new CreateOp(id,false));
            System.out.println("createAccount: Nao Realizada");
        }
        else{
        CreateOp createOp = new CreateOp(id,true);
        serverState.performOperation(createOp);
        serverState.addOperation(createOp);
        valueTs.increment(serverId);
            System.out.println("createAccount: Realizada");

        }
        // TODO: WHEN ITS UNSTABLE DO I ALSO INCREMENT THE valueTs?
        prevTs.increment(serverId);
        return valueTs.mergeVectorClocks(prevTs);
    }

    public List<Integer> transferTo(String from, String to, int value, List<Integer> prevTsList) throws AccountNotFoundException, InsufficientBalanceException, ServerUnavailableException, OtherServerUnavailableException, InvalidBalanceException, NotPrimaryServerException  {
        VectorClock prevTs = new VectorClock(new ArrayList<>(prevTsList));
        if (valueTs.compareTo(prevTs) < 0) {
            serverState.addOperation(new TransferOp(from,to,value,false));
            System.out.println("transferTo: Nao Realizada");

        }
        else {
            TransferOp transferOp = new TransferOp(from, to, value, true);
            serverState.performOperation(transferOp);
            serverState.addOperation(transferOp);
            valueTs.increment(serverId);
            System.out.println("transferTo: Realizada");

        }
        // TODO: WHEN ITS UNSTABLE DO I ALSO INCREMENT THE valueTs?
        prevTs.increment(serverId);
        return valueTs.mergeVectorClocks(prevTs);
    }
    public void deleteAccount(String id) throws AccountNotFoundException, ServerUnavailableException, OtherServerUnavailableException, NotPrimaryServerException,BalanceNotZeroException {
        // simply call serverState as this is not part of phase 3 implementation
        serverState.deleteAccount(id);
    }
}
