#!/bin/sh

cd build/ || exit
mkdir -p release
cp ../out/artifacts/cq_backend_jar/cq_backend.jar release/
cp ../src/main/resources/recipients.json release/
tar -czvf release/"$1"-release.tar.gz release/*.jar release/*.json
rm -r release/*.jar
rm -r release/*.json