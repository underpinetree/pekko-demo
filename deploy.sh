#!/bin/bash
set -e

docker build -t pekko-demo:latest .
kind load docker-image pekko-demo:latest --name pekko-demo
kubectl apply -f k8s/
kubectl rollout restart deployment/pekko-demo
kubectl rollout status deployment/pekko-demo

pkill -f "port-forward svc/pekko-demo" 2>/dev/null || true
pkill -f "port-forward svc/postgres" 2>/dev/null || true
kubectl port-forward svc/pekko-demo 8080:8080 &
kubectl port-forward svc/postgres 5432:5432 &
