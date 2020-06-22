import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.CompletableObserver;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.BiPredicate;
import io.reactivex.rxjava3.schedulers.Schedulers;

import java.util.concurrent.TimeUnit;

public class FilterDemo {
    public static void main(String[] args) {

//        testDebounce();
//        testDistinct();
//        testElementAt();
//        testFilter();
//        testFirst();
//        testIgonreElement();
//          testLast();
        testSample();
    }

    private static void testSample() {
        Observable.create(emitter -> {
            emitter.onNext(1);
            emitter.onNext(2);
            Thread.sleep(1200);
            emitter.onNext(3);
            Thread.sleep(1000);
            emitter.onNext(4);
            emitter.onNext(5);
            emitter.onNext(6);
            emitter.onNext(7);
            Thread.sleep(3000);
            emitter.onNext(8);
            Thread.sleep(500);
            emitter.onComplete();
        }).sample(1000, TimeUnit.MILLISECONDS)
                .subscribe(i -> System.out.println(i));
    }

    private static void testLast() {
        Observable.just(1, 2, 3, 4, 5)
//                .last(999)
                .lastElement()
                .subscribe(i -> System.out.println(i));
    }

    private static void testIgonreElement() {
        Observable.just(1, 3, 4, 5, 6, 7, 5, 4)
                .ignoreElements()
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        System.out.println("订阅时间：" + System.currentTimeMillis());
                    }

                    @Override
                    public void onComplete() {
                        System.out.println("完成时间：" + System.currentTimeMillis());
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {

                    }
                });
    }


    private static void testFirst() {
        Observable.just(1, 2, 3, 4, 5)
                .first(9999)
                .subscribe(i -> System.out.println(i));

        Observable.empty()
                .first(2)
                .subscribe(i -> System.out.println(i));
    }

    private static void testFilter() {
        Observable.just(1, 2, 3, 4, 5, 6)
                .filter(i -> i % 2 == 0)
                .subscribe(i -> System.out.println(i));

        Observable.just("a", 2, "c", "23", 23)
                .ofType(String.class)
                .subscribe(i -> System.out.println(i));
    }

    private static void testElementAt() {
        Observable.just("a", "b", "c", "d", "e")
                .elementAt(30)
//                .elementAt(10,"没有对应数据")
                .subscribe(i -> System.out.println(i));
    }

    private static void testDistinct() {
        Observable.just(1, 3, 3, 3, 4, 4, 5, 5, 4, 3)
//                .distinct()
//                .distinct(new Function<Integer, String>() {
//                    @Override
//                    public String apply(Integer integer) throws Throwable {
//                        if (integer%2==0){
//                            return "even";
//                        }else {
//                            return "odd";
//                        }
//                    }
//                })
                .distinctUntilChanged(new BiPredicate<Integer, Integer>() {
                    @Override
                    public boolean test(@NonNull Integer integer, @NonNull Integer integer2) throws Throwable {

                        return false;
                    }
                })
                .subscribe(i -> System.out.println(i));
    }

    private static void testDebounce() {
        Observable
                .create(emitter -> {
                    emitter.onNext(1);
                    Thread.sleep(1000);
                    emitter.onNext(2);
                    Thread.sleep(500);
                    emitter.onNext(3);
                    Thread.sleep(1000);
                    emitter.onNext(4);
                    Thread.sleep(500);
                    emitter.onNext(5);
                    Thread.sleep(500);
                    emitter.onNext(6);
                    Thread.sleep(500);
                    emitter.onNext(7);
                    Thread.sleep(1000);
                    emitter.onNext(8);
                    emitter.onNext(9);
                    Thread.sleep(1000);
                    emitter.onNext(10);
                    Thread.sleep(300);
                    emitter.onComplete();
                })
                .throttleWithTimeout(800, TimeUnit.MILLISECONDS)
                .observeOn(Schedulers.trampoline())
                .subscribe(i -> System.out.println(i));
    }


}
