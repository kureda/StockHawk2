package com.sam_chordas.android.stockhawk.rest;

import com.sam_chordas.android.stockhawk.data.pojo.Reply;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by Serg on 6/28/2016.
 */

public interface StockApi {
    @GET("/v1/public/yql")
    Call<Reply> getHistoryData(@Query("q") String query,
                               @Query("format") String format,
                               @Query("env") String env,
                               @Query("callback") String callback);
}

