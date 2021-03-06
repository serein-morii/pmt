package com.yl.pmt.security.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yl.pmt.security.mapper.UserMapper;
import com.yl.pmt.security.pojo.Menu;
import com.yl.pmt.security.pojo.Role;
import com.yl.pmt.security.pojo.User;
import com.yl.pmt.security.service.IUserService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Description 系统用户业务实现
 * @Author pch
 * @CreateTime 2020/9/14 15:57
 */
@Service("sysUserService")
public class UserService extends ServiceImpl<UserMapper, User> implements IUserService {

	/**
	 * 根据用户名查询实体
	 *
	 * @param account
	 * @return
	 */
	@Override
	public User selectUserByAccount(String account) {
		QueryWrapper<User> queryWrapper = new QueryWrapper<>();
		queryWrapper.lambda().eq(User::getAccount, account);
		return this.baseMapper.selectOne(queryWrapper);
	}

	/**
	 * 通过用户ID查询角色集合
	 *
	 * @param userCode
	 * @return
	 */
	@Override
	public List<Role> selectSysRoleByUserCode(String userCode) {
		return this.baseMapper.selectSysRoleByUserCode(userCode);
	}

	/**
	 * 根据用户ID查询权限集合
	 *
	 * @param userCode
	 * @return
	 */
	@Override
	public List<Menu> selectSysMenuByUserCode(String userCode) {
		return this.baseMapper.selectSysMenuByUserCode(userCode);
	}
}
