jq -rc . $PWD/tmp-test.json | kafka-console-producer --bootstrap-server localhost:9092 --topic any-status-changed
