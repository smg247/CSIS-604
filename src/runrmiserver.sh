#!/usr/bin/env bash
java -Djava.security.policy=rmi.policy -cp . com.stephengoeddel.distributedSorting.rmi.RMISortingServer 8000