package com.netcrest.pado.ui.swing.pado.hazelcast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.security.auth.login.LoginException;

import org.hazelcast.addon.hql.HqlQuery;

import com.hazelcast.client.AuthenticationException;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.Client;
import com.hazelcast.core.ClientListener;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.Member;
import com.hazelcast.nio.serialization.DataSerializableFactory;
import com.hazelcast.nio.serialization.PortableFactory;
import com.hazelcast.security.Credentials;
import com.hazelcast.security.UsernamePasswordCredentials;
import com.netcrest.pado.exception.PadoException;
import com.netcrest.pado.internal.util.PadoUtil;
import com.netcrest.pado.ui.swing.ISharedCache;

public class HazelcastSharedCache implements ISharedCache {
	private final static HazelcastSharedCache sharedCache = new HazelcastSharedCache();

	private HazelcastInstance hz;
	private HqlQuery hql;
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
		this.envName = envName;
		this.locators = locators;
		this.appId = appId;
		this.domain = domain;
		this.username = username;
		this.password = password;
		reset();
		refresh();
	}

	private ClientConfig registerSerializationClasses(String factoryPropertyName, ClientConfig clientConfig) {
		String factories = PadoUtil.getProperty(factoryPropertyName);
		if (factories != null) {
			String[] split = factories.split(",");
			for (String str : split) {
				String[] split2 = str.split(":");
				if (split2.length == 2) {
					String factoryIdStr = split2[0];
					String factoryClassName = split2[1];
					try {
						int factoryId = Integer.valueOf(factoryIdStr);
						Class<?> factoryClass = Class.forName(factoryClassName);
						if (factoryPropertyName.contains("portable")) {
							clientConfig.getSerializationConfig().addPortableFactoryClass(factoryId,
									(Class<PortableFactory>) factoryClass);
						} else if (factoryPropertyName.contains("data_serializable")) {
							clientConfig.getSerializationConfig().addDataSerializableFactoryClass(factoryId,
									(Class<DataSerializableFactory>) factoryClass);
						}
					} catch (Exception ex) {
						throw new PadoException(
								"Invalid serialization factory class defined: " + str + ". Please correct the property "
										+ factoryPropertyName + " in the file etc/pado.properties");
					}
				}
			}
		}
		return clientConfig;
	}

	@Override
	public void reset() throws PadoException, LoginException {

		boolean isConfigFileEnabled = PadoUtil.getBoolean("hazelcast.client.config.file.enabled", false);
		if (isConfigFileEnabled) {
			hz = HazelcastClient.newHazelcastClient();
			Set<Member> set = hz.getCluster().getMembers();
			for (Member member : set) {
				this.locators = member.getAddress().getHost() + ":" + member.getAddress().getPort();
				break;
			}
		} else {
			ClientConfig clientConfig = new ClientConfig();
			clientConfig.getNetworkConfig().addAddress(locators);
			registerSerializationClasses("hazelcast.client.config.serialization.portable.factories", clientConfig);
			registerSerializationClasses("hazelcast.client.config.serialization.dataSerializable.factories", clientConfig);

			// Hack - see if we can connect without authentication. We do this because if
			// the server has not been configured for authentication then the credential
			// configuration will fail.
			try {
				hz = HazelcastClient.newHazelcastClient(clientConfig);
			} catch (Throwable th) {
				Credentials credentials = new UsernamePasswordCredentials(username, new String(password));
				clientConfig.setCredentials(credentials);
				hz = HazelcastClient.newHazelcastClient(clientConfig);
			}
		}

		hql = HqlQuery.newHqlQueryInstance(hz);
		refresh();
	}

	@Override
	public void refresh() {
		hql.refresh();
		mapList = __getMapList();
		mapSet = __getMapSet();
		mapMap = __getMapMap();
	}

	public HazelcastInstance getHazelcastInstance() {
		return hz;
	}

	public HqlQuery getHqlQueryInstance() {
		return hql;
	}

	public String getGridName() {
		return "hazelcast";
	}

	@SuppressWarnings("rawtypes")
	public IMap getMap(String path) {
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

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private List<IMap> __getMapList() {
		return new ArrayList<IMap>(hql.getMapOfMaps().values());
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

	public TreeSet<MapItem> getMapSet() {
		return mapSet;
	}

	@SuppressWarnings("rawtypes")
	private TreeSet<MapItem> __getMapSet() {
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
	private TreeMap<String, IMap> __getMapMap() {
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

		public MapItem(String name, String mapPath, IMap map, TreeSet<MapItem> childSet) {
			this.name = name;
			this.mapPath = mapPath;
			this.map = map;
		}

		public IMap getMap() {
			return map;
		}

		public String getName() {
			return name;
		}

		public String getFullPath() {
			return mapPath;
		}

		public String getMapName() {
			if (map == null) {
				return null;
			} else {
				return map.getName();
			}
		}

		public TreeSet<MapItem> getChildSet(boolean recursive) {
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
			return mapPath.compareTo(((MapItem) o).mapPath);
		}
	}
}
