#!/usr/bin/env bash

(git submodule update --init && git submodule foreach git submodule update --init) > /dev/null
