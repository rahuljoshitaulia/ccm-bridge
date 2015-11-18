# Description
Test Program for using the CCM tool to start and stop a DSE cluster with solr search functionality. 

* CCMBridge.java - Modified CCMBridge.java from datastax-java-driver test repo
* SSLTestBase - Starts the DSE cluster via CCM bridge
* SSLEncryptionTest - Runs a test case by creating a table in a keyspace and then running cql and cqlsolr queries on it

#Take the following steps to run this program
1. Install ccm tool on dev machine. Installation instructions can be found at ```https://github.com/pcmanus/ccm```
2. Dont forget to setup loopbacks on macosx  ```https://github.com/pcmanus/ccm```
3. run ccm command to install 4.8.0 dse repository locally (This may automatically start a cluster. Stop and delete it if thats the case).
 ```ccm create dse_cluster --dse --dse-username=user@example.com --dse-password=redacted -v 4.8.0 ```
4. In the root of this repo run ```gradle clean build```
