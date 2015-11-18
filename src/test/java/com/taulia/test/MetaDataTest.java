package com.taulia.test;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ProtocolVersion;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.SimpleStatement;
import com.datastax.driver.core.VersionNumber;
import com.datastax.driver.core.exceptions.UnsupportedFeatureException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by rahul.joshi on 11/9/15.
 */
public class MetaDataTest{

  protected static String TEST_DSE_CLUSTER_NAME = "perclass-dse-cluster";
  protected final VersionNumber cassandraVersion = VersionNumber.parse(System.getProperty("dse.version"));

  private static final Logger logger = LoggerFactory.getLogger(MetaDataTest.class);

  private CCMBridge ccmBridge;
  private Session session;
  private Cluster cluster;

/*

  @Before
  public void createDseCluster(){

    System.out.println("maybeInitCluster: Started");
    try {


        logger.info("Initializing CCM DSE Cluster....");

        ccmBridge = CCMBridge.builder(TEST_DSE_CLUSTER_NAME)
          .withCassandraVersion("4.8.0").withNodes(1).withSSL(false)
          .notStarted()
          .buildDse();
        ccmBridge.start();
        logger.info("Started");
    }catch (Exception e ){
      throw new RuntimeException(e);
    }

  }

  private void createDataStaxCluster(){

  }


  @After
  public void cleanDseCluser(){

    if(cluster != null){
      cluster.close();
      session.close();
    }

    logger.debug("Terminate CCM DSE Cluster ..");
    try {
      if (ccmBridge == null) {
        logger.error("No DSE cluster to discard");
      } else {
        ccmBridge.remove();
        ccmBridge.getCcmDir().delete();
      }
    }catch(Exception e) {
      logger.error("Could not terminate DSE cluster!!!");
    }


  }


  @Test
  public void simplePagingTest() throws Throwable {

    try {

      // Insert data
      String key = "paging_test";
      for (int i = 0; i < 100; i++)
        session.execute(String.format("INSERT INTO test (k, v) VALUES ('%s', %d)", key, i));

      SimpleStatement st = new SimpleStatement(String.format("SELECT v FROM test WHERE k='%s'", key));
      st.setFetchSize(5); // Ridiculously small fetch size for testing purpose. Don't do at home.
      ResultSet rs = session.execute(st);

      Assert.assertFalse(rs.isFullyFetched());

      for (int i = 0; i < 100; i++) {
        // isExhausted makes sure we do fetch if needed
        Assert.assertFalse(rs.isExhausted());
        Assert.assertEquals(rs.getAvailableWithoutFetching(), 5 - (i % 5));
        Assert.assertEquals(rs.one().getInt(0), i);
      }

      Assert.assertTrue(rs.isExhausted());
      Assert.assertTrue(rs.isFullyFetched());

    } catch (UnsupportedFeatureException e) {
      // This is expected when testing the protocol v1
      if (cluster.getConfiguration().getProtocolOptions().getProtocolVersionEnum() != ProtocolVersion.V1)
        throw e;
    } catch (Throwable e) {
      throw e;
    }
  }
  */
}
