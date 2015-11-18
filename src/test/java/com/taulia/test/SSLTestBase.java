package com.taulia.test;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.PlainTextAuthProvider;
import com.datastax.driver.core.SSLOptions;
import com.datastax.driver.core.Session;
import com.google.common.base.Optional;
import com.google.common.base.Stopwatch;
import org.junit.After;
import org.junit.Before;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.security.KeyStore;
import java.security.SecureRandom;

import static com.taulia.test.CCMBridge.DEFAULT_CLIENT_KEYSTORE_PASSWORD;
import static com.taulia.test.CCMBridge.DEFAULT_CLIENT_TRUSTSTORE_PASSWORD;


//TODO CLEAN OUT SYSTEM.OUT.PRINTLN STATEMENTS AND CHANGE TO LOGGER STATEMENTS
public abstract class SSLTestBase {

  private final boolean requireClientAuth;

  private static Stopwatch timer = Stopwatch.createUnstarted();

  protected CCMBridge ccm;

  protected Cluster cluster;
    protected Session session;

  public SSLTestBase(boolean requireClientAuth) {
    this.requireClientAuth = requireClientAuth;
  }



  @Before
  public  void beforeDse() {
    //DSE This seems to work with SSL security

    System.out.println("Building and starting DSE cluster .....");
    timer.start();
    ccm = CCMBridge.builder("test")
      .notStarted()
      .withSSL(requireClientAuth)
      .withCassandraConfiguration("authenticator", "PasswordAuthenticator")
      .withCassandraConfiguration("authorizer" , "AllowAllAuthorizer")
      .buildDse();
    ccm.setWorkload(1, "solr");
    ccm.start();
    ccm.waitForUp(1);
    timer.stop();
    System.out.println("Time Taken to Start DSE Cluster" + timer);

  }



  @After
  public void after() {

    if(cluster != null){
      cluster.close();
    }
    if(ccm != null){
      try{
        ccm.stop();
      }catch(Exception e){
        e.printStackTrace();
      }
      try{
        ccm.remove();
      }catch(Exception e){
        e.printStackTrace();
      }
    }
    clearSystemProperties();
  }


  protected void connectWithSSLOptions(SSLOptions sslOptions) throws Exception {

    //print ccm cluster status cluster status
    ccm.clusterStatus();

    cluster = Cluster.builder()
      .addContactPoint(CCMBridge.IP_PREFIX + '1')
      .withSSL(sslOptions)
      .withAuthProvider(new PlainTextAuthProvider("cassandra", "cassandra"))
        .build();

      session=  cluster.connect();

  }

  protected void connectWithSSL() throws Exception{

    System.setProperty("javax.net.ssl.keyStore", CCMBridge.DEFAULT_CLIENT_KEYSTORE_FILE.getAbsolutePath());
    System.setProperty("javax.net.ssl.keyStorePassword", CCMBridge.DEFAULT_CLIENT_KEYSTORE_PASSWORD);
    System.setProperty("javax.net.ssl.trustStore", CCMBridge.DEFAULT_CLIENT_TRUSTSTORE_FILE.getAbsolutePath());
    System.setProperty("javax.net.ssl.trustStorePassword", CCMBridge.DEFAULT_CLIENT_TRUSTSTORE_PASSWORD);


    cluster = Cluster.builder()
        .addContactPoint(CCMBridge.IP_PREFIX + '1')
        .withSSL()
        .withAuthProvider(new PlainTextAuthProvider("cassandra", "cassandra"))
        .build();

     session= cluster.connect();

  }


  protected void connectWithoutSSL() {
    cluster = Cluster.builder()
      .addContactPoint(CCMBridge.IP_PREFIX + '1')
      .build();

    session= cluster.connect();

  }



  protected void clearSystemProperties() {
    System.clearProperty("javax.net.ssl.keyStore");
    System.clearProperty("javax.net.ssl.keyStorePassword");
    System.clearProperty("javax.net.ssl.trustStore");
    System.clearProperty("javax.net.ssl.trustStorePassword");
  }


  public SSLOptions getSSLOptions(Optional<String> keyStorePath, Optional<String> trustStorePath) throws Exception {

    TrustManagerFactory tmf = null;
    if(trustStorePath.isPresent()) {
      KeyStore ks = KeyStore.getInstance("JKS");
      ks.load(this.getClass().getResourceAsStream(trustStorePath.get()), DEFAULT_CLIENT_TRUSTSTORE_PASSWORD.toCharArray());

      tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
      tmf.init(ks);
    }

    KeyManagerFactory kmf = null;
    if(keyStorePath.isPresent()) {
      KeyStore ks = KeyStore.getInstance("JKS");
      ks.load(this.getClass().getResourceAsStream(keyStorePath.get()), DEFAULT_CLIENT_KEYSTORE_PASSWORD.toCharArray());

      kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
      kmf.init(ks, DEFAULT_CLIENT_KEYSTORE_PASSWORD.toCharArray());
    }

    SSLContext sslContext = SSLContext.getInstance("TLS");
    sslContext.init(kmf != null ? kmf.getKeyManagers() : null, tmf != null ? tmf.getTrustManagers() : null, new SecureRandom());

    return new SSLOptions(sslContext, SSLOptions.DEFAULT_SSL_CIPHER_SUITES);
  }
}
