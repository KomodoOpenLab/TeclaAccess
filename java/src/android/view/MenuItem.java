package android.view;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.ContextMenu.ContextMenuInfo;

public interface MenuItem {

	public static interface OnMenuItemClickListener{
		public abstract boolean onMenuItemClick(MenuItem item);
	}

	public static interface OnActionExpandListener{
	}

	public char getAlphabeticShortcut();

	public int getGroupId();

	public Drawable getIcon();

	public Intent getIntent();

	public int getItemId();

	public ContextMenuInfo getMenuInfo();

	public char getNumericShortcut();
	
	public int getOrder();

	public SubMenu getSubMenu();

	public CharSequence getTitle();

	public CharSequence getTitleCondensed();
	
	public boolean hasSubMenu();
	
	public boolean isCheckable();

	public boolean isChecked();	

	public boolean isEnabled();
	
	public boolean isVisible();

	public MenuItem setAlphabeticShortcut(char alphaChar);

	public MenuItem setCheckable(boolean checkable);

	public MenuItem setChecked(boolean checked);
	
	public MenuItem setEnabled(boolean enabled);
	
	public MenuItem setIcon(Drawable icon);
	
	public MenuItem setIcon(int iconRes);
	
	public MenuItem setIntent(Intent intent);
	
	public MenuItem setNumericShortcut(char numericChar);
	
	public MenuItem setOnMenuItemClickListener(
			OnMenuItemClickListener menuItemClickListener);
	
	public MenuItem setShortcut(char numericChar, char alphaChar);
	
	public MenuItem setTitle(CharSequence title);
	
	public MenuItem setTitle(int title);

	public MenuItem setTitleCondensed(CharSequence title);

	public MenuItem setVisible(boolean visible);

}
