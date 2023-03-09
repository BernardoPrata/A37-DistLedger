package pt.tecnico.distledger.server.domain.exceptions;

public class ServerUnavailableException extends Exception {

    public ServerUnavailableException() {
        super("The server is not available.");
    }
}
