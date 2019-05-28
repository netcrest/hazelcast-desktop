@SETLOCAL
@echo off
@call setenv.bat

@title "Hazelcast Desktop"

@set LAF=-Dswing.defaultlaf=org.jvnet.substance.SubstanceLookAndFeel
REM @set LAF=-Dswing.defaultlaf=com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel
REM @set LAF=-Dswing.defaultlaf=com.sun.java.swing.plaf.gtk.GTKLookAndFeel
REM @set LAF=-Dswing.defaultlaf=javax.swing.plaf.metal.MetalLookAndFeel
REM @set LAF=-Dswing.defaultlaf=com.sun.java.swing.plaf.windows.WindowsLookAndFeel

pushd ..
%JAVA% -DcodeBaseURL=%CODEBASE_URL% -DpreferenceURL=etc/desktop.properties -Dhazelcast.client.config=%HAZELCAST_CLIENT_CONFIG_FILE% -Dhazelcast.diagnostics.metric.distributed.datastructures=true -Djavax.xml.parsers.DocumentBuilderFactory=com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl %LAF% com.netcrest.ui.desktop.Desktop
popd
