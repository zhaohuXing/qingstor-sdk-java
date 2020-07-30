## Sign With Server

If the local time of the user's clients is not synchronized with the network time, will cause the request to fail.
There are two solutions:
1. Calibrate the local time and keep it consistent with the network time 
2. Deploy a signature server to obtain network time from the server

This is an example of **setting the server signature time**.

```java
// Assuming that local time is consistent with network time
String gmtTime = QSSignatureUtil.formatGmtDate(new Date());
reqHandler.getBuilder().setHeader(QSConstant.HEADER_PARAM_KEY_DATE, gmtTime);
reqHandler.sendAsync();
```

After you get the time form the server, set the time before you call ``` requestHandler1.send(); ```.

```java
reqHandler.getBuilder().setHeader(QSConstant.HEADER_PARAM_KEY_DATE, gmtTime);
reqHandler.sendAsync();
```


### Code Snippet

Take uploading object for example:
```
try {
    // Step 1: new EnvContext and set zone and bucket
    EnvContext env = new EnvContext("", "");
    Bucket bucket = new Bucket(env, "zoneName", "bucketName");

    Bucket.PutObjectInput putObjectInput = new Bucket.PutObjectInput();
    File file = new File("/filePath/fileName");
    putObjectInput.setBodyInputFile(file);
    putObjectInput.setContentLength(file.length());

    // bucket.putObject("objectName", putObjectInput);

    // Step 2: get the request object
    RequestHandler reqHandler = bucket.putObjectAsyncRequest("objectName", putObjectInput,
        new ResponseCallBack<PutObjectOutput>() {
            @Override
            public void onAPIResponse(PutObjectOutput output) {
                System.out.println("Message = " + output.getMessage());
                System.out.println("RequestId = " + output.getRequestId());
                System.out.println("Code = " + output.getCode());
                System.out.println("StatueCode = " + output.getStatueCode());
                System.out.println("Url = " + output.getUrl());
                }
            });

    // Assuming that local time is consistent with network time
    String gmtTime = QSSignatureUtil.formatGmtDate(new Date());
    reqHandler.getBuilder().setHeader(QSConstant.HEADER_PARAM_KEY_DATE, gmtTime);
            
    // Step 3: get the strToSignature. Send this string to the server.
    String strToSignature = reqHandler.getStringToSignature();

    // Step 4: serverAuthorization. Get response from server. We just sign in local here.
    String serverAuthorization = QSSignatureUtil.generateSignature("secretKey",
        strToSignature);

    // Step 5: set the signature to the request.

    // There may be a time difference between the client and the server, and the result of the signature calculation is closely related to the time.
    // So it is necessary to set the time used for the server's signature to the request.
    // You can send strToSignature to the server to get the server's signature time.
    reqHandler.getBuilder().setHeader(QSConstant.HEADER_PARAM_KEY_DATE, gmtTime);
    
    reqHandler.setSignature("accessKey", serverAuthorization);
    
    // Step 6: send request. Async requests use the method sendAsync(), sync requests use the method send().
    reqHandler.sendAsync();

} catch (QSException e) {
    e.printStackTrace();
}
```
