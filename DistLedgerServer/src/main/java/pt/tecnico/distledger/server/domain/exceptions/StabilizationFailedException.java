package pt.tecnico.distledger.server.domain.exceptions;

public class StabilizationFailedException extends DistLedgerServerException {

    public StabilizationFailedException() {
        super("Stabilization of Operation failed");
    }
}
