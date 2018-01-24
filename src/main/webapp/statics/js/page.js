function Paging(pageSize, ali, btn) {
	this.ali = ali;
	this.btn = btn;
	this.page = 1; //定义一个当前页面的全局变量
	this.page_num = pageSize; //根据文章数和每页显示数，向上取整算出页码数
	this.drc = [this.page - 2, this.page - 1, this.page, this.page + 1, this.page + 2];
}
Paging.prototype = {
	init: function() {
		var self = this;
			
		//ali事件
		for(var i = 0, len = ali.length; i < len; i++) {
			//给ali添加innertext
			if(this.drc[i] > 0 && this.drc[i] <= this.page_num) {
				$(ali[i]).show();
				ali[i].innerText = this.drc[i];
			} else {
				$(ali[i]).hide();
			}
			ali[i].onclick = function() {
				var val = this.innerText;
				alert(val );
				if(val % 1 === 0) {
					self.page = parseInt(val);
				} else {
					alert('请单击正确的页码');
					return;
				}
				self.render();
			}
		}
		btn[0].onclick = function() {
			self.page--;
			self.render();
		}
		btn[1].onclick = function() {
			self.page++;
			self.render();
			
		}
	},
	render: function() {
		if(this.page <= 0) {
			alert('已经是列表的首页');
		} else if(this.page > this.page_num) {
			alert('已经是列表的最后一页');
		} else {
			this.drc = [this.page - 2, this.page - 1, this.page, this.page + 1, this.page + 2];
			for(var i = 0, len = ali.length; i < len; i++) {
				if(this.drc[i] <= 0 || this.drc[i] > this.page_num) {
					//ali[i].innerText = '*';
					$(ali[i]).hide();
				} else {
					$(ali[i]).show();
					ali[i].innerText = this.drc[i];
				}
			}
		}
	}
}