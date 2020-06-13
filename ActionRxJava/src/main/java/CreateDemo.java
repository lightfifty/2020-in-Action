import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Action;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.functions.Supplier;
import io.reactivex.rxjava3.schedulers.Schedulers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class CreateDemo {


    /*******************************create方法测试***************************************/
    private static void testCreate() {
        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<String> emitter) throws Throwable {
                try {
                    for (int i = 0; i < 10; i++) {
                        if (!emitter.isDisposed()) {
                            emitter.onNext(i + "测试create");
                        }
                    }
                } catch (Exception e) {
                    emitter.onError(e);
                }
                emitter.onComplete();
            }
        }).subscribe(new Consumer<String>() {
            @Override
            public void accept(String s) throws Throwable {
                System.out.println("onNext:" + s);
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Throwable {
                System.out.println("onError:" + throwable.getMessage());
            }
        }, new Action() {
            @Override
            public void run() throws Throwable {
                System.out.println("onComplete:");
            }
        });

    }

    private static void testCreate1() {
        Observable.create(emitter -> {
            try {
                for (int i = 0; i < 10; i++) {
                    if (!emitter.isDisposed()) {
                        emitter.onNext(i + "测试create");
                    }
                }
            } catch (Exception e) {
                emitter.onError(e);
            }
            emitter.onComplete();
        }).subscribe(
                s -> System.out.println("onNext:" + s),
                throwable -> System.out.println("onError:" + throwable.getMessage()),
                () -> System.out.println("onComplete:")
        );
    }

    private static void testCreate2() {
        Observable.create(emitter -> {
            for (int i = 0; i < 10; i++) {
                if (!emitter.isDisposed()) {
                    emitter.onNext(i + "测试create");
                }
            }
        }).subscribe(s -> System.out.println("onNext:" + s));
    }


    /*******************************deffer方法测试***************************************/

    private static void testDefer() {
        List<String> arrayList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            arrayList.add(i + "defer item");
        }
        //创建observable
        Observable<String> observable = Observable.defer(new Supplier<ObservableSource<? extends String>>() {
            @Override
            public ObservableSource<? extends String> get() throws Throwable {
                System.out.println(System.currentTimeMillis() + " create Observable");
                return new ObservableSource<String>() {
                    @Override
                    public void subscribe(@NonNull Observer<? super String> observer) {
                        for (String item : arrayList) {
                            observer.onNext(item);
                        }
                        observer.onComplete();
                    }
                };
            }
        });

        //处理数据时间
        for (int i = 10; i < 20; i++) {
            try {
                Thread.sleep(1000);
                arrayList.add(i + "defer item");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        //开始订阅
        System.out.println(System.currentTimeMillis() + " start subscribe");
        observable.subscribe(new Consumer<String>() {
            @Override
            public void accept(String s) throws Throwable {
                System.out.println(System.currentTimeMillis() + " 收到事件:" + s);
            }
        });
    }

    private static void testDefer1() {
        List<String> arrayList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            arrayList.add(i + "defer item");
        }
        //创建observable
        Observable<String> observable = Observable.defer(() -> {
            System.out.println(System.currentTimeMillis() + " create Observable");
            return observer -> arrayList.forEach(item -> observer.onNext(item));
        });
        //处理数据时间
        for (int i = 10; i < 20; i++) {
            try {
                Thread.sleep(1000);
                arrayList.add(i + "defer item");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        //开始订阅
        System.out.println(System.currentTimeMillis() + " start subscribe");
        observable.subscribe(s -> System.out.println(System.currentTimeMillis() + " 收到事件:" + s));
    }


    /*******************************just方法测试***************************************/

    private static void testJust() {
        Observable.just(1, 2, 3, 4, 5, 6)
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Throwable {
                        System.out.println("接收到item：" + integer);
                    }
                });
        Observable.just(1, 2, 3, 4, 5, 6)
                .subscribe(i -> System.out.println("接收到item" + i));
    }


    /*******************************from 方法测试***************************************/

    private static void testFrom() {
        List<Integer> testdata = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            testdata.add(i);
        }
        Observable.fromIterable(testdata)
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Throwable {
                        System.out.println("from:" + integer);
                    }
                });
        Observable.fromArray(1, 23, 3, 4, 5)
                .subscribe(integer -> System.out.println(integer));

        Observable.fromRunnable(() -> {
                    System.out.println("runrunrun");
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
        ).subscribe(null,
                e -> System.out.println("error:" + e),
                () -> System.out.println("complete"));

        Observable.fromAction(new Action() {
            @Override
            public void run() throws Throwable {
                System.out.println(Thread.currentThread().getName());
                Thread.sleep(5000);
            }
        }).subscribe(s -> {
        }, s -> System.out.println("errror"), () -> System.out.println(Thread.currentThread().getName()));
    }

    private static void testFromAction() {
        Observable.fromAction(new Action() {
            @Override
            public void run() throws Throwable {
                System.out.println(Thread.currentThread().getName());
                Thread.sleep(5000);
            }
        }).subscribe(s -> {
        }, s -> System.out.println("errror"), () -> System.out.println(Thread.currentThread().getName()));
    }


    public static void testEmptyNeverThrow() {
        Observer observer = new Observer() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {
                System.out.println("订阅:" + d);
            }

            @Override
            public void onNext(@NonNull Object o) {
                System.out.println("onNext:" + o);
            }

            @Override
            public void onError(@NonNull Throwable e) {
                System.out.println("onError:" + e);
            }

            @Override
            public void onComplete() {
                System.out.println("onComplete：");
            }
        };
        Observable.empty().subscribe(observer);
        Observable.never().subscribe(observer);
        Observable.error(new NullPointerException()).subscribe(observer);
        Observable.error(new Supplier<Throwable>() {
            @Override
            public Throwable get() throws Throwable {
                return new Exception("ddd");
            }
        }).subscribe(observer);

    }


    /*******************************interval 方法测试***************************************/
    private static void testInterval() {
        Observer<Long> observer = new Observer<Long>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {
                System.out.println("订阅事件：" + d);
            }

            @Override
            public void onNext(Long integer) {
                System.out.println("onNext:" + integer);
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(@NonNull Throwable e) {
                System.out.println("onError:" + e);
            }

            @Override
            public void onComplete() {
                System.out.println("onComplete");
            }
        };
//        Observable.interval(50, TimeUnit.SECONDS).subscribe(observer);

        Observable.interval(5, 5, TimeUnit.SECONDS)
                .observeOn(Schedulers.trampoline())
                .subscribe(l -> {
                    System.out.println(l);
                });

    }

    public static void main(String[] args) {
        System.out.println("开始测试");
//        testCreate();
//        testCreate1();
//        testCreate2();
//        testDefer();
//        testDefer1();
//        testJust();
//        testFrom();
//        testFromAction();
//        testEmptyNeverThrow();
//        testInterval();
//        testRange();
//        testRepeat();
    }

    /*******************************interval 方法测试***************************************/

    private static void testRange() {
        Observable.range(1, 10).subscribe(i -> System.out.println(i));
        Observable.intervalRange(1,2,1,5,TimeUnit.SECONDS,Schedulers.trampoline())
                .subscribe(i-> System.out.println(i));
    }

    private static void testRepeat() {
        Observable.timer(5, TimeUnit.SECONDS,Schedulers.trampoline())
                .subscribeOn(Schedulers.trampoline())
                .subscribe(l -> System.out.println(l));
    }

}
