#!/bin/sh

cd build/ || exit
mkdir -p release
cp ../out/artifacts/cq_backend_jar/cq_backend.jar release/
cp ../src/main/resources/recipients.json release/
tar -czvf release.tar.gz release/
rm -r release