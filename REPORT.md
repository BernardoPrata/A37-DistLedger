# DistLedger's final delivery report

Distributed Systems Project 2022/2023

## Authors

**Group A37**

### Team Members


| Number | Name              | User                                 | Email                                                |
|--------|-------------------|--------------------------------------|------------------------------------------------------|
| 99184  | Bernardo Prata    | <https://github.com/BernardoPrata>   | <mailto:bernardo.almeida.prata@tecnico.ulisboa.pt>   |
| 99298  | Pedro Chaparro    | <https://github.com/PedroChaps>      | <mailto:pedro.chaparro@tecnico.ulisboa.pt>           |
| 99303  | Pedro Martins     | <https://github.com/PedromcaMartins> | <mailto:pedromcamartins@tecnico.ulisboa.pt>          |

## Report

This report explains our solution to the DistLedger project. 

### Functionalities

#### Vector Clock

Since the Gossip algorithm uses timestamps to determine the order of messages, a Vector Clock class was implemented.

The Vector Clock is a data structure that is used to keep track of the order of events in a distributed system. 
It is composed of a variable-length list of each server time (to allow >2 servers) and defines various helper methods that help compare and do timestamp's operations. 

#### Replica Manager (RM)

To encapsulate the gossip's logic, a Replica Manager (RM) class was implemented.

The RM is responsible for the replica communication's logic: managing the sending and receiving of gossip's messages, keeping track of timestamps, etc.  
It is composed of the `valueTS` and `replicaTS` timestamps from the gossip algorithm, other implementation-specific variables and many methods to help with the gossip's logic: timestamp operations, stabilizing operations and general operations.

#### Gossip propagation flow

To allow the Gossip algorithm to work, in this delivery we added a Gossip propagation flow.
In summary, the implemented flow is the following:
1. Admin writes "*Gossip X*" in the terminal (`CommandParser.java`);
2. Admin calls `gossip()` method of `AdminService` abstraction. A gossip order is sent to server X through the stub (`AdminService.java`);
3. Server X receives the gossip order and asks the RM to propagate the operations the target servers don't have (`AdminServiceImpl.java`);
4. X's RM calls `findServersAndGossip()` and propagates the new operations to the servers (`ReplicaManager.java`);
5. To each other target server, the operations are sent through the stub (`DistLedgerCrossServerService.java`);
6. Each server's RM receives X's new operations and applies them (`DistLedgerCrossServerServiceImpl.java`).

### Features

#### More than two servers

Our implementation allows for more than two servers to be used in the system.
To achieve that, we:
- used a variable-length list to store each Server's time;
- made the Naming Server assign a unique index to each server (so it knows which position in the list to use), sent in the `RegisterResponse` proto message;

#### Only send unknown operations

Our implementation doesn't send the full list of operations. Instead, each Server knows which operations are unknown for each other server and only sends those.
To achieve that, we:
- added a `lastGossipTsMap` map variable to each RM, which stores the mapping from a given Server (using its address) to the _gossiped_ timestamp last received from another server;
- update the `lastGossipTsMap` map variable whenever a new gossip message is received and uses it to only send the operations that came afterwards;

### Problems

Considering that Gossip only guarantees weak consistency, this causes problems in the context of our project.  
For instance, considering the situation:
1. Alice creates an account on server A;
2. Alice transfers money to Bob on server A;
3. Bob creates an account on server B;
4. Admin requests server A to gossip to server B;

Given how Vector Clocks are used, step 2 will fail on server B but will succeed on server A, since on A, operation 3 is considered to have happened before operation 2.