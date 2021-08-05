#!/bin/sh

cd ..
cd build/ || exit
mkdir -p release
cp ../shell/run/*.sh release/
cp ../out/artifacts/cq_backend_jar/cq_backend.jar release/
cp ../src/main/resources/*.yaml release/
# shellcheck disable=SC2035
chmod +x *.sh
tar -czvf release/"$1"-release.tar.gz release/*.jar release/*.yaml release/*.sh
rm release/*.jar
rm release/*.yaml
rm release/*.sh