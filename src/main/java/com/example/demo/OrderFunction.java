package com.example.demo;

import com.example.demo.data.*;

import java.util.*;

import com.linecorp.bot.model.action.*;
import com.linecorp.bot.model.event.*;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.message.TemplateMessage;
import com.linecorp.bot.model.message.template.*;

public class OrderFunction {

    public void parseFunction(String cmd, Event event, String replyToken, String line_id) {
        switch (cmd) {
            case "list_shop": {
                StringBuilder sb = new StringBuilder("");
                for(RestaurantData rest : Default.restaurant_list){
                    sb = sb.append(rest.id).append(". ").append(rest.name).append("\n");
                }
                controler.replyText(replyToken, sb.toString());
                break;
            }
            case "list_menu": {
                if (event instanceof PostbackEvent) {
                    controler.reply(replyToken, getRestList("ListMenu"));
                } else {
                    controler.replyText(replyToken, getRestListText("ListMenu"));
                }
                break;
            }
            case "join_order": {
                if(event instanceof PostbackEvent) {} else {}
                controler.replyText(replyToken, getOrderListText());
                break;
            }
            case "create_new_order": {
                if (event instanceof PostbackEvent) {
                    controler.reply(replyToken, getRestList("NewOrder"));
                } else {
                    controler.replyText(replyToken, getRestListText("NewOrder"));
                }
                break;
            }
            case "order" : 
            case "Order" : {
                Member member = getMember(line_id);
                if(event instanceof PostbackEvent) {} else {}
                if(member.is_owner) {
                    controler.replyText(replyToken, getMenuText(ORDER_OWNER_FUNCTION));
                } else if (member.order!=null) {
                    controler.replyText(replyToken, getMenuText(ORDER_FUNCTION));
                } else {
                    controler.replyText(replyToken, "錯誤，你還沒有新增或加入團購訂單！");
                }
                break;
            }
        }
    }
    
    public void orderFunct(String cmd[], Event event, String replyToken, String line_id) {
        Member member = getMember(line_id);
        if(member.order!=null && member.order.is_close) {
            member.order = null;
            controler.replyText(replyToken, "通知，此訂單已收單！");
        }
        try {
            switch(cmd[1]) {
                case "add" : {
                    if(cmd.length<3) {
                        controler.replyText(replyToken, "輸入\nOrder add [菜單編號] [數量]\n能新增，菜單編號可以從主選單查詢");
                    } else {
                        int rest_id = member.order.rest.id;
                        int menu_id = Integer.parseInt(cmd[2]);
                        int amount = Integer.parseInt(cmd[3]);
                        MenuData menu = Default.getMenuData(rest_id, menu_id);
                        if(amount<1) throw new RuntimeException("Order add amount error");
                        OrderData order = member.order;
                        OrderItem item = order.getOrderItem(member, menu);
                        if(item==null) {
                            item = new OrderItem(member, menu, amount);
                            order.items.add(item);
                        }
                        item.amount = amount;
                        controler.replyText(replyToken, "增加成功");
                    }
                    break;
                }
                case "del" : {
                    if(cmd.length<3) {
                        controler.replyText(replyToken, "輸入\nOrder del [菜單編號]\n能刪除，菜單編號可以從主選單查詢");
                    } else {
                        int rest_id = member.order.rest.id;
                        int menu_id = Integer.parseInt(cmd[2]);
                        MenuData menu = Default.getMenuData(rest_id, menu_id);
                        OrderData order = member.order;
                        OrderItem item = order.getOrderItem(member, menu);
                        if(item==null) {
                            controler.replyText(replyToken, "沒有這個項目");
                            return;
                        }
                        order.items.remove(item);
                        controler.replyText(replyToken, "刪除成功");
                    }
                    break;
                }
                case "close" :
                case "list" : {
                    OrderData order = member.order;
                    int total_amount=0, total_price=0;
                    StringBuilder sb = new StringBuilder("");
                    if(member.is_owner) {
                        if(cmd[1].equals("close")) {
                            order.is_close = true;
                            sb = sb.append("已收單！").append("\n\n");
                        }
                        for(OrderItem item : order.items) {
                            sb = sb.append("Name:").append(item.member.line_name).append(" 項目:").append(item.item.name)
                                    .append(" 單價:$").append(item.item.price).append(" 數量:")
                                    .append(item.amount).append(" 小計:$")
                                    .append(item.amount*item.item.price).append("\n");
                            total_amount += item.amount;
                            total_price += item.amount*item.item.price;
                        }
                        sb = sb.append("\n總數量:").append(total_amount).append("  總金額: $").append(total_price);
                    } else {
                        for(OrderItem item : order.items) {
                            if(member == item.member) {
                                sb = sb.append(" 項目:").append(item.item.name).append(" 單價:$")
                                        .append(item.item.price).append(" 數量:").append(item.amount)
                                        .append(" 小計:$").append(item.amount*item.item.price).append("\n");
                                total_amount += item.amount;
                                total_price += item.amount*item.item.price;
                            }
                        }
                        sb = sb.append("\n總數量:").append(total_amount).append("  總金額: $").append(total_price);
                    }
                    controler.replyText(replyToken, sb.toString());
                    break;
                }
                case "exit" : {
                    if(!member.is_owner) {
                        OrderData order = member.order;
                        for(int i=0;i<order.items.size();i++) {
                            if(order.items.get(i).member == member) {
                                order.items.remove(i);
                                i=0;
                            }
                        }
                        order.members.remove(member);
                        member.order = null;
                        controler.replyText(replyToken, "退出成功");
                    } else {
                        controler.replyText(replyToken, "錯誤，揪團者不得退出！");
                    }
                    break;
                }
                default : {
                    throw new RuntimeException("Order command not match");
                }
            }
        } catch(RuntimeException re) {
            re.printStackTrace();
            controler.replyText(replyToken, "錯誤，指令格式錯誤！");
        }
    }
    
