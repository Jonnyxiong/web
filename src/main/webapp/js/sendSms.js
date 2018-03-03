function smsValidate(type) {
    var valResult = true;

    var mobiles = $("#mobile").val();
    // if (mobiles.length == 0) {
    //     layer.tips('号码池不能为空', '#mobile', {
    //         tips: [2, 'red'],
    //         time: 4000
    //     });
    //     valResult = false;
    //     return false;
    // }

    if (mobiles.split(",").length >60000) {
        layer.tips('号码池短信数量超过60000', '#mobile', {
            tips: [2, 'red'],
            time: 4000
        });
        valResult = false;
        return false;
    }


    // 签名校验
    var sign = $("#sign").val();
    if (sign.length < 2 || sign.length > 8) {
        layer.tips('短信签名长度为2到8个字符', '#sign', {
            tips: [2, 'red'],
            time: 4000
        });
        valResult = false;
        return false;
    }

    // 短信内容校验
    var content = $("#content").val() || $("#content").text();
    if($('.ctx-temp').length > 0){
    	$('.ctx-temp').each(function(){
    		if(!$(this).val()){
    			layer.tips('模板内容不能为空', '#content', {
		            tips: [2, 'red'],
		            time: 2000
		        });
    			 valResult = false;
    			 return;
    		}
    	})
    } 
    if(type == "cymb"){
        if(!AuthParams(10)){
            return false;
        }
    }
    if(type == "dxmb"){
        if(!AuthParams(100)){
            return false;
        }
    }

    if(content.length > 500 && !type) {
        layer.tips('短信内容长度不能超过500个字符', '#content', {
            tips: [2, 'red'],
            time: 4000
        });
        valResult = false;
    }
    if (content.length == 0) {
        layer.tips('短信内容不能为空', '#content', {
            tips: [2, 'red'],
            time: 4000
        });
        valResult = false;
    }

    var smstype = $("#smstype").val();
    if (smstype == -1 || smstype === '' || smstype === undefined ) {
        layer.tips('请选择短信类型', '#selctType', {
            tips: [2, 'red'],
            time: 4000
        });
        valResult = false;
    }

    return valResult;
}

// 短信签名和短信内容控制事件
function contentAndSignController() {
    $("#sign").keyup(function() {
        var sign = $("#sign").val(); // 签名
        $("#fakeSign").text(sign); // 短信内容中显示的签名

        computedIndent();

        computedChargeNum();
    });
    $("#content").keyup(function() {
        computedIndent();
        computedChargeNum();
    });

}
//计算签名text-indent
function computedIndent(){
    var fakeSignLength = $("#fakeSign")[0].offsetWidth + 16;
    $("#content").css("text-indent", fakeSignLength + "px");
}
//计算计费条数（也就是短信拆分条数）
function computedChargeNum(){
    //签名长度包括在短信内容 包括括号
    var letterLength = $(".msg-ctx textarea").val().length + $("#fakeSign").text().length + 2;
    $("#letterNum").text(letterLength);

    //判断计费条数
    var charge = 0;
    if(letterLength <= 70){
        charge = 1;
    } else {
        charge = Math.ceil(letterLength/67);
    }
    var mobile = $("#mobile").val().split(',');

    $("#charge").text(charge);
    $("#charge_total").text(charge*mobile.length);
    console.log(charge * mobile.length);
}


//监听短信内容长度
function letterNumListener() {
    $(".msg-ctx").on("keyup", "textarea", function() {
        var letterLength = this.value.length + $("#fakeSign").text().length + 2;
        $("#letterNum").text(letterLength);
        
        //判断计费条数
        var charge = 0;
        if(letterLength <= 70){
        	charge = 1;
        } else {
        	charge = Math.ceil(letterLength/67);
        }


        var mobile = $("#mobile").val().split(',');

        $("#charge").text(charge);
        $("#charge_total").text(charge*mobile.length);
    });
}

