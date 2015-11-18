package com.taulia.test;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.SimpleStatement;
import com.google.common.base.Optional;
import com.google.common.io.CharStreams;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HTTP;
import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Base64;


//TODO CLEAN OUT SYSTEM.OUT.PRINTLN STATEMENTS AND CHANGE TO LOGGER STATEMENTS
public class SSLEncryptionTest extends SSLTestBase {

  public static final String SIMPLE_KEYSPACE = "ks";
  public static final String CREATE_KEYSPACE_SIMPLE_FORMATTED_STRING =
    String.format("CREATE KEYSPACE %s WITH replication = { 'class' : 'NetworkTopologyStrategy', 'Solr' : %d }", SIMPLE_KEYSPACE, 1);

  private static final String TEST_TABLE_NAME = "test";
  private static final String TEST_TABLE_KEY_COLUMN = "k";
  private static final String TEST_TABLE_VALUE1_COLUMN = "v1";
  private static final String TEST_TABLE_CREATE_FORMATTED_STRING  =
    String.format("CREATE TABLE %s (%s text, %s int, PRIMARY KEY (%s, %s))", TEST_TABLE_NAME, TEST_TABLE_KEY_COLUMN, TEST_TABLE_VALUE1_COLUMN, TEST_TABLE_KEY_COLUMN, TEST_TABLE_VALUE1_COLUMN );

  private static final String TEST_KEY_INSERT_VALUE = "page";

  private static final String TEST_TABLE_INSERT_FORMAT = "INSERT INTO test (%s, %s) VALUES ('%s', %d)";

  private static final String SOLR_CORE_REST_CALL_FORMATTED_STRING =
    String.format("http://127.0.1.1:8983/solr/admin/cores?action=CREATE&name=%s.%s&reindex=true", SIMPLE_KEYSPACE, TEST_TABLE_NAME);

  private static final String SOLR_CONFIG_UPLOAD_FORMATTED_STRING =
    String.format("http://127.0.1.1:8983/solr/resource/%s.%s/solrconfig.xml", SIMPLE_KEYSPACE, TEST_TABLE_NAME);

  private static final String SOLR_CONFIG_GET_STRING = "http://127.0.1.1:8983/solr/resource/solrconfig.xml";


  private static final String SOLR_SCHEMA_UPLOAD_FORMATTED_STRING =
    String.format("http://127.0.1.1:8983/solr/resource/%s.%s/schema.xml", SIMPLE_KEYSPACE, TEST_TABLE_NAME);


  private static final String DSE_AUTH_USER = "cassandra";
  private static final String DSE_AUTH_PASSWORD = "cassandra";

  private static final String AUTH_HEADER = "Authorization";
  private static final String AUTH_HEADER_VALUE_FORMAT = "Basic %s";


  public SSLEncryptionTest() {
    super(false);
  }



  @Test
  public void should_connect_with_ssl() throws Exception {

    connectWithSSLOptions(getSSLOptions(Optional.<String>absent(), Optional.of(CCMBridge.DEFAULT_CLIENT_TRUSTSTORE_PATH)));
    buildKeySpace();
    useKeySpace();
    createTable();
    insertTestData();
    uploadSolrConfigXml();
    uploadSolrSchemaXml();
    createSolrCore();

    ccm.flush();//flusing to make sure that the index is committed to.
    testCQLQuery();
    testCQLSolrQuery();

  }



  private void buildKeySpace() throws Exception{
    session.execute(CREATE_KEYSPACE_SIMPLE_FORMATTED_STRING);
  }

  private void useKeySpace() throws Exception{
    session.execute("USE " + SIMPLE_KEYSPACE);
  }

  private void createTable()throws Exception {
    session.execute(TEST_TABLE_CREATE_FORMATTED_STRING);
  }

  private void insertTestData() throws Exception {
    System.out.println("Inserting Data Into cassandra table");
    for (int i = 0; i < 100; i++){
      session.execute(String.format(TEST_TABLE_INSERT_FORMAT, TEST_TABLE_KEY_COLUMN, TEST_TABLE_VALUE1_COLUMN,  TEST_KEY_INSERT_VALUE, i));
    }

  }



