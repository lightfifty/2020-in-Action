# Activity的基础

Activity应该是Android系统提供的最重要的组件了，它提供窗口供应用在其中绘制界面。Android系统作为一个手机系统，用户和手机交互最多的地方就是屏幕了。通常我们在手机上完成一件事会分成多个小步骤，每个Activity会负责单独的一小块功能；从开发上来说应该让每个小模块做到高内聚低耦合，从一个Activity到另一个Activity应该尽可能传递最少的信息，减少模块之间的依赖。每个Activity也应该尽可能聚焦于自己模块内的事。刚开始学习Java的时候每写完一段demo程序都是从main函数开始运行的，同样一个APP的启动通常就是从Activity开始的，从Activity的创建到销毁的过程中，我们可以根据自己的需要在不同生命周期方法中编制想要的功能。

## 第一部分 基础部分

### 1 生命周期

按照惯例先上一幅总览图

![img](Activity%E7%9A%84%E5%90%AF%E5%8A%A8%E6%A8%A1%E5%BC%8F%E5%92%8C%E7%94%9F%E5%91%BD%E5%91%A8%E6%9C%9F.assets/activity_lifecycle.png)



（1）onCreate：第一次启动Activity会调用，表明Activity正在被创建，可以在这里进行初始化的操作，加载布局初始化数据等。值得注意的是这里会传入一个`Bundle savedInstanceState` 参数，保存的是因异常终止而保存的Activity状态，通常为null。onCreate之后Activity进入已创建不可见状态。

（2）onStart：这个时候已经Activity已经创建完成，这里如果是从别的页面或者主界面返回，此时Activity是可见的。但如果是重新创建的Activity，则是不可见的，因为界面要到onResume的时候才会绘制。

（3）onResume：此时Activity已经进入可运行状态，可以接收用户操作，除非用户触发了一些操作让焦点转移到别的地方，不然会一直维持在可运行状态。

（4）onPause：表明Activity正在停止，焦点已经不在当前Activity中，此时可以释放一些组件或者保存一些数据，在onResume被重新调用的时候初始化和还原状态。不能在这里太耗时，因为如果是打开新的Activity，必须要等旧的Activity执行完onPause之后才会调用新Activity的onResume。

（5）onStop：说明当前Activity已经对用户不可见，应该释放那些在不可见状态时不用的资源，比如暂停动画。此时也可以进行一些数据的保存，将内存中的数据保存到数据库中，当然如果需要耗时很多可以在子线程中处理。

（6）onDestroy：该方法调用说明这个Activity要从内存中清除了，这个时候需要做最后的回收操作，比如之前拿到子线程回调，一些监听等。可以使用`isFinishing()`进行区分是何种原因终止的。

（7）onRestart：说明Activity是从onStop中恢复过来的，这里可以还原一些在onStop中回收的状态和资源，之后会重新调用onStart方法。



这里需要说明Activity所处几种状态

不可见：此时Activity处于创建完成后，界面上还在显示别的Activity，就像年会节目在舞台后面等着上台。

可见：此时Activity已经在屏幕上显示出来了，但是还不能获取焦点让用户操作，就像年会节目已经上台就位了，但是灯光还在主持人那里，等他念完转场词。

可运行：此时Activity已经万事俱备了也已经拿到焦点了，可以接收用户操作，就像主持人下场了灯光已经开起来了，小伙伴们可以燥起来了。

| 系统终止进程的可能性 | 进程状态                   | Activity 状态        |
| :------------------- | :------------------------- | :------------------- |
| 较小                 | 前台（拥有或即将获得焦点） | 已创建 已开始 已恢复 |
| 较大                 | 后台（失去焦点）           | 已暂停               |
| 最大                 | 后台（不可见）             | 已停止               |
| 空                   | 已销毁                     |                      |

如果系统内存不足了，系统会根据优先级选择释放进程，所以系统不会直接清除Activity实例，而是会终止进程，同时这个进程上的Activity都会被清除，包括这个进程上的其他实例对象。当然系统也会尽可能保存一些状态，方便重建的时候恢复。

