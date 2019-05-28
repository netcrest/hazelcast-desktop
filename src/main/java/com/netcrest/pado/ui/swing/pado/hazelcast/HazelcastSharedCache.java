package com.netcrest.pado.ui.swing.pado.hazelcast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.security.auth.login.LoginException;

import org.hazelcast.addon.util.OrderBy;
import org.hazelcast.demo.nw.data.Order;
import org.hazelcast.demo.nw.data.PortableFactoryImpl;
import org.junit.Assert;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.DistributedObject;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.query.PagingPredicate;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.Predicates;
import com.hazelcast.query.SqlPredicate;
import com.netcrest.pado.exception.PadoException;
import com.netcrest.pado.ui.swing.ISharedCache;

public class HazelcastSharedCache implements ISharedCache {
	private final static HazelcastSharedCache sharedCache = new HazelcastSharedCache();

	private HazelcastInstance hz;
	private String envName;
	private String locators;
	private String appId;
	private String domain;
	private String username;
	private char[] password;
	
	private List<IMap> mapList;
	private TreeMap<String, IMap> mapMap;
	private TreeSet<MapItem> mapSet;

	private HazelcastSharedCache() {
	}

	public static HazelcastSharedCache getSharedCache() {
		return sharedCache;
	}

	@Override
	public String getLocators() {
		return locators;
	}

	@Override
	public String getUsername() {
		return username;
	}

	@Override
	public void login(String envName, String locators, String appId, String domain, String username, char[] password)
			throws PadoException, LoginException {
		reset();
		refresh();
	}

	private void testData() {
		IMap<String, Order> map = hz.getMap("nw/orders");

		String key = "10259";
		Order order = map.get(key);
		System.out.println(key + ": " + order);

		Predicate regexPredicate = Predicates.regex("shipCountry", ".*");
		Predicate allPredicate = Predicates.alwaysTrue();
		SqlPredicate sqlPredicate = new SqlPredicate("shipCountry='France'");
		Predicate equalPreciate = Predicates.equal("shipCountry", "France");
		Predicate queryPredicate = allPredicate;

		Collection<Order> values = map.values(queryPredicate);
		System.out.println("values size: " + values.size());

//	        OrderBy orderBy = new OrderBy(OrderByType.KEYS);
		OrderBy orderBy = new OrderBy("customerId", "orderId", "orderDate");

		// a predicate which filters out non ClassA students, sort them descending order
		// and fetches 4 students for each page
		PagingPredicate<String, Order> pagingPredicate = new PagingPredicate<String, Order>(queryPredicate, orderBy,
				10);

		String prevKey = "";
		Set<Map.Entry<String, Order>> entries;
		int i = 0;
		do {
			if (i > 0) {
				pagingPredicate.nextPage();
			}
			entries = map.entrySet(pagingPredicate);
			System.out.println();
			System.out.println("No\tKey\tOrderId\tCustomerId\tOrderDate\t\tShipCountry");
			for (Map.Entry<String, Order> entry : entries) {
				key = entry.getKey();
				Order order2 = entry.getValue();
				prevKey = key;
				Assert.assertTrue(prevKey.compareTo(key) <= 0);
				System.out.println(++i + "\t" + key + "\t" + order2.getOrderId() + "\t" + order2.getCustomerId() + "\t"
						+ order2.getOrderDate() + "\t" + order2.getShipCountry());
			}
		} while (entries.size() != 0);
	}

	@Override
	public void reset() throws PadoException, LoginException {
//		ClientConfig config = new ClientConfig();
//		config.getNetworkConfig().addAddress("padomac:5701");
//		config.getSerializationConfig().addPortableFactory(1, new PortableFactoryImpl());
//		hz = HazelcastClient.newHazelcastClient(config);
		hz = HazelcastClient.newHazelcastClient();
		refresh();
	}

	@Override
	public void refresh() {
		mapList = __getMapList();
		mapSet = __getMapSet();
		mapMap = __getMapMap();
	}

	public HazelcastInstance getHazelcastInstance() {
		return hz;
	}
	
	public String getGridName()
	{
		return "hazelcast";
	}
	
