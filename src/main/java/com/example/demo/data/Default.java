
package com.example.demo.data;

import java.util.*;

public class Default {
    
    public static final ArrayList<RestaurantData> restaurant_list = new ArrayList<RestaurantData>();
    public static final ArrayList<MenuData> menu_list = new ArrayList<MenuData>();
    
    public static MenuData getMenuData(int rest_id, int menu_id) {
        for(MenuData menu : menu_list) {
            if(menu.restaurant_id==rest_id && menu.id==menu_id) {
                return menu;
            }
        }
        throw new RuntimeException("Not Search Element");
    }
    
    public static RestaurantData getRestDataById(int id) {
        for(RestaurantData rest : restaurant_list) {
            if(rest.id==id) {
                return rest;
            }
        }
        return null;
    }
    
    static {
        restaurant_list.add(new RestaurantData(1, "三郎/山之狼", "03-4258547", ""));
        restaurant_list.add(new RestaurantData(2, "野味", "03-4949139", ""));
        restaurant_list.add(new RestaurantData(3, "風味", "03-4207700", ""));
        
        menu_list.add(new MenuData(1, 1, "豬排", 85));
        menu_list.add(new MenuData(1, 2, "控肉", 85));
        menu_list.add(new MenuData(1, 3, "辣子雞丁", 85));
        menu_list.add(new MenuData(1, 4, "蔥爆肉絲", 85));
        
        menu_list.add(new MenuData(2, 1, "炭烤雞腿飯", 95));
        menu_list.add(new MenuData(2, 2, "野味特餐飯", 130));
        menu_list.add(new MenuData(2, 3, "炭烤雞排飯", 90));
        menu_list.add(new MenuData(2, 4, "炭烤燒肉飯", 75));
        
        menu_list.add(new MenuData(3, 1, "招牌飯包", 80));
        menu_list.add(new MenuData(3, 2, "黃金排骨菜單", 80));
        menu_list.add(new MenuData(3, 3, "鐵路懷舊排骨", 80));
        menu_list.add(new MenuData(3, 4, "鯖魚飯包", 85));
    }
    
}
