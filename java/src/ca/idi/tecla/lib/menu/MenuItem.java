package ca.idi.tecla.lib.menu;

import java.lang.reflect.Method;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.ActionProvider;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View;

public class MenuItem implements android.view.MenuItem{

	private android.view.MenuItem menuItem;
	private ca.idi.tecla.lib.menu.SubMenu subMenu;
	private OnMenuItemClickListener mClickListener;
	private static final String TAG = "ca.idi.tecla.lib.menu.MenuItem";
	
	private static Method collapseActionView;
	private static Method expandActionView;
	private static Method getActionProvider;
	private static Method getActionView;
	private static Method isActionViewExpanded;
	private static Method setActionProvider;
	private static Method setActionView;
	private static Method setActionView_int;
	private static Method setShowAsAction;
	private static Method setShowAsActionFlags;
	private static Method setOnActionExpandListener;
	
	static{
		try {
			collapseActionView = android.view.MenuItem.class.getMethod("collapseActionView");
		} catch (Exception e) {}

		try {
			expandActionView = android.view.MenuItem.class.getMethod("expandActionView");
		} catch (Exception e) {}
		
		try {
			getActionProvider = android.view.MenuItem.class.getMethod("getActionProvider");
		} catch (Exception e) {}
		
		try {
			getActionView = android.view.MenuItem.class.getMethod("getActionView");
		} catch (Exception e) {}
		
		try {
			isActionViewExpanded = android.view.MenuItem.class.getMethod("isActionViewExpanded");
		} catch (Exception e) {}
		
		try {
			setActionProvider = android.view.MenuItem.class.getMethod("setActionProvider", ActionProvider.class);
		} catch (Exception e) {}
		
		try {
			setActionView = android.view.MenuItem.class.getMethod("setActionView", View.class);
		} catch (Exception e) {}
		
		try {
			setActionView_int = android.view.MenuItem.class.getMethod("setActionView", int.class);
		} catch (Exception e) {}
		
		try {
			setShowAsAction = android.view.MenuItem.class.getMethod("setShowAsAction", int.class);
		} catch (Exception e) {}
		
		try {
			setShowAsActionFlags = android.view.MenuItem.class.getMethod("setShowAsActionFlags", int.class);
		} catch (Exception e) {}
		
		try {
			setOnActionExpandListener = android.view.MenuItem.class.getMethod("setOnActionExpandListener", android.view.MenuItem.OnActionExpandListener.class);
		} catch (Exception e) {}

	}
	
	public MenuItem(android.view.MenuItem menuItem){
		this.menuItem = menuItem;
		subMenu = null;
	}

	/**
	 * Set the sub menu(if any) associated with this menu item
	 * @param subMenu the sub menu associated with this menu item
	 */
	public void setSubMenu(ca.idi.tecla.lib.menu.SubMenu subMenu){
		this.subMenu = subMenu;
	}

	public boolean collapseActionView() {
		try {
			return (Boolean) collapseActionView.invoke(menuItem);
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
			return false;
		}
	}

	public boolean expandActionView() {
		try {
			return (Boolean) expandActionView.invoke(menuItem);
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
			return false;
		}
	}

	public ActionProvider getActionProvider() {
		try {
			return (ActionProvider) getActionProvider.invoke(menuItem);
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
			return null;
		}
	}

	public View getActionView() {
		try {
			return (View) getActionView.invoke(menuItem);
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
			return null;
		}
	}

	public char getAlphabeticShortcut() {
		return menuItem.getAlphabeticShortcut();
	}

	public int getGroupId() {
		return menuItem.getGroupId();
	}

	public Drawable getIcon() {
		return menuItem.getIcon();
	}

	public Intent getIntent() {
		return menuItem.getIntent();
	}

	public int getItemId() {
		return menuItem.getItemId();
	}

	public ContextMenuInfo getMenuInfo() {
		return menuItem.getMenuInfo();
	}

	public char getNumericShortcut() {
		return menuItem.getNumericShortcut();
	}

	public int getOrder() {
		return menuItem.getOrder();
	}

	public ca.idi.tecla.lib.menu.SubMenu getSubMenu() {
		return subMenu;
	}

	public CharSequence getTitle() {
		return menuItem.getTitle();
	}

	public CharSequence getTitleCondensed() {
		return menuItem.getTitleCondensed();
	}

