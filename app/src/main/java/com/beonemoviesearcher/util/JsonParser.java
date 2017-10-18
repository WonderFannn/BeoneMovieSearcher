package com.beonemoviesearcher.util;

import android.util.Log;

import com.beonemoviesearcher.dao.MovieInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


/**
 * Json结果解析类
 */
public class JsonParser {

	public static List<MovieInfo> parseMovieResult(String json){

        try {
            List<MovieInfo> movieInfoList = new ArrayList<MovieInfo>();
            JSONObject jsonObject = new JSONObject(json);
            int result = jsonObject.getInt("total");
            JSONArray jsonArray = jsonObject.getJSONArray("data");
            for (int i = 0; i < jsonArray.length(); i++) {
                MovieInfo movieInfo = new MovieInfo();
                JSONObject movieData = jsonArray.getJSONObject(i);
                try{
                    movieInfo.setId(movieData.getInt("id"));
                    movieInfo.setTitle(movieData.getString("title"));
                    movieInfo.setCid(movieData.getInt("cid"));
                    movieInfo.setTitle(movieData.getString("title"));
                    movieInfo.setPic(movieData.getString("pic"));
                    movieInfo.setDirector(movieData.getString("director"));
                    movieInfo.setActor(movieData.getString("actor"));
                    movieInfo.setPingy(movieData.getString("pingy"));
                    movieInfo.setYear(movieData.getInt("year"));
                }catch (Exception e){

                }
//                movieInfo.setScore((float) movieData.get("score"));
                movieInfoList.add(movieInfo);
            }
            return movieInfoList;

        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("JsonParser", "json解析出现了问题");
        }
        return null;
    }
}
