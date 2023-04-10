package pt.tecnico.distledger.server.domain;

import java.util.HashMap;
import pt.tecnico.distledger.server.domain.ServerState;
import pt.tecnico.distledger.server.domain.exceptions.*;

public class ReplicaManager {

    //define list with size two called vetorClock
    private int[] vetorClock = new int[2];
    private ServerState serverState;

    //move this to separate class
    // true <=> pedidoPrev <= vetorClock
    private boolean isStableRequest(int[] pedidoPrev) {
        for (int i = 0; i < 2; i++) {
            if (pedidoPrev[i] > vetorClock[i])
                return false;
        }
        return true;
    }

    public ReplicaManager(ServerState serverState) {
        this.serverState = serverState;
    }

    //(String id, int[] prevVetorClock)
    public int balance(String id) throws AccountNotFoundException, ServerUnavailableException {
        // compare clocks if not stable return error, as it is a reading op
        /* if (!isStableRequest(prevVetorClock))
        //    throw new ServerUnavailableException();*/
        return serverState.balance(id);

    }
    public void createAccount(String id)  throws AccountAlreadyExistsException, ServerUnavailableException, OtherServerUnavailableException, NotPrimaryServerException {
        // compare clocks if not stable return error, as it is a reading op
        /* if (!isStableRequest(prevVetorClock))
        //    throw new ServerUnavailableException();*/
        serverState.createAccount(id);
    }
    public void transferTo(String from, String to, int value) throws AccountNotFoundException, InsufficientBalanceException, ServerUnavailableException, OtherServerUnavailableException, InvalidBalanceException, NotPrimaryServerException  {
        // compare clocks if not stable return error, as it is a reading op
        /* if (!isStableRequest(prevVetorClock))
        //    throw new ServerUnavailableException();*/
         serverState.transferTo(from,to, value);
    }
    public void deleteAccount(String id) throws AccountNotFoundException, ServerUnavailableException, OtherServerUnavailableException, NotPrimaryServerException,BalanceNotZeroException {
        // compare clocks if not stable return error, as it is a reading op
        /* if (!isStableRequest(prevVetorClock))
        //    throw new ServerUnavailableException();*/
        serverState.deleteAccount(id);
    }
}
