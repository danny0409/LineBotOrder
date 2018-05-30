/*
 * Copyright 2018 LINE Corporation
 *
 * LINE Corporation licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.example.demo;


import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import com.example.demo.data.*;

import com.linecorp.bot.model.message.template.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.action.MessageAction;
import com.linecorp.bot.model.event.BeaconEvent;
import com.linecorp.bot.model.event.Event;
import com.linecorp.bot.model.event.FollowEvent;
import com.linecorp.bot.model.event.JoinEvent;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.PostbackEvent;
import com.linecorp.bot.model.event.UnfollowEvent;
import com.linecorp.bot.model.event.message.StickerMessageContent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.event.postback.PostbackContent;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TemplateMessage;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.response.BotApiResponse;
import com.linecorp.bot.spring.boot.annotation.EventMapping;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@LineMessageHandler
public class OrderController {
    @Autowired
    private LineMessagingClient lineMessagingClient;

    @EventMapping
    public void handleTextMessageEvent(MessageEvent<TextMessageContent> event) throws Exception {
        TextMessageContent message = event.getMessage();
        handleTextContent(event.getReplyToken(), event, message, event.getSource().getUserId());
    }
    
    @EventMapping
    public void handleUnfollowEvent(UnfollowEvent event) {
        log.info("unfollowed this bot: {}", event);
    }
    
    @EventMapping
    public void handleFollowEvent(FollowEvent event) {
        String replyToken = event.getReplyToken();
        log.info("e 取得 Follow 事件 UserID: {}", event.getSource().getUserId());
        String line_id = event.getSource().getUserId();
        func.getMember(line_id);
        this.replyText(replyToken, "歡迎加入訂便當 ~\n請輸入 選單 或 menu");
    }
    
    @EventMapping
    public void handleJoinEvent(JoinEvent event) {
        String replyToken = event.getReplyToken();
        log.info("e 取得 Join 事件 UserID: {}", event.getSource().getUserId());
        String line_id = event.getSource().getUserId();
        func.getMember(line_id);
        this.replyText(replyToken, "歡迎加入訂便當 ~\n請輸入 選單 或 menu");
    }
    
    public void handleJoinOrFollow(Event event) {
        event.getSource().getUserId();
    }
    
    public void reply(@NonNull String replyToken, @NonNull Message message) {
        reply(replyToken, Collections.singletonList(message));
    }

    public void reply(@NonNull String replyToken, @NonNull List<Message> messages) {
        try {
            BotApiResponse apiResponse = lineMessagingClient
                    .replyMessage(new ReplyMessage(replyToken, messages))
                    .get();
            log.info("Sent messages: {}", apiResponse);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
    
    public void replyText(@NonNull String replyToken, @NonNull String message) {
        if (replyToken.isEmpty()) {
            throw new IllegalArgumentException("replyToken must not be empty");
        }
        if (message.length() > 1000) {
            message = message.substring(0, 1000 - 2) + "……";
        }
        this.reply(replyToken, new TextMessage(message));
    }
    
    public int test_number=0;
    public OrderFunction func = new OrderFunction(this);
    
    @EventMapping
    public void handlePostbackEvent(PostbackEvent event) {
        String replyToken = event.getReplyToken();
        String line_id = event.getSource().getUserId();
        String data="", param="";
        PostbackContent content = event.getPostbackContent();
        if( content != null ) {
            if(content.getData()!=null) data=content.getData();
            Map<String,String> params = content.getParams();
            if(params!=null) {
                for(Map.Entry<String,String> entry : params.entrySet()) {
                    param = param + entry.getValue() + " ";
                }
            } //else {
                //log.info("params is null: userId:{}", event.getSource().getUserId());
            //}
            //
            String strings[] = data.split("\\s+");
            switch(strings[0]) {
                case "UseFunction" : {
                    func.parseFunction(strings[1], event, replyToken, line_id);
                    break;
                }
                case "ListMenu" : {
                    int id = Integer.parseInt(strings[1]);
                    String ret = func.getMenuByRestId(id);
                    if(ret.equals("")) {
                        
                    } else {
                        this.replyText(replyToken, func.getMenuByRestId(id));
                    }
                    break;
                }
                case "NewOrder" : {
                    func.newOrder(strings[1], event, replyToken, line_id);
                }
                default : {
                    log.warn("postback content is wrong: userId:{}", line_id);
                }
            }
        } else {
            log.warn("postback content is null: userId:{}", event.getSource().getUserId());
        }
    }
    
    private void handleTextContent(String replyToken, Event event, TextMessageContent content, String line_id) 
            throws Exception {
        String text = content.getText();
        log.info("Got text message from {}: {}", replyToken, text);
        String cmd[] = text.split("\\s+");
        switch (cmd[0]) {
            case "menu" :
            case "選單" : {
                this.reply(replyToken, Arrays.asList(new TextMessage("若無法顯示圖形選單，可輸入 文字選單 或 menu_text"), 
                            func.getHomeMenu())  );
                break;
            }
            case "menu_text" :
            case "文字選單" : {
                this.replyText(replyToken, func.getHomeMemuText());
                break;
            }
            case "UseFunction" : {
                func.parseFunction(cmd[1], event, replyToken, line_id);
                break;
            }
            case "ListMenu" : {
                try {
                    int id = Integer.parseInt(cmd[1]);
                    String ret = func.getMenuByRestId(id);
                    if(ret.equals("")) throw new RuntimeException("No Such Element");
                    this.replyText(replyToken, func.getMenuByRestId(id));
                } catch(RuntimeException r) {
                    this.replyText(replyToken, "錯誤，沒有這間餐廳！");
                }
                break;
            }
            case "NewOrder" : {
                func.newOrder(cmd[1], event, replyToken, line_id);
                break;
            }
            case "order" : 
            case "Order" : {
                if(cmd.length==1) {
                    func.parseFunction(cmd[0], event, replyToken, line_id);
                } else {
                    func.orderFunct(cmd, event, replyToken, line_id);
                }
                break;
            }
            case "Join" : {
                try {
                    func.joinOrder(cmd[1], replyToken, line_id);
                } catch(RuntimeException r) {
                    this.replyText(replyToken, "錯誤，指令格式或訂單編號錯誤！\n" + r.getMessage());
                }
            }
            default: {
                log.info("e 回覆不支援訊息 token{}: {}", replyToken, text);
                this.replyText(replyToken, "錯誤，無效指令！\n\n" + text);
                break;
            }
        }
    }
    
    public void getLineNameSet(AtomicBoolean run, Member member, String str) {
        member.line_name = str;
        run.set(false);
    }
    
    public String getLineName(String userId) {
        AtomicBoolean run = new AtomicBoolean(true);
        Member member = new Member(userId, "");
        lineMessagingClient
                            .getProfile(userId)
                            .whenComplete((profile, throwable) -> {
                                if (throwable != null) {
                                    System.err.println(throwable.getMessage());
                                    return;
                                }
                                getLineNameSet(run, member, profile.getDisplayName());
                            });
        while(run.get()) {}
        return member.line_name;
    }
    
    @EventMapping
    public void handleBeaconEvent(BeaconEvent event) {
        log.warn("Received Beacon Event event: {}", event);
    }

    @EventMapping
    public void handleOtherEvent(Event event) {
        log.warn("Received message(Ignored): {}", event);
    }
    
    @EventMapping
    public void handleDefaultMessageEvent(Event event) {
        log.warn("Received Default Message event: {}", event);
    }
    
    @EventMapping
    public void handleStickerMessageEvent(MessageEvent<StickerMessageContent> event) {
        String replyToken = event.getReplyToken();
        log.info("e回覆不支援 StickerMessage UserID:{}", event.getSource().getUserId());
        this.replyText(replyToken, "錯誤，無效指令！");
    }
    
    public void addLogInfo(String msg, Object... args) {
        log.info(msg, args);
    }
    
}
