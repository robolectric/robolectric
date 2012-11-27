package com.xtremelabs.robolectric.shadows;

import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;

import static com.xtremelabs.robolectric.Robolectric.shadowOf_;

@Implements(Rect.class)
public class ShadowRect {
    @RealObject Rect realRect;

	private static final Pattern FLATTENED_PATTERN = Pattern.compile(
			"(-?\\d+) (-?\\d+) (-?\\d+) (-?\\d+)");

    public void __constructor__(int left, int top, int right, int bottom) {
        realRect.left = left;
        realRect.top = top;
        realRect.right = right;
        realRect.bottom = bottom;
    }

    public void __constructor__(Rect otherRect) {
        realRect.left = otherRect.left;
        realRect.top = otherRect.top;
        realRect.right = otherRect.right;
        realRect.bottom = otherRect.bottom;
    }

    @Implementation    
    public void set(Rect rect) {
        set(rect.left, rect.top, rect.right, rect.bottom);
    }
    
    @Implementation
    public void set(int left, int top, int right, int bottom) {
        realRect.left = left;
        realRect.top = top;
        realRect.right = right;
        realRect.bottom = bottom;
    }

    @Implementation
    public int width() {
        return realRect.right - realRect.left;
    }

    @Implementation
    public int height() {
        return realRect.bottom - realRect.top;
    }

    @Implementation
    public final int centerX() {
        return (realRect.left + realRect.right) >> 1;
    }

    @Implementation
    public final int centerY() {
        return (realRect.top + realRect.bottom) >> 1;
    }

    @Implementation
    public boolean equals(Object obj) {
        if (obj == null) return false;
        Object o = shadowOf_(obj);
        if (o == null) return false;
        if (getClass() != o.getClass()) return false;
        if (this == o) return true;

        Rect r = (Rect) obj;
        return realRect.left == r.left && realRect.top == r.top && realRect.right == r.right
                && realRect.bottom == r.bottom;
    }

    @Implementation
    public String toString() {
        StringBuilder sb = new StringBuilder(32);
        sb.append("Rect(");
        sb.append(realRect.left);
        sb.append(", ");
        sb.append(realRect.top);
        sb.append(" - ");
        sb.append(realRect.right);
        sb.append(", ");
        sb.append(realRect.bottom);
        sb.append(")");
        return sb.toString();
    }
    
    @Implementation
    public boolean contains(int x, int y) {
    	return x > realRect.left && x < realRect.right
    			&& y >= realRect.top && y <= realRect.bottom;
    } 

    @Implementation
    public boolean contains(Rect r) {
    	return equals(r)
    			|| (contains(r.left, r.top) && contains(r.right, r.top)
    					&& contains(r.left, r.bottom) && contains(r.right, r.bottom));
    }
    
    @Implementation
	public static boolean intersects(Rect a, Rect b) {
    	return a.left < b.right && b.left < a.right
    			&& a.top < b.bottom && b.top < a.bottom;
    }
    
    @Implementation
    public boolean intersect(Rect r) {
    	return intersects(realRect, r);
    }
    
    @Implementation
    public boolean intersect(int left, int top, int right, int bottom) {
    	return intersect(new Rect(left, top, right, bottom));
    }
    
    @Implementation
    public void offset(int dx, int dy) {
      realRect.left += dx;
      realRect.right += dx;
      realRect.top += dy;
      realRect.bottom += dy;
    }

	/**
	 * Return a string representation of the rectangle in a compact form.
	 * 
	 * @hide
	 */
	@Implementation
	public String toShortString(StringBuilder sb) {
		sb.setLength(0);
		sb.append('[');
		sb.append(realRect.left);
		sb.append(',');
		sb.append(realRect.top);
		sb.append("][");
		sb.append(realRect.right);
		sb.append(',');
		sb.append(realRect.bottom);
		sb.append(']');
		return sb.toString();
	}

	/**
	 * Return a string representation of the rectangle in a well-defined format.
	 * <p>
	 * You can later recover the Rect from this string through {@link #unflattenFromString(String)}.
	 * 
	 * @return Returns a new String of the form "realRect.left realRect.top realRect.right realRect.bottom"
	 */
	@Implementation
	public String flattenToString() {
		StringBuilder sb = new StringBuilder(32);
		// WARNING: Do not change the format of this string, it must be
		// preserved because Rects are saved in this flattened format.
		sb.append(realRect.left);
		sb.append(' ');
		sb.append(realRect.top);
		sb.append(' ');
		sb.append(realRect.right);
		sb.append(' ');
		sb.append(realRect.bottom);
		return sb.toString();
	}

