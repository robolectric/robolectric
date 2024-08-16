package org.robolectric.shadows;

import android.os.Build.VERSION_CODES;
import android.os.IBinder;
import android.se.omapi.ISecureElementChannel;
import android.se.omapi.ISecureElementListener;
import android.se.omapi.ISecureElementReader;
import android.se.omapi.ISecureElementService;
import android.se.omapi.ISecureElementSession;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;

/** Shadow of {@link SecureElementService} */
@Implements(
    value = ISecureElementService.Stub.class,
    minSdk = VERSION_CODES.P,
    isInAndroidSdk = false)
public class ShadowSecureElementService {

  private static final List<MockApplet> mockApplets = new ArrayList<>(0);

  private ShadowSecureElementService() {}

  @Implementation
  protected static ISecureElementService asInterface(IBinder binder) {
    return new SecureElementService();
  }

  @Resetter
  public static void reset() {
    mockApplets.clear();
  }

  /**
   * Add a Reader with the specified name. Creates a reader with the specified name and returns the
   * shadow of that reader which may be used to further configure the expected behavior by adding
   * fake Applets.
   */
  public static void addMockApplet(MockApplet applet) {
    if (getMockApplet(applet.readerName, applet.aid) != null) {
      throw new IllegalStateException("Applet with that reader name and aid already exists");
    }
    mockApplets.add(applet);
  }

  private static MockApplet getMockApplet(String readerName, byte[] aid) {
    for (MockApplet applet : mockApplets) {
      if (applet.readerName.equals(readerName) && Arrays.equals(aid, applet.aid)) {
        return applet;
      }
    }
    return null;
  }

  /** Provide fake SE applet behavior */
  public static class MockApplet {
    private final byte[] aid;
    private final String readerName;
    private byte[] selectResponse = {(byte) 0x90, (byte) 0x00};
    private final List<byte[]> adpuRequests = new ArrayList<>(0);
    private final List<byte[]> adpuResponses = new ArrayList<>(0);

    public MockApplet(String readerName, byte[] aid) {
      this.readerName = readerName;
      this.aid = aid;
    }

    public void addApduResponse(byte[] apduResponse) {
      adpuResponses.add(apduResponse);
    }

    public void setSelectResponse(byte[] selectResponse) {
      this.selectResponse = selectResponse;
    }

    public byte[] getSelectResponse() {
      return selectResponse;
    }

    public List<byte[]> getAdpuRequests() {
      return new ArrayList<>(adpuRequests);
    }

    public byte[] processRequestApdu(byte[] request) {
      adpuRequests.add(request);
      if (adpuResponses.isEmpty()) {
        return null;
      }
      return adpuResponses.remove(0);
    }
  }

  private static class SecureElementService extends ISecureElementService.Default {

    @Override
    public String[] getReaders() {
      String[] readerNames = new String[mockApplets.size()];
      for (int i = 0; i < mockApplets.size(); i++) {
        readerNames[i] = mockApplets.get(i).readerName;
      }
      return readerNames;
    }

    @Override
    public ISecureElementReader getReader(String reader) {
      return new SecureElementReader(reader);
    }

    @Override
    public boolean[] isNfcEventAllowed(
        String reader, byte[] aid, String[] packageNames, int userId) {
      throw new RuntimeException("UNIMPLEMENTED");
    }
  }

  private static class SecureElementReader extends ISecureElementReader.Default {

    private final String name;
    private final List<SecureElementSession> sessions = new ArrayList<>(0);

    SecureElementReader(String name) {
      this.name = name;
    }

    @Override
    public boolean isSecureElementPresent() {
      return true;
    }

    @Override
    public ISecureElementSession openSession() {
      SecureElementSession session = new SecureElementSession(this);
      sessions.add(session);
      return session;
    }

    @Override
    public void closeSessions() {
      for (SecureElementSession session : sessions) {
        session.close();
      }
    }

    @Override
    public boolean reset() {
      closeSessions();
      return true;
    }

    private MockApplet getMockApplet(byte[] aid) {
      return ShadowSecureElementService.getMockApplet(name, aid);
    }
  }

  private static class SecureElementSession extends ISecureElementSession.Default {

    private final SecureElementReader reader;
    private final List<SecureElementChannel> channels = new ArrayList<>(0);
    private boolean isClosed;

    SecureElementSession(SecureElementReader reader) {
      this.reader = reader;
    }

    @Override
    public byte[] getAtr() {
      throw new RuntimeException();
    }

    @Override
    public void close() {
      isClosed = true;
    }

    @Override
    public void closeChannels() {
      for (SecureElementChannel channel : channels) {
        channel.close();
      }
    }

    @Override
    public boolean isClosed() {
      return isClosed;
    }

    @Override
    public ISecureElementChannel openBasicChannel(
        byte[] aid, byte p2, ISecureElementListener listener) {
      return openChannel(aid, p2, listener, true);
    }

    @Override
    public ISecureElementChannel openLogicalChannel(
        byte[] aid, byte p2, ISecureElementListener listener) {
      return openChannel(aid, p2, listener, false);
    }

    private ISecureElementChannel openChannel(
        byte[] aid, byte p2, ISecureElementListener listener, boolean isBasicChannel) {
      MockApplet mockApplet = reader.getMockApplet(aid);
      if (mockApplet == null) {
        return null;
      }
      SecureElementChannel channel = new SecureElementChannel(mockApplet, isBasicChannel);
      channels.add(channel);
      return channel;
    }
  }

  private static class SecureElementChannel extends ISecureElementChannel.Default {

    private final MockApplet mockApplet;
    private final boolean isBasicChannel;
    private boolean isClosed;

    public SecureElementChannel(MockApplet mockApplet, boolean isBasicChannel) {
      this.mockApplet = mockApplet;
      this.isBasicChannel = isBasicChannel;
    }

    @Override
    public void close() {
      isClosed = true;
      throw new RuntimeException();
    }

    @Override
    public boolean isClosed() {
      return isClosed;
    }

    @Override
    public boolean isBasicChannel() {
      return isBasicChannel;
    }

    @Override
    public byte[] getSelectResponse() {
      return mockApplet.getSelectResponse();
    }

    @Override
    public byte[] transmit(byte[] command) {
      return mockApplet.processRequestApdu(command);
    }

    @Override
    public boolean selectNext() {
      throw new RuntimeException("UNIMPLEMENTED");
    }
  }
}
