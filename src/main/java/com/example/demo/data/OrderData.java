
package com.example.demo.data;

import java.util.*;

public class OrderData {
    
    public final int id;
    public final Member owner;
    public final RestaurantData rest;
    public boolean is_close = false;
    public ArrayList<Member> members;
    public ArrayList<OrderItem> items;
    
    public OrderData(int id, Member owner, RestaurantData rest) {
        this.id = id;
        this.owner = owner;
        this.rest = rest;
        members = new ArrayList<>();
        items = new ArrayList<>();
    }
    
    public OrderItem getOrderItem(Member member, MenuData menu) {
        for(OrderItem item : items) {
            if(item.member==member && item.item==menu) {
                return item;
            }
        }
        return null;
    }
    
    public static final ArrayList<OrderData> order_list = new ArrayList<OrderData>();
    
    public static OrderData getOrderData(int id) {
        for(OrderData order : order_list) {
            if(order.id==id) {
                return order;
            }
        }
        throw new RuntimeException("No Such Element");
    }
    
    public static int counter = 0;
}
