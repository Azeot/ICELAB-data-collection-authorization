#!/bin/bash

clientId="whitebunny"
clientSecret="363fc711-745d-4af0-9d72-8646f847210f"
d2hpdGVidW5ueTozNjNmYzcxMS03NDVkLTRhZjAtOWQ3Mi04NjQ2Zjg0NzIxMGY=
oauthIp="172.17.0.1"
oauthPort=8081
realm="dc"

curl -vvvvvvv -X POST http://${oauthIp}:${oauthPort}/auth/realms/${realm}/protocol/openid-connect/token \
-H "Accept: application/json;charset=UTF-8" \
-H "Content-Type: application/x-www-form-urlencoded;charset=UTF-8" \
-H "Authorization: Basic $(printf '%s' "${clientId}:${clientSecret}" | base64)" \
-H "Accept-Encoding: gzip,deflate" \
--data "grant_type=client_credentials"