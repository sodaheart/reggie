package com.example.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.reggie.model.Orders;

public interface OrdersService extends IService<Orders> {
    void submit(Orders orders);
}
