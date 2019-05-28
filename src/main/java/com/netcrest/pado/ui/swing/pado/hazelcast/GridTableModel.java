package com.netcrest.pado.ui.swing.pado.hazelcast;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.gemstone.gemfire.cache.Operation;
import com.gemstone.gemfire.cache.PartitionResolver;
import com.gemstone.gemfire.cache.query.CqEvent;
import com.gemstone.gemfire.cache.query.Struct;
import com.gemstone.gemfire.cache.query.types.ObjectType;
import com.gemstone.gemfire.cache.query.types.StructType;
import com.netcrest.pado.data.KeyMap;
import com.netcrest.pado.data.KeyType;
import com.netcrest.pado.temporal.ITemporalData;
import com.netcrest.pado.temporal.ITemporalKey;
import com.netcrest.pado.temporal.TemporalEntry;
import com.netcrest.pado.ui.swing.table.ObjectTableModel;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class GridTableModel extends ObjectTableModel
{
	private static final long serialVersionUID = 1L;

	public static enum ResultType
	{
		QUERY, KEY_VALUE
	}

	private boolean isTemporal;

	private KeyType defaultKeyType;
	private int firstRowNum = 1;
	private int keyFieldCount = 0;

	/**
	 * List of key types in the model. If null then the model does not contain
	 * KeyMap objects.
	 */
	private Class[] keyTypeClasses;

	/**
	 * Contains key names of all key type versions. Used for updates.
	 */
	private HashMap<String, Integer> keyMapColumnMap;

	private boolean showRaw = false;

	private ResultType resultType = ResultType.QUERY;

	public GridTableModel()
	{
		super();
	}

	public GridTableModel(ResultType resultType)
	{
		this.resultType = resultType;
	}

	public ResultType getResultType()
	{
		return resultType;
	}

	public KeyType getDefaultKeyType()
	{
		return defaultKeyType;
	}

	/**
	 * Returns key type classes if the model contains KeyMap objects. It returns
	 * null if there are no KeyMap objects.
	 */
	public Class[] getKeyTypeClasses()
	{
		return keyTypeClasses;
	}

	public boolean isKeyMap()
	{
		return keyTypeClasses != null;
	}

	public boolean isTemporal()
	{
		return isTemporal;
	}

	public int getFirstRowNumber()
	{
		return firstRowNum;
	}

	public void reset(int firstRowNum, List objList, String primaryColumnName, String... columnNames)
			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException
	{
		super.primaryColumnName = primaryColumnName;
		isTemporal = false;
		keyTypeClasses = null;
		removeAll();

		if (objList == null || objList.size() == 0) {
			return;
		}

		reset(firstRowNum, objList, primaryColumnName, (KeyType) null);
	}

	/**
	 * Resets the table by replacing the existing table contents with the
	 * specified result set. This method supports the following content types:
	 * <ul>
	 * <li>Objects with public members and getters</li>
	 * <li>Primitives including String</li>
	 * <li>KeyMap</li>
	 * <li>JsonLite</li>
	 * <li>OQL Struct</li>
	 * <li>OQL Struct with (ITemporalKey, ITemporalData) pairs, e.g., the result
	 * set of "select e.key, e.value from /temporal.entrySet e".
	 * <li>TemporalEntry/li>
	 * </ul>
	 * 
	 * @param firstRowNum
	 * @param objList
	 *            The result set to display.
	 * @param primaryColumnName
	 * @param defaultKeyType
	 *            The default KeyMap key type. If set, the default key type is
	 *            used to display the KeyMap contents. Otherwise, if null, the
	 *            each KeyMap object's key type is used. This means all versions
	 *            of key types are used. There is no default key type unless is
	 *            explicitly set by invoking this method.
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public void reset(int firstRowNum, List objList, String primaryColumnName, KeyType defaultKeyType)
			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException
	{
		this.firstRowNum = firstRowNum;
		this.defaultKeyType = defaultKeyType;
		this.keyTypeClasses = null;
		removeAll();

		if (objList == null || objList.size() == 0) {
			return;
		}
		Object dataObject = objList.get(0);
		if (dataObject instanceof KeyMap) {
			KeyMap keyMap = (KeyMap)dataObject;
			if (keyMap.getKeyType() == null) {
				resetMapList(firstRowNum, objList);
			} else {
				resetKeyMapList(firstRowNum, objList);
			}
		} else if (dataObject instanceof Map) {
			resetMapList(firstRowNum, objList);
		} else {
			resetObjectList(firstRowNum, objList);
		}
	}

	private int addFieldsNMethods(Object obj)
	{
		if (obj == null) {
			return 0;
		}

		if (isPrimitive(obj) || obj instanceof KeyMap) {
			fieldsMethodsVector.add(obj);
			return 1;
		}

		boolean skipNameMethod = false;
		if (obj instanceof PartitionResolver) {
			skipNameMethod = true;
		}
		Field[] fields = obj.getClass().getDeclaredFields();
		Method[] methods = obj.getClass().getMethods();
		int count = 0;

		// scan fields
		for (int i = 0; i < fields.length; i++) {
			int modifiers = fields[i].getModifiers();
			if (!Modifier.isStatic(modifiers) && Modifier.isPublic(modifiers)) {
				fieldsMethodsVector.add(fields[i]);
				count++;
			}
		}
		// scan methods
		for (int i = 0; i < methods.length; i++) {
			Method method = methods[i];
			int modifiers = method.getModifiers();
			String methodName = methods[i].getName();
			if (!Modifier.isStatic(modifiers) && Modifier.isPublic(modifiers)
					&& (methodName.startsWith("get") || methodName.startsWith("is"))
					&& (method.getParameterTypes().length == 0) && !methodName.equals("getClass")) {
				if (skipNameMethod && methodName.equals("getName")) {
					continue;
				}
				fieldsMethodsVector.add(methods[i]);
				count++;
			}
		}
		return count;
	}

	protected void resetStructList(int firstRowNum, List<Struct> objList) throws IllegalArgumentException,
			IllegalAccessException, InvocationTargetException
	{
		isTemporal = false;
		keyTypeClasses = null;
		removeAll();
		if (objList == null || objList.size() == 0) {
			return;
		}

		// Set column names
		Struct struct = objList.get(0);
		StructType structType = struct.getStructType();
		ObjectType[] fieldTypes = structType.getFieldTypes();
		String[] fieldNames = structType.getFieldNames();
		String columnNames[] = new String[fieldNames.length + 1];
		columnNames[0] = "Row";
		for (int i = 0; i < fieldNames.length; i++) {
			columnNames[i + 1] = fieldNames[i];
		}
		setColumnNames(columnNames);

		// Set values
		Object rowData[];
		int row = 0;
		for (Struct struct2 : objList) {
			Object[] fieldValues = struct2.getFieldValues();
			rowData = new Object[fieldValues.length + 2];
			rowData[0] = firstRowNum + row;
			rowData[rowData.length - 1] = struct2; // user data
			for (int i = 0; i < fieldValues.length; i++) {
				rowData[i + 1] = fieldValues[i];
			}
			addRow(rowData);
			row++;
		}
	}

	private boolean isDataStructureChanged(TreeSet<String> columnSet, int dataStartIndex)
	{
		boolean dataStructureChanged = columnSet.size() != getColumnCount() - dataStartIndex;
		if (dataStructureChanged) {
			return dataStructureChanged;
		}

		int i = dataStartIndex;
		for (String columnName : columnSet) {
			dataStructureChanged = columnName.equals(getColumnName(i++)) == false;
			if (dataStructureChanged) {
				break;
			}
		}
		return dataStructureChanged;
	}

	private boolean isDataStructureChanged(String[] columnNames)
	{
		boolean dataStructureChanged = columnNames.length != getColumnCount();
		if (dataStructureChanged) {
			return dataStructureChanged;
		}
		for (int i = 0; i < columnNames.length; i++) {
			dataStructureChanged = columnNames[i].equals(getColumnName(i)) == false;
			if (dataStructureChanged) {
				break;
			}
		}
		return dataStructureChanged;
	}

	protected boolean isDataStructureChanged(List<TemporalEntry> objList)
	{
		boolean dataStructureChanged = false;

		ITemporalData data = objList.get(0).getTemporalData();
		Field[] fields = data.getClass().getDeclaredFields();
		Method[] methods = data.getClass().getMethods();

		int count = 0;

		// scan fields
		for (int i = 0; i < fields.length; i++) {
			int modifiers = fields[i].getModifiers();
			if (!Modifier.isStatic(modifiers) && Modifier.isPublic(modifiers)) {
				// columns.add(fields[i].getName());
				dataStructureChanged = fieldsMethodsVector.contains(fields[i]) == false;
				count++;
			}
		}
		// scan methods
		for (int i = 0; i < methods.length; i++) {
			Method method = methods[i];
			int modifiers = method.getModifiers();
			String methodName = methods[i].getName();
			if (!Modifier.isStatic(modifiers) && Modifier.isPublic(modifiers) && methodName.startsWith("get")
					&& (method.getParameterTypes().length == 0) && !methodName.equals("getClass")) {
				dataStructureChanged = fieldsMethodsVector.contains(methods[i]) == false;
				count++;
			}
		}
		dataStructureChanged = count != fieldsMethodsVector.size();
		return dataStructureChanged;
	}

	protected boolean isDataStructureChangedTemporalKeyList(List<ITemporalKey> objList)
	{
		boolean dataStructureChanged = false;

		ITemporalKey key = objList.get(0);
		if (isPrimitive(key.getIdentityKey())) {
			dataStructureChanged = fieldsMethodsVector.size() != 0;
			return dataStructureChanged;
		}

		Field[] fields = key.getIdentityKey().getClass().getDeclaredFields();
		Method[] methods = key.getIdentityKey().getClass().getMethods();

		int count = 0;

		for (int i = 0; i < fields.length; i++) {
			int modifiers = fields[i].getModifiers();
			if (!Modifier.isStatic(modifiers) && Modifier.isPublic(modifiers)) {
				// columns.add(fields[i].getName());
				dataStructureChanged = fieldsMethodsVector.contains(fields[i]) == false;
				count++;
			}
		}
		// scan methods
		for (int i = 0; i < methods.length; i++) {
			Method method = methods[i];
			int modifiers = method.getModifiers();
			String methodName = methods[i].getName();
			if (!Modifier.isStatic(modifiers) && Modifier.isPublic(modifiers) && methodName.startsWith("get")
					&& (method.getParameterTypes().length == 0) && !methodName.equals("getClass")) {
				dataStructureChanged = fieldsMethodsVector.contains(methods[i]) == false;
				count++;
			}
		}
		dataStructureChanged = count != fieldsMethodsVector.size();
		return dataStructureChanged;
	}

	/**
	 * For non-KeyMap TemporalData
	 * 
	 * @param objList
	 * @return
	 */
	protected boolean isDataStructureChangedTemporalDataList(List<ITemporalData> objList)
	{
		boolean dataStructureChanged = false;

		ITemporalData data = objList.get(0);
		Field[] fields = data.getClass().getDeclaredFields();
		Method[] methods = data.getClass().getMethods();

		int count = 0;

		// scan fields
		for (int i = 0; i < fields.length; i++) {
			int modifiers = fields[i].getModifiers();
			if (!Modifier.isStatic(modifiers) && Modifier.isPublic(modifiers)) {
				// columns.add(fields[i].getName());
				dataStructureChanged = fieldsMethodsVector.contains(fields[i]) == false;
				count++;
			}
		}
		// scan methods
		for (int i = 0; i < methods.length; i++) {
			Method method = methods[i];
			int modifiers = method.getModifiers();
			String methodName = methods[i].getName();
			if (!Modifier.isStatic(modifiers) && Modifier.isPublic(modifiers) && methodName.startsWith("get")
					&& (method.getParameterTypes().length == 0) && !methodName.equals("getClass")) {
				dataStructureChanged = fieldsMethodsVector.contains(methods[i]) == false;
				count++;
			}
		}
		dataStructureChanged = count != fieldsMethodsVector.size();
		return dataStructureChanged;
	}

	private void resetKeyMapList(int firstRowNum, List<KeyMap> objList) throws IllegalArgumentException,
			IllegalAccessException, InvocationTargetException
	{
		isTemporal = false;
		keyTypeClasses = null;
		removeAll();
		if (objList == null || objList.size() == 0) {
			return;
		}

		TreeSet<String> columnSet = new TreeSet();
		HashSet<Class> keyTypeSet = new HashSet();

		// collect KeyType classes
		for (KeyMap keyMap : objList) {
			KeyType keyType = keyMap.getKeyType();
			keyTypeSet.add(keyType.getClass());
		}

		// collect columns
		if (defaultKeyType == null) {
			for (KeyMap keyMap : objList) {
				// Build columns for all versions of KeyType
				KeyType keyType = keyMap.getKeyType();
				KeyType[] keyTypes = keyType.getValues();

				for (int i = 0; i < keyTypes.length; i++) {
					KeyType kt = keyTypes[i];
					columnSet.add(kt.getName());
				}
			}
		} else {
			// Build columns specific to the default key type
			KeyType[] keyTypes = defaultKeyType.getValues();
			for (int i = 0; i < keyTypes.length; i++) {
				KeyType kt = keyTypes[i];
				columnSet.add(kt.getName());
			}
		}

		keyTypeClasses = (Class[]) keyTypeSet.toArray(new Class[0]);

		// add columns to table
		// data columns begin from the 3rd column, 2nd is for KeyType version
		int dataStartIndex = 2;
		HashMap<String, Integer> columnMap = new HashMap(columnSet.size() + 1, 1f);
		int column = dataStartIndex;
		for (String string : columnSet) {
			columnMap.put(string, column++);
		}
		boolean dataStructureChanged = isDataStructureChanged(columnSet, dataStartIndex);
		if (dataStructureChanged) {
			String columnNames[] = new String[columnSet.size() + dataStartIndex];
			columnNames[0] = "Row";
			columnNames[1] = "KeyVersion";
			int i = dataStartIndex;
			for (String columnName : columnSet) {
				columnNames[i++] = columnName;
			}
			setColumnNames(columnNames);
			setPrimaryColumnName(primaryColumnName);
		}

		// add rows
		int i = firstRowNum;
		Object[] argArr = new Object[0];
		// 1 additional items to hold temporal entry
		int rowDataSize = getColumnCount() + 1;
		KeyType keyTypes[];
		for (KeyMap keyMap : objList) {
			Object rowData[] = new Object[rowDataSize];

			// display key
			rowData[0] = i++;

			// display data
			Object obj = keyMap;
			rowData[rowData.length - 1] = keyMap; // user data
			KeyType keyType = keyMap.getKeyType();
			if (defaultKeyType == null) {
				keyTypes = keyType.getValues();
			} else {
				keyTypes = defaultKeyType.getValues();
			}
			rowData[1] = keyType.getClass().getSimpleName();
			for (KeyType keyType2 : keyTypes) {
				rowData[columnMap.get(keyType2.getName())] = keyMap.get(keyType2);
			}
			addRow(rowData);
		}
		if (dataStructureChanged) {
			fireTableStructureChanged();
		}
	}

	private void resetMapList(int firstRowNum, List<Map> objList) throws IllegalArgumentException,
			IllegalAccessException, InvocationTargetException
	{
		isTemporal = false;
		keyTypeClasses = null;
		removeAll();
		if (objList == null || objList.size() == 0) {
			return;
		}

		TreeSet<String> columnSet = new TreeSet();
		HashSet<Object> keySet = new HashSet();

		// collect KeyType classes
		for (Map map : objList) {
			Set ks = map.keySet();
			keySet.addAll(ks);
		}
		List keyList = new ArrayList(keySet);
		Collections.sort(keyList);

		// collect columns
		for (Map map : objList) {
			Set ks = map.keySet();
			columnSet.addAll(ks);
		}

		// add columns to table
		// data columns begin from the 2nd column
		int dataStartIndex = 1;
		HashMap<String, Integer> columnMap = new HashMap(columnSet.size(), 1f);
		int column = dataStartIndex;
		for (String string : columnSet) {
			columnMap.put(string, column++);
		}
		boolean dataStructureChanged = isDataStructureChanged(columnSet, dataStartIndex);
		if (dataStructureChanged) {
			String columnNames[] = new String[columnSet.size() + dataStartIndex];
			columnNames[0] = "Row";
			int i = dataStartIndex;
			for (String columnName : columnSet) {
				columnNames[i++] = columnName;
			}
			setColumnNames(columnNames);
			setPrimaryColumnName(primaryColumnName);
		}

		// add rows
		int i = firstRowNum;
		Object[] argArr = new Object[0];
		// 1 additional items to hold user data
		int rowDataSize = getColumnCount() + 1;
		KeyType keyTypes[];
		for (Map map : objList) {
			Object rowData[] = new Object[rowDataSize];

			// display key
			rowData[0] = i++;

			// display data
			Object obj = map;
			rowData[rowData.length - 1] = map; // user data

			int index = dataStartIndex;
			for (Object col : columnSet) {
				rowData[index++] = map.get(col);
			}
			addRow(rowData);
		}
		if (dataStructureChanged) {
			fireTableStructureChanged();
		}
	}

	protected Object getPrimaryColumnValue(Object dataObject)
	{
		Object result = null;
		try {
			if (primaryField != null) {
				result = primaryField.get(dataObject);
			} else if (primaryMethod != null) {
				result = primaryMethod.invoke(dataObject, new Object[0]);
			} else if (keyMapColumnMap != null) {
				if (dataObject instanceof KeyMap) {
					KeyMap keyMap = (KeyMap) dataObject;
					result = keyMap.get(primaryColumnName);
				}
			}
			if (result == null)
				result = "null";

		} catch (Exception ex) {
			// ignore
		}
		return result;
	}

	public void putRow(Object dataObject)
	{
		putRow(null, dataObject);
	}

	public void putRow(Object key, Object dataObject)
	{
		Object primaryColumnValue = getPrimaryColumnValue(dataObject);
		Object rowData[] = (Object[]) rowMap.get(primaryColumnValue);
		boolean isInsert = rowData == null;

		if (isInsert) {

			// Insert
			if (keyMapColumnMap != null) {
				rowData = new Object[fieldsMethodsVector.size() + keyMapColumnMap.size() + 1];
			} else {
				rowData = new Object[fieldsMethodsVector.size() + 2];
			}
			rowData[0] = new Integer(getRowCount() + 1);
			rowMap.put(primaryColumnValue, rowData);

		} else {

			// Update - do nothing
		}

		rowData[rowData.length - 1] = dataObject; // user data

		try {
			// Update rowData[]
			int fieldNum = 0;
			for (int k = 0; k < fieldsMethodsVector.size(); k++) {
				Object obj1 = fieldsMethodsVector.elementAt(k);
				if (obj1 instanceof Field) {
					if (key != null && fieldNum < keyFieldCount) {
						fieldNum++;
						rowData[k + 1] = ((Field) obj1).get(key);
					} else {
						rowData[k + 1] = ((Field) obj1).get(dataObject);
					}
				} else if (obj1 instanceof Method) {
					// row.add(((Method) obj1).getDefaultValue());
					Method method = (Method) obj1;
					// only interested in methods without parameters
					if (method.getParameterTypes().length == 0) {
						Object result;
						if (key != null && fieldNum < keyFieldCount) {
							result = ((Method) obj1).invoke(key);
							fieldNum++;
						} else {
							result = ((Method) obj1).invoke(dataObject);
						}
						if (result == null)
							result = "null";

						rowData[k + 1] = result;
					}
				} else if (obj1 instanceof KeyMap) {
					KeyMap keyMap = (KeyMap) dataObject;
					KeyType keyType = keyMap.getKeyType();
					KeyType keyTypes[];
					if (defaultKeyType == null) {
						keyTypes = keyType.getValues();
					} else {
						keyTypes = defaultKeyType.getValues();
					}
					rowData[k + 1] = keyType.getClass().getSimpleName();
					for (KeyType keyType2 : keyTypes) {
						rowData[keyMapColumnMap.get(keyType2.getName())] = keyMap.get(keyType2);
					}
				} else { // primitive
					if (key != null && fieldNum < keyFieldCount) {
						rowData[k + 1] = key;
						fieldNum++;
					}
				}
			}

			// Need to insert/update into the data model kept by the super class
			if (isInsert) {
				addRow(rowData);
			} else {
				int row = findRow(primaryColumnValue, primaryColumnIndex);
				if (row != -1)
					super.fireTableRowsUpdated(row, row);
			}
		} catch (Exception ex) {
			// ignore
		}

	}

	public synchronized void update(CqEvent event)
	{
		Object key = event.getKey();
		Object value = event.getNewValue();

		if (value == null || value.getClass() != dataClass) {
			return;
		}

		Operation operation = event.getQueryOperation();

		if (event.getQueryOperation() == Operation.CREATE) {
			putRow(key, value);
		} else if (event.getQueryOperation() == Operation.UPDATE) {
			putRow(key, value);
		} else if (event.getQueryOperation() == Operation.DESTROY) {
			removeRow(key, value);
		} else if (event.getQueryOperation() == Operation.REGION_CLEAR) {
			removeAll();
		}

		// RowDelta[] deltas = theUpdate.getDeltas();
		//
		// for (int i = 0; i < deltas.length; i++) {
		// RowDelta rowDelta = deltas[i];
		//
		// switch (rowDelta.getType()) {
		// case CQUpdate.INSERT: {
		// addRow(rowDelta.getNewRow());
		//
		// if (dataChangedListener != null) {
		// dataChangedListener.dataChanged(new DataChangedEvent(
		// this, DataChangedEvent.ROW_ADDED,
		// getRowCount() - 1));
		// }
		//
		// break;
		// }
		//
		// case CQUpdate.UPDATE: {
		// Object[] rowData = rowDelta.getNewRow();
		// int columns[] = rowDelta.getUpdatedColumns();
		// int row = getRow(rowData[1]);
		// if (row == -1) {
		// break;
		// }
		// updateRow(row, columns, rowData);
		//
		// if (dataChangedListener != null) {
		// dataChangedListener.dataChanged(new DataChangedEvent(
		// this, DataChangedEvent.ROW_UPDATED, row));
		// }
		//
		// break;
		// }
		//
		// case CQUpdate.DELETE: {
		// Object[] delData = rowDelta.getOldRow();
		// int row = getRow(delData[1]);
		// if (row == -1) {
		// break;
		// }
		// removeRow(row);
		//
		// if (dataChangedListener != null) {
		// dataChangedListener.dataChanged(new DataChangedEvent(
		// this, DataChangedEvent.ROW_DELETED, row));
		// }
		//
		// break;
		// }
		// }
		// }
		// }
	}

	// public void putRow(Object dataObject)
	// {
	// if (dataObject instanceof Struct) {
	// Struct struct = (Struct) dataObject;
	// StructType structType = struct.getStructType();
	// ObjectType[] fieldTypes = structType.getFieldTypes();
	// String[] fieldNames = structType.getFieldNames();
	// Object[] fieldValues = struct.getFieldValues();
	// if (resultType == ResultType.KEY_VALUE) {
	// Object key = fieldValues[0];
	// Object value = fieldValues[1];
	// if (value instanceof KeyMap) {
	// KeyMap keyMap = (KeyMap)value;
	// Object primaryColumnValue = keyMap.get(primaryColumnName);
	// Object rowData[] = (Object[])rowMap.get(primaryColumnValue);
	// boolean isInsert = rowData == null;
	// if (isInsert) {
	// // Insert
	// rowData = new Object[fieldsMethodsVector.size() + 2];
	// rowData[0] = new Integer(getRowCount() + 1);
	// rowMap.put(primaryColumnValue, rowData);
	// }
	// rowData[rowData.length - 1] = dataObject; // user data
	// }
	// }
	// } else {
	// super.putRow(dataObject);
	// }
	// }

}
