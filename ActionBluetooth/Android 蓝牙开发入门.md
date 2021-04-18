## Android 蓝牙开发入门



先说要达到的目标：使用flutter实现手机连接蓝牙打印机，打印输入内容。

阶段目标：

* 了解蓝牙开发的基本概念，理清基本步骤
* 在手机上使用原生代码实现蓝牙终端的发现和连接
* 使用flutter实现蓝牙设备连接和数据传输
* 



### 基本概念

**经典蓝牙**：就是通常意义上使用到蓝牙协议，是一种支持设备短距离通信（一般10m内）的无线电技术，蓝牙耳机，手机蓝牙传输文件。

**低功耗蓝牙**：就是BLE，通常说的蓝牙4.0（及以上版本）。低功耗，数据量小，距离50米左右。例如：蓝牙定位、蓝牙车锁。

两类蓝牙设备在使用上总体流程类似 **发现设备->配对/绑定设备->建立连接->数据通信**



### 原生实践

这里使用Android手机来扫描并连接蓝牙鼠标和手表

#### 1. 搞定权限

首先声明使用蓝牙所需的权限，

```xml
  <uses-permission android:name="android.permission.BLUETOOTH" />
  <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
  <!-- If your app targets Android 9 or lower, you can declare
       ACCESS_COARSE_LOCATION instead. -->
  <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" /> 
  <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
```

Android6.0以上的额手机需要动态申请权限，申请方式如下：

首先需要确保APP拿到定位权限，动态权限获取方式。只需要在onCreate中调用checkPermissions()即可。

```java
// Activity中定义一些常量
private static final int REQUEST_CODE_PERMISSION_LOCATION = 111;
private static final int REQUEST_CODE_OPEN_GPS = 222;
private final int START_BL_TYPE1 = 1001;

    /**
     * 检查权限
     */
    private void checkPermissions() {
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};
        List<String> permissionDeniedList = new ArrayList<>();
        for (String permission : permissions) {
            int permissionCheck = ContextCompat.checkSelfPermission(this, permission);
            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                onPermissionGranted(permission);
            } else {
                permissionDeniedList.add(permission);
            }
        }
        if (!permissionDeniedList.isEmpty()) {
            String[] deniedPermissions = permissionDeniedList.toArray(new String[permissionDeniedList.size()]);
            ActivityCompat.requestPermissions(this, deniedPermissions, REQUEST_CODE_PERMISSION_LOCATION);
        }
    }
    /**
     * 权限回调
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public final void onRequestPermissionsResult(int requestCode,@NonNull String[] permissions,@NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CODE_PERMISSION_LOCATION:
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            onPermissionGranted(permissions[i]);
                        }
                    }
                }
                break;
        }
    }

    /**
     * 开启GPS
     * @param permission
     */
    private void onPermissionGranted(String permission) {
        switch (permission) {
            case Manifest.permission.ACCESS_FINE_LOCATION:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !checkGPSIsOpen()) {
                    new AlertDialog.Builder(this)
                            .setTitle("提示")
                            .setMessage("当前手机扫描蓝牙需要打开定位功能。")
                            .setNegativeButton("取消",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            finish();
                                        }
                                    })
                            .setPositiveButton("前往设置",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                            startActivityForResult(intent, REQUEST_CODE_OPEN_GPS);
                                        }
                                    })

                            .setCancelable(false)
                            .show();
                } else {
                    //GPS已经开启了
                }
                break;
        }
    }

    /**
     * 检查GPS是否打开
     * @return
     */
    private boolean checkGPSIsOpen() {
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null)
            return false;
        return locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER);
    }
```





#### 2. 打开蓝牙

**同步打开蓝牙**

```java
// 第一种打开蓝牙的方式，同步启动蓝牙
Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
binding.btnStartBluetooth1.setOnClickListener(v -> startActivityForResult(intent, START_BL_TYPE1));
```

