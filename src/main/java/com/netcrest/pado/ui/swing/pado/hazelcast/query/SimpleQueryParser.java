package com.netcrest.pado.ui.swing.pado.hazelcast.query;

public class SimpleQueryParser {
	
	private String query;
	
	private String path;
	private String whereClause;
	private String[] orderBy;
	
	public SimpleQueryParser(String query)
	{
		this.query = query;
		parseQuery();
	}
	
	private void parseQuery()
	{
		if (query == null) {
			return;
		}
		String split[] = query.split(" ");
		
		boolean isFromFound = false;
		boolean isWhereFound = false;
		boolean isOrderFound = false;
		boolean isOrderByFound = false;
		path = null;
		whereClause = "";
		String orderByClause = "";
		for (String token : split) {
			if (isFromFound && path == null) {
				path = token;
			} else if (isWhereFound) {
				whereClause += token;
			} else if (isOrderByFound) {
				orderByClause += token;
			} else if (token.equalsIgnoreCase(("from"))) {
				isFromFound = true;
			} else if (token.equalsIgnoreCase(("where"))) {
				isWhereFound = true;
			} else if (token.equalsIgnoreCase(("order"))) {
				isOrderFound = true;
			} else if (isOrderFound && token.equalsIgnoreCase(("by"))) {
				isOrderByFound = true;
			}
		}
		split = orderByClause.split(",");
		orderBy = new String[split.length];
		int i = 0;
		for (String token : split) {
			orderBy[i++] = token.trim();
		}
	}

	public String getQuery() {
		return query;
	}

	public String getPath() {
		return path;
	}

	public String getWhereClause() {
		return whereClause;
	}

	public String[] getOrderBy() {
		return orderBy;
	}	
	
	public boolean isWhereClause() {
		return whereClause != null && whereClause.length() > 0;
	}
	
	public boolean isOrderBy() {
		return orderBy != null && orderBy.length > 0;
	}
}
