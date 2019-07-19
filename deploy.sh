#!/usr/bin/env bash
mvn clean install
sudo docker build -t rushang7/egov-chatbot:1.0.1 .
sudo docker push rushang7/egov-chatbot:1.0.1
kubectl delete pod $(kubectl get pods | grep egov-chatbot | awk '{print $1}')