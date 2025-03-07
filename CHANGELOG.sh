#! /bin/sh

VERSION=`grep mod_version gradle.properties | cut -d'=' -f2`

cat CHANGELOG.md \
    | sed -n "/## \[${VERSION}\]/,/## \[/{//b;p}" \
    | sed -e :a -e '/./,$!d;/^\n*$/{$d;N;};/\n$/ba'
