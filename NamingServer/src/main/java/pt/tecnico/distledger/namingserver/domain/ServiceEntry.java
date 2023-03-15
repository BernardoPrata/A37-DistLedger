package pt.tecnico.distledger.namingserver.domain;

import pt.tecnico.distledger.namingserver.domain.ServerEntry;

import java.util.List;
import java.util.ArrayList;

public class ServiceEntry {
    
    List<ServerEntry> serverEntries;

    String serviceName;

    public ServiceEntry(String serviceName) {
        this.serverEntries = new ArrayList<ServerEntry>();
        this.serviceName = serviceName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public List<ServerEntry> getServerEntries() {
        return serverEntries;
    }

    public void addServerEntry(ServerEntry serverEntry) {
        this.serverEntries.add(serverEntry);
    }

}
