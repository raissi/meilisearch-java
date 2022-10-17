package com.meilisearch.integration.classes;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.meilisearch.sdk.Client;
import com.meilisearch.sdk.Config;
import com.meilisearch.sdk.Index;
import com.meilisearch.sdk.Key;
import com.meilisearch.sdk.MeiliSearchHttpRequest;
import com.meilisearch.sdk.Task;
import com.meilisearch.sdk.http.ApacheHttpClient;
import com.meilisearch.sdk.utils.Movie;
import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class AbstractIT {
    private static ApacheHttpClient apacheHttpClient;
    protected Client client;
    protected final Gson gson = new Gson();

    private final Map<String, TestData<?>> testData = new HashMap<>();

    public static final String MOVIES_INDEX = "movies.json";
    public static final String NESTED_MOVIES = "nested_movies.json";

    public AbstractIT() {
        try {
            loadResource(MOVIES_INDEX);
            loadResource(NESTED_MOVIES);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setUp() {
        if (client == null) {
            Config config = new Config(getMeilisearchHost(), "masterKey");
            apacheHttpClient = new ApacheHttpClient(config);
            client = new Client(config, new MeiliSearchHttpRequest(apacheHttpClient));
        } 
            
    }

    public static void cleanup() {
        cleanup(null);
    }

    public static void cleanup(String prefix) {
        deleteAllIndexes(prefix);
        apacheHttpClient.shutdown();
    }

    public static String getMeilisearchHost() {
        String meilisearchHost = System.getenv("MEILISEARCH_HOST");
        if (meilisearchHost != null) {
            return meilisearchHost;
        }
        return "http://localhost:7700";
    }

    public Index createEmptyIndex(String indexUid) throws Exception {
        Task task = client.createIndex(indexUid);
        client.waitForTask(task.getUid());
        Thread.sleep(500);
        return client.getIndex(indexUid);
    }

    public Index createEmptyIndex(String indexUid, String primaryKey) throws Exception {
        Task task = client.createIndex(indexUid, primaryKey);
        client.waitForTask(task.getUid());
        Thread.sleep(500);
        return client.getIndex(indexUid);
    }

    public void loadResource(String fileName) throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();

        URL resource = classLoader.getResource(fileName);
        if (resource == null) {
            throw new IllegalArgumentException("file is not found!");
        }
        File file = new File(resource.getFile());
        String rawData =
                new BufferedReader(new InputStreamReader(new FileInputStream(file)))
                        .lines()
                        .collect(Collectors.joining());
        List<Movie> movies =
                gson.fromJson(
                        rawData, TypeToken.getParameterized(List.class, Movie.class).getType());
        testData.put(fileName, new TestData<>(rawData, movies));
    }

    public Key getPrivateKey() throws Exception {
        Key[] keys = client.getKeys();
        for (Key key : keys) {
            if ((key.getDescription() == null)
                    || (key.getDescription().contains("Default Admin API"))) {
                return key;
            }
        }
        return null;
    }

    public static void deleteAllIndexes(String prefix) {
        try {
            Client ms = new Client(new Config(getMeilisearchHost(), "masterKey"), 
                                                new MeiliSearchHttpRequest(apacheHttpClient));
            Index[] indexes = ms.getIndexList();
            for (Index index : indexes) {
                String indexUid = index.getUid();
                if (prefix != null && !indexUid.startsWith(prefix)) {
                    continue;
                }
                ms.deleteIndex(index.getUid());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void deleteAllKeys() {
        try {
            Client ms = new Client(new Config(getMeilisearchHost(), "masterKey"));
            Key[] keys = ms.getKeys();
            for (Key key : keys) {
                if ((key.getDescription() == null) || (key.getDescription().contains("test"))) {
                    ms.deleteKey(key.getKey());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public <T> TestData<T> getTestData(String index, Class<T> tClass) {
        return (TestData<T>) testData.get(index);
    }
}
