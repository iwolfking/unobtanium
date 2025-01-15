package xyz.iwolfking.unobtainium.api.helper;

import com.google.common.collect.Maps;

import javax.annotation.Nullable;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Map;

public class ReflectionHelper {
    public static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
    public static final Map<Class<?>, MethodHandles.Lookup> PRIVATE_LOOKUP = Maps.newHashMap();
    public static VarHandle getFieldFromClass(Class<?> clazz, String fieldName, Class<?> fieldClass, boolean isStatic) {
        VarHandle handler;
        var lookup = safeLookup(clazz);
        handler = safeVarHandler(lookup, clazz, fieldName, fieldClass, isStatic);
        if (handler == null) {
            throw new RuntimeException("VarHandler is null");
        }
        return handler;
    }
    @Nullable
    public static MethodHandles.Lookup safeLookup(Class<?> clazz) {
        MethodHandles.Lookup lookup = PRIVATE_LOOKUP.getOrDefault(clazz, null);
        if (lookup != null) {
            return lookup;
        }
        try {
            lookup = MethodHandles.privateLookupIn(clazz, LOOKUP);
        } catch (IllegalAccessException e) {
        }
        PRIVATE_LOOKUP.put(clazz, lookup);
        return lookup;
    }
    @Nullable
    private static VarHandle safeVarHandler(MethodHandles.Lookup lookup, Class<?> clazz, String fieldName, Class<?> fieldClass, boolean isStatic) {
        VarHandle handler = null;
        if (lookup == null) {
            return null;
        }
        try {
            handler = isStatic ? lookup.findStaticVarHandle(clazz, fieldName, fieldClass) : lookup.findVarHandle(clazz, fieldName, fieldClass);
        } catch (Exception e) {
        }
        return handler;
    }
}