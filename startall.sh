#!/bin/bash

LOCATOR_PORT=10334
SERVER2_PORT=40405
GEMFIRE=/Users/wwilliams/Downloads/Pivotal_GemFire_827_b18_Linux/

gfsh <<!

start locator --name=locator1 --properties-file=config/locator.properties --port=$LOCATOR_PORT --J=-Xms256m --J=-Xmx256m

configure pdx --portable-auto-serializable-classes=.* 

start server --name=server1 --locators=localhost[$LOCATOR_PORT] --J=-Xms512m --J=-Xmx512m --properties-file=config/gemfire.properties

create region --name=Accounts --type=PARTITION

start server --name=server2 --locators=localhost[$LOCATOR_PORT] --J=-Xms512m --J=-Xmx512m --properties-file=config/gemfire.properties --server-port=$SERVER2_PORT 

undeploy --jar=distributed-lock-token-cleaner-0.0.1.jar
deploy --jar=../target/distributed-lock-token-cleaner-0.0.1.jar

list members;

list regions;

exit;
!
