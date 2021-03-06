package com.yl.pmt.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yl.pmt.exception.BusinessException;
import com.yl.pmt.mapper.DemandMapper;
import com.yl.pmt.mapper.UserDetailMapper;
import com.yl.pmt.pojo.dto.UserDetailDto;
import com.yl.pmt.pojo.po.DemandPo;
import com.yl.pmt.pojo.po.UserDetailPo;
import com.yl.pmt.pojo.vo.UserDetailVo;
import com.yl.pmt.security.mapper.UserMapper;
import com.yl.pmt.security.mapper.UserRoleMapper;
import com.yl.pmt.security.pojo.User;
import com.yl.pmt.security.pojo.UserRole;
import com.yl.pmt.security.util.SecurityUtil;
import com.yl.pmt.service.IUserDetailService;
import com.yl.pmt.util.CnToPyUtil;
import com.yl.pmt.util.EntityConvertUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


/**
 * 用户信息
 *
 * @author pch
 * @date 2021/9/16 3:40 下午
 */
@Service("userService")
public class UserDetailService extends ServiceImpl<UserDetailMapper, UserDetailPo> implements IUserDetailService {

	@Autowired
	UserDetailMapper userDetailMapper;

	@Autowired
	DemandMapper demandMapper;

	@Autowired
	UserMapper userMapper;

	@Autowired
	UserRoleMapper userRoleMapper;

	/**
	 * 新增用户
	 *
	 * @param dto
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public String addUser(UserDetailDto dto) {
		// 校验用户数据
		UserDetailPo po = checkUserInfo(dto);
		String name = po.getName();
		// 生成用户编码
		String userCode = "pmt-" + UUID.randomUUID();
		// 保存操作
		po.setUserCode(userCode);
		po.setCreateUser(SecurityUtil.getAccount());
		po.setCreateTime(new Date());
		po.setModifyUser(SecurityUtil.getAccount());
		po.setModifyTime(new Date());
		int df = userDetailMapper.insert(po);
		User user = new User();
		String account = CnToPyUtil.getPingYin(name);
		// 账号重复性校验
		account = queryAccount(account);
		String password = CnToPyUtil.getPinYinHeadChar(name) + 123456;
		// 密码加密
		String scPassword = new BCryptPasswordEncoder().encode(password);
		user.setAccount(account);
		user.setPassword(scPassword);
		user.setUserCode(userCode);
		user.setStatus("NORMAL");
		int uf = userMapper.insert(user);

		// 用户角色表
		UserRole userRole = new UserRole();
		userRole.setUserCode(userCode);
		userRole.setRoleId(2L);
		int rf = userRoleMapper.insert(userRole);

		if (df == 0 || uf == 0 || rf == 0) {
			throw new BusinessException("新增失败！");
		}
		StringBuilder builder = new StringBuilder("新增成功，用户登陆账号为");
		builder.append(account);
		builder.append("，密码为");
		builder.append(password);
		return builder.toString();
	}

	/**
	 * 查询账号是否重复
	 *
	 * @param account
	 * @return
	 */
	private String queryAccount(String account) {
		QueryWrapper<User> queryWrapper = new QueryWrapper<>();
		queryWrapper.lambda().eq(User::getAccount, account);
		List<User> list = userMapper.selectList(queryWrapper);
		if (!CollectionUtils.isEmpty(list)) {
			if (account.length() > 50) {
				return account + "7";
			}
			account = account + "6";
			account = queryAccount(account);
		}
		return account;
	}

	/**
	 * 查询用户
	 *
	 * @return
	 */
	@Override
	public List<UserDetailPo> listUsers() {
		QueryWrapper<UserDetailPo> queryWrapper = new QueryWrapper<>();
		queryWrapper.lambda().eq(UserDetailPo::getLogicState, "Y");
		return this.list(queryWrapper);
	}

	/**
	 * 删除用户
	 *
	 * @param ids
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void delUsers(List<Integer> ids) {
		Optional.ofNullable(ids).orElseThrow(() -> new BusinessException("传入数据为空！"));
		List<String> userCodes = userDetailMapper.listUserCodes(ids);
		QueryWrapper<DemandPo> queryWrapper = new QueryWrapper<>();
		queryWrapper.lambda().eq(DemandPo::getLogicState, "Y").in(DemandPo::getUserCode, userCodes);
		Integer count = demandMapper.selectCount(queryWrapper);
		if (count > 0) {
			throw new BusinessException("待删除用户中有关联需求，请先删除需求再对人员进行删除！");
		}
		// 禁用账户
		UpdateWrapper<User> updateUser = new UpdateWrapper<>();
		updateUser.lambda().set(User::getStatus, "PROHIBIT")
				.in(User::getUserCode, userCodes);
		int ru = userMapper.update(null, updateUser);
		UpdateWrapper<UserDetailPo> updateUserDetail = new UpdateWrapper<>();
		updateUserDetail.lambda().set(UserDetailPo::getLogicState, "N")
				.set(UserDetailPo::getModifyUser, SecurityUtil.getUserName())
				.set(UserDetailPo::getModifyTime, new Date())
				.in(UserDetailPo::getId, ids);
		// 移除账户
		int rd = userDetailMapper.update(null, updateUserDetail);
		if (ru == 0 && rd == 0) {
			throw new BusinessException("删除失败！");
		}
	}

	/**
	 * 获取用户信息
	 *
	 * @return
	 */
	@Override
	public UserDetailVo getUserInfo() {
		String userCode = SecurityUtil.getUserCode();
		QueryWrapper<UserDetailPo> queryWrapper = new QueryWrapper<>();
		queryWrapper.lambda().eq(UserDetailPo::getUserCode, userCode);
		List<UserDetailPo> pos = userDetailMapper.selectList(queryWrapper);
		Optional.ofNullable(pos).orElseThrow(() -> new BusinessException("用户数据为空！"));
		UserDetailVo vo = (UserDetailVo) EntityConvertUtil.convert(pos.get(0), "Vo");
		return vo;
	}

	/**
	 * 修改用户信息
	 *
	 * @param dto
	 */
	@Override
	public void edit(UserDetailDto dto) {
		// 校验用户信息
		UserDetailPo po = checkUserInfo(dto);
		po.setModifyUser(SecurityUtil.getAccount());
		po.setModifyTime(new Date());
		userDetailMapper.updateById(po);
	}

	/**
	 * 用户信息校验
	 *
	 * @param dto
	 * @return
	 */
	UserDetailPo checkUserInfo(UserDetailDto dto) {
		// 非空判断
		Optional.ofNullable(dto).orElseThrow(() -> new BusinessException("用户信息不能为空！"));
		UserDetailPo po = (UserDetailPo) EntityConvertUtil.convert(dto, "Po");
		// 姓名重复判断
		String name = Optional.ofNullable(po.getName()).orElseThrow(() -> new BusinessException("用户名不能为空！"));
		QueryWrapper<UserDetailPo> queryWrapper = new QueryWrapper<>();
		queryWrapper.lambda().eq(UserDetailPo::getName, name).eq(UserDetailPo::getLogicState, "Y");
		List<UserDetailPo> pos = this.list(queryWrapper);
		if (!CollectionUtils.isEmpty(pos)) {
			throw new BusinessException("用户名已存在！");
		}
		return po;
	}
}
