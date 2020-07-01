import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

import javax.swing.plaf.TableHeaderUI;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class ToolDemo {
    public static void main(String[] args) {

        delayDemo();


        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void delayDemo(){
        Observable.create(emitter -> {
            emitter.onNext(1);
            Thread.sleep(1000);
            emitter.onNext(2);
            emitter.onNext(3);
            Thread.sleep(1000);
            emitter.onError(new NullPointerException());
//            emitter.onComplete();
            emitter.onNext(4);
        }).delay(2, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.computation())
                .observeOn(Schedulers.trampoline())
                .subscribe(i-> System.out.println(i),
                        e-> System.out.println("error  "+e.getMessage()),
                        ()-> System.out.println("complete"));
    }
}
