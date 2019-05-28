package com.netcrest.pado.ui.swing.pado.hazelcast.query;

public class MapNotFoundException extends RuntimeException {
	public MapNotFoundException() {
		super();
	}

	public MapNotFoundException(String message) {
		super(message);
	}

	public MapNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	public MapNotFoundException(Throwable cause) {
		super(cause);
	}
}