	public boolean hasSubMenu() {
		return menuItem.hasSubMenu();
	}

	public boolean isActionViewExpanded() {
		try {
			return (Boolean) isActionViewExpanded.invoke(menuItem);
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
			return false;
		}
	}

	public boolean isCheckable() {
		return menuItem.isCheckable();
	}

	public boolean isChecked() {
		return menuItem.isChecked();
	}

	public boolean isEnabled() {
		return menuItem.isEnabled();
	}

	public boolean isVisible() {
		return menuItem.isVisible();
	}

	public ca.idi.tecla.lib.menu.MenuItem setActionProvider(ActionProvider actionProvider) {
		try {
			setActionProvider.invoke(menuItem, actionProvider);
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		}
		return this;
	}

	public ca.idi.tecla.lib.menu.MenuItem setActionView(View view) {
		try {
			setActionView.invoke(menuItem, view);
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		}
		return this;
	}

	public ca.idi.tecla.lib.menu.MenuItem setActionView(int resId) {
		try {
			setActionView_int.invoke(menuItem, resId);
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		}
		return this;
	}

	public ca.idi.tecla.lib.menu.MenuItem setAlphabeticShortcut(char alphaChar) {
		menuItem.setAlphabeticShortcut(alphaChar);
		return this;
	}

	public ca.idi.tecla.lib.menu.MenuItem setCheckable(boolean checkable) {
		menuItem.setCheckable(checkable);
		return this;
	}

	public ca.idi.tecla.lib.menu.MenuItem setChecked(boolean checked) {
		menuItem.setChecked(checked);
		return this;
	}

	public ca.idi.tecla.lib.menu.MenuItem setEnabled(boolean enabled) {
		menuItem.setEnabled(enabled);
		return this;
	}

	public ca.idi.tecla.lib.menu.MenuItem setIcon(Drawable icon) {
		menuItem.setIcon(icon);
		return this;
	}

	public ca.idi.tecla.lib.menu.MenuItem setIcon(int iconRes) {
		menuItem.setIcon(iconRes);
		return this;
	}

	public ca.idi.tecla.lib.menu.MenuItem setIntent(Intent intent) {
		menuItem.setIntent(intent);
		return this;
	}

	public ca.idi.tecla.lib.menu.MenuItem setNumericShortcut(char numericChar) {
		menuItem.setNumericShortcut(numericChar);
		return this;
	}

	public ca.idi.tecla.lib.menu.MenuItem setOnActionExpandListener(
			OnActionExpandListener listener) {
		try {
			setOnActionExpandListener.invoke(menuItem, listener);
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		}
		return this;
	}

	public ca.idi.tecla.lib.menu.MenuItem setOnMenuItemClickListener(
			OnMenuItemClickListener menuItemClickListener) {
		mClickListener = menuItemClickListener;
		menuItem.setOnMenuItemClickListener(menuItemClickListener);
		return this;
	}

	public ca.idi.tecla.lib.menu.MenuItem setShortcut(char numericChar, char alphaChar) {
		menuItem.setShortcut(numericChar, alphaChar);
		return this;
	}

	public void setShowAsAction(int actionEnum) {
		try {
			setShowAsAction.invoke(menuItem, actionEnum);
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		}
	}

	public ca.idi.tecla.lib.menu.MenuItem setShowAsActionFlags(int actionEnum) {
		try {
			setShowAsActionFlags.invoke(menuItem, actionEnum);
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		}
		return this;
	}

	public ca.idi.tecla.lib.menu.MenuItem setTitle(CharSequence title) {
		menuItem.setTitle(title);
		return this;
	}

	public ca.idi.tecla.lib.menu.MenuItem setTitle(int title) {
		menuItem.setTitle(title);
		return this;
	}

	public ca.idi.tecla.lib.menu.MenuItem setTitleCondensed(CharSequence title) {
		menuItem.setTitleCondensed(title);
		return this;
	}

	public ca.idi.tecla.lib.menu.MenuItem setVisible(boolean visible) {
		menuItem.setVisible(visible);
		return this;
	}

	public OnMenuItemClickListener getOnMenuItemClickListener(){
		return mClickListener;
	}

	public boolean invokeOnMenuItemClickListener(){
		if(mClickListener == null)
			return false;
		else
			return mClickListener.onMenuItemClick(this);
	}

}
