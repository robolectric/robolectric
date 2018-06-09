package org.robolectric.shadows.gms.common;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.gms.Shadows;

/**
 * Created by diegotori on 2/14/16.
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, shadows = {ShadowGoogleApiAvailability.class})
public class ShadowGoogleApiAvailabilityTest {

    private Context roboContext;

    @Before
    public void setUp() {
        roboContext = RuntimeEnvironment.application;
    }

    @After
    public void tearDown() {
        roboContext = null;
    }

    @Test
    public void getInstance() {
        //Given the expected GoogleApiAvailability instance
        final GoogleApiAvailability expected = GoogleApiAvailability.getInstance();

        //When getting the actual one from the shadow
        final GoogleApiAvailability actual = ShadowGoogleApiAvailability.getInstance();

    // Then verify that the expected is a not null and equal to the actual one
    assertThat(expected).isEqualTo(actual);
    }

    @Test
    public void shadowOf() {
        final ShadowGoogleApiAvailability shadowGoogleApiAvailability
                = Shadows.shadowOf(GoogleApiAvailability.getInstance());
        assertThat(shadowGoogleApiAvailability).isNotNull();
    }

    @Test
    public void setIsGooglePlayServicesAvailable() {
        //Given an expected and injected ConnectionResult code
        final ShadowGoogleApiAvailability shadowGoogleApiAvailability
                = Shadows.shadowOf(GoogleApiAvailability.getInstance());
        final int expectedCode = ConnectionResult.SUCCESS;
        shadowGoogleApiAvailability.setIsGooglePlayServicesAvailable(expectedCode);

        //When getting the actual ConnectionResult code
        final int actualCode = GoogleApiAvailability.getInstance()
                .isGooglePlayServicesAvailable(roboContext);

        //Then verify that we got back our expected code and not the default one.
        assertThat(actualCode)
                .isEqualTo(expectedCode);
    }

    @Test
    public void setIsUserResolvableError() {
        //Given an injected user resolvable error flag
        final ShadowGoogleApiAvailability shadowGoogleApiAvailability
                = Shadows.shadowOf(GoogleApiAvailability.getInstance());
        shadowGoogleApiAvailability.setIsUserResolvableError(true);

        //When getting the actual flag value
        final boolean actual = GoogleApiAvailability.getInstance()
                .isUserResolvableError(ConnectionResult.API_UNAVAILABLE);

        //Then verify that its equal to true
        assertThat(actual).isTrue();
    }

    @Test
    public void setOpenSourceSoftwareLicenseInfo() {
        //Given mock open source license info
        final String expected = "Mock open source license info";
        final ShadowGoogleApiAvailability shadowGoogleApiAvailability
                = Shadows.shadowOf(GoogleApiAvailability.getInstance());
        shadowGoogleApiAvailability.setOpenSourceSoftwareLicenseInfo(expected);

        //When getting the actual value
        final String actual = GoogleApiAvailability.getInstance()
                .getOpenSourceSoftwareLicenseInfo(roboContext);

        //Then verify that its not null, not empty, and equal to the expected value
        assertThat(actual)
                .isEqualTo(expected);
    }

    @Test
    public void setErrorDialog(){
        final ShadowGoogleApiAvailability shadowGoogleApiAvailability
                = Shadows.shadowOf(GoogleApiAvailability.getInstance());
        final Dialog expectedDialog = mock(Dialog.class);
        final Activity mockActivity = mock(Activity.class);
        final int mockErrorCode = ConnectionResult.API_UNAVAILABLE;
        final int mockRequestCode = 1234;
        shadowGoogleApiAvailability.setErrorDialog(expectedDialog);

        final Dialog actualDialog = GoogleApiAvailability.getInstance()
                .getErrorDialog(mockActivity, mockErrorCode, mockRequestCode);

        assertThat(actualDialog)
                .isEqualTo(expectedDialog);
    }

    @Test
    public void setErrorDialog__OnCancelListenerMethod(){
        final ShadowGoogleApiAvailability shadowGoogleApiAvailability
                = Shadows.shadowOf(GoogleApiAvailability.getInstance());
        final Dialog expectedDialog = mock(Dialog.class);
        final Activity mockActivity = mock(Activity.class);
        final DialogInterface.OnCancelListener mockOnCancelListener =
                mock(DialogInterface.OnCancelListener.class);
        final int mockErrorCode = ConnectionResult.API_UNAVAILABLE;
        final int mockRequestCode = 1234;
        shadowGoogleApiAvailability.setErrorDialog(expectedDialog);

        final Dialog actualDialog = GoogleApiAvailability.getInstance()
                .getErrorDialog(mockActivity, mockErrorCode, mockRequestCode, mockOnCancelListener);

        assertThat(actualDialog)
                .isEqualTo(expectedDialog);
    }
}
