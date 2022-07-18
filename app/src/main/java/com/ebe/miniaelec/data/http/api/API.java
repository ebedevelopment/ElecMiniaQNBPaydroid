package com.ebe.miniaelec.data.http.api;

import com.google.gson.JsonObject;

import org.json.JSONArray;

import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;
import retrofit2.http.Url;


public interface API {

    @GET("api/bills/getCustomerBillsData/{customerNo}")
    Call<ResponseBody> getCustomerBillsData(@Path("customerNo") String customerNo);

    @GET("api/bills/getBuildingBillsData/{buildingNo}")
    Call<ResponseBody> getBuildingBillsData(@Path("buildingNo") String buildingNo);

    @GET("api/meters/getMeterReadData/{customerNo}")
    Call<ResponseBody> getMeterReadData(@Path("customerNo") String customerNo);


    @Headers("Content-Type: application/json")
    @POST("api/meters/updateMeterReadData")
    Call<ResponseBody> updateMeterReadData(@Body JsonObject body);

    @GET("api/APILogIns")
    Call<ResponseBody> logIn(@QueryMap Map<String, String> params);

    @GET("api/APIBankQNBs")
    Call<ResponseBody> billInquiry(@QueryMap Map<String, String> params);

    @GET("api/APIBankQNBPrints")
    Call<ResponseBody> rePrint(@QueryMap Map<String, String> params);

    @FormUrlEncoded
    @POST("api/APIBankQNBs")
    Call<ResponseBody> billPayment(@Field("InquiryID") String InquiryID, @Field("UserSessionID") String UserSessionID,
                                   @Field("UnitSerialNo") String UnitSerialNo, @Field("PayType") int PayType,
                                   @Field("ClientMobileNo") String ClientMobileNo, @Field("ClientID") String ClientID,
                                   @Field("BillDate") String BillDate, @Field("BillValue") String BillValue,
                                   @Field("CommissionValue") String CommissionValue, @Field("BankDateTime") String BankDateTime,
                                   @Field("BankReceiptNo") String BankReceiptNo, @Field("BankTransactionID") String BankTransactionID,
                                   @Field("ClientCreditCard") String ClientCreditCard, @Field("AcceptCode") String AcceptCode);

   /* @POST("api/APIBankQNBs")
    Call<ResponseBody> billPayment(@QueryMap Map<String, String> params);*/

    /*  @Headers({ "Content-Type: application/json;charset=UTF-8"})
      @POST("api/APIBankQNBs")
      Call<ResponseBody> billPayment(@Body Map<String, String> fields);
  */
    @Headers({"Content-Type: application/json"})
    @POST("api/APIBankQNBs")
    Call<ResponseBody> billPayment(@Body JsonObject body);

    @Headers({"Content-Type: application/json"})
    @POST
    Call<ResponseBody> billPayment(@Url String url, @Body JSONArray body);

    @DELETE("api/APIBankQNBs")
    Call<ResponseBody> cancelPayment(@QueryMap Map<String, String> params);

    @Headers({"Content-Type: application/json"})
    @POST("api/TransactionAPI/PostTransaction/?v=1")
    Call<ResponseBody> sendDRM(@Body JsonObject body);

    @GET("api/APIBankQNBClints")
    Call<ResponseBody> getOfflineClients(@QueryMap Map<String, String> params);

    @Headers({"Content-Type: application/json"})
    @POST("api/APIBankQNBClints")
    Call<ResponseBody> offlineBillsPay(@Body JsonObject body);

    @Headers({"Content-Type: application/json"})
    @POST("api/APIBankQNBKasms")
    Call<ResponseBody> sendDeducts(@Body JsonObject body);

    @GET("api/APIBankQNBKasms")
    Call<ResponseBody> khasmTypes(@QueryMap Map<String, String> params);

}
