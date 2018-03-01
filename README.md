# wtcamand

How to use this module:

- Add maven repositories in your gradle project
```
repositories {
    maven { url "https://dl.bintray.com/vwilliam/gvm" }
}
```

- Add libary dependencies:
```
implementation 'com.gvm:camera-module:1.0.3'
```

- **Example usage**:
```
Intent intent = CameraActivity.startThisActivity(context);
startActivityForResult(intent, REQUEST_CODE)

public void onActivityResult(int requestCode, int resultCode, Intent data) {
  ...
  //RESULT_OK
  if (requestCode == REQUEST_CODE) {
    //Get image file path
    String imageFilePath = data.getStringExtra(CameraActivity.CAMERA_RESULT)
  }
}

```
