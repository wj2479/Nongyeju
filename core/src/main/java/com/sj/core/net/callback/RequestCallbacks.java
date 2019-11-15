package com.sj.core.net.callback;

import android.util.Log;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sj.core.net.EventMsg;
import com.sj.core.net.Rx.databus.RxBus;

import io.reactivex.functions.Function;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.ContentValues.TAG;

/**
 * 时间callback接口
 *
 * @author shenjian
 * @date 2018/8/10
 */

public class RequestCallbacks implements Callback<String> {
    private final IRequest REQUEST;
    private final ISuccess SUCCESS;
    private final IFailure FAILURE;
    private final IError ERROR;

    public RequestCallbacks(IRequest request, ISuccess success, IFailure failure, IError error) {
        this.REQUEST = request;
        this.SUCCESS = success;
        this.FAILURE = failure;
        this.ERROR = error;
    }

    //返回结果
    @Override
    public void onResponse(Call<String> call, final Response<String> response) {
        Log.e(TAG, "onResponse: " + response.toString());
        //接口请求成功，对返回的数据进行处理
        //isSuccessful() 方法的 HTTP 状态码 是 200 到 300之间，在这之间都算是请求成功；
        // 并且在正常情况下只有 200 的时候后台才会返回数据，其他是没有数据的。因此我们还要在
        // 此处处还要添加一些判断：

        if (response.isSuccessful()) {

            //isExecuted()判断是否正在运行中
            //isCanceled(); 判断是否已经取消了
            if (call.isExecuted()) {
                if (SUCCESS != null) {
                    SUCCESS.onSuccess(response.body());
                    //全局打印json
                    Log.i("json", response.body());
                    String json = response.body();
                    if (json != null) {
                        //登录超时判断
                        final JsonObject jsonObject=new JsonParser().parse(json).getAsJsonObject();
                        if (jsonObject.get("state").getAsInt() == 3) {
                            RxBus.getInstance().chainProcess(new Function() {
                                @Override
                                public Object apply(Object o) throws Exception {
                                    EventMsg msg = new EventMsg();
                                    msg.setCode(200);
                                    msg.setMessage(jsonObject.get("message").toString());
                                    return msg;
                                }
                            });
                        }
                        ;
//                                JSONObject data = GsonUtil.getInstance().fromJson<JSONObject>(json,
//                                object : TypeToken<JSONObject>() {}.type);

                        //state 3 用户身份
//                                if (jsonObject.get("state").equals("3")) {
//                                    EventMsg msg = new EventMsg();
//                                    msg.setCode(200);
//                                    msg.setMessage("");
//                                    return msg;
//                                }
                    }
//                    RxBus.getInstance().chainProcess(new Function() {
//                        @Override
//                        public Object apply(Object o) throws Exception {
//                            EventMsg msg = new EventMsg();
//                                 msg.setCode(200);
//                                  msg.setMessage("");
//                                    return msg;
//                        }
//                    });
                }
            }
        } else {
            if (ERROR != null) {
                ERROR.onError(response.code(), response.message());
            }
        }
        if (REQUEST != null) {
            REQUEST.onRequestEnd();
        }

    }

    //当前网络情况差或者请求超时等网络请求延迟等一些错误处理
    @Override
    public void onFailure(Call<String> call, Throwable t) {
        call.cancel();
        if (FAILURE != null) {
            FAILURE.onFailure();
        }
        if (REQUEST != null) {
            REQUEST.onRequestEnd();
        }
    }
}
