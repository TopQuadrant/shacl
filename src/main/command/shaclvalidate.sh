#!/bin/sh
## Licensed under the terms of http://www.apache.org/licenses/LICENSE-2.0

resolveLink() {
  local NAME=$1

  if [ -L "$NAME" ]; then
    case "$OSTYPE" in
      darwin*|bsd*)
        # BSD style readlink behaves differently to GNU readlink
        # Have to manually follow links
        while [ -L "$NAME" ]; do
          NAME=$( cd $NAME && pwd -P ) ;
        done
        ;;
      *)
        # Assuming standard GNU readlink with -f for
        # canonicalize and follow
        NAME=$(readlink -f "$NAME")
        ;;
    esac
  fi

  echo "$NAME"
}

# If SHACL_HOME is empty
if [ -z "$SHACL_HOME" ]; then
  SCRIPT="$0"
  # Catch common issue: script has been symlinked
  if [ -L "$SCRIPT" ]; then
    SCRIPT=$(resolveLink "$0")
    # If link is relative
    case "$SCRIPT" in
      /*)
        # Already absolute
        ;;
      *)
        # Relative, make absolute
        SCRIPT=$( dirname "$0" )/$SCRIPT
        ;;
    esac
  fi

  # Work out root from script location
  SHACL_HOME="$( cd "$( dirname "$SCRIPT" )/.." && pwd )"
  export SHACL_HOME
fi

# If SHACL_HOME is a symbolic link need to resolve
if [ -L "${SHACL_HOME}" ]; then
  SHACL_HOME=$(resolveLink "$SHACL_HOME")
  # If link is relative
  case "$SHACL_HOME" in
    /*)
      # Already absolute
      ;;
    *)
      # Relative, make absolute
      SHACL_HOME=$(dirname "$SHACL_HOME")
      ;;
  esac
  export SHACL_HOME
fi

# ---- Setup
# JVM_ARGS : don't set here but it can be set in the environment.
# Expand SHACL_HOME but literal *
SHACL_CP="$SHACL_HOME"'/lib/*'
SOCKS=
LOGGING="${LOGGING:--Dlog4j.configurationFile=file:$SHACL_HOME/log4j2.properties}"

# Platform specific fixup
# On CYGWIN convert path and end with a ';' 
case "$(uname)" in
   CYGWIN*) SHACL_CP="$(cygpath -wp "$SHACL_CP");";;
esac

# Respect TMPDIR or TMP (windows?) if present
# important for tdbloader spill
if [ -n "$TMPDIR" ]
	then
	JVM_ARGS="$JVM_ARGS -Djava.io.tmpdir=\"$TMPDIR\""
elif [ -n "$TMP" ]
	then
	JVM_ARGS="$JVM_ARGS -Djava.io.tmpdir=\"$TMP\""
fi

java $JVM_ARGS $LOGGING -cp "$SHACL_CP" org.topbraid.shacl.tools.Validate "$@" 
