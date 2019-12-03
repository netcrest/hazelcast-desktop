package com.netcrest.pado.ui.swing.pado.hazelcast.query;

import java.util.List;

import org.hazelcast.addon.exception.MapNotFoundException;
import org.hazelcast.addon.hql.CompiledQuery;
import org.hazelcast.addon.hql.HqlQuery;
import org.hazelcast.addon.hql.IPageResults;
import org.hazelcast.addon.hql.impl.PageResultsImpl;

import com.netcrest.pado.index.exception.GridQueryResultSetExpiredException;
import com.netcrest.pado.index.service.GridQuery;
import com.netcrest.pado.index.service.IScrollableResultSet;
import com.netcrest.pado.ui.swing.pado.hazelcast.common.HazelcastSharedCache;

@SuppressWarnings("rawtypes")
public class QueryResultSet<T> implements IScrollableResultSet<T> {
	private static final long serialVersionUID = 1L;
	
	private IPageResults<T> pageResults;

//	public QueryResultSet(PageResults<T> pageResults) {
//		this.pageResults = pageResults;
//	}
	
	public QueryResultSet(String queryString, int fetchSize) {
		
		HqlQuery<T> hql = HazelcastSharedCache.getSharedCache().getHqlQueryInstance();
		CompiledQuery cq = hql.compile(queryString, fetchSize);
		if (hql.isMapExist(cq.getMapName()) == false) {
			throw new MapNotFoundException(cq.getMapName() + ": " + queryString);
		}
		pageResults = cq.execute();
	}

//	public QueryResultSet(String queryString, int fetchSize) {
//		SimpleQueryParser parser = new SimpleQueryParser(queryString);
//		IMap map = HazelcastSharedCache.getSharedCache().getMap(parser.getPath());
//		if (map == null) {
//			throw new MapNotFoundException(parser.getPath() + ": " + queryString);
//		}
//		Predicate queryPredicate;
//		if (parser.isWhereClause()) {
//			queryPredicate = new SqlPredicate(parser.getWhereClause());
//		} else {
//			queryPredicate = Predicates.alwaysTrue();
//		}
//		OrderBy orderBy = new OrderBy(parser.getOrderBy());
//		PagingPredicate pagingPredicate = new PagingPredicate(queryPredicate, orderBy, fetchSize);
//		Set<?> results = map.entrySet(pagingPredicate);
//		pageResults = new PageResults(map, results, pagingPredicate, org.hazelcast.addon.hql.ResultType.KEYS_VALUES);
//	}

	public IPageResults getPageResults() {
		return pageResults;
	}

	@Override
	public int getTotalSize() {
		if (pageResults == null) {
			return 0;
		}
		return pageResults.getSize();
	}

	@Override
	public List<T> toList() {
		if (pageResults == null) {
			return null;
		}
		return pageResults.toList();
	}

	@Override
	public int getSetNumber() {
		if (pageResults == null) {
			return 0;
		}
		return pageResults.getPage() + 1;
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
		if (pageResults == null) {
			return 0;
		}
		return pageResults.getSize();
	}

	@Override
	public int getFetchSize() {
		if (pageResults == null) {
			return 0;
		}
		return pageResults.getFetchSize();
	}

	@Override
	public void setFetchSize(int fetchSize) {
		// Hazelcast does not support this
		throw new UnsupportedOperationException();
	}

	@Override
	public int getCurrentIndex() {
		return pageResults.getStartIndex();
	}

	@Override
	public int getSetCount() {
		// Hazelcast does not support this
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean nextSet() throws GridQueryResultSetExpiredException {
		if (pageResults == null) {
			return false;
		}
		return pageResults.nextPage();
	}

	@Override
	public boolean previousSet() throws GridQueryResultSetExpiredException {
		if (pageResults == null) {
			return false;
		}
		return pageResults.previousPage();
	}

	@Override
	public boolean goToSet(int index) throws GridQueryResultSetExpiredException {
		int pageNumber = index / getFetchSize();
		return pageResults.setPage(pageNumber);
	}

	@Override
	public int getStartIndex() {
		if (pageResults == null) {
			return -1;
		}
		return pageResults.getStartIndex();
	}

	@Override
	public int getEndIndex() {
		if (pageResults == null) {
			return -1;
		}
		return pageResults.getEndIndex();
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
		if (pageResults == null) {
			return;
		}
		((PageResultsImpl)pageResults).dump();
	}

	@Override
	public void close() {
	}

	@Override
	public boolean isClosed() {
		// TODO Auto-generated method stub
		return false;
	}

	public String getResultName() {
		return "hazelcast";
	}

	public int getLargestPageVisted() {
		if (pageResults == null) {
			return -1;
		}
		return pageResults.getLargestPageVisted() + 1;
	}

	public boolean isLastPage() {
		if (pageResults == null) {
			return false;
		}
		return pageResults.isLastPage();
	}
}
