# Android-SMS

* Android P禁止App使用所有未加密的連接，遇到錯誤`W/System.err: java.io.IOException: Cleartext HTTP traffic to xxx.xxx.x.xx(IP address) not permitted`，解決方法為更改網路安全配置。        
```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <base-config cleartextTrafficPermitted="true" />
</network-security-config>
```        
