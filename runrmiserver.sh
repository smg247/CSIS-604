#!/usr/bin/env bash
java -Djava.security.policy=rmi.policy -Djava.rmi.server.hostname=127.0.0.1 -cp . com.stephengoeddel.distributedSorting.rmi.RMISortingServer 8000