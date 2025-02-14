package com.meilisearch.sdk.api.index;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.meilisearch.sdk.GenericServiceTemplate;
import com.meilisearch.sdk.api.documents.Task;
import com.meilisearch.sdk.http.AbstractHttpClient;
import com.meilisearch.sdk.http.factory.BasicRequestFactory;
import com.meilisearch.sdk.http.factory.RequestFactory;
import com.meilisearch.sdk.http.request.BasicHttpRequest;
import com.meilisearch.sdk.http.request.HttpRequest;
import com.meilisearch.sdk.http.response.BasicHttpResponse;
import com.meilisearch.sdk.json.JacksonJsonHandler;
import com.meilisearch.sdk.json.JsonHandler;
import java.util.ArrayDeque;
import java.util.Deque;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class IndexHandlerTest {

    private final AbstractHttpClient client = mock(AbstractHttpClient.class);
    private final JsonHandler jsonHandler = new JacksonJsonHandler(new ObjectMapper());
    private final SettingsHandler settingsService = mock(SettingsHandler.class);
    private final RequestFactory requestFactory = new BasicRequestFactory(jsonHandler);
    private final GenericServiceTemplate serviceTemplate =
            new GenericServiceTemplate(client, jsonHandler, requestFactory);
    private final IndexHandler classToTest =
            new IndexHandler(serviceTemplate, requestFactory, settingsService);

    @Test
    void create() throws Exception {
        Mockito.when(client.post(any(HttpRequest.class)))
                .thenAnswer(
                        invocation ->
                                new BasicHttpResponse(
                                        null,
                                        200,
                                        "{\"uid\":\"movies\",\"primaryKey\":\"id\",\"createdAt\":\"2019-11-20T09:40:33.711476Z\",\"updatedAt\":\"2019-11-20T09:40:33.711476Z\"}"));
        Index index = classToTest.createIndex("movies");
        assertEquals("movies", index.getUid());
        assertEquals("id", index.getPrimaryKey());
    }

    @Test
    void createWithPrimaryKey() throws Exception {
        Mockito.when(client.post(any(HttpRequest.class)))
                .thenAnswer(
                        invocation ->
                                new BasicHttpResponse(
                                        null,
                                        200,
                                        "{\"uid\":\"movies\",\"primaryKey\":\"movie_id\",\"createdAt\":\"2019-11-20T09:40:33.711476Z\",\"updatedAt\":\"2019-11-20T09:40:33.711476Z\"}"));
        Index index = classToTest.createIndex("movies", "movie_id");
        assertEquals("movies", index.getUid());
        assertEquals("movie_id", index.getPrimaryKey());
    }

    @Test
    void get() throws Exception {
        Mockito.when(client.get(any(HttpRequest.class)))
                .thenAnswer(
                        invocation ->
                                new BasicHttpResponse(
                                        null,
                                        200,
                                        "{\"uid\":\"movies\",\"primaryKey\":\"movie_id\",\"createdAt\":\"2019-11-20T09:40:33.711476Z\",\"updatedAt\":\"2019-11-20T09:40:33.711476Z\"}"));
        Index index = classToTest.getIndex("movies");
        assertEquals("movies", index.getUid());
        assertEquals("movie_id", index.getPrimaryKey());
    }

    @Test
    void getAll() throws Exception {
        Mockito.when(client.get(any(HttpRequest.class)))
                .thenAnswer(
                        invocation ->
                                new BasicHttpResponse(
                                        null,
                                        200,
                                        "[{\"uid\":\"movies\",\"primaryKey\":\"movie_id\",\"createdAt\":\"2019-11-20T09:40:33.711476Z\",\"updatedAt\":\"2019-11-20T09:40:33.711476Z\"}]"));
        Index[] index = classToTest.getAllIndexes();
        assertEquals(1, index.length);
        assertEquals("movies", index[0].getUid());
        assertEquals("movie_id", index[0].getPrimaryKey());
    }

    @Test
    void update() throws Exception {
        Deque<BasicHttpRequest> deque = new ArrayDeque<>();
        Mockito.when(client.put(any(HttpRequest.class)))
                .then(
                        invocationOnMock -> {
                            deque.add((BasicHttpRequest) invocationOnMock.getArguments()[0]);
                            return new BasicHttpResponse(
                                    null,
                                    200,
                                    "{\"uid\":\"movies\",\"primaryKey\":\"movie_id\",\"createdAt\":\"2019-11-20T09:40:33.711476Z\",\"updatedAt\":\"2019-11-20T09:40:33.711476Z\"}");
                        });
        Index index = classToTest.updateIndex("movies", "movie_id");
        assertEquals("movies", index.getUid());
        assertEquals("movie_id", index.getPrimaryKey());
    }

    @Test
    void delete() throws Exception {
        Mockito.when(client.delete(any(HttpRequest.class)))
                .thenAnswer(invocation -> new BasicHttpResponse(null, 204, ""));
        assertTrue(classToTest.deleteIndex("movies"));
        Mockito.when(client.delete(any(HttpRequest.class)))
                .thenAnswer(invocation -> new BasicHttpResponse(null, 404, ""));
        assertFalse(classToTest.deleteIndex("movies"));
        Mockito.when(client.delete(any(HttpRequest.class)))
                .thenAnswer(invocation -> new BasicHttpResponse(null, 100, ""));
        assertFalse(classToTest.deleteIndex("movies"));
    }

    @Test
    void settings() {
        Settings dummySettings = new Settings();
        dummySettings.setDistinctAttribute("test");
        Task dummyTask = new Task();
        Mockito.when(settingsService.getSettings(any())).thenReturn(dummySettings);
        Mockito.when(settingsService.resetSettings(any())).thenReturn(dummyTask);
        Mockito.when(settingsService.updateSettings(any(), any())).thenReturn(dummyTask);

        assertThat(classToTest.getSettings("test"), is(equalTo(dummySettings)));
        assertThat(classToTest.resetSettings("test"), is(equalTo(dummyTask)));
        assertThat(classToTest.updateSettings("test", dummySettings), is(equalTo(dummyTask)));
    }
}
