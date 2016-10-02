#!/usr/bin/env bash
cd target/classes && java -cp . com.stephengoeddel.synchronization.NodeServer $1 $2 box.sgoeddel.com 8000 box1.sgoeddel.com 8000 box2.sgoeddel.com 8000