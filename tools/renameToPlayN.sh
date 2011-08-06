#!/bin/sh
# TODO(pdr): clean up this hacked together mess
# This script performs the necessary renaming for forplay->playn
# Use at your own risk!

# recursively replace the text forplay with playn in files
find find ! -wholename "*.git/*" -type f -not -name "renameToPlayN.sh" -exec perl -p -i -e 's/ForPlay/PlayN/g' {} \;
find find ! -wholename "*.git/*" -type f -not -name "renameToPlayN.sh" -exec perl -p -i -e 's/Forplay/Playn/g' {} \;
find find ! -wholename "*.git/*" -type f -not -name "renameToPlayN.sh" -exec perl -p -i -e 's/forplay/playn/g' {} \;
find find ! -wholename "*.git/*" -type f -not -name "renameToPlayN.sh" -exec perl -p -i -e 's/FORPLAY/PLAYN/g' {} \;
find find ! -wholename "*.git/*" -type f -not -name "renameToPlayN.sh" -exec perl -p -i -e 's/forPlay/playN/g' {} \;

# recusively rename directories called forplay to playn
find -type d -name "forplay" | perl -e 'print sort {length $b <=> length $a} <>' | awk '{print "mv " $1 " " $1}' | sed 's/\(.*\)forplay/\1playn/' | sh
