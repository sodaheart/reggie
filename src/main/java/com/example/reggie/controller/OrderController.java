package com.example.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.reggie.common.BaseContext;
import com.example.reggie.common.R;
import com.example.reggie.dto.OrderDto;
import com.example.reggie.model.OrderDetail;
import com.example.reggie.model.Orders;
import com.example.reggie.service.OrderDetailService;
import com.example.reggie.service.OrdersService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/order")
@Slf4j
public class OrderController {
    private final OrdersService ordersService;
    private final OrderDetailService orderDetailService;

    public OrderController(OrdersService ordersService, OrderDetailService orderDetailService) {
        this.ordersService = ordersService;
        this.orderDetailService = orderDetailService;
    }

    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders){
        // 方法重写：有订单的细节
        ordersService.submit(orders);
        return R.success("下单成功");
    }
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, Long number,String beginTime,String endTime){
        //构造分页构造器对象
        Page<Orders> pageInfo = new Page<>(page,pageSize);
        Page<OrderDto> orderDtoPage = new Page<>();
        //条件构造器
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        // 时间查询条件
        queryWrapper.ge(beginTime!=null,Orders::getCheckoutTime,beginTime);
        queryWrapper.ne(endTime!=null,Orders::getCheckoutTime,endTime);
        // 订单号查询条件
        queryWrapper.eq(number != null,Orders::getNumber,number);
        // 添加排序条件
        queryWrapper.orderByDesc(Orders::getCheckoutTime);
        // 执行分页查询
        ordersService.page(pageInfo,queryWrapper);
        BeanUtils.copyProperties(pageInfo,orderDtoPage,"records");
        // 田间订单的细节
        List<Orders> records = pageInfo.getRecords();
        List<OrderDto> list = records.stream().map((item) -> {
            OrderDto orderDto = new OrderDto();
            // order拷贝到orderDto
            BeanUtils.copyProperties(item,orderDto);
            // 当前订单的id
            Long ordersId = item.getId();
            // 查询select * from order_detail where orderId = ?
            LambdaQueryWrapper<OrderDetail> queryWrapper1 = new LambdaQueryWrapper<>();
            queryWrapper1.eq(OrderDetail::getOrderId,ordersId);
            List<OrderDetail> orderDetail = orderDetailService.list(queryWrapper1);
            // 将列表插入
            orderDto.setOrderDetails(orderDetail);
            return orderDto;
        }).collect(Collectors.toList());
        orderDtoPage.setRecords(list);
        return R.success(orderDtoPage);
    }
    @PutMapping
    public R<String> endOrder(@RequestBody Orders orders){
        ordersService.updateById(orders);
        return R.success("派送成功");
    }

    @GetMapping("/userPage")
    public R<Page> userPage(int page, int pageSize){

        //构造分页构造器对象
        Page<Orders> pageInfo = new Page<>(page,pageSize);
        Page<OrderDto> orderDtoPage = new Page<>();
        //条件构造器
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        //添加过滤条件
//        queryWrapper.ge(beginTime!=null,Orders::getCheckoutTime,beginTime);
//        queryWrapper.ne(endTime!=null,Orders::getCheckoutTime,endTime);
//        queryWrapper.eq(number != null,Orders::getNumber,number);
        queryWrapper.eq(Orders::getUserId, BaseContext.getCurrentId());
        //添加排序条件
        queryWrapper.orderByDesc(Orders::getCheckoutTime);
        //执行分页查询
        ordersService.page(pageInfo,queryWrapper);
        List<Orders> records = pageInfo.getRecords();
        List<OrderDto> list = records.stream().map((item) -> {
            OrderDto orderDto = new OrderDto();
            BeanUtils.copyProperties(item,orderDto);
            Long ordersId = item.getId();
            LambdaQueryWrapper<OrderDetail> queryWrapper1 = new LambdaQueryWrapper<>();
            queryWrapper1.eq(OrderDetail::getOrderId,ordersId);
            List<OrderDetail> orderDetail = orderDetailService.list(queryWrapper1);
            orderDto.setOrderDetails(orderDetail);
            return orderDto;
        }).collect(Collectors.toList());
        orderDtoPage.setRecords(list);
        return R.success(pageInfo);
    }
}
