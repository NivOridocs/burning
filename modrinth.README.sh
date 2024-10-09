#! /bin/sh

sed '
s_src/main/java/niv/burning/api_https://github.com/NivOridocs/burning/tree/main/src/main/java/niv/burning/api_g
' README.md > build/modrinth.README.md
