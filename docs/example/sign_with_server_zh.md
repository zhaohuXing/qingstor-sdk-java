## 使用服务端签名

如果 bucket 为 App 开发者所拥有，而 App 客户端众多，可能包括不少平台各异和跨时区的手机端，PC端等。
将签名密钥内置到 App 客户端中，带来安全方面的隐患，同时不同的 App 客户端的时间跟网络时间可能存在较大差异难以校准。
所以建议用户使用签名服务器完成签名颁发的工作。

具体工作步骤为：

1. App 客户端把[签名串](https://docs.qingcloud.com/qingstor/api/common/signature.html#%E8%AF%B7%E6%B1%82%E5%A4%B4%E7%AD%BE%E5%90%8D)发送给签名服务器
2. 签名服务器根据签名串，校准的网络时间，以及配置中的 access key，secret key 生成签名
3. App 客户端把签名和时间设置进请求参数，同对象存储交互，完成上传或下载等操作

具体签名服务器的使用参考[官方文档](https://docs.qingcloud.com/qingstor/solutions/app_integration.html#%E5%BC%80%E5%8F%91%E8%80%85%E5%AE%9E%E7%8E%B0%E7%AD%BE%E5%90%8D%E6%9C%8D%E5%8A%A1%E5%99%A8)

以下只描述上述`步骤1`和`步骤3`涉及 App 客户端中调用 SDK 的部分。

### 代码片段

```
try {
    // 第一步: 创建 EnvContext 并设置 zone 和 bucket
    EnvContext env = new EnvContext("", "");
    Bucket bucket = new Bucket(env, "zone 名称", "bucket 名称");

    Bucket.PutObjectInput putObjectInput = new Bucket.PutObjectInput();
    File file = new File("/文件路径/文件名");
    putObjectInput.setBodyInputFile(file);
    putObjectInput.setContentLength(file.length());

    // 第二步：获取 RequestHandler
    RequestHandler reqHandler = bucket.putObjectAsyncRequest(objectName, putObjectInput,
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

    // 第三步：获取签名串
    String strToSignature = reqHandler.getStringToSignature(); 

    // 第四步：用签名串作为参数，请求签名服务器，对签名串进行签名。
    // 签名服务器应返回 access_key_id，签名和时间。
    String accessKeyID = "从服务端获取的 access_key_id"
    String serverSignature = "从服务端获取取的签名";
    Date date = "从签名服务端获取的时间"; 
    // 签名服务器计算签名用的是 GMT 时间，时间格式需保持一致。
    String gmtTime = QSSignatureUtil.formatGmtDate(date);

    // 第五步：将获取的签名设置到 request 中
    reqHandler.getBuilder().setHeader(QSConstant.HEADER_PARAM_KEY_DATE, gmtTime);
    reqHandler.setSignature(accessKeyID, serverSignature);
    
    // 第六步：发送请求。异步请求使用 sendAsync() 方法。同步请求使用 send() 方法。
    reqHandler.sendAsync();

} catch (QSException e) {
    e.printStackTrace();
}
```
