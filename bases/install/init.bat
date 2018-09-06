taskkill /F /IM java.exe /T
taskkill /F /IM postgres.exe /T
rmdir /s /q itone
7za x itone.zip -y -aoa
7za x jdk.zip -oitone -aoa -y
7za x pgsql.zip -oitone\db -aoa -y

rem @echo off
cd itone\db
rmdir /s /q data

set PATH=pgsql\bin;%PATH%
initdb -D data -E UTF8 --no-locale -U postgres

echo '�������ݿ�'
pg_ctl start -s -l ..\log\db.log -D data -w

cd /d sql
set PATH=..\pgsql\bin;%PATH%
set PGHOSTADDR=127.0.0.1
set PGUSER=postgres
psql -f create_db.sql

set PGDATABASE=itone
pause
psql -f init.sql

echo 'ִ�����,�ر����ݿ�'

taskkill /F /IM pg_ctl.exe /T
taskkill /F /IM postgres.exe /T
