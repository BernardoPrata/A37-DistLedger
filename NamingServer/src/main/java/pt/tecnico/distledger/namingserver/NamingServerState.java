package pt.tecnico.distledger.namingserver;

import pt.tecnico.distledger.namingserver.domain.ServiceEntry;

import pt.tecnico.distledger.namingserver.domain.ServerEntry;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public class NamingServerState {
    
    HashMap<String, ServiceEntry> serviceEntries;
    private boolean toDebug;

    public NamingServerState(boolean toDebug) {
        HashMap services = new HashMap<String, ServiceEntry>();
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

    public List<String> lookup(String serviceName, String qualifier) {
        ServiceEntry serviceEntry = this.serviceEntries.get(serviceName);
        if (serviceEntry == null) {
            debug("Service " + serviceName + " not found");
            return new ArrayList<String>();
        }

        return serviceEntry.lookup(qualifier);
    }

}
