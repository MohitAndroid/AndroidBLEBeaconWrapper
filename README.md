<h1>Overview</h1>
<p>An Android library providing Wrapper APIs to interact with beacons.
<b><br>IMPORTANT: By default, this library will only detect ibeacons specification.</b>
<h1>What does this library do?</h1>
<p>This library scan iBeacons and provide appropriate sorted data based on beacon payload.In addition it will provide data based on how much beacon far/near from person.
<h1>How does this library works?</h1>


 <h1>Gradle Dependancy</h1>
 <p>
 <pre>
  dependencies {
	        implementation 'com.github.MohitAndroid:AndroidBleBeaconWrapper:1.0.0
  }
 </pre>
 


<pre>

        BLEBeaconWrapper bleBeaconWrapper = new BLEBeaconWrapper(this);
        String url = "http://api.plos.org/search?q=title:DNA";
        
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Accept", "*/*");
        headerMap.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/66.0.3359.117 Safari/537.36");

        bleBeaconWrapper.getBeaconData(url,
                DNAEntity.class, headerMap, 3000, new BleBeaconListener<BeaconResultEntity>() {
                    @Override
                    public void onResult(List<BeaconResultEntity> list) {
                        Log.d("BLE-RESPONSE", "Total : " + list.size());
                        for (int i = 0; i < list.size(); i++) {
                            Log.d("BLE-RESPONSE", "Inside : " + list.get(i).getResult().size());
                        }

                    }

                    @Override
                    public void onError(String errorMsg) {
                        Log.d("BLE-RESPONSE", "" + errorMsg);
                    }

                    @Override
                    public void onShowProgress() {
                        Log.d("BLE-RESPONSE", "onShowProgress");
                    }
                });
   </pre>
