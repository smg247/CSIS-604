#!/usr/bin/env bash
java -Djava.security.policy=rmi.policy -Djava.rmi.server.hostname=127.0.0.1 -cp csis-604-1.0-SNAPSHOT.jar com.stephengoeddel.distributedSorting.rmi.RMISortingServer 8000