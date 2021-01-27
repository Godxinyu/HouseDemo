package com.lxinyu.house.biz.mapper;

import com.lxinyu.house.common.model.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;


@Mapper
public interface UserMapper {

    public User selectUserWithEmail(String email);

    public List<User> selectUsers();

    public int insert(User account);

    public int delete(String email);

    public int updateUser(String email);
}