    public void joinOrder(String str_id, String replyToken, String line_id) {
        int id = Integer.parseInt(str_id);
        OrderData order = OrderData.getOrderData(id);
        if(order.is_close) throw new RuntimeException();
        Member member = getMember(line_id);
        if(member.order!=null && member.order.is_close) member.order = null;
        if(member.order!=null) {
            controler.replyText(replyToken, "錯誤，你有訂單還沒收單或退出！");
            return;
        }
        order.members.add(member);
        member.order = order;
        controler.replyText(replyToken, "成功加入訂單，可以輸入 Order 開啟選單");
    }
    
    public String getOrderListText() {
        StringBuilder sb = new StringBuilder("");
        for (OrderData order : OrderData.order_list) {
            if (!order.is_close) {
                sb = sb.append(order.id).append(". ").append(order.rest.name).append("  ")
                        .append(order.owner.line_name).append("\n");
            }
        }
        String ret = sb.toString();
        if(ret.equals("")) {
            ret="目前沒有任何進行中的訂單！";
        } else {
            ret += "\n 輸入 Join [訂單編號] 加入團購訂單";
        }
        return ret;
    }
    
    public String getMenuText(String[][] template) {
        StringBuilder sb = new StringBuilder("");
        for (String[] funct_str : template) {
            sb = sb.append("功能:").append(funct_str[0]).append("  命令:").append(funct_str[1]).append("\n");
        }
        sb = sb.append("\n").append("請輸入命令！").append("\n");
        return sb.toString();
    }
    
    public void newOrder(String cmd, Event event, String replyToken, String line_id) {
        Member member = getMember(line_id);
        if(member.order!=null && member.order.is_close) member.order = null;
        if(member.order!=null) {
            controler.replyText(replyToken, "錯誤，你有訂單還沒收單！");
            return;
        }
        try {
            int rest_id = Integer.parseInt(cmd);
            RestaurantData rest = Default.getRestDataById(rest_id);
            if(rest==null) throw new RuntimeException("No Such Element");
            OrderData order = new OrderData(OrderData.counter++, member, rest);
            OrderData.order_list.add(order);
            member.order = order;
            member.is_owner = true;
            controler.replyText(replyToken, "訂單建立成功，可以輸入 Order 開啟選單");
        } catch(RuntimeException r) {
            controler.replyText(replyToken, "錯誤，沒有這間餐廳！");
        }
        
    }
    
    public Member getMember(String line_id) {
        Member member = Member.getMemberById(line_id);
        if(member!=null) return member;
        member = new Member(line_id, controler.getLineName(line_id));
        Member.member_list.add(member);
        return member;
    }
    
