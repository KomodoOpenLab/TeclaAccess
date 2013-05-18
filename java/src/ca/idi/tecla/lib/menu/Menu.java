package ca.idi.tecla.lib.menu;

import java.util.HashMap;
import android.content.ComponentName;
import android.content.Intent;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.SubMenu;

public class Menu implements android.view.Menu{

	private android.view.Menu menu;
	private HashMap<android.view.MenuItem, ca.idi.tecla.lib.menu.MenuItem> menuItemMap;

	public Menu(android.view.Menu menu){
		this.menu = menu;
		menuItemMap = new HashMap<android.view.MenuItem, ca.idi.tecla.lib.menu.MenuItem>();
		refreshMap();
	}

	public Menu() {
		menuItemMap = new HashMap<android.view.MenuItem, ca.idi.tecla.lib.menu.MenuItem>();
	}

	/**
	 * Set the standard menu to support this custom Menu class
	 * @param menu the options menu to be displayed
	 */
	public void setMenu(android.view.Menu menu){
		this.menu = menu;
		if(menuItemMap != null)
			menuItemMap.clear();
		else{
			menuItemMap = new HashMap<android.view.MenuItem, ca.idi.tecla.lib.menu.MenuItem>();
		}
		refreshMap();
	}

	public ca.idi.tecla.lib.menu.MenuItem add(CharSequence title) {
		return addMenuItemToMap(menu.add(title));
	}

	public ca.idi.tecla.lib.menu.MenuItem add(int titleRes) {
		return addMenuItemToMap(menu.add(titleRes));
	}

	public ca.idi.tecla.lib.menu.MenuItem add(int groupId, int itemId, int order, CharSequence title) {
		return addMenuItemToMap(menu.add(groupId,itemId,order,title));
	}

	public ca.idi.tecla.lib.menu.MenuItem add(int groupId, int itemId, int order, int titleRes) {
		return addMenuItemToMap(menu.add(groupId,itemId,order,titleRes));
	}

	public int addIntentOptions(int groupId, int itemId, int order,
			ComponentName caller, Intent[] specifics, Intent intent, int flags,
			MenuItem[] outSpecificItems) {
		int N = menu.addIntentOptions(groupId, itemId, order, caller, specifics, intent, flags, outSpecificItems);
		refreshMap();
		return N;
	}

	public ca.idi.tecla.lib.menu.SubMenu addSubMenu(CharSequence title) {
		SubMenu subMenu = menu.addSubMenu(title);
		//custom subMenu
		ca.idi.tecla.lib.menu.SubMenu mSubMenu = new ca.idi.tecla.lib.menu.SubMenu(subMenu);
		//custom MenuItem
		ca.idi.tecla.lib.menu.MenuItem mItem = addMenuItemToMap(subMenu.getItem());
		mItem.setSubMenu(mSubMenu);
		mSubMenu.setMenuItem(mItem);
		return mSubMenu;
	}

	public ca.idi.tecla.lib.menu.SubMenu addSubMenu(int titleRes) {
		SubMenu subMenu = menu.addSubMenu(titleRes);
		//custom subMenu
		ca.idi.tecla.lib.menu.SubMenu mSubMenu = new ca.idi.tecla.lib.menu.SubMenu(subMenu);
		//custom MenuItem
		ca.idi.tecla.lib.menu.MenuItem mItem = addMenuItemToMap(subMenu.getItem());
		mItem.setSubMenu(mSubMenu);
		mSubMenu.setMenuItem(mItem);
		return mSubMenu;
	}

	public ca.idi.tecla.lib.menu.SubMenu addSubMenu(int groupId, int itemId, int order,
			CharSequence title) {
		SubMenu subMenu = menu.addSubMenu(groupId, itemId, order, title);
		//custom subMenu
		ca.idi.tecla.lib.menu.SubMenu mSubMenu = new ca.idi.tecla.lib.menu.SubMenu(subMenu);
		//custom MenuItem
		ca.idi.tecla.lib.menu.MenuItem mItem = addMenuItemToMap(subMenu.getItem());
		mItem.setSubMenu(mSubMenu);
		mSubMenu.setMenuItem(mItem);
		return mSubMenu;
	}

	public ca.idi.tecla.lib.menu.SubMenu addSubMenu(int groupId, int itemId, int order, int titleRes) {
		SubMenu subMenu = menu.addSubMenu(groupId, itemId, order, titleRes);
		//custom subMenu
		ca.idi.tecla.lib.menu.SubMenu mSubMenu = new ca.idi.tecla.lib.menu.SubMenu(subMenu);
		//custom MenuItem
		ca.idi.tecla.lib.menu.MenuItem mItem = addMenuItemToMap(subMenu.getItem());
		mItem.setSubMenu(mSubMenu);
		mSubMenu.setMenuItem(mItem);
		return mSubMenu;
	}

	public void clear() {
		//clear the map since all menu items have been removed
		menuItemMap.clear();
		menu.clear();
	}

	public void close() {
		menu.close();
	}

	public ca.idi.tecla.lib.menu.MenuItem findItem(int id) {
		MenuItem item = menu.findItem(id);
		//return the custom menu item from the hash map
		return menuItemMap.get(item);
	}

	public ca.idi.tecla.lib.menu.MenuItem getItem(int index) {
		MenuItem item = menu.getItem(index);
		return menuItemMap.get(item);
	}

	public boolean hasVisibleItems() {
		return menu.hasVisibleItems();
	}

	public boolean isShortcutKey(int keyCode, KeyEvent event) {
		return menu.isShortcutKey(keyCode, event);
	}

	public boolean performIdentifierAction(int id, int flags) {
		return menu.performIdentifierAction(id, flags);
	}

	public boolean performShortcut(int keyCode, KeyEvent event, int flags) {
		return menu.performShortcut(keyCode, event, flags);
	}

	public void removeGroup(int groupId) {
		menu.removeGroup(groupId);
		refreshMap();
	}

	public void removeItem(int id) {
		menu.removeItem(id);
		refreshMap();
	}

	public void setGroupCheckable(int group, boolean checkable,
			boolean exclusive) {
		menu.setGroupCheckable(group, checkable, exclusive);
	}

	public void setGroupEnabled(int group, boolean enabled) {
		menu.setGroupEnabled(group, enabled);
	}

	public void setGroupVisible(int group, boolean visible) {
		menu.setGroupVisible(group, visible);
	}

	public void setQwertyMode(boolean isQwerty) {
		menu.setQwertyMode(isQwerty);
	}

	public int size() {
		return menu.size();
	}

	/**
	 * Refreshes the menu item hash map with the menu items currently in the menu
	 */
	private void refreshMap(){
		menuItemMap.clear();
		for(int i=0;i<menu.size();i++){
			addMenuItemToMap(menu.getItem(i));
		}
	}
	
	/**
	 *Stores the menuItem in a HashMap with its custom MenuItem counterpart and returns the custom MenuItem
	 * @param menuItem is the MenuItem to be stored in the hash map
	 * @return the menuItem's custom counterpart
	 */
	private ca.idi.tecla.lib.menu.MenuItem addMenuItemToMap(android.view.MenuItem menuItem){
		ca.idi.tecla.lib.menu.MenuItem mItem = new ca.idi.tecla.lib.menu.MenuItem(menuItem);
		menuItemMap.put(menuItem, mItem);
		return mItem;
	}
}
