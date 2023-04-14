# DistLedger

Distributed Systems Project 2022/2023

## Authors

**Group A37**

### Code Identification

In all source files (namely in the *groupId*s of the POMs), replace __GXX__ with your group identifier. The group
identifier consists of either A or T followed by the group number - always two digits. This change is important for 
code dependency management, to ensure your code runs using the correct components and not someone else's.

### Team Members


| Number | Name              | User                                 | Email                                                |
|--------|-------------------|--------------------------------------|------------------------------------------------------|
| 99184  | Bernardo Prata    | <https://github.com/BernardoPrata>   | <mailto:bernardo.almeida.prata@tecnico.ulisboa.pt>   |
| 99298  | Pedro Chaparro    | <https://github.com/PedroChaps>      | <mailto:pedro.chaparro@tecnico.ulisboa.pt>           |
| 99303  | Pedro Martins     | <https://github.com/PedromcaMartins> | <mailto:pedromcamartins@tecnico.ulisboa.pt>          |

## Getting Started

The overall system is made up of several modules. The main server is the _DistLedgerServer_. The clients are the _User_ 
and the _Admin_. The definition of messages and services is in the _Contract_. The future naming server
is the _NamingServer_.

See the [Project Statement](https://github.com/tecnico-distsys/DistLedger) for a complete domain and system description.

### Prerequisites

The Project is configured with Java 17 (which is only compatible with Maven >= 3.8), but if you want to use Java 11 you
can too -- just downgrade the version in the POMs.

To confirm that you have them installed and which versions they are, run in the terminal:

```s
javac -version
mvn -version
```

### Installation

To compile and install all modules:

```s
mvn clean install
```

### Running a module

#### Contract

If the definitions in the Contract are changed, those changes must be compiled in the contract before the other modules can be compiled.
To compile the Contract:

```s
cd Contract
mvn compile
```

#### Naming Server

The Naming Server must be running before the servers and requests to them are made (because servers need to register themselves in the Naming Server).  
To run the Naming Server:
)
```s
cd NamingServer
mvn compile exec:java <optional: -Dexec.args="-debug">
```

- `-debug`: optional flag that enables verbose output.

#### DistLedger Server

```s
cd DistLedgerServer
mvn compile exec:java -Dexec.args="<port> <qualifier> <optional: -debug>"
```

- `port`: port number where the server runs;
- `qualifier`: server identifier (E.g. "A", "B", "C", etc.);
- `-debug`: optional flag that enables verbose output.

Example execution:
```s
cd DistLedgerServer
mvn compile exec:java -Dexec.args="2001 A -debug"
```

#### User & Admin

```s
cd User # or `cd Admin`
mvn compile exec:java <optional: -Dexec.args="-debug">
```

- `-debug`: optional flag that enables verbose output.

To know the available commands, type `help` in the terminal.


## Built With

* [Maven](https://maven.apache.org/) - Build and dependency management tool;
* [gRPC](https://grpc.io/) - RPC framework.