	@SuppressWarnings("rawtypes")
	public IMap getMap(String path)
	{
		if (path == null || path.length() == 0) {
			return null;
		}
		IMap map = mapMap.get(path);
		if (map == null && path.startsWith("/")) {
			map = mapMap.get(path.substring(1));
		}
		return map;
	}

//	@SuppressWarnings("rawtypes")
//	public IMap[] getAllMaps() {
//		Collection<DistributedObject> col = hz.getDistributedObjects();
//		ArrayList<IMap> mapList = new ArrayList<IMap>();
//		for (DistributedObject dobj : col) {
//			if (dobj instanceof IMap) {
//				mapList.add((IMap)dobj);
//			}
//		}
//		return mapList.toArray(new IMap[mapList.size()]);
//	}
	
	@SuppressWarnings("rawtypes")
	private List<IMap> __getMapList() {
		Collection<DistributedObject> col = hz.getDistributedObjects();
		ArrayList<IMap> mapList = new ArrayList<IMap>();
		for (DistributedObject dobj : col) {
			if (dobj instanceof IMap) {
				mapList.add((IMap)dobj);
			}
		}
		return mapList;
	}
	
	public List<IMap> getMapList() {
		return mapList;
	}

//	@SuppressWarnings("rawtypes")
//	public String[] getAllMapNames()
//	{
//		IMap[] maps = getAllMaps();
//		String[] mapNames = new String[maps.length];
//		for (int i = 0; i < maps.length; i++) {
//			mapNames[i] = maps[i].toString();
//		}
//		Arrays.sort(mapNames);
//		return mapNames;
//	}
//	
//	@SuppressWarnings("rawtypes")
//	public List<String> getAllMapNameList()
//	{
//		List<IMap> mapList = getAllMapList();
//		List<String> mapNameList = new ArrayList<String>(mapList.size());
//		for (IMap map : mapList) {
//			mapNameList.add(map.toString());
//		}
//		return mapNameList;
//	}
	
	public TreeSet<MapItem> getMapSet()
	{
		return mapSet;
	}
	
	@SuppressWarnings("rawtypes")
	private TreeSet<MapItem> __getMapSet()
	{
		TreeSet<MapItem> allSet = new TreeSet<MapItem>();
		MapItem rootMapItem = new MapItem("", "/", null, null);
		allSet.add(rootMapItem);
		HashMap<String, MapItem> allMap = new HashMap<String, MapItem>();
		if (mapList.size() > 0) {
			rootMapItem.childSet = new TreeSet<MapItem>();
			for (IMap map : mapList) {
				String mapName = map.getName();
				String[] tokens = mapName.split("\\/");
				String mapPath = "";
				MapItem mapItem = null;
				MapItem parentItem = rootMapItem;
				for (int i = 0; i < tokens.length; i++) {
					String name = tokens[i];
					mapPath = mapPath + "/" + name;
					
					mapItem = allMap.get(mapPath);
					if (mapItem == null) {
						mapItem = new MapItem(tokens[i], mapPath, null, null);
						allMap.put(mapPath, mapItem);
						allSet.add(mapItem);
					}
					
					if (parentItem.childSet == null) {
						parentItem.childSet = new TreeSet<MapItem>();
					}
					parentItem.childSet.add(mapItem);
					parentItem = mapItem;
				}
				if (mapItem != null) {
					mapItem.map = map;
				}
				
			}
		}
		return allSet;
	}
	
	@SuppressWarnings("rawtypes")
	private TreeMap<String, IMap> __getMapMap()
	{
		TreeMap<String, IMap> mapMap = new TreeMap<String, IMap>();
		if (mapList.size() > 0) {
			for (IMap map : mapList) {
				mapMap.put(map.getName(), map);
			}
		}
		return mapMap;
	}
	

	@SuppressWarnings("rawtypes")
	public static class MapItem implements Comparable {
		String mapPath;
		String name;
		IMap map;
		TreeSet<MapItem> childSet;
		
		public MapItem(String name, String mapPath, IMap map, TreeSet<MapItem> childSet)
		{
			this.name = name;
			this.mapPath = mapPath;
			this.map = map;
		}
		
		public IMap getMap()
		{
			return map;
		}
		
		public String getName()
		{
			return name;
		}
		
		public String getFullPath()
		{
			return mapPath;
		}
		
		public TreeSet<MapItem> getChildSet(boolean recursive)
		{
			if (recursive) {
				// TODO:
				return childSet;
			} else {
				return childSet;
			}
		}
		
		@Override
		public String toString() {
//			return "MapItem [mapPath=" + mapPath + ", name=" + name + ", map=" + map + ", childSet=" + childSet + "]";
			return name;
		}

		@Override
		public int compareTo(Object o) {
			if (o == null) {
				return 1;
			}
			return mapPath.compareTo(((MapItem)o).mapPath);
		}
	}
}
