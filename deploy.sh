#!/bin/bash
set -e

docker build -t pekko-demo:latest .
kind load docker-image pekko-demo:latest
kubectl apply -f k8s/
kubectl rollout undo deployment/pekko-demo 2>/dev/null || true
kubectl rollout restart deployment/pekko-demo
kubectl rollout status deployment/pekko-demo
