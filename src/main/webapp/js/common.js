

$(function() {
	resizeWrapAndSidebar();
    $(".a1").closest("li").hide();
    // seesion超时情况下ajax请求跳转到登陆页面
    $.ajaxSetup( {
		/*设置ajax请求结束后的执行动作*/
        complete :
            function(XMLHttpRequest, textStatus) {
				/*通过XMLHttpRequest取得响应头，sessionstatus*/
                var sessionstatus = XMLHttpRequest.getResponseHeader("sessionstatus");
                var status = XMLHttpRequest.status;
                if (sessionstatus == "TIMEOUT") {

                    parent.layer.open({
                        type: 2,
                        title: false,
                        offset: '120px',
                        area: ['600px', '500px'],
                        fixed: true, //固定
                        maxmin: false,
                        loading:1,
                        closeBtn: 0,
                        content: XMLHttpRequest.getResponseHeader("CONTEXTPATH")
                    });
                }
            }
    });

 	//sidebar和main对齐
//	var mainH = $("#main").height();
//	$("#sidebar").height(mainH);
 	
 	
	// 二级菜单显示
	$(".nav>ul>li").click(function(event) {
		$(this).find(".sub-nav").slideToggle().end().siblings().find(".sub-nav").hide();
		$(this).find("i").toggleClass("show").end().siblings().find("i").removeClass("show");
		$(this).children('a').addClass('cur').end().siblings().children('a').removeClass('cur');
	});
	$(".leave-help>div").click(function() {
		$(this).children('a').addClass('cur').end().siblings().children('a').removeClass('cur').end().parent().siblings('.nav').find('a').removeClass('cur');
	});
	$(".sub-nav>li").click(function(event) {
		$(this).parent(".sub-nav").show();
		$(this).children('a').addClass('cur').end().siblings().children('a').removeClass('cur');
		event.stopPropagation();
	});

	//首页产品包数量变化
	$(".product-pack .add").each(function(){
		$(this).click(function(){
			var ele = $(this).siblings("input[name='count']");
			var count = new Number(ele.attr("value"));
			count += 1;
			if(count > 1000000){
				return;
			}
			ele.attr("value",count);
			$(ele).val(count);
			
			//数量增加让其选中
			var td = $(this).parents(".product_row").children()[0];
			var spanCheck = $(td).children()[0];
			$(spanCheck).addClass('checked');
			
			//计算最后一列的价格
			var row = $(this).parents(".product_row");
			var quantity = parseFloat(row.children(".quantity").val());
			var productPrice = parseFloat(row.children(".productPrice").val());
			var productType = row.children(".productType").val();
			var totalPrice;
			if(productType == '2'){
				totalPrice = quantity*count*productPrice;
			}else{
				totalPrice = productPrice * count;
			}
			
			totalPrice = returnFloatFor2(totalPrice);
			row.children(".totalPrice").html(totalPrice);
			
			priceSum();
			
		})
		
	});
	
	$(".count").keyup(function(){
		  var val;
		  if(!isNaN(this.value)){
			  if(this.value < 1){
				  val = 1;
			  }else if(this.value > 1000000){
				  val = 1000000;
			  }else{
				  val = parseFloat(this.value);
			  }
		  }else{
			  val = 1; 
		  }
		  
		  //设置input框的value属性
		  $(this).attr("value", val);
		  //设置框的显示value
		  $(this).val(val);
 		  
		  
		//数量增加让其选中
		var td = $(this).parents(".product_row").children()[0];
		var spanCheck = $(td).children()[0];
		$(spanCheck).addClass('checked');
		
		//计算最后一列的价格
		var row = $(this).parents(".product_row");
		var quantity = parseFloat(row.children(".quantity").val());
		var productPrice = parseFloat(row.children(".productPrice").val());
		var productType = row.children(".productType").val();
		var totalPrice;
		if(productType == '2'){
			totalPrice = quantity*val*productPrice;
		}else{
			totalPrice = productPrice * val;
		}
		
		totalPrice = returnFloatFor2(totalPrice);
		row.children(".totalPrice").html(totalPrice);
		
		priceSum();
		  
		  
	});
	
	
	

	$(".product-pack .reduce").each(function(){
		$(this).click(function(){
			var ele = $(this).siblings("input[name='count']");
			var count = new Number(ele.attr("value"));
			if (count == 1) {
				return ;
			}
			count -= 1;
			ele.attr("value",count);
			$(ele).val(count);
			//数量减少让其选中
			
			var td = $(this).parents(".product_row").children()[0];
			var spanCheck = $(td).children()[0];
			if(count > 0){
				$(spanCheck).addClass('checked');
			}
			if(count == 0){
				$(spanCheck).removeClass('checked');
			}
			
			//计算最后一列的价格
			var row = $(this).parents(".product_row");
			var quantity = parseFloat(row.children(".quantity").val());
			var productPrice = parseFloat(row.children(".productPrice").val());
			var productType = row.children(".productType").val();
			var totalPrice;
			if(productType == '2'){
				totalPrice = quantity*count*productPrice;
			}else{
				totalPrice = productPrice * count;
			}
			
			totalPrice = returnFloatFor2(totalPrice);
			row.children(".totalPrice").html(totalPrice);
			
			priceSum();
		})

	});
	
	//计算产品的总价格
	function priceSum(){
		
		var spanList = $(".checked");
		var total = 0;
		for(var i=0;i<spanList.length;i++){
			var row = $(spanList[i]).parents(".product_row");
			if(row.length>0){
				var totalPrice = parseFloat(row.children(".totalPrice").html());
				total = total + totalPrice;
			}
		}
		
		total = returnFloatFor2(total);
		
		$('#price_sum').text(total);
		
	}
	
	
	$(".pagination .show-count .select").click(function() {
		$(this).children('ul').toggle();
	});
	$(".pagination .show-num li").click(function() {
		var ele = $(".pagination .show-count .select");
		var txt = $(this).text();
		//ele.text(txt);
		ele.find("span").text(txt);
		$("#pageSize").val(Number(txt));
		$("#pageRowCount").val(Number(txt));
		$(".pagination .show-num").fadeOut("slow");
		
		if(typeof(reloadFun) != "undefined" && typeof(reloadFun) == "function"){
			reloadFun();
		}

	});
	$(".pagination .show-num").mouseleave(function(){
		$(".pagination .show-num").fadeOut("slow");
	});

	// 表格内部
	$("table thead .check").click(function() {
		if ($(this).hasClass('checked')) {
			$("table tr .check").removeClass('checked');
			priceSum();
		} else {
			$("table tr .check").addClass('checked');
			priceSum();
		}
	})

	//选中和取消选中行的时候
	$("table tbody .check").click(function() {
		$(this).toggleClass('checked');
		priceSum();
	})
	
//	$("table tr .cancel").each(function() {
//		$(this).click(function() {
//			var html = '<div class="dialog-bg">' + 
//							'<div class="dialog">' +
//								'<img src="../img/dialog.png" alt="取消" />' +
//								'<h2>取消订单</h2>' +
//								'<p>确定取消该订单吗？</p>' +
//								'<div class="btn-wrap">' + 
//									'<a class="cancel" href="">取消</a>' +
//									'<a class="okay" href="javascript:;" onclick="cancelOrder()">确定</a>' +
//								'</div>' +
//							'</div>' +
//							'<div class="close"></div>' +
//						'</div>'
//			$(this).parents("tr").append(html);
//			$(this).children('.dialog-bg').click(function(event) {
//				$(this).parent("tr").hide();
//			});
//			$(".dialog-bg .close").click(function(){
//				$(".dialog-bg").hide();
//			});
//		})
//	})


	$(".login-con .for-password span").click(function() {
		$(this).children('i').toggleClass('selected');
		rememberAccount();
	})
	
	
	
	
	

	$(".order-option .state").click(function() {
		$(this).siblings('.state-list').toggle();
	})
	$(".order-option .state-list li").click(function(event) {
		var ele = $(".order-option .state");
		var txt = $(this).text();
		ele.text(txt);
		$(".order-option .state-list").hide();
		
		var liVal = $(this).val();
		$('#orderStatus').val(liVal);
	});

	$("#change-pass .close").click(function() {
		window.close();
	})
	
	
	if($('#orderStatus').length == 1){
		var status = $('#orderStatus').val();
		
		if(status != ""){
			if(status == 0){
				$('.state').html('待审核');
			}
			if(status == 1){
				$('.state').html('订单生效');
			}
			if(status == 2){
				$('.state').html('订单完成');
			}
			if(status == 3){
				$('.state').html('订单失败');
			}
			if(status == 4){
				$('.state').html('订单取消');
			}
		}
	}
	
	
})
		

