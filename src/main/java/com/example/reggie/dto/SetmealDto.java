package com.example.reggie.dto;

import com.example.reggie.model.Setmeal;
import com.example.reggie.model.SetmealDish;
import lombok.Data;

import java.util.List;

@Data
public class SetmealDto extends Setmeal {

    private List<SetmealDish> setmealDishes;
    private String categoryName;
}
