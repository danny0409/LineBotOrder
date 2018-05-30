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

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.linecorp.bot.model.event.Event;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.spring.boot.annotation.EventMapping;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;

//@SpringBootApplication
//@LineMessageHandler
public class LineBotTest {
    public static void mains(String[] args) {
        SpringApplication.run(LineBotTest.class, args);
    }

    //@EventMapping
    public TextMessage handleTextMessageEvent(MessageEvent<TextMessageContent> event) {
        System.out.println("event: " + event);
        TextMessage reply;
        switch(event.getMessage().getText()) {
            case "光頭哥哥": reply=new TextMessage("母湯ㄡ~"); break;
            default : reply=new TextMessage("吃屎拉~ㄍㄢˋ");
        }
        return reply;
    }

    //@EventMapping
    public void handleDefaultMessageEvent(Event event) {
        System.err.println("handleDefaultMessageEvent");
        System.out.println("event: " + event);
    }
}
