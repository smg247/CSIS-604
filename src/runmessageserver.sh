#!/usr/bin/env bash
mvn exec -Dexec.mainClass="com.stephengoeddel.distributedSorting.messages.MessageServer" -Dexec.args="http://10.0.2.2 61616"