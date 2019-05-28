package com.netcrest.pado.ui.swing.pado.hazelcast.query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.hazelcast.core.IMap;
import com.hazelcast.query.PagingPredicate;
import com.netcrest.pado.index.exception.GridQueryResultSetExpiredException;
import com.netcrest.pado.index.service.GridQuery;
import com.netcrest.pado.index.service.IScrollableResultSet;

@SuppressWarnings("rawtypes")
public class QueryResultSet<T> implements IScrollableResultSet<T> {

	private final IMap map;
	private Set<Map.Entry<?, ?>> entries;
	private PagingPredicate pagingPredicate;
	private int largestPageVisted = 1;

	public QueryResultSet(IMap map, Set<Map.Entry<?, ?>> entries, PagingPredicate pagingPredicate) {
		this.map = map;
		this.entries = entries;
		this.pagingPredicate = pagingPredicate;
	}

	public Set<Map.Entry<?, ?>> getEntries() {
		return entries;
	}

	public PagingPredicate getPagingPredicate() {
		return pagingPredicate;
	}

	@Override
	public int getTotalSize() {
		if (entries == null) {
			return 0;
		}
		return entries.size();
	}

	@Override
	public List<T> toList() {
		if (pagingPredicate == null) {
			return new ArrayList();
		}
//		return new ArrayList(entries);
		
		List list = new ArrayList();
		for (Map.Entry entry : entries) {
			list.add(entry.getValue());
		}
		return list;
	}

	@Override
	public int getSetNumber() {
		if (pagingPredicate == null) {
			return 0;
		}
		return pagingPredicate.getPage() + 1;
	}

	/**
	 * Returns the total number of pages
	 * 
	 * @return
	 */
	public int getPageCount() {
		// TODO: Hazelcast does not support this?
		return getSetSize() / getFetchSize();
	}

	@Override
	public int getSetSize() {
		if (entries == null) {
			return 0;
		}
		return entries.size();
	}

	@Override
	public int getFetchSize() {
		if (pagingPredicate == null) {
			return 0;
		}
		return pagingPredicate.getPageSize();
	}

	@Override
	public void setFetchSize(int fetchSize) {
		// Hazelcast does not support this
		throw new UnsupportedOperationException();
	}

	@Override
	public int getCurrentIndex() {
		return pagingPredicate.getPage() * getFetchSize();
	}

	@Override
	public int getSetCount() {
		// Hazelcast does not support this
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean nextSet() throws GridQueryResultSetExpiredException {
		pagingPredicate.nextPage();
		entries = map.entrySet();
		boolean pageExists = entries.size() > 0;
		if (pageExists) {
			if (largestPageVisted < getSetNumber()) {
				largestPageVisted = getSetNumber();
			}
		}
		return pageExists;
	}

	@Override
	public boolean previousSet() throws GridQueryResultSetExpiredException {
		pagingPredicate.previousPage();
		entries = map.entrySet();
		boolean pageExists = entries.size() > 0;
		if (pageExists) {
			if (largestPageVisted < getSetNumber()) {
				largestPageVisted = getSetNumber();
			}
		}
		return pageExists;
	}

	@Override
	public boolean goToSet(int index) throws GridQueryResultSetExpiredException {
		int page = index / getFetchSize();
		pagingPredicate.setPage(page);
		entries = map.entrySet(pagingPredicate);
		boolean pageExists = entries.size() > 0;
		if (pageExists) {
			if (largestPageVisted < getSetNumber()) {
				largestPageVisted = getSetNumber();
			}
		}
		return pageExists;
	}

	@Override
	public int getStartIndex() {
		if (pagingPredicate == null) {
			return 0;
		}
		return pagingPredicate.getPage() * getFetchSize();
	}

	@Override
	public int getEndIndex() {
		if (pagingPredicate == null) {
			return 0;
		}
		return getStartIndex() + getSetCount();
	}

	@Override
	public int getViewStartIndex() {
		return getStartIndex();
	}

	@Override
	public int getViewEndIndex() {
		return getEndIndex();
	}

	@Override
	public GridQuery getQuery() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<T> toDomainList() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<T> toValueList() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void dump() {
		int index = getViewStartIndex();
		for (Map.Entry<?, ?> entry : entries) {
			System.out.println(index++ + ". key=" + entry.getKey() + ", value=" + entry.getValue());
		}
	}

	@Override
	public void close() {
	}

	@Override
	public boolean isClosed() {
		// TODO Auto-generated method stub
		return false;
	}
	
	public String getResultName()
	{
		return "hazelcast";
	}
	
	public int getLargestPageVisted()
	{
		return largestPageVisted;
	}
	
	public boolean isLastPage()
	{
		return entries.size() <  getFetchSize();
	}
}
