# Requirements
* JDK1.8+
* Gradle
* Lombok(turn on annotation processor to your IDEA)
![Lombok_IDEA](./docs/images/lombok.png)
# How to test it
```bash
gradle wrapper
gradle clean build
gradle boorRun
curl "http://localhost:8080/download?uris=ftp://speedtest.tele2.net/1KB.zip,sftp://demo:password@test.rebex.net:22/pub/example/WinFormClient.png,http://file-examples.com/wp-content/uploads/2018/04/file_example_AVI_1280_1_5MG.avi,ftp://speedtest.tele2.net/NOTFOUND.zip" | json_pp
```
```json
[
   {
      "localFileFullPath" : "/tmp/1ff547645cbcc08f7151204b9d95fec8feb34db7f558222cc286a5e7bbdb2ed9/c07febadafd9c7a34fbd4c31e568e30562de0a4af532ecbdf3856a5e801f3061.avi",
      "uri" : "http://file-examples.com/wp-content/uploads/2018/04/file_example_AVI_1280_1_5MG.avi",
      "code" : 200
   },
   {
      "errorMsg" : "java.lang.RuntimeException: Could not download the file at - speedtest.tele2.net/NOTFOUND.zip"
      "code" : 500
   },
   {
      "uri" : "ftp://speedtest.tele2.net/1KB.zip",
      "code" : 200,
      "localFileFullPath" : "/tmp/c5d190b594a73533b59a7106fec5d802b56bf3a328b96ff09a6433ccb7e7f0fa/89fcc7bddba84b41f243ebe4bf4521005bf0afb4ad2e54b42ca47813ec161029.zip"
   },
   {
      "code" : 200,
      "uri" : "sftp://demo:password@test.rebex.net:22/pub/example/WinFormClient.png",
      "localFileFullPath" : "/tmp/d181482cb3eaf0a61e6cac437a6ec4b583e05a2644d1fe424e73e976ef3a6649/d39a14e3a0fd1dbb0f01725133998f572a443e15d42baae7cef7a713f1822fd1.png"
   }
]

```
# How to add another protocols
Add a configuration properties to `/src/main/resources/application.properties`.
Create a class file to `/src/main/java/com/happyprg/downloader/service/{protocol}/Downloader{protocol}Config.java`.
Create a `Downloader{procotol}Service` class file and `implements DownloaderService` and implement abstract method of `DownloaderService`.

# How to change downloading path on your local machine.
you should change `downloader.{protocol}.localDiskRootPath` to what you want path in `/src/main/resources/application.properties`.

# Etc
* TestCoverage
![TestCoverage](./docs/images/test_coverage_of_src.png)
* TestExecutionResult
![TestExecutionResult](./docs/images/test_execution_time.png)
