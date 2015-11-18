# CCMBridge POC (Proof of concept)

This is a test Program for using the CCM tool to start and stop a DSE cluster with solr search functionality. ````In order to use this effectively we should plan on extracting some of this code into the gradle plugins library. Also we need to install a CCM cluster tool on jenkins.````

## Usage
1. Install ccm tool on dev machine. Installation instructions can be found at ```https://github.com/pcmanus/ccm```
2. Dont forget to setup loopbacks on macosx  ```https://github.com/pcmanus/ccm```
3. run ccm command to install 4.8.0 dse repository locally (This may automatically start a cluster. Stop and delete it if thats the case).
 ```ccm create dse_cluster --dse --dse-username=user@example.com --dse-password=redacted -v 4.8.0 ```
4. In the root of this repo run ```gradle clean build```

## CCMBridge.java: 
This is a Modified CCMBridge.java from datastax-java-driver test repo.
`https://github.com/datastax/java-driver/blob/2.1/driver-core/src/test/java/com/datastax/driver/core/CCMBridge.java.`

## SLTestBase
This class does the following

1. Starts the DSE cluster via CCM bridge `@Before`
2. Cluster is configured to start with SSLoptions and client authentication. Certs are in the resources directory
3. Cleans out cluster and stops cluster `@After`

## SSLEncryptionTest
Does the following in order

1. connects to cluster with SSLoptions
2. creates a keyspace , uses the keyspace used by the session, and creates a test 
3. inserts test data into table  
4. uploads a standard solrconfig.xml and a schema.xml from the resources directory into the dse cluster 
5. creates a solr core based on resources uploaded
6. runs both types of test queries. cql and cqlsolr