// 弹出框-提交成功
function dialog(text) {
	var dialogHtml = 	'<div class="dialog-out"></div>' +
	'<div class="dialog-in">' +
	'<h4>提示</h4>' +
	'<div class="txt">' + text + '</div>' +
	'<div class="btn"><a href="">确定</a></div>' +
	'<span class="close"></span>' +
	'</div>'
	$("body").append(dialogHtml);
	$(".dialog-in .btn a,.dialog-in .close").click(function() {
		$(".dialog-out").hide();
		$(".dialog-in").hide();			
	})
} 
//提交失败
function dialogFail(text) {
	dialog(text)
	$(".dialog-in .txt").css('backgroundPosition', '0 -69px');
}
//提示前没有图标
function dialogNormal(text) {
	dialog(text)
	$(".dialog-in .txt").css('background', 'none');
}

//设置cookie
function setCookie(name,value) 
{ 
    var Days = 7; 
    var exp = new Date(); 
    exp.setTime(exp.getTime() + Days*24*60*60*1000); 
    document.cookie = name + "="+ escape (value) + ";expires=" + exp.toGMTString(); 
} 

//读取cookie
function getCookie(name) 
{ 
    var arr,reg=new RegExp("(^| )"+name+"=([^;]*)(;|$)");
 
    if(arr=document.cookie.match(reg))
 
        return unescape(arr[2]); 
    else 
        return null; 
} 

