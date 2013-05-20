/** Copyright (C) 2012 
 * Arindam Nath (strider2023@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.touchmenotapps.widget.radialmenu.semicircularmenu;

import java.util.LinkedHashMap;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.touchmenotapps.widget.radialmenu.RadialMenuColors;

/**
 * This is the core class that handles the widget display and user interaction.
 * TODO At times the arc area bound check fails. Gotta check that.
 * 
 * @author Arindam Nath (strider2023@gmail.com), Philipp Ebert
 *         philebert@gmail.com
 */
public class SemiCircularRadialMenu extends View {

	// Static Access Variables
	public static final int VERTICAL_RIGHT = 0;
	public static final int VERTICAL_LEFT = 1;
	public static final int HORIZONTAL_TOP = 2;
	public static final int HORIZONTAL_BOTTOM = 3;
	// Private non-shared variables (internal logic)
	private boolean isMenuVisible = false;
	private boolean isMenuTogglePressed = false;
	private boolean isMenuItemPressed = false;
	private String mPressedMenuItemID = null;
	private int mStartAngle = 0;
	private int mMenuDiameter = 0;
	private float mMenuRadius = 0.0f;
	private int mIconDimen = 64;
	private RectF mMenuRect;
	private RectF mMenuCenterButtonRect;
	private Point mViewAnchorPoints;
	private int mCenterCurrentBackgroundColor = Color.WHITE;
	private int mCenterCurrentTextColor = Color.DKGRAY;
	private Paint mMenuPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private LinkedHashMap<String, SemiCircularRadialMenuItem> mMenuItems = new LinkedHashMap<String, SemiCircularRadialMenuItem>();
	private int mOrientation = HORIZONTAL_BOTTOM;

	// Variables that can be user defined
	// Scale
	private float textSize = 12 * getResources().getDisplayMetrics().density;
	private float menuToItemRatio = 3;
	private float menuScaleFactor = 1;

	// Shadows
	private boolean showShadows = true;
	private float mShadowRadius = 5 * getResources().getDisplayMetrics().density;
	private int mShadowColor = Color.GRAY;

	// Center button
	private boolean showCenterText = false;
	private boolean showCenterIcon = false;
	private String openMenuText = "Open";
	private String closeMenuText = "Close";
	private Drawable centerIcon = null;
	private int centerBackgroundColor = Color.WHITE;
	private int centerToggleBackgroundColor = RadialMenuColors.HOLO_LIGHT_BLUE;
	private int centerTextColor = Color.DKGRAY;
	private int centerToggleTextColor = Color.DKGRAY;
	private String centerMenuText = openMenuText; // Not to be set using setter method

	// border
	private Paint mMenuBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private RectF mMenuCenterButtonBorderRec;
	private Rect mMenuCenterButtonIconRect;

	public SemiCircularRadialMenu(Context context) {
		super(context);
		init();
	}

