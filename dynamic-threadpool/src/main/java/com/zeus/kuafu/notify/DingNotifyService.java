package com.zeus.kuafu.notify;

import com.alibaba.fastjson.JSON;
import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiRobotSendRequest;
import com.dingtalk.api.response.OapiRobotSendResponse;
import com.zeus.kuafu.common.DynamicProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @ClassName DingNotifyService
 * @Description
 * @Author Jack.hu
 * @Date 2023/6/15
 **/

@Slf4j
public class DingNotifyService {

    private DynamicProperties dynamicProperties;

    public DingNotifyService(DynamicProperties dynamicProperties) {
        this.dynamicProperties = dynamicProperties;
    }

    /**
     * 发送消息
     * @param title
     * @param content
     * @return
     */
    public static Long sendMsg(String title, String content){
        return sendMsg(title, content, null);
    }

    /**
     * 发送消息，@人  目前@需要调试
     * @param title
     * @param content
     * @param atUser
     * @return
     */
    public static Long sendMsg(String title, String content, List<String> atUser){

        try {
            log.info("[动态线程池]sendMsg 收到参数title={},content={}", JSON.toJSONString(title), JSON.toJSONString(content));
//            DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/robot/send?access_token=f83bca018632bc05d9bb9edd3d662bafbb6a22241264d494d39ea2bc3a5f27a3");
            DingTalkClient client = new DefaultDingTalkClient("");
            OapiRobotSendRequest req = new OapiRobotSendRequest();
            // 消息格式
            req.setMsgtype("markdown");
            OapiRobotSendRequest.Markdown markdown = new OapiRobotSendRequest.Markdown();
            markdown.setText(content);
            markdown.setTitle(title);
            req.setMarkdown(markdown);

            // 艾特的人
            if (!CollectionUtils.isEmpty(atUser)) {
                OapiRobotSendRequest.At at = new OapiRobotSendRequest.At();
                at.setAtMobiles(atUser);
                at.setIsAtAll(true);
                req.setAt(at);

            }

            OapiRobotSendResponse rsp = client.execute(req);
            log.info("[动态线程池]sendMsg rsp={}", JSON.toJSONString(rsp));
            return rsp.getErrcode();
        } catch (Exception e) {
            log.error("[动态线程池][error]dingding推送失败", e);
        }

        return -1L;
    }

    // public static void main(String[] args) {
    //     List<String> user = new ArrayList<>();
    //     user.add("17810656080");
    //     sendMsg("下班下班","宽明下班了，哈哈哈", user);
    // }



}