function mobileCheckListener() {

    $("#mobile").bind("blur", function() {
        var mobileStr = this.value;
        if ($.trim(mobileStr).length == 0) {
            return;
        }
        var mobileTempList = mobileStr.split(",")
            // 过滤不合法的手机号码
            // 			var mobileReg = /(00\d{8,})|(13\d{9})|(14[5|7|9]\d{8})|(15[0|1|2|3|5|6|7|8|9]\d{8})|(170[0|1|2|3|4|5|6|7|8|9]\d{7})|(17[1|5|6|7|8]\d{8})|(173\d{8})|(18\d{9})(?=,|$)/g;
            // 			var mobileList = mobileStr.match(mobileReg);
        if(mobileTempList.length>60000){
            layer.alert('号码池中号码超过60000', {
                icon: 2
            });
            return;
        }
        var mobileList = []; // 保留合法的手机号码
        for (var pos = 0; pos < mobileTempList.length; pos++) {
            var mobile = mobileTempList[pos];
            if (Validate.isValidMobile(mobile)) {
                mobileList.push(mobile);
            }
        }
        if (mobileList != null) {
            this.value = mobileList.join(",");
        } else {
            this.value = "";
        }
        var resultList = [], hash = {};
        for (var i = 0, elem; (elem = mobileList[i]) != null; i++) {
            if (!hash[elem]) {
                resultList.push(elem);
                hash[elem] = true;
            }
        }
        if (resultList != null) {
            this.value = resultList.join(",");
        } else {
            this.value = "";
        }
        var checkPreNum = mobileTempList.length; // 过滤不合法手机前的数量
        var checkAfterNum = mobileList.length; // 过滤不合法手机后的数量
        computedChargeNum();
        var filterNum1 =0;
        var filterNum =0;
        if (checkAfterNum != resultList.length) {
             filterNum1 = checkAfterNum - resultList.length;
        }
        if (checkPreNum != checkAfterNum) {
             filterNum = checkPreNum - checkAfterNum;
        }
        if(filterNum1>0 && filterNum>0){
            layer.alert('过滤掉' + filterNum + '个不合法号码,'+ filterNum1 +'重复号码', {
                time: 2000,
                icon: 2
            });
        }
        if(filterNum1>0 && filterNum==0){
            layer.alert('过滤掉'+ filterNum1 +'重复号码', {
                time: 2000,
                icon: 2
            });
        }
        if(filterNum1==0 && filterNum>0){
            layer.alert('过滤掉' + filterNum + '个不合法号码', {
                time: 2000,
                icon: 2
            });
        }
    })

    $("#mobile").bind("keyup", function() {
        // 只能输入 数字和英文逗号
        this.value = this.value.replace(/[^\r\n0-9\,]/g, '');
    })

}

var maxsize = 5 * 1024 * 1024; //5M  
var errMsg = "您选择的文件大于5M,请将excel拆分后重新导入";
var tipMsg = "您的浏览器暂不支持计算上传文件的大小，确保上传文件不要超过5M，建议使用高版本浏览器。";
var browserCfg = {};

function checkFile(mainFlag) {
    var fileName = $("#importMobile").val();
    var suffix = fileName.substring(fileName.lastIndexOf("."), fileName.length);
    suffix = suffix.toLowerCase();
    if (suffix!='.xlsx' && suffix!='.xls') {
        layer.alert("导入文件格式错误，目前只支持Excel导入，请使用模板", {icon: 2});
        return false;
    }

    var ua = window.navigator.userAgent;
    if (ua.indexOf("MSIE") >= 1) {
        browserCfg.ie = true;
    } else if (ua.indexOf("Firefox") >= 1) {
        browserCfg.firefox = true;
    } else if (ua.indexOf("Chrome") >= 1) {
        browserCfg.chrome = true;
    }

    var filesize = 0;
    var excelFile = $("#importMobile");
    if (browserCfg.firefox || browserCfg.chrome) {
        filesize = excelFile[0].files[0].size;
    } else if (browserCfg.ie) {
        var obj_img = $("#preview");
        obj_img.dynsrc = excelFile.value;
        filesize = obj_img.fileSize;
    }


    if (filesize == -1) {
        layer.alert(tipMsg, {
            icon: 2
        });
        return false;
    } else if (filesize > maxsize) {
        layer.alert(errMsg, {
            icon: 2
        });
        return false;
    }

    return true;
}


//页面跳转
function jumpPage(option, vm){
	var opt = {
		url : '../data.json',
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
           vm.msg_data      = res.list;
           vm.pageNow       = res.currentPage;
           vm.total         = res.totalPage;
           vm.totalCount    = res.totalCount;
           vm.pageRowCount  = res.pageRowCount;
        }
    })
}