	public SemiCircularRadialMenu(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public SemiCircularRadialMenu(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void initPaint() {
		mMenuPaint.setTextSize(textSize);
		mMenuPaint.setStrokeWidth(5);
		mMenuPaint.setStyle(Style.FILL_AND_STROKE);
	}

	private void init() {
		initPaint();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (showShadows) {
			mMenuPaint.setShadowLayer(mShadowRadius, 0.0f, 0.0f, mShadowColor);
		}
		// Draw the menu if the menu is to be displayed.
		if (isMenuVisible) {
			canvas.drawArc(mMenuRect, mStartAngle, 180, true, mMenuPaint);
			// See if there is any item in the collection
			if (mMenuItems.size() > 0) {
				float mStart = mStartAngle;
				// Get the sweep angles based on the number of menu items
				float mSweep = 180 / mMenuItems.size();
				for (SemiCircularRadialMenuItem item : mMenuItems.values()) {
					mMenuPaint.setColor(item.getBackgroundColor());
					item.setMenuPath(mMenuCenterButtonRect, mMenuRect, mStart, mSweep, mMenuRadius, mViewAnchorPoints);
					canvas.drawPath(item.getMenuPath(), mMenuPaint);
					
					if (mMenuBorderPaint != null) {
						item.setBorderPath(mMenuCenterButtonRect, mMenuRect, mStart, mSweep, mMenuRadius, mViewAnchorPoints);
						canvas.drawPath(item.getBorderPath(), mMenuBorderPaint);
					}

					if (showCenterText) {
						if (isShowShadows()) {
							mMenuPaint.setShadowLayer(mShadowRadius, 0.0f, 0.0f, Color.TRANSPARENT);
						}
						mMenuPaint.setColor(item.getTextColor());
						canvas.drawTextOnPath(item.getText(), item.getMenuPath(), 5, textSize, mMenuPaint);
						if (isShowShadows()) {
							mMenuPaint.setShadowLayer(mShadowRadius, 0.0f, 0.0f, mShadowColor);
						}
					}
					item.getIcon().draw(canvas);
					mStart += mSweep;
				}
			}
		}

		// Draw the center menu toggle piece
		mMenuPaint.setColor(mCenterCurrentBackgroundColor);
		canvas.drawArc(mMenuCenterButtonRect, mStartAngle, 180, true, mMenuPaint);

		if (mMenuBorderPaint != null) {
			canvas.drawArc(mMenuCenterButtonBorderRec, mStartAngle, 180, true, mMenuBorderPaint);
		}

		if (showShadows) {
			mMenuPaint.setShadowLayer(mShadowRadius, 0.0f, 0.0f, Color.TRANSPARENT);
		}

		// Draw the center text
		if (showCenterText) {
			drawCenterText(canvas, mMenuPaint);
		}
		//draw the center icon
		if (showCenterIcon) {
			drawCenterIcon(canvas);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int x = (int) event.getX();
		int y = (int) event.getY();

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			if (mMenuCenterButtonRect.contains(x, y)) {
				mCenterCurrentTextColor = centerToggleTextColor;
				mCenterCurrentBackgroundColor = centerToggleBackgroundColor;
				isMenuTogglePressed = true;
				invalidate();
			} else if (isMenuVisible) {
				if (mMenuItems.size() > 0) {
					for (SemiCircularRadialMenuItem item : mMenuItems.values()) {
						if (mMenuRect.contains((int) x, (int) y))
							if (item.getBounds().contains((int) x, (int) y)) {
								isMenuItemPressed = true;
								mPressedMenuItemID = item.getMenuID();
								break;
							}
					}

					if (mPressedMenuItemID != null) {
						mMenuItems.get(mPressedMenuItemID).setBackgroundColor(
								mMenuItems.get(mPressedMenuItemID).getMenuSelectedColor());
					}

					invalidate();
				}
			}

			if (isMenuItemPressed || isMenuTogglePressed) {
				return true;
			} else if (isMenuVisible) {
				isMenuVisible = false;
				return true;
			}
			break;
		case MotionEvent.ACTION_UP:
			if (isMenuTogglePressed) {
				mCenterCurrentBackgroundColor = centerBackgroundColor;
				mCenterCurrentTextColor = centerTextColor;
				if (isMenuVisible) {
					isMenuVisible = false;
					centerMenuText = openMenuText;
				} else {
					isMenuVisible = true;
					centerMenuText = closeMenuText;
				}
				isMenuTogglePressed = false;
				invalidate();
				return true;
			}

			if (isMenuItemPressed) {
				if (mMenuItems.get(mPressedMenuItemID).getCallback() != null) {
					mMenuItems.get(mPressedMenuItemID).getCallback().onMenuItemPressed();
				}
				mMenuItems.get(mPressedMenuItemID).setBackgroundColor(mMenuItems.get(mPressedMenuItemID).getMenuNormalColor());
				isMenuItemPressed = false;
				invalidate();
				return true;
			}
			break;
		}

		return false;
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		// Determine the diameter and the radius based on device orientation
		if (w > h) {
			mMenuDiameter = h;
			mMenuRadius = menuScaleFactor * (mMenuDiameter / 2 - (getPaddingTop() + getPaddingBottom()));
		} else {
			mMenuDiameter = w;
			mMenuRadius = menuScaleFactor * (mMenuDiameter / 2 - (getPaddingLeft() + getPaddingRight()));
		}
		// Init the draw arc Rect objects
		mMenuRect = getRadialMenuRect(false);
		mMenuCenterButtonRect = getRadialMenuRect(true);
		mMenuCenterButtonIconRect = getCenterIconRect();

		// two pixels have to be added as a hotfix, otherwise the background is
		// visible around the stroke.
		mMenuCenterButtonBorderRec = new RectF(mMenuCenterButtonRect.left - 2, mMenuCenterButtonRect.top - 2,
				mMenuCenterButtonRect.right + 2, mMenuCenterButtonRect.bottom + 2);
	}

	/**
	 * Draw the toggle menu button text.
	 * 
	 * @param canvas
	 * @param paint
	 */
	private void drawCenterText(Canvas canvas, Paint paint) {
		paint.setColor(mCenterCurrentTextColor);
		switch (mOrientation) {
		case VERTICAL_RIGHT:
			canvas.drawText(centerMenuText, getWidth() - paint.measureText(centerMenuText), getHeight() / 2, paint);
			break;
		case VERTICAL_LEFT:
			canvas.drawText(centerMenuText, 2, getHeight() / 2, paint);
			break;
		case HORIZONTAL_TOP:
			canvas.drawText(centerMenuText, (getWidth() / 2) - (paint.measureText(centerMenuText) / 2), textSize, paint);
			break;
		case HORIZONTAL_BOTTOM:
			canvas.drawText(centerMenuText, (getWidth() / 2) - (paint.measureText(centerMenuText) / 2), getHeight() - (textSize),
					paint);
			break;
		}
	}

	/**
	 * Draw the toggle menu button icon.
	 * 
	 * @param canvas
	 * @param paint
	 */
	private void drawCenterIcon(Canvas canvas) {

		if (centerIcon != null) {
			centerIcon.setBounds(mMenuCenterButtonIconRect);
			centerIcon.draw(canvas);
		}

	}

	private Rect getCenterIconRect() {
		int centerX;
		int centerY;

		switch (mOrientation) {
		case VERTICAL_RIGHT:
			centerX = (int) (mViewAnchorPoints.x - (mMenuRadius / menuToItemRatio) / 2.5);
			centerY = (int) (mViewAnchorPoints.y);
			break;
		case VERTICAL_LEFT:
			centerX = (int) (mViewAnchorPoints.x + (mMenuRadius / menuToItemRatio) / 2.5);
			centerY = (int) (mViewAnchorPoints.y);
			break;
		case HORIZONTAL_TOP:
			centerX = (int) (mViewAnchorPoints.x);
			centerY = (int) (mViewAnchorPoints.y - (mMenuRadius / menuToItemRatio) / 2.5);
			break;
		case HORIZONTAL_BOTTOM:
			centerX = (int) (mViewAnchorPoints.x);
			centerY = (int) (mViewAnchorPoints.y - (mMenuRadius / menuToItemRatio) / 2.5);
			break;
		default:
			return null;
		}

		int left = (int) (centerX - (mIconDimen / 2));
		int top = (int) (centerY - (mIconDimen / 2));
		int right = left + (mIconDimen);
		int bottom = top + (mIconDimen);

		return new Rect(left, top, right, bottom);
	}

	/**
	 * Get the arc drawing rects
	 * 
	 * @param isCenterButton
	 * @return
	 */
	private RectF getRadialMenuRect(boolean isCenterButton) {
		int left, right, top, bottom;
		left = right = top = bottom = 0;
		switch (mOrientation) {
		case VERTICAL_RIGHT:
			if (isCenterButton) {
				left = getWidth() - (int) (mMenuRadius / menuToItemRatio);
				right = getWidth() + (int) (mMenuRadius / menuToItemRatio);
				top = (getHeight() / 2) - (int) (mMenuRadius / menuToItemRatio);
				bottom = (getHeight() / 2) + (int) (mMenuRadius / menuToItemRatio);
			} else {
				left = getWidth() - (int) mMenuRadius;
				right = getWidth() + (int) mMenuRadius;
				top = (getHeight() / 2) - (int) mMenuRadius;
				bottom = (getHeight() / 2) + (int) mMenuRadius;
			}
			mStartAngle = 90;
			mViewAnchorPoints = new Point(getWidth(), getHeight() / 2);
			break;
		case VERTICAL_LEFT:
			if (isCenterButton) {
				left = -(int) (mMenuRadius / menuToItemRatio);
				right = (int) (mMenuRadius / menuToItemRatio);
				top = (getHeight() / 2) - (int) (mMenuRadius / menuToItemRatio);
				bottom = (getHeight() / 2) + (int) (mMenuRadius / menuToItemRatio);
			} else {
				left = -(int) mMenuRadius;
				right = (int) mMenuRadius;
				top = (getHeight() / 2) - (int) mMenuRadius;
				bottom = (getHeight() / 2) + (int) mMenuRadius;
			}
			mStartAngle = 270;
			mViewAnchorPoints = new Point(0, getHeight() / 2);
			break;
		case HORIZONTAL_TOP:
			if (isCenterButton) {
				left = (getWidth() / 2) - (int) (mMenuRadius / menuToItemRatio);
				right = (getWidth() / 2) + (int) (mMenuRadius / menuToItemRatio);
				top = -(int) (mMenuRadius / menuToItemRatio);
				bottom = (int) (mMenuRadius / menuToItemRatio);
			} else {
				left = (getWidth() / 2) - (int) mMenuRadius;
				right = (getWidth() / 2) + (int) mMenuRadius;
				top = -(int) mMenuRadius;
				bottom = (int) mMenuRadius;
			}
			mStartAngle = 0;
			mViewAnchorPoints = new Point(getWidth() / 2, 0);
			break;
		case HORIZONTAL_BOTTOM:
			if (isCenterButton) {
				left = (getWidth() / 2) - (int) (mMenuRadius / menuToItemRatio);
				right = (getWidth() / 2) + (int) (mMenuRadius / menuToItemRatio);
				top = getHeight() - (int) (mMenuRadius / menuToItemRatio);
				bottom = getHeight() + (int) (mMenuRadius / menuToItemRatio);
			} else {
				left = (getWidth() / 2) - (int) mMenuRadius;
				right = (getWidth() / 2) + (int) mMenuRadius;
				top = getHeight() - (int) mMenuRadius;
				bottom = getHeight() + (int) mMenuRadius;
			}
			mStartAngle = 180;
			mViewAnchorPoints = new Point(getWidth() / 2, getHeight());
			break;
		}
		Rect rect = new Rect(left, top, right, bottom);
		Log.i(VIEW_LOG_TAG, " Top " + top + " Bottom " + bottom + " Left " + left + "  Right " + right);
		return new RectF(rect);
	}

	/********************************************************************************************
	 * Getter and setter methods
	 ********************************************************************************************/

	/**
	 * Set the orientation the semi-circular radial menu. There are four
	 * possible orientations only VERTICAL_RIGHT , VERTICAL_LEFT ,
	 * HORIZONTAL_TOP, HORIZONTAL_BOTTOM
	 * 
	 * @param orientation
	 */
	public void setOrientation(int orientation) {
		mOrientation = orientation;
		mMenuRect = getRadialMenuRect(false);
		mMenuCenterButtonRect = getRadialMenuRect(true);
		invalidate();
	}

	/**
	 * Add a menu item with it's identifier tag
	 * 
	 * @param idTag
	 *            - Menu item identifier id
	 * @param mMenuItem
	 *            - RadialMenuItem object
	 */
	public void addMenuItem(SemiCircularRadialMenuItem mMenuItem) {
		mMenuItems.put(mMenuItem.getMenuID(), mMenuItem);
		invalidate();
	}

	/**
	 * Remove a menu item with it's identifier tag
	 * 
	 * @param idTag
	 *            - Menu item identifier id
	 */
	public void removeMenuItemById(String idTag) {
		mMenuItems.remove(idTag);
		invalidate();
	}

	/**
	 * Remove a all menu items
	 */
	public void removeAllMenuItems() {
		mMenuItems.clear();
		invalidate();
	}

	/**
	 * Dismiss an open menu.
	 */
	public void dismissMenu() {
		isMenuVisible = false;
		centerMenuText = openMenuText;
		invalidate();
	}

	/**
	 * @return the mShadowRadius
	 */
	public float getShadowRadius() {
		return mShadowRadius;
	}

	/**
	 * @param mShadowRadius
	 *            the mShadowRadius to set
	 */
	public void setShadowRadius(int mShadowRadius) {
		this.mShadowRadius = mShadowRadius * getResources().getDisplayMetrics().density;
		invalidate();
	}

	/**
	 * @return the mOrientation
	 */
	public int getOrientation() {
		return mOrientation;
	}

	/**
	 * @return the mShadowColor
	 */
	public int getShadowColor() {
		return mShadowColor;
	}

	/**
	 * @param mShadowColor
	 *            the mShadowColor to set
	 */
	public void setShadowColor(int mShadowColor) {
		this.mShadowColor = mShadowColor;
		invalidate();
	}

	/**
	 * @return the openMenuText
	 */
	public String getOpenMenuText() {
		return openMenuText;
	}

	/**
	 * @param openMenuText
	 *            the openMenuText to set
	 */
	public void setOpenMenuText(String openMenuText) {
		this.openMenuText = openMenuText;
		if (!isMenuTogglePressed)
			centerMenuText = openMenuText;
		invalidate();
	}

	/**
	 * @return the closeMenuText
	 */
	public String getCloseMenuText() {
		return closeMenuText;
	}

	/**
	 * @param closeMenuText
	 *            the closeMenuText to set
	 */
	public void setCloseMenuText(String closeMenuText) {
		this.closeMenuText = closeMenuText;
		if (isMenuTogglePressed)
			centerMenuText = closeMenuText;
		invalidate();
	}

	public int getCenterToggleBackgroundColor() {
		return centerToggleBackgroundColor;
	}

	public void setCenterToggleBackgroundColor(int centerToggleBackgroundColor) {
		this.centerToggleBackgroundColor = centerToggleBackgroundColor;
	}

	public int getCenterBackgroundColor() {
		return centerBackgroundColor;
	}

	public void setCenterBackgroundColor(int centerBackgroundColor) {
		this.centerBackgroundColor = centerBackgroundColor;
		this.mCenterCurrentBackgroundColor = centerBackgroundColor;
	}

	/**
	 * @return the textSize
	 */
	public float getTextSize() {
		return textSize;
	}

	/**
	 * @param textSize
	 *            the textSize to set
	 */
	public void setTextSize(int textSize) {
		this.textSize = textSize * getResources().getDisplayMetrics().density;
		mMenuPaint.setTextSize(this.textSize);
		invalidate();
	}

	public float getMenuToItemRatio() {
		return menuToItemRatio;
	}

	public void setMenuToItemRatio(float menuToItemRatio) {
		this.menuToItemRatio = menuToItemRatio;
		invalidate();
	}

	public LinkedHashMap<String, SemiCircularRadialMenuItem> getmMenuItems() {
		return mMenuItems;
	}

	public void setmMenuItems(LinkedHashMap<String, SemiCircularRadialMenuItem> mMenuItems) {
		this.mMenuItems = mMenuItems;
	}

	public boolean isShowShadows() {
		return showShadows;
	}

	public void setShowShadows(boolean showShadows) {
		this.showShadows = showShadows;
	}

	public boolean isShowCenterText() {
		return showCenterText;
	}

	public void setShowCenterText(boolean showCenterText) {
		this.showCenterText = showCenterText;
	}

	public boolean isShowCenterIcon() {
		return showCenterIcon;
	}

	public void setShowCenterIcon(boolean showCenterIcon) {
		this.showCenterIcon = showCenterIcon;
	}

	public int getCenterTextColor() {
		return centerTextColor;
	}

	public void setCenterTextColor(int centerTextColor) {
		this.centerTextColor = centerTextColor;
		this.mCenterCurrentTextColor = centerTextColor;
	}

	public int getCenterToggleTextColor() {
		return centerToggleTextColor;
	}

	public void setCenterToggleTextColor(int centerToggleTextColor) {
		this.centerToggleTextColor = centerToggleTextColor;
	}

	public float getMenuScaleFactor() {
		return menuScaleFactor;
	}

	public void setMenuScaleFactor(float menuScaleFactor) {
		this.menuScaleFactor = menuScaleFactor;
	}

	public Drawable getCenterIcon() {
		return centerIcon;
	}

	public void setCenterIcon(Drawable centerIcon) {
		this.centerIcon = centerIcon;
	}

	public Paint getmMenuBorderPaint() {
		return mMenuBorderPaint;
	}

	public void setMenuBorderPaint(Paint mMenuBorderPaint) {
		this.mMenuBorderPaint = mMenuBorderPaint;
		this.mMenuBorderPaint.setStyle(Style.STROKE);
	}

}
