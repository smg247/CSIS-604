#!/usr/bin/env bash
#java -cp . com.stephengoeddel.distributedSorting.messages.MessageServer 61616
mvn exec -Dexec.mainClass="com.stephengoeddel.distributedSorting.messages.MessageServer" -Dexec.args="60000"