#!/bin/bash

DOCKER_USER=zjor
IMAGE_NAME=mqtt2telegram
TAG=$(git rev-parse --short HEAD)

IMAGE_NAME=${DOCKER_USER}/${IMAGE_NAME}:${TAG}

set -x

docker buildx build --platform linux/amd64 -t ${IMAGE} .
docker tag ${IMAGE_NAME} ${IMAGE_NAME}
docker push ${IMAGE_NAME}