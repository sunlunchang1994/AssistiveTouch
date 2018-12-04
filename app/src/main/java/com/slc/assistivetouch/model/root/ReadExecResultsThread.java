package com.slc.assistivetouch.model.root;

import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by on the way on 2018/9/14.
 */

public class ReadExecResultsThread {
    private Process localProcess = null;
    private final int SUCCESS = 0;
    private final int ERROR = 1;
    private final Object lock;
    private boolean who = false;
    private OnNotifyCommandResult onNotifyCommandResult;

    public ReadExecResultsThread(Process localProcess,OnNotifyCommandResult onNotifyCommandResult) {
        this.localProcess = localProcess;
        this.onNotifyCommandResult = onNotifyCommandResult;
        lock = new Object();

    }

    /**
     * 开始监听
     */
    public void startListener() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    BufferedReader successResult = new BufferedReader(new InputStreamReader(localProcess.getInputStream()));
                    StringBuilder successMsg = new StringBuilder();//成功信息
                    String temp;
                    while ((temp = successResult.readLine()) != null) {
                        synchronized (lock) {
                            switch (temp) {
                                case RootConstant.COMMAND_SIGN_HEAD:
                                    Log.i("myNotify", "successMsg：" + temp);
                                    commandResult = new RootPerformer.CommandResult(RootConstant.ROOT_STATUS_AUTHORIZE_ROOT);
                                    successMsg.delete(0, successMsg.length());
                                    who = true;
                                    lock.notify();
                                    break;
                                case RootConstant.COMMAND_SIGN_END:
                                    if (!who) {
                                        myNotify(successMsg.toString(), SUCCESS);
                                        Log.i("myNotify", "successMsg：" + temp);
                                    } else {
                                        lock.wait();
                                        myNotify(successMsg.toString(), SUCCESS);
                                        Log.i("myNotify", "successMsg：" + temp);
                                    }
                                    break;
                                default:
                                    successMsg.append(temp);
                                    who = false;
                                    break;
                            }
                        }
                    }
                    Log.i("myNotify", "正确流结束");
                    localProcess.destroy();
                    onNotifyCommandResult=null;
                    localProcess = null;
                } catch (IOException e) {

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    InputStream errorStream = localProcess.getErrorStream();
                    String errorMsg = null;
                    while (errorStream.read() != -1) {
                        synchronized (lock) {
                            byte[] dataArray = new byte[errorStream.available()];
                            errorStream.read(dataArray);
                            errorMsg = new String(dataArray, "utf-8");
                            if (who) {
                                myNotify(errorMsg, ERROR);
                                who = false;
                            } else {
                                lock.wait();
                                myNotify(errorMsg, ERROR);
                                who = false;
                                lock.notify();
                            }
                        }
                    }

                    Log.i("myNotify", "错误流结束");
                } catch (IOException e) {

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private RootPerformer.CommandResult commandResult;

    /**
     * 通知方法
     *
     * @param msg
     * @param sign
     */
    private void myNotify(String msg, int sign) {
        switch (sign) {
            case SUCCESS:
                commandResult.setSuccessMsg(msg);
                commandResult.setResult(!TextUtils.isEmpty(commandResult.getErrorMsg()) ? RootConstant.ROOT_EXECUTE_SUCCEED : RootConstant.ROOT_EXECUTE_FAILURE);
                if (!TextUtils.isEmpty(commandResult.getSuccessMsg())) {
                    commandResult.setResult(RootConstant.ROOT_EXECUTE_SUCCEED);
                    Log.i("myNotify", "successMsg：" + msg);
                }
                if(onNotifyCommandResult!=null){
                    onNotifyCommandResult.onNotify(commandResult);
                }
                break;
            case ERROR:
                commandResult.setErrorMsg(msg);
                Log.i("myNotify", "errorMsg：" + msg);
                break;
        }
    }

    public interface OnNotifyCommandResult {
        void onNotify(RootPerformer.CommandResult commandResult);
    }
}