在设置界面的应用item中可以直接模拟 停止进程的情况。





### 2 状态恢复

Activity界面在配置变更（横竖屏、多窗口）的时候会销毁并重新创建Activity，如果需要保存一些状态，系统提供了如下生命周期方法可以进行处理。

在`onSaveInstanceState`中进行状态保存。这个方法只会在异常终止时在`onStop`方法之前进行调用，所以最好只保存轻量级的数据。

```java
static final String STATE_SCORE = "playerScore";
static final String STATE_LEVEL = "playerLevel";
// ...

@Override
public void onSaveInstanceState(Bundle savedInstanceState) {
    // Save the user's current game state
    savedInstanceState.putInt(STATE_SCORE, currentScore);
    savedInstanceState.putInt(STATE_LEVEL, currentLevel);

    // Always call the superclass so it can save the view hierarchy state
    super.onSaveInstanceState(savedInstanceState);
}
```

在异常终止系统重新创建Activity时，会先调用`onCreate`方法，此时的参数savedInstanceState是不为空的，可以通过判空逻辑来识别是否是异常终止。

```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState); // Always call the superclass first

    // Check whether we're recreating a previously destroyed instance
    if (savedInstanceState != null) {
        // Restore value of members from saved state
        currentScore = savedInstanceState.getInt(STATE_SCORE);
        currentLevel = savedInstanceState.getInt(STATE_LEVEL);
    } else {
        // Probably initialize members with default values for a new instance
    }
    // ...
}
```

当然你也可以在`onStart`之后的`onRestoreInstanceState`方法中进行状态恢复，这样可以简化onCreate中的代码。

```java
public void onRestoreInstanceState(Bundle savedInstanceState) {
    // Always call the superclass so it can restore the view hierarchy
    super.onRestoreInstanceState(savedInstanceState);

    // Restore state members from saved instance
    currentScore = savedInstanceState.getInt(STATE_SCORE);
    currentLevel = savedInstanceState.getInt(STATE_LEVEL);
}
```

这里的恢复和保存都是在主线程进行的，Bundle能保存的数据格式有限，自定义的对象需要序列化。所以大量的数据还是尽可能之前就持久化到本地。

当然如果是正常关闭Activity是不会走上面的方法的。

很多原生组件在异常销毁并重建后会自动恢复之前的状态，比如输入框输入的内容。View中就提供了onSaveInstanceState和onRestoreInstanceState方法，个性化的控件只需要重写自己特点的保存和恢复逻辑即可。



### 3 页面跳转

之前提到每个Activity会单独负责一部分功能，而一件事件通常需要多个Activity进行合作完成，就需要从一个Activity跳转到另一个Activity，整个过程中需要传递数据进行协作。

A -> B 

```java
Intent intent = new Intent(Intent.ACTION_SEND);
intent.putExtra(Intent.EXTRA_EMAIL, recipientArray);
startActivity(intent);
```

A -> B -> A 

A 中启动B

```java
startActivityForResult(
                 new Intent(Intent.ACTION_PICK,
                 new Uri("content://contacts")),
                 PICK_CONTACT_REQUEST);
```

B中在退出前调用setResult ，使用Intent传递A所需的数据

```
setResult(int resultCode, Intent data)
```

A中处理B中返回的数据

```java
protected void onActivityResult(int requestCode, int resultCode,Intent data) {
         if (requestCode == PICK_CONTACT_REQUEST) {
             if (resultCode == RESULT_OK) {
                 // A contact was picked.  Here we will just display it
                 // to the user.
                 startActivity(new Intent(Intent.ACTION_VIEW, data));
             }
         }
     }
```





如何让ActivityA-》ActivityB-》back   

如果ActivityB的主题设置中有

```xml
<item name="android:windowIsTranslucent">true</item>
```

则ActivityA在启动ActivityB时不会调用onStop，当由ActivityB返回ActivityA时，也不会调用ActivityA的onRestart 和onStart。





































