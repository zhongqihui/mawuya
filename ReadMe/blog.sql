create table article_info(
	sn int(11) auto_increment primary key comment '主键sn',
	category_sn int(11) not null default '0' comment '分类主键',
	read_num int(11) not null default '0' comment '阅读次数',
	review_num int(11) not null default '0' comment '评论次数',
	praise_num int(11) not null default '0' comment '赞次数',
	tease_num int(11) not null default '0' comment '踩次数',
	picture_url varchar(1000) comment '图片url，多张图片路径用分号隔开',
	a_title varchar(50) not null comment '文章标题',
	a_content text comment '文章内容',
	a_summary varchar(200) COMMENT  '文章概要',
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
	r_name varchar(40) comment '评论人的名字',
	r_content varchar(140) comment '评论的内容',
	r_date datetime comment '评论的时间'
)default charset=utf8 comment='评论信息表';


create table category_info (
	sn int(11) auto_increment primary key comment '分类表主键sn',
	c_name varchar(20) not null comment '分类名称'
)default charset=utf8 comment='文章分类表';


create table blog_info (
	blogger_name varchar(30)
)default charset=utf8 comment='博客信息表';

