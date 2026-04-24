import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

public class LoadTest {

    static final String URL = "http://localhost:8081/api/orders";
    static final int THREADS = 4;
    static final int REQUESTS_PER_THREAD = 4;

    public static void main(String[] args) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        ExecutorService executor = Executors.newFixedThreadPool(THREADS);

        AtomicInteger success = new AtomicInteger();
        AtomicInteger rateLimited = new AtomicInteger();
        AtomicInteger errors = new AtomicInteger();
        AtomicLong totalLatency = new AtomicLong();

        long start = System.currentTimeMillis();

        for (int i = 0; i < THREADS; i++) {
              final String userId = String.valueOf(i);
              final int threadIndex = i;
            executor.submit(() -> {
                      try {
                            Thread.sleep(threadIndex * 20L);
                          } catch (InterruptedException e) {}
                for (int j = 0; j < REQUESTS_PER_THREAD; j++) {
                    try {
                        HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create(URL))
                            .header("User-Id", "user-" + userId)
                            .header("Authorization", "Bearer eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJ3RHU3WU5VS3JWWjAzZ3I1bHUwZXhiUHV2X2RiVDYwRUd2M0FNd3A2OTdrIn0.eyJleHAiOjE3NzcwNjE1MDMsImlhdCI6MTc3NzA2MTIwMywiYXV0aF90aW1lIjoxNzc3MDYwMDc1LCJqdGkiOiI5Yzk1ZmYyOC01NTA1LTQ2MWItODI5Yi0wYmYxYTFlZGRiMjMiLCJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjgxODEvcmVhbG1zL0FzdHJhY29yZSIsImF1ZCI6ImFjY291bnQiLCJzdWIiOiI4YzVkOTVhNC1jODM0LTQzMDctODI3Yy03ZTVlMGYyM2U1MjUiLCJ0eXAiOiJCZWFyZXIiLCJhenAiOiJyYXRlTGltaXRlciIsIm5vbmNlIjoiTG5wNWJVWjZkRXQtY0ZOWWVrTnNVRlp1V0cxa2ZsOU5NbkJyYVRCVk1sbFVjMk01UW1ZMVpXdzNhazlYIiwic2Vzc2lvbl9zdGF0ZSI6ImE2ZGQxMjZjLWM1M2QtNDM4Mi05ODk0LTdhNjIxMmExZTgzNyIsImFjciI6IjAiLCJhbGxvd2VkLW9yaWdpbnMiOlsiKiJdLCJyZWFsbV9hY2Nlc3MiOnsicm9sZXMiOlsiZGVmYXVsdC1yb2xlcy1hc3RyYWNvcmUiLCJvZmZsaW5lX2FjY2VzcyIsInVtYV9hdXRob3JpemF0aW9uIl19LCJyZXNvdXJjZV9hY2Nlc3MiOnsiYWNjb3VudCI6eyJyb2xlcyI6WyJtYW5hZ2UtYWNjb3VudCIsIm1hbmFnZS1hY2NvdW50LWxpbmtzIiwidmlldy1wcm9maWxlIl19fSwic2NvcGUiOiJvcGVuaWQgcHJvZmlsZSBlbWFpbCIsInNpZCI6ImE2ZGQxMjZjLWM1M2QtNDM4Mi05ODk0LTdhNjIxMmExZTgzNyIsImVtYWlsX3ZlcmlmaWVkIjpmYWxzZSwibmFtZSI6IlRlc3QgVXNlciIsInByZWZlcnJlZF91c2VybmFtZSI6InRlc3R1c2VyMSIsImdpdmVuX25hbWUiOiJUZXN0IiwiZmFtaWx5X25hbWUiOiJVc2VyIiwiZW1haWwiOiJ0ZXN0dXNlckBnbWFpbC5jb20ifQ.xF3I9NwOnqN4UvFJVoqIcxL5JRWlkNh_KWtgFQodkF46uKNFYtZwBEmVjy_wCCCXyQbK_BWqHvTgdPCkNA78F6tDUzuTJWaI87_REDrBAvaLbQbOQEX59l1f-XolHsu-eUOZje6k9Rpi4s77VV5mX-Fkf0_-XV6GrTELC_CLSKhZTFzrNp3gpLiIZqPaj7Xce-3v8lAhMvtKn45qYpHXxPFYfPR6Q90oe6icDc-fMjAqDWf8KQ2MLzktXibPzDMd5sLQ0lGLxD7RXM-MjxuPw1_3iGR6g1MvTqNoC5DAhxX91wNN7JM8aN29xGW24PqUYDYN-SwDJChJgbceKkXCog") 
                            .GET()
                            .build();

                        long reqStart = System.currentTimeMillis();
                        HttpResponse<String> response = client.send(
                            request, HttpResponse.BodyHandlers.ofString()
                        );
                        totalLatency.addAndGet(System.currentTimeMillis() - reqStart);

                        if (response.statusCode() == 200) success.incrementAndGet();
                        else if (response.statusCode() == 429) rateLimited.incrementAndGet();
                        else errors.incrementAndGet();

                    } catch (Exception e) {
                        errors.incrementAndGet();
                    }
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(2, TimeUnit.MINUTES);

        long duration = System.currentTimeMillis() - start;
        int total = THREADS * REQUESTS_PER_THREAD;

        System.out.println("=== Load Test Results ===");
        System.out.println("Total requests  : " + total);
        System.out.println("Duration        : " + duration + "ms");
        System.out.println("Throughput      : " + (total * 1000 / duration) + " req/s");
        System.out.println("Avg latency     : " + (totalLatency.get() / total) + "ms");
        System.out.println("200 OK          : " + success.get());
        System.out.println("429 Rate limited: " + rateLimited.get());
        System.out.println("Errors          : " + errors.get());
    }
}