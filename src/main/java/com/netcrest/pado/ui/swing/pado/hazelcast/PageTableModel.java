package com.netcrest.pado.ui.swing.pado.hazelcast;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import com.netcrest.pado.data.KeyType;
import com.netcrest.pado.index.service.IScrollableResultSet;

@SuppressWarnings("rawtypes")
public class PageTableModel extends GridTableModel {
	private static final long serialVersionUID = 1L;

	private ResultType resultType = ResultType.QUERY;

	private IScrollableResultSet resultSet;

	public PageTableModel() {
		this(ResultType.QUERY);
	}

	public PageTableModel(ResultType resultType) {
		super();
		this.resultType = resultType;
	}

	public int getPage() {
		return resultSet.getSetNumber();
	}

	public int getPageCount() {
		return resultSet.getSetCount();
	}
	
	public void reset(int firstRowNum, IScrollableResultSet resultSet, String primaryColumnName)
			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		reset(firstRowNum, resultSet, primaryColumnName, null);
	}

	/**
	 * Resets the table by replacing the existing table contents with the specified
	 * result set. This method supports the following content types:
	 * <ul>
	 * <li>Objects with public members and getters</li>
	 * <li>Primitives including String</li>
	 * <li>MapLite</li>
	 * <li>OQL Struct</li>
	 * <li>OQL Struct with (ITemporalKey, ITemporalData) pairs, e.g., the result set
	 * of "select e.key, e.value from /temporal.entrySet e".
	 * <li>TemporalEntry/li>
	 * </ul>
	 * 
	 * @param firstRowNum
	 * @param resultSet         The result set to display.
	 * @param primaryColumnName
	 * @param defaultKeyType    The default MapLite key type. If set, the default
	 *                          key type is used to display the MapLite contents.
	 *                          Otherwise, if null, the each MapLite object's key
	 *                          type is used. This means all versions of key types
	 *                          are used. There is no default key type unless is
	 *                          explicitly set by invoking this method.
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public void reset(int firstRowNum, IScrollableResultSet resultSet, String primaryColumnName, KeyType defaultKeyType)
			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		this.resultSet = resultSet;
		removeAll();

		if (resultSet == null || resultSet.getSetSize() == 0) {
			return;
		}

		List objList = resultSet.toList();
		super.reset(firstRowNum, objList, primaryColumnName, defaultKeyType);
	}

	/**
	 * Advances to the specified page. Returns false it the specified page cannot be
	 * reached.
	 * @param pageNumber
	 * @return
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public boolean page(int pageNumber)
			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		return page(pageNumber, resultSet.getFetchSize());
	}

	/**
	 * Advances to the specified page. Returns false it the specified page cannot be
	 * reached.
	 * 
	 * @param pageNumber
	 * @param fetchSize
	 * @return
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public boolean page(int pageNumber, int fetchSize)
			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		if (pageNumber < 1) {
			return false;
		}

//		resultSet.setFetchSize(fetchSize);
		int index = (pageNumber - 1) * resultSet.getFetchSize();
		resultSet.goToSet(index);

		// Hazelcast does not support getSetCount(). Use the set size to
		// determine the end of results.
		if (resultSet.getSetSize() == 0) {
			return false;
		}
		reset(resultSet.getCurrentIndex() + 1, resultSet, null);

		return true;
	}

	public IScrollableResultSet getResultSet() {
		return resultSet;
	}
}