  private void uploadSolrConfigXml() throws IOException{

    String url = SOLR_CONFIG_UPLOAD_FORMATTED_STRING;
    HttpPost request = new HttpPost(url);
    HttpClient httpClient = HttpClientBuilder.create().build();

    String encoding = Base64.getEncoder().encodeToString(new String(DSE_AUTH_USER + ":" + DSE_AUTH_PASSWORD).getBytes());
    request.setHeader(AUTH_HEADER, String.format(AUTH_HEADER_VALUE_FORMAT, encoding));
    request.setHeader("Content-type", "text/xml; charset=utf-8");
    InputStream configXmlIs =  SSLEncryptionTest.class.getResourceAsStream("/solr/solrconfig.xml");
    String xmlString = CharStreams.toString(new InputStreamReader(configXmlIs, "UTF-8"));
    StringEntity stringEntity = new StringEntity(xmlString);
    request.setEntity(stringEntity);

    System.out.println("Making Rest call to upload Solr Config xml");
    try{
      // add request header
      request.addHeader("User-Agent", HTTP.USER_AGENT);
      HttpResponse response = httpClient.execute(request);
      int response_code = response.getStatusLine().getStatusCode();
      handleRestError(response_code, response);
    }
    finally{
      request.releaseConnection();
    }

  }

  private void uploadSolrSchemaXml() throws IOException{


    String url = SOLR_SCHEMA_UPLOAD_FORMATTED_STRING;
    HttpPost request = new HttpPost(url);
    HttpClient httpClient = HttpClientBuilder.create().build();

    String encoding = Base64.getEncoder().encodeToString(new String(DSE_AUTH_USER + ":" + DSE_AUTH_PASSWORD).getBytes());
    request.setHeader(AUTH_HEADER, String.format(AUTH_HEADER_VALUE_FORMAT, encoding));
    request.setHeader("Content-type", "text/xml; charset=utf-8");
    InputStream configXmlIs =  SSLEncryptionTest.class.getResourceAsStream("/solr/schema.xml");
    String xmlString = CharStreams.toString(new InputStreamReader(configXmlIs, "UTF-8"));
    StringEntity stringEntity = new StringEntity(xmlString);
    request.setEntity(stringEntity);

    System.out.println("Making Rest call to create Solr schema");
    try{
      // add request header
      request.addHeader("User-Agent", HTTP.USER_AGENT);
      HttpResponse response = httpClient.execute(request);
      int response_code = response.getStatusLine().getStatusCode();
      handleRestError(response_code, response);
    }
    finally{
      request.releaseConnection();
    }

  }

  private void createSolrCore() throws IOException {

    String url = SOLR_CORE_REST_CALL_FORMATTED_STRING;
    HttpGet request = new HttpGet(url);
    HttpClient httpClient = HttpClientBuilder.create().build();

    String encoding = Base64.getEncoder().encodeToString(new String(DSE_AUTH_USER+":"+DSE_AUTH_PASSWORD).getBytes());
    request.setHeader(AUTH_HEADER, String.format(AUTH_HEADER_VALUE_FORMAT, encoding));

    System.out.println("Making Rest call to create solr core from uploaded resources");
    try{
      // add request header
      request.addHeader("User-Agent", HTTP.USER_AGENT);
      HttpResponse response = httpClient.execute(request);
      int response_code = response.getStatusLine().getStatusCode();
      handleRestError(response_code, response);
    }
    finally{
      request.releaseConnection();
    }

  }


  private void handleRestError(int response_code, HttpResponse response ) throws IOException {

    if(response_code != HttpStatus.SC_OK){
      System.out.println("Response Code : "
        + response.getStatusLine().getStatusCode());
      BufferedReader rd = new BufferedReader(
        new InputStreamReader(response.getEntity().getContent()));

      StringBuffer result = new StringBuffer();
      String line = "";
      while ((line = rd.readLine()) != null) {
        result.append(line);
      }

      System.out.println("Solr Response Body:"+result);
      throw new RuntimeException(String.format("Solr Response Code %d:  with body %s", response_code, result));
    }

  }


  private void testCQLQuery(){

    SimpleStatement st = new SimpleStatement(String.format("SELECT %s FROM %s WHERE %s='%s'", TEST_TABLE_VALUE1_COLUMN, TEST_TABLE_NAME, TEST_TABLE_KEY_COLUMN, TEST_KEY_INSERT_VALUE));
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

  }



  private void testCQLSolrQuery() throws Exception{
   String jsonQuery =  "{\"q\":\"k:page\", \"commit\":true, \"sort\":\"v1 asc\"}"; //makes sure results are sorted
    String cqlSolrQuery = String.format("SELECT %s FROM %s WHERE solr_query='%s'",TEST_TABLE_VALUE1_COLUMN, TEST_TABLE_NAME, jsonQuery );
    SimpleStatement solrStatement = new SimpleStatement(cqlSolrQuery);
    System.out.println("Executing Solr query:"+solrStatement.getQueryString());
    ResultSet solrRs = session.execute(solrStatement);

    //Default fetch size is 10 for solr queries
    for (int i = 0; i < 10; i++) {
      int value = solrRs.one().getInt(0);
      //System.out.println("Value:"+value);
      Assert.assertEquals(value, i);
    }
    //Default fetch size is 10 for solr queries
    Assert.assertTrue(solrRs.isExhausted());

  }




}