@echo off
@rem Licensed under the terms of http://www.apache.org/licenses/LICENSE-2.0

if "%SHACLROOT%" == "" goto :rootNotSet
set SHACL_HOME=%SHACLROOT%
:rootNotSet

if NOT "%SHACL_HOME%" == "" goto :okHome
echo SHACL_HOME not set
exit /B

:okHome
set SHACL_CP=%SHACL_HOME%\lib\*;
set LOGGING=file:%SHACL_HOME%/log4j.properties

@rem JVM_ARGS comes from the environment.
java %JVM_ARGS% -Dlog4j.configuration="%LOGGING%" -cp "%SHACL_CP%" org.topbraid.shacl.cli.Validate %*
exit /B
