# Sequential Data Store Java Sample

**Version:** 1.2.11

[![Build Status](https://dev.azure.com/AVEVA-VSTS/Cloud%20Platform/_apis/build/status%2Fproduct-readiness%2FADH%2FAVEVA.sample-adh-waveform-java?repoName=AVEVA%2Fsample-adh-waveform-java&branchName=main)](https://dev.azure.com/AVEVA-VSTS/Cloud%20Platform/_build/latest?definitionId=16150&repoName=AVEVA%2Fsample-adh-waveform-java&branchName=main)

The sample code described in this topic demonstrates how to use Java to store and retrieve data from SDS using only the SDS REST API. By examining the code, you will see how to establish a connection to SDS, obtain an authorization token, obtain an SdsNamespace, create an SdsType and SdsStream, and how to create, read, update, and delete values in SDS.

[SDS documentation](https://docs.aveva.com/bundle/data-hub/page/developer-guide/sequential-data-store-dev/sds-lp-dev.html)

This project is built using Apache Maven. To run the code in this example, you must first download and install the Apache Maven software. See [Apache Maven Project](https://maven.apache.org/download.cgi) for more information. All of the necessary dependencies are specified within the pom.xml file.

Developed against Maven 3.6.1 and Java 1.8.0_181.

## Summary of steps to run the Java demo

1. Clone a local copy of the GitHub repository.
1. Install the [Java Client Library](https://github.com/AVEVA/sample-adh-sample_libraries-java) (see its [readme](https://github.com/AVEVA/sample-adh-sample_libraries-java/blob/main/README.md) for instructions)
1. The sample is configured using the file [appsettings.placeholder.json](appsettings.placeholder.json). Before editing, rename this file to `appsettings.json`.
   - This repository's `.gitignore` rules should prevent the file from ever being checked in to any fork or branch, to ensure credentials are not compromised.
1. Replace the configuration strings in `appsettings.json`
1. Build and run the project:
   1. cd to your project location.
   1. run `mvn package exec:java` on cmd. or `mvn test` to run the test

\*Currently this project is not hosted on the central Maven repo and must be compiled and installed locally.

## Java Samples: Building a Client using the SDS REST API

This sample is written using the ocs_sample_library_preview library which uses the SDS REST API. The API allows you to create SDS Service clients in any language that can make HTTP calls. Objects are passed between client and server as JSON strings. The sample uses the Gson library for the Java client, but you can use any method to create a JSON representation of objects.

## Instantiate an ADHClient or EDSClient

Each REST API call consists of an HTTP request along with a specific URL and HTTP method. The URL contains the server name plus the extension that is specific to the call. Like all REST APIs, the SDS REST API maps HTTP methods to CRUD operations as shown in the following table:

| HTTP Method | CRUD Operation | Content Found In |
| ----------- | -------------- | ---------------- |
| POST        | Create         | message body     |
| GET         | Retrieve       | URL parameters   |
| PUT         | Update         | message body     |
| DELETE      | Delete         | URL parameters   |

The constructor for the ADHClient and EDSClient classes take the base URL (that is, the protocol, server address and port number) and the api version. They also create a new Gson serializer/deserializer to convert between Java Objects and JSON. This is all done in a shared baseClient that is used amongst the the various services that we can interact with.

The sample library specifies to use `gzip` compression by adding the `Accept-Encoding` header to each request inside the `BaseClient.getConnection()` method, and decompressing the response inside the `BaseClient.getResponse()` method.

## Configure the Sample

Included in the sample is a configuration file with placeholders that need to be replaced with the proper values. They include information for authentication, connecting to the SDS Service, and pointing to a namespace.

### CONNECT data services

The SDS Service is secured using Azure Active Directory. The sample application is an example of a _confidential client_. Confidential clients provide an application ID and secret that are authenticated against the directory. These are referred to as client IDs and client secrets, which are associated with a given tenant. The steps necessary to create a new client ID and secret are described below.

First, log on to the [Data Hub Portal](https://datahub.connect.aveva.com) as a user with the Tenant Admission role, and navigate to the `Clients` page under the `Security` tab, which is situated along the left side of the webpage. Three types of clients may be created; we will use a `client-credentials` client in this sample, but for the complete explanation all of three types consult the [CONNECT data services clients](https://docs.aveva.com/bundle/aveva-data-hub/page/1263323.html) documentation. 

To create a new client, click on the `+ Add Client` button along the top, and select the desired roles for this client. This sample program covers data creation, deletion, and retrieval, so a role or roles with Read, Write, and Delete permissions on the streams collection and individual streams will be necessary. It is encouraged to not use the Tenant Administrator role for a client if possible, but that is an existing role with the necessary permissions for this sample. Tenant Contributor is a role that by default has Read and Write permissions to the necessary collections, so if deletions are not desired, it is recommended to skip those steps in the sample rather than elevating the client in order to follow along.

After naming the client and selecting the role(s), clicking continue lets you generate a secret. Secrets can always be generated or removed later for the client, but the secret itself can only be seen at creation time; naming the secret something meaningful will assist with maintenance of the secrets over time. Provide an expiration date for the secret; if the client is mapped the Tenant Administrator role, it is strongly encouraged to generate a new, fast-expiring secret for each use.

A pop-up will appear with the tenant ID, client ID and client secret. These must replace the corresponding values in the sample's configuration file.

Finally, a valid namespace ID for the tenant must be given. To create a namespace, click on the `Manage` tab then navigate to the `Namespaces` page. At the top the add button will create a new namespace after the required forms are completed. This namespace is now associated with the logged-in tenant and may be used in the sample.

### Edge Data Store

To run this sample against the Edge Data Store, the sample must be run locally on the machine where Edge Data Store is installed. In addition, the same config information must be entered with the exception of the `[Credentials]` section of the file. For a typical or default installation, the values will be:

- `"Resource": "http://localhost:5590"`
- `"TenantId": "default"`
- `"NamespaceId": "default"`
- `"ApiVersion": "v1"`

### Appsettings Schema

The values to be replaced are in `appsettings.json`:

```json
{
  "Resource": "https://uswe.datahub.connect.aveva.com",
  "ApiVersion": "v1",
  "TenantId": "PLACEHOLDER_REPLACE_WITH_TENANT_ID",
  "NamespaceId": "PLACEHOLDER_REPLACE_WITH_NAMESPACE_ID",
  "CommunityId": "",
  "ClientId": "PLACEHOLDER_REPLACE_WITH_APPLICATION_IDENTIFIER",
  "ClientSecret": "PLACEHOLDER_REPLACE_WITH_APPLICATION_SECRET"
}
```

## Obtain an Authentication Token

Near the end of the `BaseClient.Java` file is a method called `AcquireAuthToken`. The first step in obtaining an authorization token is to connect to the Open ID discovery endpoint and get a URI for obtaining the token. Thereafter, the token based on `ClientId` and `ClientSecret` is retrieved.

The token is cached, but as tokens have a fixed lifetime, typically one hour. It can be refreshed by the authenticating authority for a longer period. If the refresh period has expired, the credentials must be presented to the authority again. To streamline development, the `AcquireToken` method hides these details from client programmers. As long as you call `AcquireToken` before each HTTP call, you will have a valid token.

## Create an SdsType

To use SDS, you define SdsTypes that describe the kinds of data you want to store in SdsStreams. SdsTypes are the model that define SdsStreams. SdsTypes can define simple atomic types, such as integers, floats, or strings, or they can define complex types by grouping other SdsTypes. For more information about SdsTypes, refer to the [SDS documentation](https://docs.aveva.com/bundle/data-hub/page/developer-guide/sequential-data-store-dev/sds-lp-dev.html>).

In the sample code, the SdsType representing WaveData is defined in the `getWaveDataType` method of Program.java. WaveData contains properties of integer and double atomic types. The function begins by defining a base SdsType for each atomic type.

```java
SdsType intType = new SdsType();
intType.Id = "intType";
intType.SdsTypeCode = SdsTypeCode.Int32;

SdsType doubleType = new SdsType();
doubleType.Id = "doubleType";
doubleType.SdsTypeCode = SdsTypeCode.Double;
```

Now you can create the key property, which is an integer type and isnamed `Order`.

```java
SdsTypeProperty orderProperty = new SdsTypeProperty();
orderProperty.Id = "Order";
orderProperty.SdsType = intType;
orderProperty.IsKey = true;
```

The double value properties are created in the same way, without setting IsKey. Shown below is the code for creating the `Radians` property:

```java
SdsTypeProperty radiansProperty = new SdsTypeProperty();
radiansProperty.Id = "Radians";
radiansProperty.SdsType = doubleType;
```

After all of the necessary properties are created, you assign them to a `SdsType` which defines the overall `WaveData` class. This is done by creating an array of `SdsTypeProperty` instances and assigning it to the `Properties` property of `SdsType`:

```java
SdsType type = new SdsType();
type.Name = "WaveData";
type.Id = "WaveData";
type.Description = "This is a sample stream for storing WaveData type events";
SdsTypeProperty[] props = {orderProperty, tauProperty, radiansProperty, sinProperty, cosProperty, tanProperty, sinhProperty, coshProperty, tanhProperty};
type.Properties = props;
```

The WaveData type is created in SDS using the `createType` method in SdsClient.java.

```java
String evtTypeString = adhClient.Types.CreateType(type);
evtType = adhClient.mGson.fromJson(evtTypeString, SdsType.class);
```

All SdsTypes are constructed in a similar manner. Basic SdsTypes form the basis for SdsTypeProperties, which are then assigned to a complex user-defined type. These types can then be used in properties and become part of another SdsType's property list.

## Create an SdsStream

A SdsStream stores an ordered series of events. To create a SdsStream instance, you simply provide an Id, assign it a type, and submit it to the SDS service. The `createStream` method of SdsClient is similar to createType, except that it uses a different URL. Here is how it is called from the main program:

```java
SdsStream sampleStream = new SdsStream(sampleStreamId, sampleTypeId);
String streamJson = adhClient.Streams.createStream(tenantId, namespaceId, sampleStream);
sampleStream = adhClient.mGson.fromJson(streamJson, SdsStream.class);
```

Note that you set the `TypeId` property of the stream to the Id of the SdsType previously created. SdsTypes are reference counted, so after a type is assigned to one or more streams, it cannot be deleted until all streams that reference it are deleted.

## Create and Insert Values into the Stream

A single SdsValue is a data point in the stream. It cannot be empty and must have at least the key value of the SdsType for the event. Events are passed in JSON format and are serialized in `SdsClient.java`, which is then sent along with a POST request.

The main program creates a single `WaveData` event with the `Order` value of zero and inserts it into the SdsStream. Then, the program creates several more sequential events and inserts them with a single call:

```java
// insert a single event
List<WaveData> event = new ArrayList<WaveData>();
WaveData evt = WaveData.next(1, 2.0, 0);
event.add(evt);
adhClient.Streams.insertValues(tenantId, namespaceId, sampleStreamId, sdsclient.mGson.toJson(event));

// insert an a collection of events
List<WaveData> events = new ArrayList<WaveData>();
for (int i = 2; i < 20; i+=2) {
evt = WaveData.next(1, 2.0, i);
events.add(evt);
}
adhClient.Streams.insertValues(tenantId, namespaceId, sampleStreamId, sdsclient.mGson.toJson(events));
```

## Retrieve Values from a Stream

There are many methods in the SDS REST API that allow for the retrieval of events from a stream. Many of the retrieval methods accept indexes, which are passed using the URL. The index values must be capable of conversion to the type of the index assigned in the SdsType.

In this sample, five of the available methods are implemented in StreamsClient: `getLastValue`, `getValue`, `getWindowValues`, `getRangeValues`, and `getSampledValues`. `getWindowValues` can be used to retrieve events over a specific index range. `getRangeValues` can be used to retrieve a specified number of events from a starting index. `getSampledValues` can retrieve a sample of your data to show the overall trend. In addition to the start and end index, we also provide the number of intervals and a sampleBy parameter. Intervals parameter determines the depth of sampling performed and will affect how many values are returned. SampleBy allows you to select which property within your data you want the samples to be based on.

Get single value:

```java
String jsonSingleValue = adhClient.Streams.getValue(tenantId, namespaceId, sampleStreamId, "0");
WaveData data = adhClient.mGson.fromJson(jsonSingleValue, WaveData.class);
```

Get last value inserted:

```java
jsonSingleValue = adhClient.Streams.getLastValue(tenantId, namespaceId, sampleStreamId);
data = adhClient.mGson.fromJson(jsonSingleValue, WaveData.class));
```

Get window of values:

```java
String jsonMultipleValues = adhClient.Streams.getWindowValues(tenantId, namespaceId, sampleStreamId, "0", "18");
Type listType = new TypeToken<ArrayList<WaveData>>() {}.getType(); // necessary for gson to decode list of WaveData, represents ArrayList<WaveData> type
ArrayList<WaveData> foundEvents = adhClient.mGson.fromJson(jsonMultipleValues, listType);
```

Get range of values:

```java
jsonMultipleValues = adhClient.Streams.getRangeValues(tenantId, namespaceId, sampleStreamId, "1", 0, 3, false, SdsBoundaryType.ExactOrCalculated);
foundEvents = adhClient.mGson.fromJson(jsonMultipleValues, listType);
```

Get sampled values:

```java
jsonMultipleValues = adhClient.Streams.getSampledValues(tenantId, namespaceId, sampleStreamId, "0", "40", 4, "Sin");
foundEvents = adhClient.mGson.fromJson(jsonMultipleValues, listType);
```

## Updating and Replacing Values

The examples in this section demonstrate updates by taking the values that were created and updating them with new values. If you attempt to update values that do not exist they will be created. The sample updates the original ten values and then adds another ten values by updating with a collection of twenty values.

After you have modified the client-side events, you submit them to the SDS Service with `updateValues` as shown here:

```java
adhClient.Streams.updateValues(tenantId, namespaceId, sampleStreamId, adhClient.mGson.toJson(newEvent));
adhClient.Streams.updateValues(tenantId, namespaceId, sampleStreamId, adhClient.mGson.toJson(newEvents));
```

In contrast to updating, replacing a value only considers existing values and will not insert any new values into the stream. The sample program demonstrates this by replacing all twenty values. The calling conventions are identical to `updateValues`:

```java
adhClient.Streams.replaceValues(tenantId, namespaceId, sampleStreamId, adhClient.mGson.toJson(newEvent));
adhClient.Streams.replaceValues(tenantId, namespaceId, sampleStreamId, adhClient.mGson.toJson(newEvents));
```

## Property Overrides

SDS has the ability to override certain aspects of an SDS Type at the SDS Stream level. Meaning we apply a change to a specific SDS Stream without changing the SDS Type or the read behavior of any other SDS Streams based on that type.

In the sample, the InterpolationMode is overridden to a value of Discrete for the property Radians. Now if a requested index does not correspond to a real value in the stream, null, or the default value for the data type, is returned by the SDS Service. The following shows how this is done in the code:

```java
// Create a Discrete stream PropertyOverride indicating that we do not want SDS to calculate a value for Radians and update our stream
SdsStreamPropertyOverride propertyOverride = new SdsStreamPropertyOverride();
propertyOverride.setSdsTypePropertyId("Radians");
propertyOverride.setInterpolationMode(SdsInterpolationMode.Discrete);
List<SdsStreamPropertyOverride> propertyOverrides = new ArrayList<SdsStreamPropertyOverride>();
propertyOverrides.add(propertyOverride);

// update the stream
sampleStream.setPropertyOverrides(propertyOverrides);
adhClient.Streams.updateStream(tenantId, namespaceId, sampleStreamId, sampleStream);
```

The process consists of two steps. First, the Property Override must be created, then the stream must be updated. Note that the sample retrieves three data points before and after updating the stream to show that it has changed. See the [SDS documentation](https://docs.aveva.com/bundle/data-hub/page/developer-guide/sequential-data-store-dev/sds-lp-dev.html) for more information about SDS Property Overrides.

## SdsStreamViews

A SdsStreamView provides a way to map stream data requests from one data type to another. You can apply a stream view to any read or GET operation. SdsStreamView is used to specify the mapping between source and target types.

SDS attempts to determine how to map properties from the source to the destination. When the mapping is straightforward, such as when the properties are in the same position and of the same data type, or when the properties have the same name, SDS will map the properties automatically.

```java
jsonMultipleValues = adhClient.Streams.getRangeValues(tenantId, namespaceId, sampleStream.getId(), "1", 0, 3, false, SdsBoundaryType.ExactOrCalculated, sampleStreamViewId);
```

To map a property that is beyond the ability of SDS to map on its own, you should define an SdsStreamViewProperty and add it to the SdsStreamView's Properties collection.

```java
SdsStreamViewProperty vp2 = new SdsStreamViewProperty();
vp2.setSourceId("Sin");
vp2.setTargetId("SinInt");
...
SdsStreamView manualStreamView = new SdsStreamView();
manualStreamView.setId(sampleManualStreamViewId);
manualStreamView.setName("SampleManualStreamView");
manualStreamView.setDescription("This is a StreamView mapping SampleType to SampleTargetType");
manualStreamView.setSourceTypeId(sampleTypeId);
manualStreamView.setTargetTypeId(integerTargetTypeId);
manualStreamView.setProperties(props);
```

## SdsStreamViewMap

When an SdsStreamView is added, SDS defines a plan mapping. Plan details are retrieved as an SdsStreamViewMap. The SdsStreamViewMap provides a detailed Property-by-Property definition of the mapping. The SdsStreamViewMap cannot be written, it can only be retrieved from SDS.

```java
String jsonStreamViewMap = adhClient.Streams.getStreamViewMap(tenantId, namespaceId, sampleStreamViewId);
```

## Deleting Values from a Stream

There are two methods in the sample that illustrate removing values from a stream of data. The first method deletes only a single value. The second method removes a window of values, much like retrieving a window of values. Removing values depends on the value's key type ID value. If a match is found within the stream, then that value will be removed. Below are the declarations of both functions:

```java
adhClient.Streams.removeValue(tenantId, namespaceId, sampleStreamId, "0");
adhClient.Streams.removeWindowValues(tenantId, namespaceId, sampleStreamId, "2", "40");
```

As when retrieving a window of values, removing a window is inclusive; that is, both values corresponding to Order=2 and Order=40 are removed from the stream.

## Additional Methods

Notice that there are more methods provided in SdsClient than are discussed in this document, including get methods for types, and streams. Each has both a single get method and a multiple get method, which reflect the data retrieval methods covered above. Below is an example demonstrating getStream and getStreams:

```java
// get a single stream
String stream = adhClient.Streams.getStream(tenantId, namespaceId, sampleStreamId);
SdsStream = adhClient.mGson.fromJson(returnedStream, SdsStream.class));
// get multiple streams
String returnedStreams = adhClient.Streams.getStreams(tenantId, namespaceId, "","0", "100");
Type streamListType = new TypeToken<ArrayList<SdsStream>>(){}.getType();
ArrayList<SdsStream> streams = adhClient.mGson.fromJson(returnedStreams, streamListType);
```

For a complete list of HTTP request URLs refer to the [SDS documentation](https://docs.aveva.com/bundle/data-hub/page/developer-guide/sequential-data-store-dev/sds-lp-dev.html).

## Cleanup: Deleting Types, Stream Views and Streams

In order for the program to run repeatedly without collisions, the sample performs some cleanup before exiting. Deleting streams, stream, stream views and types can be achieved by a DELETE REST call and passing the corresponding Id.

```java
adhClient.Streams.deleteStream(tenantId, namespaceId, sampleStreamId);
adhClient.Streams.deleteStreamView(tenantId, namespaceId, sampleStreamViewId);
```

Note that the IDs of the objects are passed, not the object themselves. Similarly, the following code deletes the type from the SDS Service:

```java
adhClient.Types.deleteType(tenantId, namespaceId, sampleTypeId);
```

---

Tested against Maven 3.6.1 and Java 1.8.0_212.

For the main Cds waveform samples page [ReadMe](https://github.com/AVEVA/AVEVA-Samples-CloudOperations/blob/main/docs/SDS_WAVEFORM.md)  
For the main Cds samples page [ReadMe](https://github.com/AVEVA/AVEVA-Samples-CloudOperations)  
For the main AVEVA samples page [ReadMe](https://github.com/AVEVA/AVEVA-Samples)
