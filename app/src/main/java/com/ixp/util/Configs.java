package com.ixp.util;

/**
 * Created by ixp on 11/6/16.
 */

public class Configs {

    public static final boolean DEBUG = true;

    public static final String HOST_URL =  "http://47.94.138.223:80";

    public static final String LOGIN_URL = HOST_URL + "/interface/login";

    public static final String ADD_USER_URL = HOST_URL + "/interface/addUserInfo";

    public static final String ADD_MACHINE_URL = HOST_URL + "/interface/addMachineInfo";

    public static final String MACHINE_LIST_URL = HOST_URL + "/interface/getMachineList/";

    public static final String MACHINE_INFO_URL = HOST_URL + "/interface/getMachineInfo/";

    public static final String AD_LIST_URL = HOST_URL + "/interface/getAdvertiseListByCity/";

    public static final String START_PAGE_URL = HOST_URL + "/interface/getStartPageListByCity/";

    public static final String SET_MACHINE_STATE = HOST_URL + "/interface/setRelaySwitch/";

    public static final String DELETE_MACHINE_URL = HOST_URL + "/interface/deleteMachineInfo/";

    public static final String SET_MACHINE_NAME = HOST_URL + "/interface/setMechineName/";

    public static UserInfo userInfo = new UserInfo();
}
