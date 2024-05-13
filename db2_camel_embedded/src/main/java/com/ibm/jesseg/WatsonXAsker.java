package com.ibm.jesseg;

import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

// curl -H "Authorization:Bearer eyJraWQiOiIyMDI0MDUwNTA4MzkiLCJhbGciOiJSUzI1NiJ9.eyJpYW1faWQiOiJJQk1pZC0wNjAwMDE5SEpDIiwiaWQiOiJJQk1pZC0wNjAwMDE5SEpDIiwicmVhbG1pZCI6IklCTWlkIiwianRpIjoiODQ5ZTY0NTktZWUwZS00M2ZhLWI0NTYtMzAxMzhiNTA0OGZjIiwiaWRlbnRpZmllciI6IjA2MDAwMTlISkMiLCJnaXZlbl9uYW1lIjoiSmVzc2UiLCJmYW1pbHlfbmFtZSI6IkdvcnppbnNraSIsIm5hbWUiOiJKZXNzZSBHb3J6aW5za2kiLCJlbWFpbCI6Impnb3J6aW5zQHVzLmlibS5jb20iLCJzdWIiOiJqZ29yemluc0B1cy5pYm0uY29tIiwiYXV0aG4iOnsic3ViIjoiamdvcnppbnNAdXMuaWJtLmNvbSIsImlhbV9pZCI6IklCTWlkLTA2MDAwMTlISkMiLCJuYW1lIjoiSmVzc2UgR29yemluc2tpIiwiZ2l2ZW5fbmFtZSI6Ikplc3NlIiwiZmFtaWx5X25hbWUiOiJHb3J6aW5za2kiLCJlbWFpbCI6Impnb3J6aW5zQHVzLmlibS5jb20ifSwiYWNjb3VudCI6eyJ2YWxpZCI6dHJ1ZSwiYnNzIjoiNDljNGEwNDAzYzBlZjdiZDc5ZmZkYmE0ZmJhNTUwZTIiLCJpbXNfdXNlcl9pZCI6IjcwMjAzNDkiLCJmcm96ZW4iOnRydWUsImltcyI6IjE1NzgyNzkifSwiaWF0IjoxNzE1MzYwMTE4LCJleHAiOjE3MTUzNjM3MTgsImlzcyI6Imh0dHBzOi8vaWFtLmNsb3VkLmlibS5jb20vaWRlbnRpdHkiLCJncmFudF90eXBlIjoidXJuOmlibTpwYXJhbXM6b2F1dGg6Z3JhbnQtdHlwZTphcGlrZXkiLCJzY29wZSI6ImlibSBvcGVuaWQiLCJjbGllbnRfaWQiOiJkZWZhdWx0IiwiYWNyIjoxLCJhbXIiOlsicHdkIl19.hyfR2QMWZ9o4CRo5O0wnZvfzb9yJXqukwe3ksWxtta_JQCO6zmO4reje29Ff5vmK0a9luYrwgh7xI5plqX0stuGMGWcN5oQxcfhBALPTBzKWg4eNXQ4lOjGCTQeavtGlo3n5GtyTfJmWZKQZSma8mPLqucYSLvYO_HJ-dpk0JYb30v328Kz930uCJc80KawgwACByM-MJUEZxdP8jJOTVOblmaf6aiGo50XZ6dhJ9nLEciDo6pL1Th7V49Y8kqJOTn0X6LnaQTB5i-pudZBWmQn-OlVatryYaYClJ-2sSwAIQxfg40wzm_-Webhvlc7y_KfRlQsHVQD66I-pOjC8qQ" -X POST "https://us-south.ml.cloud.ibm.com/ml/v1/text/generation?version=2023-07-07" -H 'Content-Type: application/json' -H 'Accept: application/json' --data-raw '{ "model_id": "meta-llama/llama-2-13b-chat","input": "Translate the following text as how a rude New Yorker would say it (include only the translation in your response):\nThat dog is loud!", "parameters": { "max_new_tokens": 100, "time_limit": 1000 },"space_id":  "7ccb6334-8f1c-424a-9c3a-6ccd98f09c34" }'

public class WatsonXAsker {
    private String m_model;
    private String m_token;
    private final String m_baseUrl;
    private String m_spaceId;
    private final OkHttpClient m_client;

    public WatsonXAsker(String _baseUrl, String _model, String _token, String _spaceID) {
        m_baseUrl = StringUtils.isEmpty(_baseUrl) ? "https://us-south.ml.cloud.ibm.com/ml/v1/text/generation?version=2023-07-07"
                : _baseUrl;
        m_model = _model;
        m_token = _token;
        m_spaceId = _spaceID;
        m_client = new OkHttpClient.Builder().protocols(Arrays.asList(Protocol.HTTP_1_1)).build();
    }

    @SuppressWarnings("resource")
    public String ask(String _question, boolean _assert200) throws IOException {
        int max_new_tokens = 500;
        int time_limit = 1000;
        System.out.println("==> "+_question);
        String jsonIn = String.format(
                "{ \"model_id\": \"%s\",\"input\": \"%s\", \"parameters\": { \"max_new_tokens\": %d, \"time_limit\": %d },\"space_id\":  \"%s\" }",
                json(m_model), json(_question), max_new_tokens, time_limit, json(m_spaceId));
        RequestBody body = RequestBody.create(jsonIn.getBytes("UTF-8"));

        Request request = new Request.Builder()
                .url(m_baseUrl)
                .post(body)
                .header("Authorization", "Bearer "+ m_token)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .build();

        Call call = m_client.newCall(request);
        Response response = call.execute();
        final int responseCode = response.code();
        if (_assert200 && (responseCode < 200 || responseCode > 299)) {
            throw new IOException("HTTP response code was " + responseCode + " (" + response.message() + "): "
                    + response.body().string());
        }
        JsonObject parsed =JsonParser.parseReader(response.body().charStream()).getAsJsonObject();
        JsonObject resultSet = parsed.get("results").getAsJsonArray().get(0).getAsJsonObject();
        JsonElement generated_text = resultSet.get("generated_text");
        String ret = generated_text.getAsString().trim();
        return ret;
    }

    public String generate(String _in) throws IOException {
        return ask(_in, true).trim().replaceAll("^\\.+", "").trim();
    }

    private String json(String _str) {
        return _str.replace("\n", "\\n");
    }
    public static WatsonXAsker getWithHardcodedDefaults() {
        String token = "<your_token_here>";
        String spaceId = "<your_spaceid_here>";
        String baseUrl = "https://us-south.ml.cloud.ibm.com/ml/v1/text/generation?version=2023-07-07";
        String model = "google/flan-t5-xxl";
        // some of the other options include: 
        // meta-llama/llama-2-70b-chat
        // ibm/granite-13b-chat-v2
        // google/flan-t5-xxl
        WatsonXAsker asker = new WatsonXAsker(baseUrl,  model, token, spaceId);
        return asker;
    }

    public static void main(String[] args) {
        System.out.println(System.getProperty("java.home"));
        WatsonXAsker asker = getWithHardcodedDefaults();
        
        String question = "Translate the following text into how a rude New Yorker would say it (include only the translation in your response):\nWhy have I not received my order?";
        System.out.println("Q: " + question);
        String response;
        try {
            response = asker.ask(question, true);
            System.out.println("A: " + response);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