function aotuTemplateHandle(option, vm){
    $.ajax({
        url : option.url,
        type : option.type,
        data : option.data,
        dataType :'json',
        success : function(res){
            vm.msg_data      = res.entityList;
            vm.pageNow       = res.currentPage;
            vm.total         = res.totalPage;
            vm.totalCount    = res.totalCount;
            vm.pageRowCount  = res.pageRowCount;
        }
    })
}
function commonTemplateHandle(option, vm){
    $.ajax({
        url : option.url,
        type : option.type,
        data : option.data,
        dataType :'json',
        success : function(res){
            vm.msg_common_data      = res.entityList;
            vm.pageNow_common       = res.currentPage;
            vm.total_common         = res.totalPage;
            vm.totalCount_common    = res.totalCount;
            vm.pageRowCount_common  = res.pageRowCount;
        }
    })
}
$(function(){
    var ctx_flag = $(".msg-ctx").clone();
    //品牌弹出选择模板
	$(".js-template").click(function(){
		layer.open({
		  type: 1,
		  title: false, //不显示标题
		  area: ['800px'],
		  content: $('.grid-smsTemplate')
		});
	})
    var _index = 1;
    //oem弹出智能模板
    $(".js-autoTemplate").click(function(){
        var layer_index = layer.open({
            type: 1,
            title: false, //不显示标题
            area: ['800px','500px'],
            content: $('.grid-autoTemplate'),
            cancel: function(){
            }
        });
    })
    //导入模板
	$(".grid-template").on("click", ".js-import", function(){
        if($(this).hasClass("btn-disable")){
            return false;
        }
        layer.closeAll();
		var sign          = $(this).closest("tr").find(".sign").text(),
			mold          = $(this).closest("tr").find(".template-smstypename").val(),
            smstype       = $(this).closest("tr").find(".template-smstype").val(),
			ctx           = $(this).closest("tr").find(".ctx").text(),
            templateId    = $(this).closest("tr").find(".template-id").text(),
        	ctx_          = ctx.replace(/(\{.*?\})/g, '<input class="ctx-temp" type="text" placeholder="$&">');

        $("#selctType").text(mold).addClass('js-disable');
        $("#smstype").val(smstype);
        $("#sign").val(sign).addClass('js-disable').attr('disabled', 'disabled');
        $("#templateId").val(templateId);


        
        if($(this).hasClass("cymb")){
            var tip = "USSD和闪信目前只支持中国移动号码显示，电信、联通暂不支持"
            var button = '<div class="ctx ft-l">' + 
                        '<a class="btn btn-green js-sendTemp" href="javascript:;" data-type="cymb">发送</a>' +
                        '<p class="mt10" style="margin-left:0;"><span></span>'+ tip +'</p>' +
                    '</div>'
        } else{
            var tip = '签名长度范围2-8个字符，模板内容字符范围5~500字符';
            var button = '<div class="ctx ft-l">' + 
                            '<a class="btn btn-green js-sendTemp" href="javascript:;" data-type="dxmb">发送</a>' +
                            '<p class="mt10" style="margin-left:0;"><span></span>'+ tip +'</p>' +
                        '</div>'
        }

        

        $(".msg-ctx").html('<div><p class="msg-template" id="content" style="margin-left:0;">【'+ sign +'】'+ ctx_ +'</p></div>' + button);

        initStyle();
	})
    //导入智能模板
    $(".grid-template").on("click", ".js-autoImport", function(){
        if($(this).hasClass("btn-disable")){
            return false;
        }
        layer.closeAll();
        var sign          = $(this).closest("tr").find(".sign").text(),
            smstype       = $(this).closest("tr").find(".template-smstype").val(),
            mold          = $(this).closest("tr").find(".mold").text(),
            ctx           = $(this).closest("tr").find(".ctx").text();
        $("#selctType").text("请选择...");
        //$("#smstype").val(smstype);
        $("#sign").val(sign);
        $("#content").val(ctx);
        $("#fakeSign").text(sign);

        computedIndent()
        computedChargeNum();
        if(smstype == "10"){
            $(".js-hyyx").hide();
            $(".js-yz").show();
            $(".js-tz").show();

        }else if(smstype == "11"){
            $(".js-yz").hide();
            $(".js-tz").hide();
            $(".js-hyyx").show();
        }

    })

	$(".js-clear-template").click(function(){
		$(".msg-ctx").html(ctx_flag.html());
		$("#selctType").text('请选择').removeClass('js-disable');
        $("#sign").val('').removeClass('js-disable').removeAttr('disabled');
        $("#smstype").val('')
        $("#templateId").val('');
	})
	// 输入同步
	$(".msm-type").on("keyup", ".js-ctx-template", function(){
		var val 	= $(this).val(),
			index 	= $(this).data("index"); 

		$(".msg-template span").eq(index).text(val);

	})


})
