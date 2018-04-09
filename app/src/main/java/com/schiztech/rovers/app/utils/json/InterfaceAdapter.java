package com.schiztech.rovers.app.utils.json;

import android.content.Intent;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.schiztech.rovers.app.roveritems.IRover;
import com.schiztech.rovers.app.utils.LogUtils;

import java.lang.reflect.Type;
import java.net.URISyntaxException;

/**
 * Created by schiz_000 on 5/13/2014.
 */
public class InterfaceAdapter<T> implements JsonSerializer<T>, JsonDeserializer<T> {
    private static final String TAG = LogUtils.makeLogTag("InterfaceAdapter");

    public JsonElement serialize(T object, Type interfaceType, JsonSerializationContext context) {
        final JsonObject wrapper = new JsonObject();
        wrapper.addProperty("type", object.getClass().getName());
        wrapper.add("data", context.serialize(object));

        return wrapper;
    }

    public T deserialize(JsonElement elem, Type interfaceType, JsonDeserializationContext context) throws JsonParseException {
        final JsonObject wrapper = (JsonObject) elem;
        final JsonElement typeName = get(wrapper, "type");
        final JsonElement data = get(wrapper, "data");
        final Type actualType = typeForName(typeName);
        return context.deserialize(data, actualType);
    }

    private Type typeForName(final JsonElement typeElem) {
        try {
            return Class.forName(typeElem.getAsString());
        } catch (ClassNotFoundException e) {
            throw new JsonParseException(e);
        }
    }

    private JsonElement get(final JsonObject wrapper, String memberName) {
        final JsonElement elem = wrapper.get(memberName);
        if (elem == null) throw new JsonParseException("no '" + memberName + "' member found in what was expected to be an interface wrapper");
        return elem;
    }

    public static Gson getBuiltGsonObject(){
        return new GsonBuilder()
                .registerTypeAdapter(IRover.class, new InterfaceAdapter<IRover>())
                .registerTypeAdapter(Intent.class, UriSerializer.getInstance())
                .create();
    }

    public static class UriSerializer implements JsonSerializer<Intent>, JsonDeserializer<Intent> {
        public static UriSerializer getInstance(){
            return new UriSerializer();
        }
        public JsonElement serialize(Intent src, Type typeOfSrc, JsonSerializationContext context) {

            return new JsonPrimitive(src.toUri(Intent.URI_INTENT_SCHEME));
        }
        public Intent deserialize(final JsonElement src, final Type srcType,
                               final JsonDeserializationContext context) throws JsonParseException {

//            Uri uri = Uri.parse(src.getAsString());

            try {
                return Intent.parseUri(src.getAsString(), 0);
            } catch (URISyntaxException e) {

            }

            return null;
//            return result;
        }
    }


}
