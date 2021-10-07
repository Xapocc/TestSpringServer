package com.testTask.server.controllers;


import org.apache.catalina.webresources.ClasspathURLStreamHandler;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;

public abstract class RequestHandler {

    private static final Logger logger = LoggerFactory.getLogger(RequestHandler.class);

    private static final int numberOfResults = 20;

    public static String GetResult(String taskNumber, String userID, String levelID, String result) {

        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        JSONParser parser = new JSONParser();

        logger.info("Request handling started");

        // File reading
        try (Reader reader = new FileReader("usersResultsJSON.json")) {

            jsonObject = (JSONObject) parser.parse(reader);
            jsonArray = (JSONArray) jsonObject.get("users");

        } catch (Exception e) {
            e.printStackTrace();
        }

        switch (taskNumber) {
            case "0":
                return FirstAndSecondTask(userID, jsonArray,"user_id","level_id");
            case "1":
                return FirstAndSecondTask(levelID, jsonArray,"level_id","user_id");
            case "2":
                return ThirdTask(userID, levelID, result, jsonArray) ? "File overwritten successfully!"
                        : "Something went wrong while overwriting the file!";
            default:
                return "Input correct task number (from 0 to 2)";

        }
    }

    public static String FirstAndSecondTask(String arg, JSONArray jsonArray, String primaryField, String secondaryField) {

        // checking for incorrect IDs
        int targetFieldValue = 0;
        try {
            targetFieldValue = Integer.parseInt(arg);
        } catch (Exception ex) {
            return "Incorrect arg:\n" + ex.getMessage();
        }

        JSONArray topResults = new JSONArray();

        for (Object jsonObject : jsonArray) {

            // skipping wrong users
            if (Integer.parseInt(((JSONObject) jsonObject).get(primaryField).toString()) != targetFieldValue)
                continue;


            JSONObject object = new JSONObject();

            object.put("user_id", ((JSONObject) jsonObject).get("user_id"));
            object.put("level_id", ((JSONObject) jsonObject).get("level_id"));
            object.put("result", ((JSONObject) jsonObject).get("result"));


            if (topResults.size() == 0) {
                // adding first result
                topResults.add(object);
            } else {

                boolean isObjectAdded = false;

                for (int j = 0; j < topResults.size(); j++) {
                    // checking if our value is bigger or equal(but secondary fields value is bigger) to value in result list
                    if (Integer.parseInt(object.get("result").toString()) > Integer.parseInt(((JSONObject) topResults.get(j)).get("result").toString()) ||
                            Integer.parseInt(object.get("result").toString()) == Integer.parseInt(((JSONObject) topResults.get(j)).get("result").toString()) &&
                                    Integer.parseInt(object.get(secondaryField).toString()) > Integer.parseInt(((JSONObject) topResults.get(j)).get(secondaryField).toString())) {

                        // removing the last result before adding the new one
                        if (topResults.size() == numberOfResults)
                            topResults.remove(topResults.size() - 1);

                        topResults.add(j, object);
                        isObjectAdded = true;
                        break;

                    }
                }

                if (!isObjectAdded && topResults.size() < numberOfResults)
                    topResults.add(object);
            }
        }

        StringBuilder result = new StringBuilder("for " + primaryField + " #" + targetFieldValue + "<br />[<br />");

        for (Object topResult : topResults)
            result.append(topResult.toString()).append(topResult.equals(topResults.get(topResults.size() - 1)) ? "<br />" : ",<br />" );

        result.append("]<br />");

        return result.toString();
    }

    // returns true if succeeded, false if failed
    public static boolean ThirdTask(String userID, String levelID, String result, JSONArray jsonArray) {

        int resultInt = Integer.parseInt(result);
        int targetUser = Integer.parseInt(userID);
        int targetLevel = Integer.parseInt(levelID);
        boolean isFound = false;
        JSONArray newJsonArray = jsonArray;
        JSONObject newJsonObject = new JSONObject();


        for (Object jsonObject : newJsonArray) {
            if (Integer.parseInt(((JSONObject) jsonObject).get("level_id").toString()) == targetLevel &&
                    Integer.parseInt(((JSONObject) jsonObject).get("user_id").toString()) == targetUser) {
                ((JSONObject) jsonObject).clear();
                ((JSONObject) jsonObject).put("user_id", targetUser);
                ((JSONObject) jsonObject).put("level_id", targetLevel);
                ((JSONObject) jsonObject).put("result", resultInt);

                isFound = true;
                break;
            }
        }

        if(!isFound) {
            JSONObject object = new JSONObject();
            object.put("user_id", targetUser);
            object.put("level_id", targetLevel);
            object.put("result", resultInt);
            newJsonArray.add(object);
        }

        newJsonObject.put("users", newJsonArray);

        File file = new File("newUsersResultsJSON.json");

        try(FileWriter fileWriter = new FileWriter(file)){
            fileWriter.write(newJsonObject.toJSONString());
            fileWriter.flush();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }
}