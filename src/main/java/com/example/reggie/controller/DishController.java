package com.example.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.reggie.common.R;
import com.example.reggie.dto.DishDto;
import com.example.reggie.model.Category;
import com.example.reggie.model.Dish;
import com.example.reggie.model.DishFlavor;
import com.example.reggie.service.CategoryService;
import com.example.reggie.service.DishFlavorService;
import com.example.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
// 对菜品的操作
@Slf4j
@RestController
@RequestMapping("/dish")
public class DishController {
    /**
     * 对用户端的菜品按照分类进行缓存处理
     * 缓存的关键字为 key = "dish_{CategoryId}_{Status}"
     * 每当设计对数据库的增加、修改、删除的时候就要删除缓存
     * 没有查询到时在数据库中查询，然后加入缓存
     */
    private final DishService dishService;
    private final CategoryService categoryService;
    private final DishFlavorService dishFlavorService;
    private final RedisTemplate<Object, Object> redisTemplate;

    public DishController(DishService dishService, CategoryService categoryService, DishFlavorService dishFlavorService, RedisTemplate<Object, Object> redisTemplate) {
        this.dishService = dishService;
        this.categoryService = categoryService;
        this.dishFlavorService = dishFlavorService;
        this.redisTemplate = redisTemplate;
    }

    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto){
        // 这里方法重写：菜品需要绑定口味等信息
        dishService.saveWithFlavor(dishDto);
        // 新增后删除缓存，只需要删除当前分类的缓存
        String key = "dish_" + dishDto.getCategoryId() + "_" + dishDto.getStatus();
        redisTemplate.delete(key);
        return R.success("新增菜品成功");
    }


    @GetMapping("/page")
    public R<Page<DishDto>> page(int page, int pageSize, String name){

        //构造分页构造器对象
        Page<Dish> pageInfo = new Page<>(page,pageSize);
        Page<DishDto> dishDtoPage = new Page<>();
        //条件构造器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        //添加过滤条件
        queryWrapper.like(name != null,Dish::getName,name);
        //添加排序条件
        queryWrapper.orderByDesc(Dish::getUpdateTime);
        //执行分页查询
        dishService.page(pageInfo,queryWrapper);
        //对象拷贝
        BeanUtils.copyProperties(pageInfo,dishDtoPage,"records");
        List<Dish> records = pageInfo.getRecords();
        List<DishDto> list = records.stream().map((item) -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item,dishDto);
            Long categoryId = item.getCategoryId();//分类id
            //根据id查询分类对象
            Category category = categoryService.getById(categoryId);
            if(category != null){
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }
            return dishDto;
        }).collect(Collectors.toList());
        dishDtoPage.setRecords(list);
        return R.success(dishDtoPage);
    }

    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id){
        // 方法重写：给菜品绑定口味信息
        DishDto dishDto = dishService.getByIdWithFlavor(id);
        return R.success(dishDto);
    }

    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto){
        // 方法重写：对口味的操作
        dishService.updateWithFlavor(dishDto);
        // 更新后删除缓存
        String key = "dish_" + dishDto.getCategoryId() + "_" + dishDto.getStatus();
        redisTemplate.delete(key);
        return R.success("新增菜品成功");
    }

    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish){
        // 按照分类设置key，这是用户页面展示
        String key = "dish_" + dish.getCategoryId() + "_" + dish.getStatus();
        // 先在缓存中查询
        List<DishDto> ans = (List<DishDto>) redisTemplate.opsForValue().get(key);
        // 可以在缓存中查找到
        if(ans!=null){
            return R.success(ans);
        }
        // 没有查找到就要在数据库中查找
        // 构造查询条件
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        // 如果传进来的分类id不为空，加入查找条件
        queryWrapper.eq(dish.getCategoryId() != null ,Dish::getCategoryId,dish.getCategoryId());
        //添加条件，查询状态为1（起售状态）的菜品
        queryWrapper.eq(Dish::getStatus,1);
        //添加排序条件
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        List<Dish> list = dishService.list(queryWrapper);
        List<DishDto> dishDtoList = list.stream().map((item) -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item,dishDto);
            Long categoryId = item.getCategoryId();//分类id
            //根据id查询分类对象
            Category category = categoryService.getById(categoryId);
            if(category != null){
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }
            //当前菜品的id
            Long dishId = item.getId();
            LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(DishFlavor::getDishId,dishId);
            //SQL:select * from dish_flavor where dish_id = ?
            List<DishFlavor> dishFlavorList = dishFlavorService.list(lambdaQueryWrapper);
            dishDto.setFlavors(dishFlavorList);
            return dishDto;
        }).collect(Collectors.toList());
        // 记得添加缓存
        redisTemplate.opsForValue().set(key,dishDtoList,60, TimeUnit.MINUTES);
        return R.success(dishDtoList);
    }

    @PostMapping ("/status/{toStatus}")
    public R<String> updateStatus(@RequestParam List<Long> ids, @PathVariable Integer toStatus){
        // 构造更新条件器
        UpdateWrapper<Dish> updateWrapper = new UpdateWrapper<>();
        // 将状态修改为前端传过来的参数
        updateWrapper.set("status", toStatus);
        // id在传进来的线性表中就修改
        updateWrapper.in("id",ids);
        dishService.update(updateWrapper);
        // 修改可能设计多个菜品，我们直接删除所有关于菜品的缓存
        Set<Object> keys = redisTemplate.keys("dish_*");
        assert keys != null;
        redisTemplate.delete(keys);
        return R.success("成功修改");
    }
    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids){
        // 方法重写：需要同时处理口味的信息
        dishService.removeDish(ids);
        // 删除可能设计多个菜品，我们直接删除所有关于菜品的缓存
        Set<Object> keys = redisTemplate.keys("dish_*");
        assert keys != null;
        redisTemplate.delete(keys);
        return R.success("删除成功");
    }
}
