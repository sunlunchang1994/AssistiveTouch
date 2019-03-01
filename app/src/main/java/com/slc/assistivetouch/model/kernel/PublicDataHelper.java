package com.slc.assistivetouch.model.kernel;

import com.slc.assistivetouch.model.XpLog;

/**
 * Created by achang on 2019/3/1.
 */

public class PublicDataHelper {
    private static Boolean isOxygenOsRomOrH2OsRom = false;

    public static void initPublicData() {
        if (Utils.isOxygenOsRom() || Utils.isH2OsRom()) {
            isOxygenOsRomOrH2OsRom = true;
        }
    }

    public static boolean isOxygenOsRomOrH2OsRom() {
        XpLog.log("PublicDataHelper：" + isOxygenOsRomOrH2OsRom.hashCode(), true);
        return isOxygenOsRomOrH2OsRom;
    }
}
