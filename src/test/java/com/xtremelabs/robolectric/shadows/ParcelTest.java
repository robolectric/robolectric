package com.xtremelabs.robolectric.shadows;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.os.Parcel;

import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;

@RunWith(WithTestDefaultsRunner.class)
public class ParcelTest {

	private Parcel parcel;
	private ShadowParcel shadowParcel;

	@Before
	public void setup() {
		parcel = Parcel.obtain();
		shadowParcel = Robolectric.shadowOf( parcel );		
	}
	
	@Test
	public void testObtain() {
		assertThat( parcel, notNullValue() );
		assertThat( shadowParcel.getIndex(), equalTo( 0 ) );
		assertThat( shadowParcel.getParcelData().size(), equalTo( 0 ) );
	}
	
	@Test
	public void testReadIntWhenEmpty() {
		assertThat( parcel.readInt(), equalTo( 0 ) );
	}
	
	@Test
	public void testReadStringWhenEmpty() {
		assertThat( parcel.readString(), nullValue() );		
	}
	
	@Test
	public void testReadWriteSingleString() {
		String val = "test";
		parcel.writeString( val );
		assertThat( parcel.readString(), equalTo( val ) );
	}
	
	@Test
	public void testWriteNullString() {
		parcel.writeString( null );
		assertThat( parcel.readString(), nullValue() );
		assertThat( shadowParcel.getIndex(), equalTo( 0 ) );
		assertThat( shadowParcel.getParcelData().size(), equalTo( 0 ) );
	}
	
	@Test
	public void testReadWriteMultipleStrings() {
		for( int i = 0; i < 10; ++i ) {
			parcel.writeString( Integer.toString( i ) );
		}
		for( int i = 0; i < 10; ++i ) {
			assertThat( parcel.readString(), equalTo( Integer.toString( i ) ) );
		}		
		// now try to read past the number of items written and see what happens
		assertThat( parcel.readString(), nullValue() );
	}
	
	@Test
	public void testReadWriteSingleInt() {
		int val = 5;
		parcel.writeInt( val );
		assertThat( parcel.readInt(), equalTo( val ) );
	}
	
	@Test
	public void testReadWriteMultipleInts() {
		for( int i = 0; i < 10; ++i ) {
			parcel.writeInt( i );
		}
		for( int i = 0; i < 10; ++i ) {
			assertThat( parcel.readInt(), equalTo( i ) );
		}		
		// now try to read past the number of items written and see what happens
		assertThat( parcel.readInt(), equalTo( 0 ) );
	}
	
	@Test
	public void testReadWriteStringInt() {
		for( int i = 0; i < 10; ++i ) {
			parcel.writeString( Integer.toString( i ) );
			parcel.writeInt( i );
		}
		for( int i = 0; i < 10; ++i ) {
			assertThat( parcel.readString(), equalTo( Integer.toString( i ) ) );
			assertThat( parcel.readInt(), equalTo( i ) );
		}		
		// now try to read past the number of items written and see what happens
		assertThat( parcel.readString(), nullValue() );
		assertThat( parcel.readInt(), equalTo( 0 ) );
	}
	
	@Test( expected = ClassCastException.class )
	public void testWriteStringReadInt() {
		String val = "test";
		parcel.writeString( val );
		parcel.readInt();
	}
	
	@Test( expected = ClassCastException.class )
	public void testWriteIntReadString() {
		int val = 9;
		parcel.writeInt( val );
		parcel.readString();
	}
}
