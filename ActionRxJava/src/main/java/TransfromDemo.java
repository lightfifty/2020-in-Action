import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.functions.BiFunction;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.observables.GroupedObservable;
import org.w3c.dom.ls.LSOutput;

import java.util.concurrent.TimeUnit;

public class TransfromDemo {
    public static void main(String[] args) {
        System.out.println("hello");
//        testBuffer();
//        testMap();
//        testFlatMap();
//        testGroupBy();
//        testScan();
        testWindow();
    }
    private static void testWindow(){
        Observable.range(1,50)
                .window(10)
                .subscribe(new Consumer<Observable<Integer>>() {
                    @Override
                    public void accept(Observable<Integer> integerObservable) throws Throwable {
                        System.out.println("窗口开始");
                        integerObservable.subscribe(integer -> System.out.println(integer));
                    }
                });


    }

    private static void testScan(){

        Observable.range(1,20)
                .scan(10,new BiFunction<Integer, Integer, Integer>() {
                    @Override
                    public Integer apply(Integer integer, Integer integer2) throws Throwable {
                        System.out.println("a:"+integer+"b:"+integer2);
                        return integer+integer2;
                    }
                })
                .subscribe(integer -> {
                    System.out.println(integer);
                });
    }

    private static void testGroupBy(){

        Observable.range(1,100)
                .groupBy(i->i%2)// int类型的分组标签
                .subscribe(new Consumer<GroupedObservable<Integer, Integer>>() {
                    @Override
                    public void accept(GroupedObservable<Integer, Integer> integerIntegerGroupedObservable) throws Throwable {
                        if (integerIntegerGroupedObservable.getKey()==0){
                            integerIntegerGroupedObservable.subscribe(integer -> System.out.println("偶数："+integer));
                        }else {
                            integerIntegerGroupedObservable.subscribe(integer -> System.out.println("奇数："+integer));
                        }
                    }
                });
        Observable.range(1,100)
                .groupBy(new Function<Integer, String>() {
                    @Override
                    public String apply(Integer integer) throws Throwable {
                        if (integer%2==0){
                            return "aaa";// groupTag
                        }else {
                            return "bbb";
                        }
                    }
                })
                .subscribe(new Consumer<GroupedObservable<String, Integer>>() {
                    @Override
                    public void accept(GroupedObservable<String, Integer> stringIntegerGroupedObservable) throws Throwable {
                        String key = stringIntegerGroupedObservable.getKey();
                        switch (key){
                            case "aaa":
                                stringIntegerGroupedObservable.subscribe(i-> System.out.println("aaa:"+i));
                                break;
                            case "bbb":
                                stringIntegerGroupedObservable.subscribe(i-> System.out.println("bbb:"+i));
                        }
                    }
                });

    }

    private static void testBuffer(){
//        Observable.range(1,100)
//                .buffer(3)
//                .subscribe(list-> System.out.println(list));

        Observable.range(0,100)
                .buffer(5,3)
                .subscribe(list-> System.out.println(list));

        Observable.range(1,20000)
                .buffer(5, TimeUnit.MICROSECONDS)
                .subscribe(l-> System.out.println(l));
    }

    private static void testMap(){
        Observable.range(1,10)
                .map(new Function<Integer, String>() {
                    @Override
                    public String apply(Integer integer) throws Throwable {
                        return "第"+integer+"个数据";
                    }
                })
                .subscribe(s-> System.out.println(s));
    }

    private static void testFlatMap(){
            Observable.just("测试,flatMap,","123,45","用于IO密集型的操作，例如读写SD卡文件，查询数据库，访问网络等，具有线程缓存机制，在此调度器接收到任务后，先检查线程缓存池中，是否有空闲的线程，如果有，则复用，如果没有则创建新的线程，并加入到线程池中，如果每次都没有空闲线程使用，可以无上限的创建新线程。\n" +
                    "\n" +
                    "作者：Man不经心\n" +
                    "链接：https://www.jianshu.com/p/a298af026a83\n" +
                    "来源：简书\n" +
                    "著作权归作者所有。商业转载请联系作者获得授权，非商业转载请注明出处。")
                    .flatMap(i->Observable.fromArray(i.split("，")))
                    .subscribe(s-> System.out.println(s));
    }
}
