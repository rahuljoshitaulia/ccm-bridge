package com.taulia.test;

import com.datastax.driver.core.NettyOptions;
import io.netty.channel.EventLoopGroup;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.TimeUnit;

public class CCMTestUtils {

  public static int findAvailablePort(int startingWith) {
    IOException last = null;
    for (int port = startingWith; port < startingWith + 100; port++) {
      try {
        ServerSocket s = new ServerSocket(port);
        s.close();
        return port;
      } catch (IOException e) {
        last = e;
      }
    }
    // If for whatever reason a port could not be acquired throw the last encountered exception.
    throw new RuntimeException("Could not acquire an available port", last);
  }

  public static NettyOptions nonQuietClusterCloseOptions = new NettyOptions() {
    @Override
    public void onClusterClose(EventLoopGroup eventLoopGroup) {
      eventLoopGroup.shutdownGracefully(0, 15, TimeUnit.SECONDS).syncUninterruptibly();
    }
  };

}
