
# COM3026 Distributed Systems Coursework

A coursework which implements a distributed system of nodes, talking over UDP sockets and messages serialised via JAXB. The 5 requirements of the coursework have been implemented:

- Synchronisation - logical vector clocks
- Replication - all members store the current members list
- Fault Tolerance - bully leader election algorithm
- Recovery - checkpointing via log files
- Security - hashing and signing messages verified by the receiver

## Set-up
### Requirements
- Java 1.8
- An IDE  (Eclipse, IntelliJ, NetBeans etc.)

### Guide
1. Clone this repository
	``` 
	git clone https://github.com/jacksimmonds0/com3026_coursework.git 
	```

2. Import the project as a maven project (via the pom.xml file)
3. Run the build using maven and/or tests to ensure its working e.g.
	```
	mvn clean install assembly:single
	```

## Running
### Manually
To run the program,  first need to generate X keystores for the digital signature scheme to function. Run the class `KeyGenerator` with the command line parameter for number of keystores to generate e.g. for 20:
```
java KeyGenerator 20
```
Once all the keystores have been generated the members can join group via 2 or 3 command line parameters. These are ID, port number to listen on and (optionally) IP address:port number to contact the previous member on. 

For example, to create a leader (the first member in the group): 
```
java JoinGroup 1 8001
```

And then to create another member who joins by contacting the first member/leader:
```
java JoinGroup 2 8002 127.0.0.1:8001
```


### Integration Tests
A suite of integration tests exist to cover all 5 areas of functionality outlined previously. To run all 5 test classes the class `IntegrationTestSuite` has been created to run all tests.