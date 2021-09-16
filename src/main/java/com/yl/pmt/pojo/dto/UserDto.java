package com.yl.pmt.pojo.dto;

import lombok.Data;

/**
 * 用户实体类
 *
 * @author pch
 * @date 2021/9/16 3:03 下午
 */
@Data
public class UserDto extends BaseDto {

	/**
	 * 主键
	 */
	private Integer id;

	/**
	 * 用户ID
	 */
	private Integer userId;

	/**
	 * 部门（基础资料/服务质量...）
	 */
	private String dept;

	/**
	 * 地区（上海/深圳）
	 */
	private String area;

	/**
	 * 备注
	 */
	private String remark;

	/**
	 * 启用状态
	 */
	private String enable;

}