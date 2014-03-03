#!/bin/sh
#
# Deploys artifacts to SonaType snapshots repository if we're building master branch.
#
# This ensures that we don't try to deploy to SonaType if we're building a pull request branch or
# doing other miscellaneous business.

if [[ $TRAVIS_BRANCH == 'master' ]]; then
    mvn deploy --settings target/travis/settings.xml
fi
