package org.mobileapi.helper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Named;

import org.apache.log4j.Logger;
import org.apache.zookeeper.*;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.data.*;
import org.apache.zookeeper.CreateMode;
import org.mobileapi.server.api.service.Factory;

/*
 * Wrapper for  Zookeeper
 * http://zookeeper.apache.org/
 */

public class Zoo {

	private Logger L = Logger.getLogger(Zoo.class);
	
	public static final String UPDATED = "/updated";
	public static final String BROKER0QUEUE = "/broker0/queue";
	public static final String GATE0QUEUE = "/gate0/queue";
	public static final String GATE1QUEUE = "/gate0/queue";

	private ZkConnector _ZkConnector;
	private ZooKeeper _ZooKeeper;

	public String get(final String path) throws Exception{
		try {
			byte[] b = _ZooKeeper.getData(path, false, null);
			return new String(b, "UTF-8");
		} catch (KeeperException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		throw new Exception("No zookeeper path for " + path);
	}
	
	public void delete(String path) {
		try {
			_ZooKeeper.delete(path, -1);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (KeeperException e) {
			e.printStackTrace();
		}
	}
	
	public void set(String path, String val) {

		byte ptext[] =  val.getBytes();
		try {
			Stat stat = _ZooKeeper.exists(path, false);
			if(stat != null)
			{

			}
			else
			{
				createNode( path);
			}
			_ZooKeeper.setData(path, ptext, -1);
		} catch (KeeperException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void setDate(String path, Date d) {

		byte ptext[] =  d.toString().getBytes();
		try {
			_ZooKeeper.setData(path, ptext, -1);
		} catch (KeeperException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public List<String> list(String path)
	{
		try {
			return _ZooKeeper.getChildren(path, false);
		} catch (KeeperException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return new ArrayList<String>();
	}
	
	public boolean createNode(String path)
	{
		String[] parts = path.split("/");
		try {
			List<ACL> acl = new ArrayList<ACL>(ZooDefs.Ids.OPEN_ACL_UNSAFE );	
			String newPath = "";
			for(int i=1; i < parts.length;i++)
			{
				newPath += "/" +  parts[i];
				Stat stat = _ZooKeeper.exists(newPath, false);
				if(stat !=null)
				{
					continue;
				}
				else
				{
					_ZooKeeper.create(newPath, new byte[]{'-'}, acl, CreateMode.PERSISTENT );
				}
			}
			return true;
		} catch (KeeperException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return false;
	}

	public void startup() {
		try {
			_ZkConnector = new ZkConnector();
			_ZkConnector.connect("localhost");
			_ZooKeeper = _ZkConnector.getZooKeeper();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// Test Zookeeper
		Date d = new Date();
		System.out.println("Setting Zookepper to " + d);
		createNode(UPDATED);
		setDate(UPDATED, d);

		// get Date
		try {
			System.out.println("Zoo returns: " + get(UPDATED) );
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// TODO 
	public void process(WatchedEvent event)
	{
		System.out.println("WatchedEvent " + event);
	}

	public void shutdown() {
		System.out.println("Zoo shutdown : ");
		try {
			_ZooKeeper.close();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
