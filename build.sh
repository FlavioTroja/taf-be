#!/bin/bash
export $(cat ../.env | xargs)
sh gradlew clean build --refresh-dependencies