//删除cookies 
function delCookie(name) 
{ 
    var exp = new Date(); 
    exp.setTime(exp.getTime() - 1); 
    var cval=getCookie(name); 
    if(cval!=null){
    	document.cookie= name + "="+cval+";expires="+exp.toGMTString(); 
    } 
}


// 获取url参数
function getUrlParam(name) {
    var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
    var r = window.location.search.substr(1).match(reg);
    if (r != null)
        return unescape(r[2]);
    return null;
}

//计算同样保存两位小数
function returnFloatFor2(value){
	 var value=Math.round(parseFloat(value)*10000)/10000;
	 var xsd=value.toString().split(".");
	 
	 if(xsd.length==1){
		 value=value.toString()+".0000";
		 return value;
	 }
	 
	 if(xsd.length>1){
		 if(xsd[1].length<2){
		 	value=value.toString()+"000";
		 } else if(xsd[1].length<3){
             value=value.toString()+"00";
         } else if(xsd[1].length<4){
             value=value.toString()+"0";
         }
		return value;
	 }
}

//判断数字是否是整数
function isInteger(obj) {
	 return Math.floor(obj) === obj;
}

/**
 * 新窗口查看文件
 * 
 * @param img
 *            img标签
 */
function viewFile(img) {
	open($(img).attr("src"), "viewFile");
}

function resizeWrapAndSidebar(){
	var mainH = $("#main").height();
	var mainWindow = window.screen.availHeight;
	if(mainH<mainWindow){
		mainH=mainWindow;
	}
	$("#sidebar").height(mainH);
	$("#wrap").height(mainH); 
}

function ityzl_SHOW_LOAD_LAYER(msg){  
    return layer.msg('<div style="color:#506470;font-family: "microsoft yahei","Arial Narrow",HELVETICA;">'+msg+'</div>', {icon: 16,shade: [0.5, '#f5f5f5'],scrollbar: false,offset: 'auto',time: 600000}) ;  
}

function SHOW_LOAD_LAYER(msg){
    console.log("进入loading");
    return layer.msg('<div style="color:#506470;font-family: "microsoft yahei","Arial Narrow",HELVETICA;">'+msg+'</div>', {icon: 16,shade: [0.5, '#f5f5f5'],scrollbar: false,offset: 'auto',time: 30000}) ;
}

function isSessionValid(){
	var url = "/isSessionValid";
	var valid = false;
    $.ajax({
        type: "post",
		async:false,
		url:url,
		dataType:'json',
		success:function (data) {
			if(data.isSuccess == true){
                valid = true;
			}else{
                valid = false;
			}
        }
    });
    return valid
}

$(function(){
    $(".sub-nav>li").on("click", "a" ,function (e) {
        var href= $(this).attr("href") || '';
        var ishref = href.split('/').length > 1 ? true : false;
        if(ishref && !isSessionValid()){
            e.preventDefault();
            return false
        }
    });
})

