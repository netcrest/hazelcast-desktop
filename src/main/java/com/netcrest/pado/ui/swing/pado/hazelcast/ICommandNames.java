package com.netcrest.pado.ui.swing.pado.hazelcast;

import com.netcrest.commandspace.ICommandSpaceNames;
import com.netcrest.commandspace.IDesktopCommandNames;
import com.netcrest.ui.desktop.IDesktopNames;

public interface ICommandNames extends IDesktopNames, IDesktopCommandNames, ICommandSpaceNames
{
    public final PadoInfoCommands CS_PADO_INFO = new PadoInfoCommands();
    public final GridObjectCommands CS_GRID_OBJECT = new GridObjectCommands();
    public final RefreshCommands CS_REFRESH = new RefreshCommands();

    public final static class PadoInfoCommands
    {
        public final static String TOPIC = "PadoInfo";
        public final static String COMMAND_onPadoInfo = "onPadoInfo";
        public final static String COMMAND_onMapItem = "onMapItem";
    }
    
    public final static class GridObjectCommands
    {
    	public final static String TOPIC = "GridObject";
        public final static String COMMAND_onGridObject = "onGridObject";
    }
    
    public final static class RefreshCommands
    {
        public final static String TOPIC = "Refresh";
        public final static String COMMAND_onRefresh = "onRefresh";
    }
}