package Utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class PojoConverter {

    public static String convertPojoToPrettyJsonString(Object pojoObject) {
        ObjectMapper mapper = new ObjectMapper();
        String jsonInString = null;
        try {
            jsonInString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(pojoObject);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return jsonInString;
    }

    public static String convertStringToPrettyJsonString(String jsonString) {
        JsonParser parser = new JsonParser();
        JsonElement json;
        String prettyJson = "";
        try {
            if (parser.parse(jsonString).isJsonObject()) json = parser.parse(jsonString).getAsJsonObject();
            else if (parser.parse(jsonString).isJsonArray()) json = parser.parse(jsonString).getAsJsonArray();
            else if (parser.parse(jsonString).isJsonPrimitive()) json = parser.parse(jsonString).getAsJsonPrimitive();
            else json = parser.parse(jsonString).getAsJsonNull();
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            prettyJson = gson.toJson(json);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return prettyJson;
    }
}
