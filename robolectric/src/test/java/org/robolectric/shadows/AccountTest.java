package org.robolectric.shadows;

import android.accounts.Account;
import android.os.Parcel;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(TestRunners.WithDefaults.class)
public class AccountTest {

  @Test
  public void shouldHaveStringsConstructor() throws Exception {
    Account account = new Account("name", "type");

    assertThat(account.name).isEqualTo("name");
    assertThat(account.type).isEqualTo("type");
  }

  @Test
  public void shouldHaveParcelConstructor() throws Exception {
    Account expected = new Account("name", "type");
    Parcel p = Parcel.obtain();
    expected.writeToParcel(p, 0);
    p.setDataPosition(0);

    Account actual = new Account(p);
    assertThat(expected).isEqualTo(actual);
  }

  @Test
  public void shouldBeParcelable() throws Exception {
    Account expected = new Account("name", "type");
    Parcel p = Parcel.obtain();
    expected.writeToParcel(p, 0);
    p.setDataPosition(0);
    Account actual = Account.CREATOR.createFromParcel(p);
    assertThat(expected).isEqualTo(actual);
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
    assertThat(account.toString()).isEqualTo("Account {name=name, type=type}");
  }

  @Test
  public void shouldProvideEqualAndHashCode() throws Exception {
    assertThat(new Account("a", "b")).isEqualTo(new Account("a", "b"));
    assertThat(new Account("a", "b")).isNotEqualTo(new Account("c", "b"));
    assertThat(new Account("a", "b").hashCode()).isEqualTo(new Account("a", "b").hashCode());
    assertThat(new Account("a", "b").hashCode()).isNotEqualTo(new Account("c", "b").hashCode());
  }
}