// 对Date的扩展，将 Date 转化为指定格式的String
// 月(M)、日(d)、小时(h)、分(m)、秒(s)、季度(q) 可以用 1-2 个占位符，
// 年(y)可以用 1-4 个占位符，毫秒(S)只能用 1 个占位符(是 1-3 位的数字)
// 例子：
// (new Date()).Format("yyyy-MM-dd hh:mm:ss.S") ==> 2006-07-02 08:09:04.423
// (new Date()).Format("yyyy-M-d h:m:s.S")      ==> 2006-7-2 8:9:4.18
Date.prototype.Format = function (fmt) { //author: meizz
    var o = {
        "M+": this.getMonth() + 1, //月份
        "d+": this.getDate(), //日
        "h+": this.getHours(), //小时
        "m+": this.getMinutes(), //分
        "s+": this.getSeconds(), //秒
        "q+": Math.floor((this.getMonth() + 3) / 3), //季度
        "S": this.getMilliseconds() //毫秒
    };
    if (/(y+)/.test(fmt)) fmt = fmt.replace(RegExp.$1, (this.getFullYear() + "").substr(4 - RegExp.$1.length));
    for (var k in o)
        if (new RegExp("(" + k + ")").test(fmt)) fmt = fmt.replace(RegExp.$1, (RegExp.$1.length == 1) ? (o[k]) : (("00" + o[k]).substr(("" + o[k]).length)));
    return fmt;
}


// 从品牌客户端 复制
//彩印参数验证
function AuthParams(num){
    if($('.ctx-temp').length > 0){
        var valResult = true;
        $('.ctx-temp').each(function(){
            if($(this).val().length > num){
                layer.tips('参数长度不能超过'+ num +'个字符', '#content', {
                    tips: [2, 'red'],
                    time: 2000
                });
                valResult = false;
                return;
            }
        })
        return valResult;
    } else {
        return true;
    }
}
function initStyle(){
    var mainH = $("#main").height() > $(window).height() ? $("#main").height() : $(window).height();
    $("#sidebar").height(mainH);
}

/* 加载表格 */
function loadTable(option, vm){
    var opt = {
        url : '../template.json',
        type : 'GET',
        data : {}
    }
    var option = $.extend(opt, option);
    $.ajax({
        url : option.url,
        type : option.type,
        data : option.data,
        dataType :'json',
        success : function(res){

            vm.table = res;
            var mainH = $("#main").height() > $(window).height() ? $("#main").height() : $(window).height();
            $("#sidebar").height(mainH);
        },
        error: function(res){

        }
    })
}

/* 添加模板 验证 */
function AuthSign(str){
    var sign = str;
    if(sign.length < 2 || sign.length > 8 ){
        layer.alert('签名长度为2到8个字符之间', {
            icon: 2
        });
        return false;
    }
    if(/^[0-9]*$/.test(str)){
        layer.alert('签名不能为纯数字', {
            icon: 2
        });
        return false;
    }

    return true;
}

function AuthType(str){
    if(str === '' || str === undefined){
        layer.alert('请选择模板类型', {
            icon: 2
        });
        return false;
    }
    return true;6
}

function AuthContent(str){
    var reg = /^[^\{\}]*([^\{\}]*\{[^\{\}]*\}[^\{\}]*){0,4}[^\{\}]*$/;
    if(str === '' || str === undefined){
        layer.alert('请填写短信内容', {
            icon: 2
        });
        return false;
    }
    if(!reg.test(str)){
        layer.alert('仅支持0-4个参数', {
            icon: 2
        });
        return false;
    }
    return true;
}

//复制彩印操作
$(".msm-type .select").click(function() {
    if($(this).hasClass('js-disable')){
        return false;
    }
    $(this).siblings('.select-list').toggle();
})
$(".grid-select").click(function() {
    $(this).siblings('.select-list').toggle();
})
$(".hover-select").mouseleave(function(){
    $(".grid-select-list").hide();
})
$(".grid-select-list li").click(function(event) {
	/*var ele = $(".grid-select");
	 $("#smstype").val($(this).attr("value"))
	 var txt = $(this).text();
	 ele.text(txt);
	 $(this).closest(".grid-select-list").hide();*/
});
$(".msm-type .select-list li").click(function(event) {
    var ele = $(".msm-type .select");
    $("#smstype").val($(this).attr("value"))
    var txt = $(this).text();
    ele.text(txt);
    $(".msm-type .select-list").hide();
});


//点击显示内容
$("body").on("click", ".temp-cont", function(){
    var contentDetail = $(this).text();
    layer.open({
        type: 1,
        title:"模板内容",
        skin: 'layui-layer-rim', //加上边框
        move: false,
        area: ['600px'], //宽高
        content: '<div style="padding:20px;" class="breakall">' + contentDetail + "</div>"
    });
})