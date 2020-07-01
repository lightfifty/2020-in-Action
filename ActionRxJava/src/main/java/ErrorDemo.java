import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableSource;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.schedulers.Schedulers;

import javax.swing.plaf.TableHeaderUI;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ErrorDemo {
    public static void main(String[] args) throws IOException {
//        errorReturnDemo();
//        onErrorResumeNextDemo();
//        retryDemo();
        retryWhenDemo();
        System.in.read();
    }

    private static void retryWhenDemo() {
        AtomicInteger tmp = new AtomicInteger(1);
        Observable.create(emitter -> {
            emitter.onNext(1);
            Thread.sleep(100);
            emitter.onNext(2);
            Thread.sleep(1000);
            emitter.onError(new RuntimeException());
            emitter.onNext(3);
        }).retryWhen(new Function<Observable<Throwable>, ObservableSource<?>>() {
            @Override
            public ObservableSource<?> apply(Observable<Throwable> throwableObservable) throws Throwable {
                tmp.getAndIncrement();
                if (tmp.get() < 5) {
                    return Observable.just(999).delay(1, TimeUnit.SECONDS);
                }
                return Observable.error(new Throwable());
            }
        }).subscribeOn(Schedulers.computation())
                .observeOn(Schedulers.trampoline())
                .subscribe(i -> System.out.println(i));
    }

    private static void retryDemo() {
        AtomicInteger tmp = new AtomicInteger(1);
        Observable.create(emitter -> {
            emitter.onNext(1);
            emitter.onNext(2);
            tmp.getAndIncrement();
            if (tmp.get() <= 2) {
                emitter.onError(new RuntimeException());
            }
            emitter.onNext(3);
        })
                .retry(5)
                .observeOn(Schedulers.computation())
                .subscribeOn(Schedulers.trampoline())
                .subscribe(i -> System.out.println(i));


    }

    private static void onExceptionResumeNext() {
        Observable.create(emitter -> {
            emitter.onNext(1);
            emitter.onNext(2);
            emitter.onError(new Exception());
            emitter.onNext(3);
        }).onErrorReturn(e -> 999)
                .subscribe(
                        i -> System.out.println(i),
                        throwable -> System.out.println("error:"),
                        () -> System.out.println("complete")
                );
    }

    private static void errorReturnDemo() {
        Observable.create(emitter -> {
            emitter.onNext(1);
            emitter.onNext(2);
            emitter.onError(new Exception());
            emitter.onNext(3);
        }).onErrorReturn(e -> 999)
                .subscribe(
                        i -> System.out.println(i),
                        throwable -> System.out.println("error:"),
                        () -> System.out.println("complete")
                );
    }

    private static void onErrorResumeNextDemo() {
        Observable.create(emitter -> {
            emitter.onNext(1);
            emitter.onNext(2);
            emitter.onError(new Exception());
            emitter.onNext(3);
        }).onErrorResumeNext(e -> Observable.just(7, 8, 9))
                .subscribe(
                        i -> System.out.println(i),
                        throwable -> System.out.println("error:"),
                        () -> System.out.println("complete")
                );
    }
}
