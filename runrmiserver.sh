#!/usr/bin/env bash
java -Djava.security.policy=rmi.policy -Djava.rmi.server.hostname=10.0.2.2 -cp csis-604-1.0-SNAPSHOT.jar com.stephengoeddel.distributedSorting.rmi.RMISortingServer 40001