    public String getMenuByRestId(int id) {
        StringBuilder sb = new StringBuilder("");
        for (MenuData menu : Default.menu_list) {
            if (menu.restaurant_id == id) {
                sb = sb.append(menu.name).append(",  $").append(menu.price).append("\n");
            }
        }
        return sb.toString();
    }

    public String getRestListText(String next_action) {
        String dataset[][] = genRestList(next_action);
        StringBuilder sb = new StringBuilder();
        for (String[] funct_str : dataset) {
            sb = sb.append("功能名稱:選擇-").append(funct_str[0]).append("  功能命令:").append(funct_str[1]).append("\n");
        }
        sb = sb.append("\n").append("請輸入功能命令！").append("\n");
        return sb.toString();
    }

    public TemplateMessage getRestList(String next_action) {
        ArrayList list = new ArrayList();
        for (RestaurantData rest : Default.restaurant_list) {
            list.add(new PostbackAction(rest.name, next_action + " " + rest.id));
        }
        String imageUrl = ComFunct.createUri("/static/bang.jpg");
        ButtonsTemplate bt = new ButtonsTemplate(
                imageUrl,
                "訂便當 Bot",
                "請選擇餐廳",
                list
        );
        return new TemplateMessage("Button alt text", bt);
    }

    public String[][] genRestList(String next_action) {
        String data[][] = new String[Default.restaurant_list.size()][];
        for (int i = 0; i < Default.restaurant_list.size(); i++) {
            data[i] = new String[2];
            data[i][0] = Default.restaurant_list.get(i).name;
            data[i][1] = next_action + " " + (i+1);
        }
        return data;
    }
    
    private static String get_home_memu_text_str = null;
    private static TemplateMessage get_home_memu_bt = null;

    public TemplateMessage getHomeMenu() {
        if (get_home_memu_bt != null) {
            return get_home_memu_bt;
        }
        String imageUrl = ComFunct.createUri("/static/bang.jpg");
        ButtonsTemplate bt = new ButtonsTemplate(
                imageUrl,
                "訂便當 Bot",
                "主選單",
                getHomeMenuList()
        );
        TemplateMessage templateMessage = new TemplateMessage("Button alt text", bt);
        get_home_memu_bt = templateMessage;
        return get_home_memu_bt;
    }

    public List getHomeMenuList() {
        ArrayList list = new ArrayList();
        for (String[] funct_str : HOME_FUNCTION_LIST) {
            list.add(new PostbackAction(funct_str[0], funct_str[1]));
        }
        return list;
    }

    public String getHomeMemuText() {
        if (get_home_memu_text_str != null) {
            return get_home_memu_text_str;
        }
        StringBuilder sb = new StringBuilder();
        for (String[] funct_str : HOME_FUNCTION_LIST) {
            sb = sb.append("功能:").append(funct_str[0]).append("  命令:").append(funct_str[1]).append("\n");
        }
        sb = sb.append("\n").append("請輸入命令！").append("\n");
        get_home_memu_text_str = sb.toString();
        return get_home_memu_text_str;
    }
    
    public static final String ORDER_OWNER_FUNCTION[][] = {
        {"增加項目", "Order add"},
        {"刪除項目", "Order del"},
        {"顯示訂單明細", "Order list"},
        {"收單", "Order close"}
    };
    
    public static final String ORDER_FUNCTION[][] = {
        {"增加項目", "Order add"},
        {"刪除項目", "Order del"},
        {"顯示訂單明細", "Order list"},
        {"退出", "Order exit"}
    };
    
    public static final String HOME_FUNCTION_LIST[][] = {
        {"列出店家", "UseFunction list_shop"},
        {"顯示菜單", "UseFunction list_menu"},
        {"加入團購", "UseFunction join_order"},
        {"開始新團購", "UseFunction create_new_order"}
    };

    private final OrderController controler;

    public OrderFunction(OrderController _controler) {
        controler = _controler;
    }
    
    private String getLineUserId(Event event) {
        String line_id;
        if (event instanceof PostbackEvent) {
            PostbackEvent post = (PostbackEvent)event;
            line_id = post.getSource().getUserId();
        } else {
            MessageEvent<TextMessageContent> mesg = (MessageEvent<TextMessageContent>)event;
            line_id = mesg.getSource().getUserId();
        }
        return line_id;
    }
    
}
