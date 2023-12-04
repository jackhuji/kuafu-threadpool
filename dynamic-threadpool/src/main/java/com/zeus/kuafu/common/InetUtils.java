package com.zeus.kuafu.common;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @ClassName InetUtils
 * @Description
 * @Author Jack.hu
 * @Date 2023/7/20
 **/


@Slf4j
public class InetUtils {

    private static String LOCAL_IP = StringUtils.EMPTY;

    public static String getLocalIp(){
        if (StringUtils.isBlank(LOCAL_IP)) {
            try {
                InetAddress localHost = InetAddress.getLocalHost();
                LOCAL_IP = localHost.getHostAddress();
            } catch (UnknownHostException e) {
                log.error("[动态线程池][error]InetUtils getLocalIp获取本机ip发生异常", e);
            }
        }

        return LOCAL_IP;
    }


}
