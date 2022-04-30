package com.example.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.reggie.common.R;
import com.example.reggie.model.Employee;
import com.example.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;


@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {
    private final EmployeeService employeeService;
    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    // 后台登录
    @RequestMapping("/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee){
        // 页面密码进行md5加密
        String password= employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        // 根据username进行数据库查询
        LambdaQueryWrapper<Employee> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername,employee.getUsername());
        Employee emp=employeeService.getOne(queryWrapper);
        // 进行查询结果判定
        if(emp==null){
            return R.error("登录失败");
        }
        // 进行账号密码相等判定
        if(!password.equals(emp.getPassword())){
            return R.error("登录失败");
        }
        //查看员工的状态
        if(emp.getStatus()==0){
            return R.error("账号禁用");
        }
        request.getSession().setAttribute("employee",emp.getId());
        return R.success(emp);
    }

    // 后台登出
    @RequestMapping("/logout")
    public R<String> logout(HttpServletRequest request){
        // 清理Session
        request.getSession().removeAttribute("employee");
        return R.success("成功");
    }

    @PostMapping("")
    public R<String> save(@RequestBody Employee employee){
        // 对明文密码进行md5加密
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
        // 对于createTime、updateTime等由 MetaObjectHandler 统一自动处理
        employeeService.save(employee);
        return R.success("新增成功");
    }

    // 后台查找页面
    @GetMapping("/page")
    public R<Page<Employee>> page(int page, int pageSize, String name){
        // 构造分页器 page是第几页，pageSize是一页多少个
        Page<Employee> pageInfo = new Page<>(page,pageSize);
        // 构造条件
        LambdaQueryWrapper<Employee> wrapper=new LambdaQueryWrapper<>();
        // 名字为空时，全局查找；不为空时按照名字相似查询
        wrapper.like(StringUtils.isNotEmpty(name),Employee::getName,name);
        // 按照更新时间进行降序排列
        wrapper.orderByDesc(Employee::getUpdateTime);
        // 启动查询
        employeeService.page(pageInfo,wrapper);
        return  R.success(pageInfo);
    }

    // 修改员工信息
    @PutMapping
    public R<String> update(@RequestBody Employee employee){
        employeeService.updateById(employee);
        return R.success("员工信息修改成功");
    }

    // 查看某个员工具体信息
    @GetMapping("/{id}")
    public R<Employee> getById(@PathVariable Long id){
        Employee employee = employeeService.getById(id);
        if(employee!=null){
            return R.success(employee);
        }
        return R.error("为查询到相关员工信息");
    }
}
