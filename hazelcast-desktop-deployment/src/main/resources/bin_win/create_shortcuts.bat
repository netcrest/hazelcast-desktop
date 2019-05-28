@echo off
@setlocal ENABLEDELAYEDEXPANSION
@call setenv

set DIR=%CD%
set SHORTCUT_DIR=Shortcut

pushd ..
rmdir /s /q %SHORTCUT_DIR%
mkdir %SHORTCUT_DIR%


REM
REM All
REM

bin_win\XXMKLINK.EXE /q "%SHORTCUT_DIR%\1. Desktop.lnk" %DIR%\desktop.bat "" %DIR% "launches Hazelcast Desktop" 7 %DIR%"\images\pado_40x40.bmp"
bin_win\XXMKLINK.EXE /q "%SHORTCUT_DIR%\2. Clean.lnk" %DIR%\clean.bat "" %DIR% "removes log and stats files" 1 %DIR%"\images\Clean.ico"
bin_win\XXMKLINK.EXE /q "%SHORTCUT_DIR%\3 bin_win.lnk" %windir%\system32\cmd.exe "/K setenv.bat" %DIR% "bin_win" 1

REM
REM Explorer
REM
bin_win\XXMKLINK.EXE /q "%SHORTCUT_DIR%\%SYSTEM_NAME% Dir.lnk" %windir%\explorer.exe "%CD%" %windir% "%SYSTEM_NAME% Dir" 1

REM
REM README and RELEASE_NOTES files in main folder
REM
bin_win\XXMKLINK.EXE /q "%SHORTCUT_DIR%\README.lnk" %DIR%\..\README.txt "" %DIR% "README.txt" 1
bin_win\XXMKLINK.EXE /q "%SHORTCUT_DIR%\RELEASE_NOTES.lnk" %DIR%\..\RELEASE_NOTES.txt "" %DIR% "RELEASE_NOTES.txt" 1

REM
REM Create a shortcut on desktop
REM

bin_win\XXMKLINK.EXE /q "%USERPROFILE%\Desktop\Hazelcast Desktop.lnk" %CD%\%SHORTCUT_DIR% "" %CD%\%SHORTCUT_DIR% "Hazelcast Desktop" 1

popd

