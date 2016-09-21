#!/usr/bin/env bash
mvn exec:java -Dexec.mainClass="com.stephengoeddel.distributedSorting.messages.MessageServer" -Dexec.args="10.0.2.2 61616"