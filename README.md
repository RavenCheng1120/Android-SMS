# Android-SMS

* Android P禁止App使用所有未加密的連接，遇到錯誤`W/System.err: java.io.IOException: Cleartext HTTP traffic to xxx.xxx.x.xx(IP address) not permitted`，解決方法為更改網路安全配置。        
        
在資源文件夾下創建一個XML文件夾，然後創建一個network_security_config.xml文件，文件內容如下：        
```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <base-config cleartextTrafficPermitted="true" />
</network-security-config>
```        
        
在AndroidManifest.xml中文件下的應用程序標籤增加以下屬性：        
```xml
<application
...
...
android:networkSecurityConfig="@xml/network_security_config"
/>
```        
        
        
* 從Android studio app訪問django網頁，url為主機ip address，django中settings.py要更改`ALLOWED_HOSTS = ['xxx.xxx.x.xx']`        
```java
new Thread(new Runnable() {
    @Override
    public void run() {
        try {
            String url = "http://xxx.xxx.x.xx:8000/getCode/";//填上ip address
            URL urltest = new URL(url);
            //得到connection对象。
            HttpURLConnection connection = (HttpURLConnection) urltest.openConnection();
            //设置请求方式
            connection.setRequestMethod("GET");
            //连接
            connection.connect();
            //得到响应码
            int responseCode = connection.getResponseCode();
            if(responseCode == HttpURLConnection.HTTP_OK){
                //得到响应流
                InputStream inputStream = connection.getInputStream();
                //将响应流转换成字符串
                String result = is2String(inputStream);//将流转换为字符串。
                Log.d("kwwl","result============="+result);
            }

        } catch (Exception e) {
            e.printStackTrace();
            Log.d("kwwl","ERROR!!!!");
        }
    }
}).start();
```
