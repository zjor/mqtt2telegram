#!/bin/bash

NS=app-mqtt2telegram
APP=mqtt2telegram
VERSION=$(git rev-parse --short HEAD)

set -x

helm upgrade --namespace ${NS} --install ${APP} --set version=${VERSION} ./ops/mqtt2telegram