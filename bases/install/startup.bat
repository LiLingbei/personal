@echo off
taskkill /F /IM postgres.exe /T
taskkill /F /IM java.exe /T
taskkill /F /IM nginx.exe /T

set ITONE_HOME=%cd%\itone

set PG_HOME=%ITONE_HOME%\db\pgsql
set JAVA_HOME=%ITONE_HOME%\jdk
set PATH=%ITONE_HOME%\bin;%JAVA_HOME%\bin;%PG_HOME%\bin;%PATH%
echo db
pg_ctl start -s -l %ITONE_HOME%\log\db.log -D %ITONE_HOME%\db\data -w

cd nginx
@start "nginx" /min nginx

echo cmdbuild
cd ..\cmdbuild\bin
cmd /c startup.bat

echo itone
cd %ITONE_HOME%

set JAVA=java

set CLASSPATH=conf
set CLASSPATH=%CLASSPATH%;lib\3rd\*;lib\app\*
set CLASSPATH=%CLASSPATH%;dcs\lib\3rd\*;dcs\lib\app\*
set CLASSPATH=%CLASSPATH%;ccs\lib\3rd\*;ccs\lib\app\*
set CLASSPATH=%CLASSPATH%;web\lib\3rd\*;web\lib\app\*

set JAVA_OPTS=-Xms512m -Xmx512m -Dfile.encoding=UTF8 -Ditone.properties=soapui.properties "-Ditone.home=%ITONE_HOME%"

"%JAVA%" %JAVA_OPTS% -cp "%CLASSPATH%" com.its.itone.App %*
pause
