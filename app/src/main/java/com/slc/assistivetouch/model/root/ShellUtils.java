package com.slc.assistivetouch.model.root;

public class ShellUtils {
/*

    public static final String COMMAND_SU = "su";
    public static final String COMMAND_SH = "sh";
    public static final String COMMAND_EXIT = "exit\n";
    public static final String COMMAND_LINE_END = "\n";

    private ShellUtils() {
        throw new AssertionError();
    }


    public static boolean checkRootPermission() {
        return execCommand("echo root", true, false).result == 0;
    }

    public static CommandResult execCommand(String command, boolean isShowToast) {
        return execCommand(new String[]{command}, isShowToast, true);
    }


    public static CommandResult execCommand(List<String> commands, boolean isShowToast) {
        return execCommand(commands == null ? null : commands.toArray(new String[]{}), isShowToast, true);
    }


    public static CommandResult execCommand(String[] commands, boolean isShowToast) {
        return execCommand(commands, isShowToast, true);
    }


    public static CommandResult execCommand(String command, boolean isShowToast, boolean isNeedResultMsg) {
        return execCommand(new String[]{command}, isShowToast, isNeedResultMsg);
    }


    public static CommandResult execCommand(List<String> commands, boolean isShowToast, boolean isNeedResultMsg) {
        return execCommand(commands == null ? null : commands.toArray(new String[]{}), isShowToast, isNeedResultMsg);
    }


    public static CommandResult execCommand(String[] commands, boolean isShowToast, boolean isNeedResultMsg) {
        if (RootManage.isRooted()) {
            if (RootManage.isAuthorizeRoot()) {
                int result = -1;
                if (commands == null || commands.length == 0) {
                    return new CommandResult(result, null, null);
                }
                Process process = null;//进程
                BufferedReader successResult = null;
                BufferedReader errorResult = null;
                StringBuilder successMsg = null;//成功信息
                StringBuilder errorMsg = null;//错误信息

                DataOutputStream os = null;
                try {
                    process = Runtime.getRuntime().exec(COMMAND_SU);
                    //  process = Runtime.getRuntime().exec(isRoot ? COMMAND_SU : COMMAND_SH);
                    os = new DataOutputStream(process.getOutputStream());
                    for (String command : commands) {
                        if (command == null) {
                            continue;
                        }
                        os.write(command.getBytes());
                        os.writeBytes(COMMAND_LINE_END);
                        os.flush();
                    }
                    os.writeBytes(COMMAND_EXIT);
                    os.flush();

                    result = process.waitFor();
                    if (isNeedResultMsg) {
                        successMsg = new StringBuilder();
                        errorMsg = new StringBuilder();
                        successResult = new BufferedReader(new InputStreamReader(process.getInputStream()));
                        errorResult = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                        String s;
                        while ((s = successResult.readLine()) != null) {
                            successMsg.append(s);
                        }
                        while ((s = errorResult.readLine()) != null) {
                            errorMsg.append(s);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (os != null) {
                            os.close();
                        }
                        if (successResult != null) {
                            successResult.close();
                        }
                        if (errorResult != null) {
                            errorResult.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (process != null) {
                        process.destroy();
                    }
                }
                return new CommandResult(result, successMsg == null ? null : successMsg.toString(), errorMsg == null ? null
                        : errorMsg.toString());
            } else if (isShowToast) {

            }
        } else if (isShowToast) {

        }
        return new CommandResult(-1);
    }
*/


    /*public static class CommandResult {


        public int result;

        public String successMsg;

        public String errorMsg;

        public CommandResult(int result) {
            this.result = result;
        }

        public CommandResult(int result, String successMsg, String errorMsg) {
            this.result = result;
            this.successMsg = successMsg;
            this.errorMsg = errorMsg;
        }
    }*/

}
