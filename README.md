# Big_Testbed
2018-19 Testbed MultiAgent Code

## Getting Started

These instructions will get you a copy of the project up and running on the testbed machine for development and testing purposes.

### Prerequisites

What things you need to install the software and how to install them

```
Eclipse
https://www.eclipse.org/

jeasyopc 
https://github.com/luoyan35714/OPC_Client/tree/master/OPC_Client_Jeasyopc
```

## Running the program

Open OPC Test Client and open OPC Test Client excel file in the program
Open Logix Designer and download the MultiAgent PLC program onto the testbed
Turn on the PLC via the HMI
Turn on Robots and air for the grippers
Turn on CNCs, set to automatically define program to run
Run startup/startJADE.java
Shift click all of the agents and press resume (can be set to automatic by commenting out code)
Right click the Product agents and press resume as they are created (can be set to automatic by commenting out code)

### Overview

This is the JADE project for the big testbed in the SMART Manufacturing Laboratory, to be used with OPC Test Client and Rockwell Logix Designer PLC to build a MultiAgent System.

## Testbed System

The testbed consists of a conveyor line, 2 cells, 2 robots, and 4 conveyors. Their locations and capabilities are detailed at https://drive.google.com/drive/u/1/folders/1UnJGCyukSsqO0-tFE_p8DtPKPJp2yLC1.

## Agents

The resource agents for the current code consist of

```
ConveyorAgent
Robot1Agent
Robot2Agent
```

## OPCLayer

The OPCLayer is the handshake between the agents and the PLC. Agents request tags from this layer to create products and to performs actions on products.

### Future tasks/suggestions

* The messages being sent between RAs and OPCLayer to monitor tags are creating a rapidly growing message queue, slowing down execution. A suggestion is to change the requesting of tags from RAs to the pushing of changes in tags from the OPCLayer to the RAs.

* Add PA-PA logic, such as for collaborative scheduling, product priority, rendezvous(combining of products), etc.

* Add Pallet agents, to handle empty pallets, these use proximity sensor tags as opposed to RFID sensor tags

* Change PLC to allow for retraction of Cell 3 stop to allow product to circulate

* Find a better way to keep track of product agents, to not make new PAs for the same product
