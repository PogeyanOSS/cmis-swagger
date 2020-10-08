export MONGO_CLIENT_URL=$1
export CMIS_USERNAME=$2
export CMIS_PASSWORD=$3
export CMIS_REPO_ID=$4
export HOSTURL=$5
export REQUEST_URL=$6
export EMAIL=$7
export PASWORD=$8

echo "clearing the db"

node clearDB.js

echo "runing swagger tests"

npm run swagger

echo "clearing the db"

node clearDB.js