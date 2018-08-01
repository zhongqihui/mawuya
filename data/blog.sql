create table article_info(
	sn int(11) auto_increment primary key comment '主键sn',
	category_sn int(11) not null default '0' comment '分类主键',
	read_num int(11) not null default '0' comment '阅读次数',
	review_num int(11) not null default '0' comment '评论次数',
	praise_num int(11) not null default '0' comment '赞次数',
	tease_num int(11) not null default '0' comment '踩次数',
	picture_url varchar(1000) comment '图片url，多张图片路径用分号隔开',
	article_title varchar(50) not null comment '文章标题',
	article_content text comment '文章内容',
	article_summary varchar(200) COMMENT  '文章概要',
	insert_time datetime comment '插入时间',
	update_time datetime comment '修改时间'
) default charset=utf8 comment='文章信息表';


create table review_info (
	sn int(11) auto_increment primary key comment '主键sn',
	article_sn int(11) not null comment '评论文章的主键sn',
	psn int(11) not null default '0' comment '该评论的父节点sn',
	csn int(11) comment '该评论的子节点',
	praise_num int(11) not null default '0' comment '赞次数',
	tease_num int(11) not null default '0' comment '踩次数',
	review_name varchar(40) comment '评论人的名字',
	review_content varchar(140) comment '评论的内容',
	review_date datetime comment '评论的时间'
)default charset=utf8 comment='评论信息表';


create table category_info (
	sn int(11) auto_increment primary key comment '分类表主键sn',
	category_name varchar(20) not null comment '分类名称'
)default charset=utf8 comment='文章分类表';


create table blog_info (
	blogger_name varchar(30)
)default charset=utf8 comment='博客信息表';

create table log_info(
	sn int(11) auto_increment primary key comment '主键sn',
	ip_addr VARCHAR(50) comment '访客ip地址',
	country VARCHAR(50) comment '访客国家',
	province VARCHAR(50) comment '访客省份',
	city VARCHAR(50) comment '访客城市',
	area VARCHAR(50) comment '区/县',
	detail_position varchar(100) comment '详细地理位置',
	isp varchar(50) comment '访客ip运营商',
	try_times int(1) not null DEFAULT '0' comment '推送次数',
	req_time varchar(50) COMMENT  '访客访问时间',
	resp_time VARCHAR(50) COMMENT '响应访客时间',
	consume_time VARCHAR(30) COMMENT '该次请求消耗时间',
	req_url VARCHAR(200) comment '请求路径',
	req_method VARCHAR(10) COMMENT '请求方式，post，get...',
	params VARCHAR(500) COMMENT '请求参数',
	browser VARCHAR(100) COMMENT '访客浏览器信息',
	resp_status VARCHAR(1) COMMENT '响应状态，0表示成功；1表示失败，如抛异常响应500等',
	except_message VARCHAR(1000) comment '报异常错误信息'
) default charset=utf8 comment='访客记录表';