	/**
	 * Returns a Rect from a string of the form returned by {@link #flattenToString}, or null if the string is not of
	 * that form.
	 */
	@Implementation
	public static Rect unflattenFromString(String str) {
		Matcher matcher = FLATTENED_PATTERN.matcher(str);
		if (!matcher.matches()) {
			return null;
		}
		return new Rect(
				Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)), Integer.parseInt(matcher
						.group(3)), Integer.parseInt(matcher.group(4)));
	}

	/**
	 * Print short representation to given writer.
	 * 
	 * @hide
	 */
	@Implementation
	public void printShortString(PrintWriter pw) {
		pw.print('[');
		pw.print(realRect.left);
		pw.print(',');
		pw.print(realRect.top);
		pw.print("][");
		pw.print(realRect.right);
		pw.print(',');
		pw.print(realRect.bottom);
		pw.print(']');
	}

	/**
	 * Returns true if the rectangle is empty (realRect.left >= realRect.right or.top >= realRect.bottom)
	 */
	@Implementation
	public final boolean isEmpty() {
		return realRect.left >= realRect.right || realRect.top >= realRect.bottom;
	}

	/**
	 * @return the exact horizontal center of the rectangle as a float.
	 */
	@Implementation
	public final float exactCenterX() {
		return (realRect.left + realRect.right) * 0.5f;
	}

	/**
	 * @return the exact vertical center of the rectangle as a float.
	 */
	@Implementation
	public final float exactCenterY() {
		return (realRect.top + realRect.bottom) * 0.5f;
	}

	/**
	 * Set the rectangle to (0,0,0,0)
	 */
	@Implementation
	public void setEmpty() {
		realRect.left = realRect.right = realRect.top = realRect.bottom = 0;
	}

	/**
	 * Offset the rectangle to a specific (realRect.left, realRect.top) position, keeping its width and height the same.
	 * 
	 * @param newLeft The new "realRect.left" coordinate for the rectangle
	 * @param newTop The new "realRect.top" coordinate for the rectangle
	 */
	@Implementation
	public void offsetTo(int newLeft, int newTop) {
		realRect.right += newLeft - realRect.left;
		realRect.bottom += newTop - realRect.top;
		realRect.left = newLeft;
		realRect.top = newTop;
	}

	/**
	 * Inset the rectangle by (dx,dy). If dx is positive, then the sides are moved inwards, making the rectangle
	 * narrower. If dx is negative, then the sides are moved outwards, making the rectangle wider. The same holds true
	 * for dy and the realRect.top and realRect.bottom.
	 * 
	 * @param dx The amount to add(subtract) from the rectangle's realRect.left(realRect.right)
	 * @param dy The amount to add(subtract) from the rectangle's realRect.top(realRect.bottom)
	 */
	@Implementation
	public void inset(int dx, int dy) {
		realRect.left += dx;
		realRect.top += dy;
		realRect.right -= dx;
		realRect.bottom -= dy;
	}

	/**
	 * Returns true iff the 4 specified sides of a rectangle are inside or equal to this rectangle. i.e. is this
	 * rectangle a superset of the specified rectangle. An empty rectangle never contains another rectangle.
	 * 
	 * @param realRect.left The realRect.left side of the rectangle being tested for containment
	 * @param realRect.top The realRect.top of the rectangle being tested for containment
	 * @param realRect.right The realRect.right side of the rectangle being tested for containment
	 * @param realRect.bottom The realRect.bottom of the rectangle being tested for containment
	 * @return true iff the the 4 specified sides of a rectangle are inside or equal to this rectangle
	 */
	@Implementation
	public boolean contains(int left, int top, int right, int bottom) {
		// check for empty first
		return this.realRect.left < this.realRect.right && this.realRect.top < this.realRect.bottom
				// now check for containment
				&& this.realRect.left <= realRect.left && this.realRect.top <= realRect.top
				&& this.realRect.right >= realRect.right && this.realRect.bottom >= realRect.bottom;
	}

	/**
	 * If rectangles a and b intersect, return true and set this rectangle to that intersection, otherwise return false
	 * and do not change this rectangle. No check is performed to see if either rectangle is empty. To just test for
	 * intersection, use intersects()
	 * 
	 * @param a The first rectangle being intersected with
	 * @param b The second rectangle being intersected with
	 * @return true iff the two specified rectangles intersect. If they do, set this rectangle to that intersection. If
	 *         they do not, return false and do not change this rectangle.
	 */
	@Implementation
	public boolean setIntersect(Rect a, Rect b) {
		if (a.left < b.right && b.left < a.right && a.top < b.bottom && b.top < a.bottom) {
			realRect.left = Math.max(a.left, b.left);
			realRect.top = Math.max(a.top, b.top);
			realRect.right = Math.min(a.right, b.right);
			realRect.bottom = Math.min(a.bottom, b.bottom);
			return true;
		}
		return false;
	}

	/**
	 * Returns true if this rectangle intersects the specified rectangle. In no event is this rectangle modified. No
	 * check is performed to see if either rectangle is empty. To record the intersection, use intersect() or
	 * setIntersect().
	 * 
	 * @param realRect.left The realRect.left side of the rectangle being tested for intersection
	 * @param realRect.top The realRect.top of the rectangle being tested for intersection
	 * @param realRect.right The realRect.right side of the rectangle being tested for intersection
	 * @param realRect.bottom The realRect.bottom of the rectangle being tested for intersection
	 * @return true iff the specified rectangle intersects this rectangle. In no event is this rectangle modified.
	 */
	@Implementation
	public boolean intersects(int left, int top, int right, int bottom) {
		return this.realRect.left < realRect.right && realRect.left < this.realRect.right
				&& this.realRect.top < realRect.bottom && realRect.top < this.realRect.bottom;
	}

	/**
	 * Update this Rect to enclose itself and the specified rectangle. If the specified rectangle is empty, nothing is
	 * done. If this rectangle is empty it is set to the specified rectangle.
	 * 
	 * @param realRect.left The realRect.left edge being unioned with this rectangle
	 * @param realRect.top The realRect.top edge being unioned with this rectangle
	 * @param realRect.right The realRect.right edge being unioned with this rectangle
	 * @param realRect.bottom The realRect.bottom edge being unioned with this rectangle
	 */
	@Implementation
	public void union(int left, int top, int right, int bottom) {
		if ((realRect.left < realRect.right) && (realRect.top < realRect.bottom)) {
			if ((this.realRect.left < this.realRect.right) && (this.realRect.top < this.realRect.bottom)) {
				if (this.realRect.left > realRect.left) {
					this.realRect.left = realRect.left;
				}
				if (this.realRect.top > realRect.top) {
					this.realRect.top = realRect.top;
				}
				if (this.realRect.right < realRect.right) {
					this.realRect.right = realRect.right;
				}
				if (this.realRect.bottom < realRect.bottom) {
					this.realRect.bottom = realRect.bottom;
				}
			} else {
				this.realRect.left = realRect.left;
				this.realRect.top = realRect.top;
				this.realRect.right = realRect.right;
				this.realRect.bottom = realRect.bottom;
			}
		}
	}

	/**
	 * Update this Rect to enclose itself and the specified rectangle. If the specified rectangle is empty, nothing is
	 * done. If this rectangle is empty it is set to the specified rectangle.
	 * 
	 * @param r The rectangle being unioned with this rectangle
	 */
	public void union(Rect r) {
		union(r.left, r.top, r.right, r.bottom);
	}

	/**
	 * Update this Rect to enclose itself and the [x,y] coordinate. There is no check to see that this rectangle is
	 * non-empty.
	 * 
	 * @param x The x coordinate of the point to add to the rectangle
	 * @param y The y coordinate of the point to add to the rectangle
	 */
	public void union(int x, int y) {
		if (x < realRect.left) {
			realRect.left = x;
		} else if (x > realRect.right) {
			realRect.right = x;
		}
		if (y < realRect.top) {
			realRect.top = y;
		} else if (y > realRect.bottom) {
			realRect.bottom = y;
		}
	}

	/**
	 * Swap realRect.top/realRect.bottom or.left/realRect.right if there are flipped (i.e. realRect.left >
	 * realRect.right and/or.top > realRect.bottom). This can be called if the edges are computed separately, and may
	 * have crossed over each other. If the edges are already correct (i.e. realRect.left <= realRect.right and
	 * realRect.top <= realRect.bottom) then nothing is done.
	 */
	public void sort() {
		if (realRect.left > realRect.right) {
			int temp = realRect.left;
			realRect.left = realRect.right;
			realRect.right = temp;
		}
		if (realRect.top > realRect.bottom) {
			int temp = realRect.top;
			realRect.top = realRect.bottom;
			realRect.bottom = temp;
		}
	}

	/**
	 * Parcelable interface methods
	 */
	public int describeContents() {
		return 0;
	}

	/**
	 * Write this rectangle to the specified parcel. To restore a rectangle from a parcel, use readFromParcel()
	 * 
	 * @param out The parcel to write the rectangle's coordinates into
	 */
	public void writeToParcel(Parcel out, int flags) {
		out.writeInt(realRect.left);
		out.writeInt(realRect.top);
		out.writeInt(realRect.right);
		out.writeInt(realRect.bottom);
	}

	public static final Parcelable.Creator<Rect> CREATOR = new Parcelable.Creator<Rect>() {
		/**
		 * Return a new rectangle from the data in the specified parcel.
		 */
		@Override
		public Rect createFromParcel(Parcel in) {
			Rect r = new Rect();
			r.readFromParcel(in);
			return r;
		}

		/**
		 * Return an array of rectangles of the specified size.
		 */
		@Override
		public Rect[] newArray(int size) {
			return new Rect[size];
		}
	};

	/**
	 * Set the rectangle's coordinates from the data stored in the specified parcel. To write a rectangle to a parcel,
	 * call writeToParcel().
	 * 
	 * @param in The parcel to read the rectangle's coordinates from
	 */
	public void readFromParcel(Parcel in) {
		realRect.left = in.readInt();
		realRect.top = in.readInt();
		realRect.right = in.readInt();
		realRect.bottom = in.readInt();
	}

	/**
	 * Scales up the rect by the given scale.
	 * 
	 * @hide
	 */
	public void scale(float scale) {
		if (scale != 1.0f) {
			realRect.left = (int) (realRect.left * scale + 0.5f);
			realRect.top = (int) (realRect.top * scale + 0.5f);
			realRect.right = (int) (realRect.right * scale + 0.5f);
			realRect.bottom = (int) (realRect.bottom * scale + 0.5f);
		}
	}
}