如果手机蓝牙未开启，会有打开蓝牙的系统弹窗，点击确定后展示正在打开蓝牙。如果蓝牙是打开的则直接会在onActivityResult接收到RESULT_OK。

```java
@Override
protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    Log.d(TAG, "onActivityResult: reauestCode:" + requestCode + "  resultCode:" + resultCode + "   data:");
    switch (requestCode) {
        case START_BL_TYPE1:
            if (resultCode == RESULT_OK) {
                showToast("蓝牙打开成功！");
            }
            break;
    }

}
```



![QtScrcpy_20200720_161623_300](Android%20%E8%93%9D%E7%89%99%E5%BC%80%E5%8F%91%E5%85%A5%E9%97%A8.assets/QtScrcpy_20200720_161623_300-1595233208077.png)

![QtScrcpy_20200720_161623_300](Android%20%E8%93%9D%E7%89%99%E5%BC%80%E5%8F%91%E5%85%A5%E9%97%A8.assets/QtScrcpy_20200720_161625_159.png)





**异步打开蓝牙**

第二种打开蓝牙的方式，使用BluetoothAdapter这种方式在Android10上还是会有系统弹窗让用户确认打开蓝牙。

```java
// 首先获取BluetoothAdapter，这是个系统级别的单例类
// 静默打开蓝牙
BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
binding.btnStartBluetooth2.setOnClickListener(v->{
    if (bluetoothAdapter!=null){
        if (!bluetoothAdapter.isEnabled()){
            bluetoothAdapter.enable();
            showToast("正在打开蓝牙！");
        }else {
            showToast("蓝牙已经打开了，无须重复打开！");
        }
    }else {
        showToast("当前设备不支持蓝牙！");
    }
});
```

即便用户打开蓝牙，也需要一个过程才会完全启动，所以如果连续点击会调用两次enable。

**关闭蓝牙**

相应的关闭蓝牙的过程也类似，使用BluetoothAdapter关闭蓝牙服务。

```java
// 静默关闭蓝牙
binding.btnStartBluetooth3.setOnClickListener(v -> {
    if (bluetoothAdapter!=null){
        if (bluetoothAdapter.isEnabled()){
            bluetoothAdapter.disable();
            showToast("正在关闭蓝牙！");
        }else {
            showToast("蓝牙已经关闭，无须重复关闭！");
        }
    }else {
        showToast("当前设备不支持蓝牙！");
    }
});
```

打开蓝牙后需要扫描周围的蓝牙设备。



#### 3. 扫描周围设备

此处我们通过一个按钮触发扫描，使用一个list来展示扫描到的设备。

(1) 创建广播接收器

扫描周围的蓝牙设备需要使用广播接收器来扫描到的设备信息，所以首先创建所需的广播接收器，定义回调接口。

```java
public class BlueToothReceiver extends BroadcastReceiver {
    private static final String TAG = BlueToothReceiver.class.getSimpleName();
    private BlueToothCallBack callBack;

    public BlueToothReceiver(BlueToothCallBack callBack) {
        this.callBack = callBack;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "onReceive: Action:" + action);
        // 获取扫描得到的设备
        BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        switch (action) {
            case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                Log.d(TAG, "onReceive: 开始扫描！");
                callBack.onStartScaning();
                break;
            case BluetoothDevice.ACTION_FOUND:
                if (bluetoothDevice != null) {
                    Log.d(TAG, "onReceive: 找到设备" + bluetoothDevice.getName());
                    callBack.onFindDevice(bluetoothDevice);
                }
                break;
            case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                Log.d(TAG, "onReceive: 扫描结束！");
                callBack.onStopScaning();
                break;
            default:
                Log.d(TAG, "onReceive: Default" + action);
                break;
        }
    }

    /**
     * 扫描回调接口
     */
    interface BlueToothCallBack {
        void onStartScaning();
        void onFindDevice(BluetoothDevice device);
        void onStopScaning();
    }
}
```

