#!/usr/bin/env bash
parent_path=$( cd "$(dirname "${BASH_SOURCE[0]}")" || exit ; pwd -P )

cd "$parent_path" || exit
java -jar build/libs/kengine-0.1.1.jar
