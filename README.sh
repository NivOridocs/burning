#! /bin/sh

set -e

cat README.md \
    | sed '
        s_src/main/java/niv/burning/api_https://github.com/NivOridocs/burning/tree/main/src/main/java/niv/burning/api_g;
        s_\[`BurningStorage`\]_\[BurningStorage\]_g;
        s_\[`Burning`\]_\[Burning\]_g;'
