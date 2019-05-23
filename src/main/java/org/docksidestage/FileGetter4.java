package org.docksidestage;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * うまくいった。全スレッドが並列で動いて、全スレッド終了時にイベントが呼ばれた。
 * 参考：https://lowreal.net/2015/12/03/1
 * @author nabsan
 *
 */
public class FileGetter4 {

    public static void main(String[] args) {
        new FileGetter4().doMain(args);
    }

    private static void heavyProcess(String name) {
        System.out.println(String.format("%s -- START", name));
        try {
            Thread.currentThread().sleep((long) (Math.random() * 1000));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(String.format("%s -- END", name));
    }

    public void doMain(String[] args) {
        System.out.println("CallableSample4 start.");

        // Executorクラスの生成：引数にスレッド数を渡す。
        ExecutorService exec = Executors.newFixedThreadPool(3, r -> new Thread(r, "download"));
        ExecutorService storeExecutor = Executors.newFixedThreadPool(3, r -> new Thread(r, "store"));
        ExecutorService metaExecutor = Executors.newSingleThreadExecutor(r -> new Thread(r, "meta"));

        // 各スレッドの処理を待つためのTaskリストを用意
        List<Future<InputStream>> submitTaskList = new ArrayList<Future<InputStream>>();

        List<String> urlList = Arrays.asList("https://www.cs.kent.ac.uk/projects/ofa/jcsp/",
                "https://s.yimg.jp/images/bbportal/bb/cmn/logo-170307.png", "https://s.yimg.jp/c/logo/f/2.0/news_r_34_2x.png");
        final String dir = "/Users/manabu/develop/fess12/download_test";

        Thread.currentThread().setName("MainExecutor");
        System.out.println(String.format("[%s] START", Thread.currentThread().getName()));
        System.out.println("STT");

        final List<CompletableFuture<Void>> futures = urlList.stream().map(url -> CompletableFuture.runAsync(() -> {
            heavyProcess(String.format("[%s] downloading for %s", Thread.currentThread().getName(), url));
            final URI uri = URI.create(url);
            System.out.println("stt=" + url);
            String fileName = new File(uri.getPath()).getName();
            Path filePath = Paths.get(dir, fileName);
            try {
                InputStream in = uri.toURL().openStream();
                try {
                    byte[] buf = new byte[4096];
                    for (int read = in.read(buf); read != -1; read = in.read(buf)) {
                        Files.copy(in, filePath, REPLACE_EXISTING);
                    }
                    Thread.sleep(7000);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    in.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                //１スレッドがおわった
                System.out.printf("downloaded: %s => %s\n", uri, fileName);
            }

        }, exec).thenRunAsync(() -> {
            System.out.println("store  source here");
            heavyProcess(String.format("[%s] store for %s", Thread.currentThread().getName(), url));
        }, storeExecutor).thenRunAsync(() -> {
            System.out.println("meta updated source here.");
            heavyProcess(String.format("[%s] meta update for %s", Thread.currentThread().getName(), url));
        }, metaExecutor)

        ).collect(Collectors.toList());

        //全、子スレッドが終わった時
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()])).thenRun(() -> {
            System.out.println(String.format("[%s] FINISH", Thread.currentThread().getName()));
        });

        System.out.println(String.format("[%s] END OF MAIN", Thread.currentThread().getName()));

        // 4.実行サービスを終了
        //exec.shutdown();
        System.out.println("CallableSample4 end.");
        return;
    }
}
