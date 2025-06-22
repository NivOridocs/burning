#! /bin/sh

set -e

VERSION=`grep mod_version gradle.properties | sed -n "s/^.*=\([0-9\.]*\).*$/\1/p"`

grep -q "\[$VERSION\]" CHANGELOG.md

cat CHANGELOG.md \
    | sed -n "/## \[${VERSION}\]/,/## \[/{//b;p}" \
    | sed -e :a -e '/./,$!d;/^\n*$/{$d;N;};/\n$/ba'
