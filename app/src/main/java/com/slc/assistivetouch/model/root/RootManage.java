package com.slc.assistivetouch.model.root;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

/**
 * root权限管理
 * Created by ontheway on 2017/10/10.
 */

class RootManage {

    private static boolean isRooted;

    /**
     * 检查设备是否root
     *
     * @return
     */
    static boolean isRooted() {
        if (isRooted) {
            return true;
        }
        String[] paths = {"/system/xbin/", "/system/bin/", "/system/sbin/", "/sbin/", "/vendor/bin/", "/su/bin/"};
        try {
            for (int i = 0; i < paths.length; i++) {
                String path = paths[i] + "su";
                if (new File(path).exists()) {
                    isRooted = true;
                    return true;
                }
            }
        } catch (Exception e) {
            //e.printStackTrace();
        }
        return false;
    }

    /**
     * 判断是否授予root权限
     *
     * @return 有权限返回真，没权限返回假
     */
    static boolean isAuthorizeRoot() {
        int result = execRootCmdSilent(RootConstant.COMMAND_ECHO + RootConstant.COMMAND_TEST); // 通过执行测试命令来检测
        return result == 0;
    }

    /**
     * 获取root权限的对象
     *
     * @return
     */
    static RootObject getRootObject() {
        RootObject rootObject = null;
        if (isAuthorizeRoot()) {
            rootObject = new RootObject();
            try {
                rootObject.setLocalProcess(Runtime.getRuntime().exec(RootConstant.COMMAND_SU));
                rootObject.setDataOutputStream(new DataOutputStream(rootObject.getLocalProcess().getOutputStream()));
                rootObject.setResult(0);
            } catch (IOException e) {
                rootObject.destroy();
            }
        }
        return rootObject;
    }

    /**
     * 执行命令判断有没有权限
     *
     * @param paramString 要执行的命令
     */
    private static int execRootCmdSilent(String paramString) {
        Process localProcess = null;
        DataOutputStream localDataOutputStream = null;
        int result = RootConstant.ROOT_STATUS_NO_AUTHORIZE_ROOT;
        try {
            localProcess = Runtime.getRuntime().exec(RootConstant.COMMAND_SU);
            OutputStream outputStream = localProcess.getOutputStream();
            localDataOutputStream = new DataOutputStream(outputStream);
            localDataOutputStream.writeBytes(paramString + RootConstant.COMMAND_LINE_END);
            localDataOutputStream.flush();
            localDataOutputStream.writeBytes(RootConstant.COMMAND_EXIT);
            localDataOutputStream.flush();
            localProcess.waitFor();
            result = localProcess.exitValue();
        } catch (Exception e) {
            //e.printStackTrace();
        } finally {
            if (localDataOutputStream != null) {
                try {
                    localDataOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (localProcess != null) {
                localProcess.destroy();
            }
        }
        return result;
    }

    public static class RootObject {
        private Process localProcess = null;
        private DataOutputStream dataOutputStream = null;
        private int result = RootConstant.ROOT_STATUS_NO_AUTHORIZE_ROOT;

        public Process getLocalProcess() {
            return localProcess;
        }

        public void setLocalProcess(Process localProcess) {
            this.localProcess = localProcess;
        }

        public DataOutputStream getDataOutputStream() {
            return dataOutputStream;
        }

        public void setDataOutputStream(DataOutputStream dataOutputStream) {
            this.dataOutputStream = dataOutputStream;
        }

        public int getResult() {
            return result;
        }

        public void setResult(int result) {
            this.result = result;
        }

        public boolean isRoot() {
            return this.result == RootConstant.ROOT_STATUS_AUTHORIZE_ROOT;
        }

        public void destroy() {
            dataOutputStream=null;
            localProcess=null;
        }
    }

}
