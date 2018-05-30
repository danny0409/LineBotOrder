
package com.example.demo.data;

import java.util.ArrayList;

public class Member {
    
    public final String line_id;
    public String line_name;
    public OrderData order=null;
    public boolean is_owner = false;
    
    public Member(String line_id, String line_name) {
        this.line_id = line_id;
        this.line_name = line_name;
    }
    
    public static ArrayList<Member> member_list = new ArrayList<Member>();
    
    public static Member getMemberById(String line_id) {
        for(Member member : member_list) {
            if(member.line_id.equals(line_id)) {
                return member;
            }
        }
        return null;
    }
    
    public int command_use_counts = 0;
    
}
