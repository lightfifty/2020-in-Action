import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class CombiningDemo {
    public static void main(String[] args) {
//        testAndThenWhen();
//        testCombine();
//        testJoin();
//        testJoin1();
//        testMerge();
//        testStartWith();
//        testSwitch();
//        testSwitchOnNext();
        testZip();
//try {
//    Thread.sleep(10000);
//} catch (InterruptedException e) {
//    e.printStackTrace();
//}
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void testZip(){
        Observable observableA= Observable.create(emitter -> {
            emitter.onNext(1);
            Thread.sleep(500);
            emitter.onNext(2);
            Thread.sleep(500);
            emitter.onNext(3);
            Thread.sleep(500);
        });

        Observable observableB = Observable.create(emitter -> {
            emitter.onNext("A");
            Thread.sleep(1000);
            emitter.onNext("B");
            Thread.sleep(1000);
            emitter.onNext("C");
            Thread.sleep(1000);
            emitter.onNext("D");
            Thread.sleep(1000);
            emitter.onNext("E");
            Thread.sleep(1000);
            emitter.onNext("F");
        });
        Observable.zip(observableA,observableB,(a,b)->a+":"+b)
                .observeOn(Schedulers.computation())
                .subscribeOn(Schedulers.trampoline())
                .subscribe(c-> System.out.println(c));

    }

    private static void testSwitchOnNext() {
        Observable.switchOnNext(Observable.interval(100, TimeUnit.MILLISECONDS)
                .map(i -> Observable.interval(30, TimeUnit.MILLISECONDS).map(i2 -> i)))
                .take(9)
                .subscribe(System.out::println);
    }


    private static void testSwitch() {

        Observable observableA = Observable.create(emitter -> {
            emitter.onNext(1);
            emitter.onNext(2);
            emitter.onNext(3);
            Thread.sleep(2000);
            emitter.onNext(4);
            emitter.onNext(5);
        }).subscribeOn(Schedulers.computation());
        Observable observableB = Observable.just(6, 7, 8, 9, 10);
        Observable observableC = Observable.create(emitter -> {
            emitter.onNext(observableA);
            Thread.sleep(500);
            emitter.onNext(observableB);
        }).subscribeOn(Schedulers.computation());

        Observable.switchOnNext(observableC)
                .subscribe(i -> System.out.println(i));
    }

    private static void testStartWith() {
        Observable observableA = Observable.just(1, 2, 3, 4, 5);
        Observable observableB = Observable.create(emitter -> {
            emitter.onNext("A");
            Thread.sleep(1000);
            emitter.onNext("B");
            emitter.onNext("C");
            emitter.onComplete();
        });
        observableA.startWith(observableB)
                .observeOn(Schedulers.trampoline())
                .subscribe(i -> System.out.println(i));
    }

    private static void testMerge() {
        Observable observableA = Observable.just(1, 2, 3, 4, 5).subscribeOn(Schedulers.computation());
        Observable observableB = Observable.just(6, 7, 8, 9).subscribeOn(Schedulers.computation());
        observableA.mergeWith(observableB)
                .subscribe(i -> System.out.println(i));
// 休眠时间，等待输出
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void testJoin1() {
        final long start = System.currentTimeMillis();
        System.out.println("start:" + start);
        Observable<Object> observableA = Observable.create(emitter -> {
            emitter.onNext(1);
            emitter.onNext(2);
            emitter.onComplete();
        }).subscribeOn(Schedulers.computation());
        Observable<Object> observableB = Observable.create(emitter -> {
            Thread.sleep(1000);
            emitter.onNext("A");
            emitter.onNext("B");
            emitter.onComplete();
        }).subscribeOn(Schedulers.computation());

        observableA.join(observableB,
                left -> {
                    System.out.println("接收到Left数据：" + left + "  " + (System.currentTimeMillis() - start));
                    return Observable.timer(2000, TimeUnit.MILLISECONDS);
                },
                right -> {
                    System.out.println("接收到Right数据：" + right + "  " + (System.currentTimeMillis() - start));
                    return Observable.timer(1000, TimeUnit.MILLISECONDS);
                },
                (left, right) -> {
                    System.out.println("left:" + left + "  right:" + right + "  " + (System.currentTimeMillis() - start));
                    return left + ":" + right;
                }
        ).subscribeOn(Schedulers.computation())
                .observeOn(Schedulers.trampoline())
                .subscribe(
                        output -> System.out.println(output)
                        , error -> {
                        }
                        , () -> System.out.println("结束时间：" + (System.currentTimeMillis() - start))
                );
// 休眠时间，等待输出
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void testJoin() {
        Observable observableA = Observable.just(1, 2, 3);
        Observable observableB = Observable.just(4, 5, 6);
        observableA.join(observableB,
                a -> Observable.just(a).delay(1, TimeUnit.MILLISECONDS),
                b -> Observable.just(b).delay(1, TimeUnit.MILLISECONDS),
                (a, b) -> a + ":" + b
        ).subscribe(i -> System.out.println(i));

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void testCombine() {
        final long start = System.currentTimeMillis();
        System.out.println("start:" + start);
        Observable<String> observableB = Observable.create(emitter -> {
            Thread.sleep(1000);
            emitter.onNext("A");
            System.out.println("发射Right数据：" + (System.currentTimeMillis() - start));
            Thread.sleep(1000);
            emitter.onNext("B");
            Thread.sleep(1000);
            emitter.onNext("C");
            Thread.sleep(1000);
            emitter.onNext("D");
//            emitter.onComplete();


        });
        Observable<Integer> observableA = Observable.create(emitter -> {
            emitter.onNext(1);
            Thread.sleep(1000);
            emitter.onNext(2);
            Thread.sleep(1000);
            emitter.onNext(3);
            Thread.sleep(1000);
            emitter.onNext(4);
            Thread.sleep(1000);
            emitter.onNext(4);
            Thread.sleep(1000);
            emitter.onNext(4);
            Thread.sleep(1000);
            emitter.onNext(4);
            Thread.sleep(200);
            emitter.onNext(5);
//            emitter.onComplete();
        });
        Observable.merge(observableA, observableB)
                .subscribe(i -> System.out.println(i));

    }

    private static void testsample() {
        Observable<Integer> observableA = Observable.range(1, 5);

        List<Integer> data = Arrays.asList(6, 7, 8, 9, 10);
        Observable<Integer> observableB = Observable.fromIterable(data);

    }

    private static void testAndThenWhen() {
        final long start = System.currentTimeMillis();
        System.out.println("start:" + start);

        Observable<Object> observableB = Observable.create(emitter -> {

            emitter.onNext("A");
//            System.out.println("发射Right数据：" + (System.currentTimeMillis() - start));
            Thread.sleep(1000);
            emitter.onNext("B");
            Thread.sleep(1000);
            emitter.onNext("C");
            Thread.sleep(1000);
            emitter.onNext("D");
//            emitter.onComplete();
        }).subscribeOn(Schedulers.computation());
        Observable<Object> observableA = Observable.create(emitter -> {
            emitter.onNext(1);
            Thread.sleep(1000);
            emitter.onNext(2);
            Thread.sleep(1000);
            emitter.onNext(3);
            Thread.sleep(1000);
            emitter.onNext(4);
            Thread.sleep(1000);
            emitter.onNext(4);
            Thread.sleep(1000);
            emitter.onNext(4);
            Thread.sleep(1000);
            emitter.onNext(4);
            Thread.sleep(200);
            emitter.onNext(5);
//            emitter.onComplete();
        }).subscribeOn(Schedulers.computation());


        observableA.join(observableB,
                left -> {
                    System.out.println("接收到Left数据：" + left + "  " + (System.currentTimeMillis() - start));
                    return Observable.just(1).delay(2, TimeUnit.SECONDS);
                },
                right -> {
                    System.out.println("接收到Right数据：" + right + "  " + (System.currentTimeMillis() - start));
                    return Observable.just(2).delay(2, TimeUnit.SECONDS);
                },
                (left, right) -> {
                    System.out.println("left:" + left + "  right:" + right + "  " + (System.currentTimeMillis() - start));
                    return left + ":" + right;
                }
        ).subscribeOn(Schedulers.computation())
                .observeOn(Schedulers.trampoline())
                .subscribe(
                        r -> System.out.println(r)
                );

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
