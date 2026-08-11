#!/bin/bash

envs="
apiAccessToken=${GH_API_ACCESS_TOKEN}
"

(echo "$envs" | grep -E '.+=.+') >> develop.properties
