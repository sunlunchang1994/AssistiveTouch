package com.slc.assistivetouch.model.root;

/**
 * Created by on the way on 2018/9/13.
 */

public class RootConstant {
    public static final String COMMAND_SU = "su";
    public static final String COMMAND_EXIT = "exit\n";
    public static final String COMMAND_LINE_END = "\n";
    public static final String COMMAND_ECHO = "echo ";
    public static final String COMMAND_TEST = "test";
    public static final String COMMAND_SIGN_HEAD = "sign_head";
    public static final String COMMAND_SIGN_END = "sign_end";
    public static final int ROOT_STATUS_NO_ROOTED = -2;
    public static final int ROOT_STATUS_NO_AUTHORIZE_ROOT = -1;
    public static final int ROOT_STATUS_AUTHORIZE_ROOT = 0;
    public static final int ROOT_EXECUTE_FAILURE = -1;
    public static final int ROOT_EXECUTE_SUCCEED = 0;
    public static RootPerformer.OnExecuteListener ON_EXECUTE_LISTENER;
}
