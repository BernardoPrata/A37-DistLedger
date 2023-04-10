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

    public ReplicaManager(ServerState serverState) {
        this.valueTs = new VectorClock();
        this.serverState = serverState;
    }

    //(String id, int[] prevVetorClock)
    public int balance(String id, List prevTSList) throws AccountNotFoundException, ServerUnavailableException {
        VectorClock prevTs = new VectorClock();

        prevTs.setVectorClock(prevTSList);
        System.out.println("prevTs:%s\n" + prevTs.toString());
        System.out.println("valueTs.compareTo(prevTs:%d\n" + valueTs.compareTo(prevTs));

        if (valueTs.compareTo(prevTs) < 0) {
            throw new ServerUnavailableException();
        }
        //if (valueTs.compareTo(prevTs) >= 0) {
        //    // valueTs and prevTs are equal or valueTs is greater than prevTs
        //    // STABLE
        //} else {
        //    // valueTs is less than prevTs <=>
        //    throw new ServerUnavailableException();
        //}

        // ESTAVEL? SIM -> EXECUTAR
        return serverState.balance(id);

    }
    public void createAccount(String id)  throws AccountAlreadyExistsException, ServerUnavailableException, OtherServerUnavailableException, NotPrimaryServerException {
        // OPERACAO ESCRITA ---
        // ESTAVEL? SIM -> EXECUTAR / ADICIONAR AO LEDGER COM ESTAVEL
        // ESTAVEL? NAO ->          / ADICIONAR AO LEDGER COM INSTAVEL
        // RETORNA SEMPRE O TIMESTAMP+1

        serverState.createAccount(id);
    }
    public void transferTo(String from, String to, int value) throws AccountNotFoundException, InsufficientBalanceException, ServerUnavailableException, OtherServerUnavailableException, InvalidBalanceException, NotPrimaryServerException  {
        // OPERACAO ESCRITA ---
        // ESTAVEL? SIM -> EXECUTAR / ADICIONAR AO LEDGER COM ESTAVEL
        // ESTAVEL? NAO ->          / ADICIONAR AO LEDGER COM INSTAVEL
        // RETORNA SEMPRE O TIMESTAMP+1

        serverState.transferTo(from,to, value);
    }
    public void deleteAccount(String id) throws AccountNotFoundException, ServerUnavailableException, OtherServerUnavailableException, NotPrimaryServerException,BalanceNotZeroException {
        // simply call serverState as this is not part of phase 3 implementation
        serverState.deleteAccount(id);
    }
}
