package com.example.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.reggie.common.R;
import com.example.reggie.dto.SetmealDto;
import com.example.reggie.model.Category;
import com.example.reggie.model.Setmeal;
import com.example.reggie.service.CategoryService;
import com.example.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("setmeal")
public class SetmealController {

    /**
     * 开启Spring框架提供的缓存
     * 用户端查--命中缓存或者添加缓存
     * 后台对菜品操作时，删除缓存
     */
    private final CategoryService categoryService;
    private final SetmealService setmealService;

    public SetmealController(SetmealService setmealService, CategoryService categoryService) {
        this.setmealService = setmealService;
        this.categoryService = categoryService;
    }

    @PostMapping
    @CacheEvict(value = "setmealCache", allEntries = true) //删除所有缓存
    public R<String> save(@RequestBody SetmealDto setmealDto){
        // 方法重写
        setmealService.saveWithDish(setmealDto);
        return R.success("新增套餐成功");
    }

    @GetMapping("/page")
    public R<Page<SetmealDto>> page(int page, int pageSize, String name){

        Page<Setmeal> pageInfo = new Page<>(page,pageSize);

        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        // 传入名字就迷糊查询
        queryWrapper.like(name!=null,Setmeal::getName,name);
        // 根据时间降序排列
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        // 查询
        setmealService.page(pageInfo,queryWrapper);

        Page<SetmealDto> pageDtoInfo = new Page<>();

        BeanUtils.copyProperties(pageInfo,pageDtoInfo,"records");
        // 返回前端的page
        List<Setmeal> records = pageInfo.getRecords();
        // 补充分类的名字
        List<SetmealDto> list = records.stream().map((item)->{

            SetmealDto setmealDto = new SetmealDto();
            // 拷贝
            BeanUtils.copyProperties(item,setmealDto);
            // 得到当前分类的id
            Long id = item.getCategoryId();
            // 查询分类信息
            Category category = categoryService.getById(id);

            if(category!=null){
                String nameTmp = category.getName();
                setmealDto.setCategoryName(nameTmp);
            }

            return setmealDto;

        }).collect(Collectors.toList());

        pageDtoInfo.setRecords(list);

        return R.success(pageDtoInfo);
    }

    @DeleteMapping
    @CacheEvict(value = "setmealCache", allEntries = true) //删除所有缓存
    public R<String> delete(@RequestParam List<Long> ids){
        setmealService.removeWithDish(ids);
        return R.success("删除套餐成功");
    }
    @GetMapping("/list")
    @Cacheable(value = "setmealCache", key = "#setmeal.categoryId+'_'+#setmeal.status") // 加入缓存
    public R<List<Setmeal>> list(Setmeal setmeal){

        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        // 分类不为空
        queryWrapper.eq(setmeal.getCategoryId() != null,Setmeal::getCategoryId,setmeal.getCategoryId());
        // 状态
        queryWrapper.eq(setmeal.getStatus() != null,Setmeal::getStatus,setmeal.getStatus());
        // 时间降序
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);

        List<Setmeal> list = setmealService.list(queryWrapper);

        return R.success(list);
    }


    @GetMapping("/{id}")
    public R<SetmealDto> get(@PathVariable Long id){
        SetmealDto setmealDto = setmealService.getByIdWithDish(id);
        return R.success(setmealDto);
    }
    @PostMapping ("/status/{toStatus}")
    @CacheEvict(value = "setmealCache", allEntries = true) //删除所有缓存
    public R<String> updateStatus(@RequestParam List<Long> ids, @PathVariable Integer toStatus){
        log.info("状态"+ toStatus);
        log.info(ids.toString());
        UpdateWrapper<Setmeal> updateWrapper = new UpdateWrapper<>();
        updateWrapper.set("status", toStatus);
        updateWrapper.in("id",ids);
        setmealService.update(updateWrapper);
        if(toStatus ==0){
            return R.success("成功停售");
        }else{
            return R.success("成功起售");
        }
    }
    @PutMapping
    @CacheEvict(value = "setmealCache", allEntries = true) //删除所有缓存
    public R<String> update(@RequestBody SetmealDto setmealDto){
        setmealService.updateWithFlavor(setmealDto);
        return R.success("修改套餐成功");
    }
}
