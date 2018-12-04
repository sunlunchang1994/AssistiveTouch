package com.slc.assistivetouch.model.root;

import android.os.Handler;
import android.os.Looper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by on the way on 2018/9/13.
 */

public class RootPerformer implements ReadExecResultsThread.OnNotifyCommandResult {
    private RootManage.RootObject rootObject;
    private Handler mHandle = new Handler();
    private Handler mainHandle = new Handler(Looper.getMainLooper());
    private List<Integer> requestCodeList;
    private OnExecuteListener onExecuteListener;

    /**
     * 初始化
     */
    public static void init(OnExecuteListener onExecuteListener) {
        RootConstant.ON_EXECUTE_LISTENER = onExecuteListener;
    }

    public static RootPerformer getInstance(OnExecuteListener onExecuteListener) {
        return new RootPerformer(onExecuteListener);
    }

    private RootPerformer(OnExecuteListener onExecuteListener) {
        this.onExecuteListener = onExecuteListener;
        requestCodeList = new ArrayList<>();
        rootObject = RootManage.getRootObject();
        if (rootObject.isRoot()) {
            ReadExecResultsThread readExecResultsThread = new ReadExecResultsThread(rootObject.getLocalProcess(), this);
            readExecResultsThread.startListener();
        }
    }

    public void execCommand(String... commands) {
        execCommand(0, Arrays.asList(commands));
    }

    public void execCommand(int requestCode, String... commands) {
        execCommand(requestCode, Arrays.asList(commands));
    }

    /**
     * 此处传值存在全局的集合里面，取出的时候从最前面取，因为cmd执行是有序的
     *
     * @param commandList
     */
    public void execCommand(List<String> commandList) {
        execCommand(0, commandList);
    }

    public void execCommand(int requestCode, List<String> commandList) {
        if (onExecuteListener != null) {
            requestCodeList.add(requestCode);
        }
        if (isRooted()) {
            if (isAuthorizeRootRoot()) {
                try {
                    rootObject.getDataOutputStream().writeBytes(RootConstant.COMMAND_ECHO + RootConstant.COMMAND_SIGN_HEAD + RootConstant.COMMAND_LINE_END);
                    rootObject.getDataOutputStream().flush();
                    for (String command : commandList) {
                        if (command == null) {
                            continue;
                        }
                        rootObject.getDataOutputStream().writeBytes(command);
                        rootObject.getDataOutputStream().writeBytes(RootConstant.COMMAND_LINE_END);
                        rootObject.getDataOutputStream().flush();
                    }
                    rootObject.getDataOutputStream().writeBytes(RootConstant.COMMAND_ECHO + RootConstant.COMMAND_SIGN_END + RootConstant.COMMAND_LINE_END);
                    rootObject.getDataOutputStream().flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                onNotify(new CommandResult(RootConstant.ROOT_STATUS_NO_AUTHORIZE_ROOT));
            }
        } else {
            onNotify(new CommandResult(RootConstant.ROOT_STATUS_NO_ROOTED));
        }
    }


    /**
     * 设备设备是否root
     *
     * @return
     */
    public static boolean isRooted() {
        return RootManage.isRooted();
    }

    /**
     * 是否授权root
     *
     * @return
     */
    public boolean isAuthorizeRootRoot() {
        return rootObject.isRoot();
    }

    /**
     * 销毁
     */
    public void destroy() {
        if (rootObject != null) {
            try {
                rootObject.getDataOutputStream().writeBytes(RootConstant.COMMAND_EXIT);
                rootObject.getDataOutputStream().flush();
                rootObject.getDataOutputStream().close();
            } catch (IOException e) {
                //e.printStackTrace();
            } finally {
                rootObject.destroy();
                rootObject = null;
            }
        }
        mHandle.removeCallbacksAndMessages(null);
        mHandle = null;
    }

    @Override
    public void onNotify(final CommandResult commandResult) {
        if (onExecuteListener != null) {
            final int requestCode = requestCodeList.get(0);
            requestCodeList.remove(0);
            mHandle.post(new Runnable() {
                @Override
                public void run() {
                    if (!onExecuteListener.onExecuteCommand(requestCode, commandResult)) {
                        mainHandle.post(new Runnable() {
                            @Override
                            public void run() {
                                RootConstant.ON_EXECUTE_LISTENER.onExecuteCommand(requestCode, commandResult);
                            }
                        });
                    }
                }
            });
        } else {
            mainHandle.post(new Runnable() {
                @Override
                public void run() {
                    RootConstant.ON_EXECUTE_LISTENER.onExecuteCommand(0, commandResult);
                }
            });
        }
    }

    /**
     * 执行监听
     */
    public interface OnExecuteListener {
        /**
         * 执行结果
         *
         * @param commandResult
         * @return 返回true代表调用者已处理，则不通知全局得监听
         */
        boolean onExecuteCommand(int requestCode, CommandResult commandResult);
    }

    public static class CommandResult {
        private int rootStatus = RootConstant.ROOT_STATUS_NO_ROOTED;
        /**
         * result of command
         **/
        private int result = RootConstant.ROOT_EXECUTE_FAILURE;
        /**
         * success message of command result
         **/
        private String successMsg;
        /**
         * error message of command result
         **/
        private String errorMsg;

        public CommandResult(int rootStatus) {
            this.rootStatus = rootStatus;
        }

        public CommandResult(int rootStatus, int result) {
            this.rootStatus = rootStatus;
            this.result = result;
        }

        public CommandResult(int rootStatus, int result, String successMsg, String errorMsg) {
            this.rootStatus = rootStatus;
            this.result = result;
            this.successMsg = successMsg;
            this.errorMsg = errorMsg;
        }

        public int getRootStatus() {
            return rootStatus;
        }

        void setRootStatus(int rootStatus) {
            this.rootStatus = rootStatus;
        }

        public int getResult() {
            return result;
        }

        void setResult(int result) {
            this.result = result;
        }

        public String getSuccessMsg() {
            return successMsg;
        }

        void setSuccessMsg(String successMsg) {
            this.successMsg = successMsg;
        }

        public String getErrorMsg() {
            return errorMsg;
        }

        void setErrorMsg(String errorMsg) {
            this.errorMsg = errorMsg;
        }
    }
}
