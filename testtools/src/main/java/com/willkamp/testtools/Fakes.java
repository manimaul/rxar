package com.willkamp.testtools;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URL;

public class Fakes {

    /**
     * Create mock data for use in Unit tests.
     *
     * @param unitTest     the unit test.
     * @param cls          the class of the mock object.
     * @param resourceName the name of the file in fla-android/app/src/test/resources/
     * @return the mock object.
     */
    public static <T> T createData(Object unitTest, Class<T> cls, String resourceName) {
        Gson gson = new Gson();
        final String jsonStr = getFileContentFromResource(unitTest, resourceName);
        return gson.fromJson(jsonStr, cls);
    }

    /**
     * Create mock data for use in Unit tests.
     *
     * @param unitTest     the unit test.
     * @param typeOfT      the type of the mock object.
     * @param resourceName the name of the file in fla-android/app/src/test/resources/
     * @return the mock object.
     */
    public static <T> T createData(Object unitTest, Type typeOfT, String resourceName) {
        final String jsonStr = getFileContentFromResource(unitTest, resourceName);

        Gson gson = new Gson();
        return gson.fromJson(jsonStr, typeOfT);
    }

    /**
     * Get a resource file for a unit test.
     *
     * @param unitTest     the unit test.
     * @param resourceName the name of the resource file.
     *                     eg. "data.json" when "data.json" exists in the test/resources directory.
     * @return the resource file.
     */
    private static File getFileFromPath(Object unitTest, String resourceName) {
        ClassLoader classLoader = unitTest.getClass().getClassLoader();
        URL resource = classLoader.getResource(resourceName);
        return new File(resource.getPath());
    }

    /**
     * Reads a unit test resource file as a string.
     *
     * @param unitTest     the unit test.
     * @param resourceName the name of the resource file.
     *                     eg. "data.json" when "data.json" exists in the test/resources directory.
     * @return the contents of the resource file.
     */
    public static String getFileContentFromResource(Object unitTest, String resourceName) {
        StringBuilder sb = new StringBuilder();
        try (InputStream is = new FileInputStream(getFileFromPath(unitTest, resourceName));
             BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }
}
