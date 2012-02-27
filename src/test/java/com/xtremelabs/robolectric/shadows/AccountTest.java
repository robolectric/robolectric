package com.xtremelabs.robolectric.shadows;

import android.accounts.Account;
import android.os.Parcel;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

@RunWith(WithTestDefaultsRunner.class)
public class AccountTest {

    @Test
    public void shouldHaveStringsConstructor() throws Exception {
        Account account = new Account("name", "type");

        assertThat(account.name, equalTo("name"));
        assertThat(account.type, equalTo("type"));
    }

    @Test
    public void shouldHaveParcelConstructor() throws Exception {
        Parcel p = Parcel.obtain();
        p.writeString("name");
        p.writeString("type");

        Account account = new Account(p);
        assertThat(account.name, equalTo("name"));
        assertThat(account.type, equalTo("type"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIfNameIsEmpty() throws Exception {
        new Account("", "type");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIfTypeIsEmpty() throws Exception {
        new Account("name", "");
    }

    @Test
    public void shouldHaveToString() throws Exception {
        Account account = new Account("name", "type");
        assertThat(account.toString(), equalTo("Account {name=name, type=type}"));
    }

    @Test
    public void shouldProvideEqualAndHashCode() throws Exception {
        assertThat(new Account("a", "b"), equalTo(new Account("a", "b")));
        assertThat(new Account("a", "b"), not(equalTo(new Account("c", "b"))));
        assertThat(new Account("a", "b").hashCode(), equalTo(new Account("a", "b").hashCode()));
        assertThat(new Account("a", "b").hashCode(), not(equalTo(new Account("c", "b").hashCode())));
    }
}
