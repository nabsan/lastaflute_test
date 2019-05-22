package org.docksidestage;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class FileGetter {

    public static void main(String[] args) throws Exception {

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        ExecutorService exec = Executors.newCachedThreadPool();

        final String dir = args[0];
        String urlString = null;

        while ((urlString = reader.readLine()) != null) {
            final URI uri = URI.create(urlString);

            //URL接続処理の非同期実行
            final Future<InputStream> stream = exec.submit(new Callable<InputStream>() {
                public InputStream call() throws Exception {
                    return uri.toURL().openStream();
                }
            });

            //ダウンロード処理の非同期実行
            final Future<Path> file = exec.submit(new Callable<Path>() {
                public Path call() throws Exception {
                    String fileName = new File(uri.getPath()).getName();
                    Path filePath = Paths.get(dir, fileName);

                    try (InputStream in = stream.get()) {
                        Files.copy(in, filePath, REPLACE_EXISTING);
                    }
                    return filePath;
                }
            });

            //結果出力です
            exec.submit(new Runnable() {
                public void run() {
                    try {
                        System.out.printf("downloaded: %s => %s\n", uri, file.get());
                    } catch (Exception ex) {
                        System.out.printf("failed: %s, %s\n", uri, ex);
                    }
                }
            });
        }

        //全処理の完了待ち
        exec.shutdown();
    }

}
