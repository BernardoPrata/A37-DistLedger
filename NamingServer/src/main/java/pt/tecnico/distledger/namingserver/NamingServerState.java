package pt.tecnico.distledger.namingserver.domain;

import pt.tecnico.distledger.namingserver.domain.ServiceEntry;

import java.util.HashMap;

public class NamingServerState {
    
    HashMap<String, ServiceEntry> serviceEntries;
    private boolean toDebug;

    public NamingServerState(boolean toDebug) {
        this.serviceEntries = new HashMap<String, ServiceEntry>();
        this.toDebug = toDebug;
    }

    private void debug(String debugMessage) {
        if (toDebug)
            System.err.println(debugMessage);
    }

    public void addServiceEntry(ServiceEntry serviceEntry) {
        this.serviceEntries.put(serviceEntry.getServiceName(), serviceEntry);
    }

    public ServiceEntry getServiceEntry(String serviceName) {
        return this.serviceEntries.get(serviceName);
    }

    public void removeServiceEntry(String serviceName) {
        this.serviceEntries.remove(serviceName);
    }

    public void addServerEntryToServiceEntry(String serviceName, ServerEntry serverEntry) {
        // if service exists
        ServiceEntry serviceEntry = this.serviceEntries.get(serviceName);
        // else create service //TODO

        serviceEntry.addServerEntry(serverEntry);
    }

    // TODO: choose a server from the list to give to the client (write to Primary, read from Primary and Backup)

}
