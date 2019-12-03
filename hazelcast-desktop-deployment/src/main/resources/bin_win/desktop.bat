@SETLOCAL
@echo off
@call setenv.bat

@title "Hazelcast Desktop"

@set LAF=-Dswing.defaultlaf=org.jvnet.substance.SubstanceLookAndFeel
REM @set LAF=-Dswing.defaultlaf=com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel
REM @set LAF=-Dswing.defaultlaf=com.sun.java.swing.plaf.gtk.GTKLookAndFeel
REM @set LAF=-Dswing.defaultlaf=javax.swing.plaf.metal.MetalLookAndFeel
REM @set LAF=-Dswing.defaultlaf=com.sun.java.swing.plaf.windows.WindowsLookAndFeel

@set LOG_FILE=%LOG_DIR%\desktop.log

pushd ..
%JAVA% %JAVA_OPTS% -Djavax.xml.parsers.DocumentBuilderFactory=com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl %LAF% com.netcrest.ui.desktop.Desktop > %LOG_FILE% 2>&1
popd
