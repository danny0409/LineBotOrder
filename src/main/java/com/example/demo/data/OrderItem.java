
package com.example.demo.data;

public class OrderItem {
    public final Member member;
    public final MenuData item;
    public int amount;
    
    public OrderItem(Member member, MenuData item, int amount) {
        this.member = member;
        this.item = item;
        this.amount = amount;
    }
    
    
}