（2）创建Adapter

创建展示的部分，创建Adapter，这里用到了viewbinding。

```java
public class MyDeviceListAdapter extends RecyclerView.Adapter<MyDeviceListAdapter.MyViewHolder> {
    private List<BluetoothDevice> data;
    private OnItemClickListener onItemClickListener;

    public MyDeviceListAdapter() {
        this.data = new ArrayList<>();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void addData(BluetoothDevice device) {
        if (device != null) {
            data.add(device);
            notifyDataSetChanged();
        }
    }

    public void clearData(){
        data.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemDeviceBinding binding = ItemDeviceBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new MyViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.binding.tvItemDeviceAddress.setText(data.get(position).toString());
        if (onItemClickListener != null) {
            holder.binding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClickListener.onClick(data.get(position));
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        ItemDeviceBinding binding;

        public MyViewHolder(@NonNull ItemDeviceBinding viewBinding) {
            super(viewBinding.getRoot());
            binding = viewBinding;
        }
    }

    interface OnItemClickListener {
        void onClick(BluetoothDevice device);
    }
}
```

搞定列表部分。

```java
private List<BluetoothDevice> devices;
private MyDeviceListAdapter adapter;

// 搞定展示的部分
devices = new ArrayList<>();
adapter = new MyDeviceListAdapter();
adapter.setOnItemClickListener(new MyDeviceListAdapter.OnItemClickListener() {
    @Override
    public void onClick(BluetoothDevice device) {
        // todo 后续开启连接设备的过程
    }
});
binding.rvDevicesList.setLayoutManager(new LinearLayoutManager(this));
binding.rvDevicesList.setAdapter(adapter);
```



（3）创建回调，注册广播接收器。

```java
// 创建广播接收回调
blueToothCallBack = new BlueToothReceiver.BlueToothCallBack() {
    @Override
    public void onStartScaning() {
        binding.btnLookDevices.setText("正在扫描中！");
        binding.tvDevices.append("开始扫描！\n");
    }

    @Override
    public void onFindDevice(BluetoothDevice device) {
        if (!devices.contains(device)){
            devices.add(device);
            adapter.addData(device);
        }
        binding.tvDevices.append("name:" + device.getName() + "  address:" + device.getAddress()+"\n");
    }

    @Override
    public void onStopScaning() {
        binding.btnLookDevices.setText("开始扫描！");
        binding.tvDevices.append("结束扫描！");
    }
};

// 指定要接收的广播类型
blueToothReceiver = new BlueToothReceiver(blueToothCallBack);
IntentFilter startScanFilter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
IntentFilter finishScanFilter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
IntentFilter findDeviceFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
registerReceiver(blueToothReceiver, startScanFilter);
registerReceiver(blueToothReceiver, finishScanFilter);
registerReceiver(blueToothReceiver, findDeviceFilter);

binding.btnLookDevices.setOnClickListener(v -> {
    if (bluetoothAdapter.isDiscovering()){
        boolean result = bluetoothAdapter.cancelDiscovery();
        binding.btnLookDevices.setText("开始扫描！"+result);
    }else {
        boolean result= bluetoothAdapter.startDiscovery();
        binding.tvDevices.setText("");
        binding.btnLookDevices.setText("结束扫描！"+result);
    }
});

// 取消广播接收器
@Override
protected void onDestroy() {
    super.onDestroy();
    unregisterReceiver(blueToothReceiver);
}

```

这里需要说讲解一下几个方法，

`boolean result= bluetoothAdapter.startDiscovery();` startDiscovery() 开启扫描，返回是否开启成功，大约会扫描12秒左右。

`bluetoothAdapter.isDiscovering()`查询当前是否是在扫描状态中。

`boolean result = bluetoothAdapter.cancelDiscovery();` 取消蓝牙扫描。



#### 4.连接指定设备































































引用：

https://blog.csdn.net/zqf_888/article/details/80982337









