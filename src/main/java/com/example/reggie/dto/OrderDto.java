package com.example.reggie.dto;

import com.example.reggie.model.OrderDetail;
import com.example.reggie.model.Orders;
import lombok.Data;

import java.util.List;
@Data
public class OrderDto extends Orders {
//    private String userName;
//
//    private String phone;
//
//    private String address;
//
//    private String consignee;

    private List<OrderDetail> orderDetails;
}
