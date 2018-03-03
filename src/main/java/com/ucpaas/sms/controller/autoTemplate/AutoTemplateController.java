package com.ucpaas.sms.controller.autoTemplate;

import com.jsmsframework.audit.entity.JsmsAutoTemplate;
import com.jsmsframework.audit.service.JsmsAutoTemplateService;
import com.jsmsframework.common.dto.JsmsPage;
import com.jsmsframework.common.dto.R;

import com.jsmsframework.common.enums.AutoTemplateLevel;
import com.jsmsframework.common.enums.AutoTemplateStatus;
import com.jsmsframework.common.enums.AutoTemplateSubmitType;
import com.jsmsframework.common.enums.WebId;

import com.jsmsframework.common.enums.balckAndWhiteTemplate.TemplateLevel;
import com.jsmsframework.user.audit.service.JsmsUserAutoTemplateService;
import com.jsmsframework.user.entity.JsmsAccount;
import com.jsmsframework.user.entity.JsmsAgentInfo;
import com.jsmsframework.user.service.JsmsAccountService;
import com.jsmsframework.user.service.JsmsAgentInfoService;
import com.jsmsframework.user.service.JsmsUserService;

import com.ucpaas.sms.constant.Constant;
import com.ucpaas.sms.controller.BaseController;

import com.ucpaas.sms.model.PageContainer;

import com.ucpaas.sms.model.dto.JsmsAutoTemplateDTO;
import com.ucpaas.sms.model.po.SmsAccountModelPo;

import com.ucpaas.sms.util.ConfigUtils;
import com.ucpaas.sms.util.JsonUtils;
import com.ucpaas.sms.util.PageConvertUtil;
import com.ucpaas.sms.util.file.FileUtils;

import com.ucpaas.sms.util.web.ServletUtil;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.springframework.web.servlet.ModelAndView;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by xiongfenglin on 2017/10/19.
 *
 * @author: xiongfenglin
 */
@Controller
@RequestMapping(value="/autoTemplate")
public class AutoTemplateController extends BaseController{
    @Autowired
    private JsmsAutoTemplateService jsmsAutoTemplateService;
    @Autowired
    private JsmsAccountService jsmsAccountService;
    @Autowired
    private JsmsAgentInfoService jsmsAgentInfoService;
    @Autowired
    private JsmsUserAutoTemplateService jsmsUserAutoTemplateService;
    private static final Logger LOGGER = LoggerFactory.getLogger(AutoTemplateController.class);
    @RequestMapping(path="/list",method = RequestMethod.GET )
    public ModelAndView autoTemplate(HttpSession session,HttpServletRequest request, ModelAndView mv)throws Exception{
        SmsAccountModelPo accountModel = (SmsAccountModelPo) session.getAttribute(Constant.LOGIN_USER_INFO);
        mv.addObject("accountModel", accountModel);
        everyPageShouldHave(mv, request.getServerName(), "/autoTemplate/list", request);
        mv.setViewName("autoTemplate/list");
        return mv;
    }
    @RequestMapping(path="/list",method = RequestMethod.POST)
    @ResponseBody
    public String autoTemplateQuery(HttpSession session, HttpServletRequest request,@RequestParam Map<String,String> params){
        JsmsAccount jsmsAccount = null;
        JsmsAgentInfo jsmsAgentInfo = null;
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss");
        int index =0;
        // 创建时间：开始时间
        String createStartTime = request.getParameter("createStartTime");
        String createEndTime = request.getParameter("createEndTime");
        if (StringUtils.isNotBlank(createStartTime)) {
            params.put("createStartTime", createStartTime+" 00:00:00");
        }
        // 创建时间：结束时间
        if (StringUtils.isNotBlank(createEndTime)) {
            params.put("createEndTime", createEndTime+" 23:59:59");
        }
        params.put("currentPage",params.get("page"));
        params.put("pageRowCount",params.get("rows"));
        params.put("clientId",String.valueOf(session.getAttribute("clientid")));
        JsmsPage jsmsPage = PageConvertUtil.paramToPage(params);
        jsmsPage.setParams(params);
        jsmsPage.setOrderByClause("a.create_time DESC");
        JsmsPage queryPage = jsmsUserAutoTemplateService.findListOfAutoTemplate(jsmsPage,WebId.客户端.getValue(), AutoTemplateLevel.用户级别);
        List<JsmsAutoTemplateDTO> list = new ArrayList<>();
        for (Object temp : queryPage.getData()) {
            index =index+1;
            JsmsAutoTemplateDTO jsmsAutoTemplateDTO = new JsmsAutoTemplateDTO();
            BeanUtils.copyProperties(temp , jsmsAutoTemplateDTO);
            //com.jsmsframework.user.entity.JsmsUser jsmsUser = jsmsUserService.getById(String.valueOf(jsmsAutoTemplateDTO.getAdminId()));
            if(jsmsAutoTemplateDTO.getSubmitType().equals(AutoTemplateSubmitType.客户提交.getValue())){
                jsmsAccount = jsmsAccountService.getByClientId(String.valueOf(jsmsAutoTemplateDTO.getUserId()));
                if(jsmsAccount!=null){
                    jsmsAutoTemplateDTO.setUserName(jsmsAccount.getName());
                }
            }else if(jsmsAutoTemplateDTO.getSubmitType().equals(AutoTemplateSubmitType.代理商提交.getValue())){
                jsmsAgentInfo = jsmsAgentInfoService.getByAgentId(Integer.parseInt(String.valueOf(jsmsAutoTemplateDTO.getUserId())));
                if(jsmsAgentInfo!=null){
                    jsmsAutoTemplateDTO.setUserName(jsmsAgentInfo.getAgentName());
                }
            }else{
                jsmsAutoTemplateDTO.setUserName("系统");
            }
            if(jsmsAutoTemplateDTO.getRemark()==null){
                jsmsAutoTemplateDTO.setRemark(" ");
            }
            if(jsmsAutoTemplateDTO.getClientId()!=null && StringUtils.isNotBlank(String.valueOf(jsmsAutoTemplateDTO.getClientId()))){
                if(jsmsAutoTemplateDTO.getClientId().equals(session.getAttribute("clientid").toString())&&jsmsAutoTemplateDTO.getWebId().equals(WebId.客户端.getValue())&&jsmsAutoTemplateDTO.getSubmitType().equals(0)){
                    if(!jsmsAutoTemplateDTO.getState().equals(0)){
                        jsmsAutoTemplateDTO.setIsCreateor(1);
                    }
                }else{
                    jsmsAutoTemplateDTO.setIsCreateor(0);
                }
            }else{
                jsmsAutoTemplateDTO.setIsCreateor(0);
            }
            jsmsAutoTemplateDTO.setOrderNo(index);
            jsmsAutoTemplateDTO.setCreateTimeStr(format.format(jsmsAutoTemplateDTO.getCreateTime()));
            if(jsmsAutoTemplateDTO.getUpdateTime()!=null){
                jsmsAutoTemplateDTO.setUpdateTimeStr(format.format(jsmsAutoTemplateDTO.getUpdateTime()));
            }
            list.add(jsmsAutoTemplateDTO);
        }
        queryPage.setData(list);
        PageContainer page  = PageConvertUtil.pageToContainer(queryPage);
        return JsonUtils.toJson(page);
    }
    @RequestMapping(path="/add",method = RequestMethod.GET )
    public ModelAndView autoTemplateAddView(HttpSession session, HttpServletRequest request, ModelAndView mv){
        mv.setViewName("autoTemplate/add");
        return mv;
    }

    @RequestMapping(path="/autoTemplateModify",method = RequestMethod.GET)
    public ModelAndView autoTemplateAdd(HttpSession session, HttpServletRequest request, ModelAndView mv){
        JsmsAutoTemplate jsmsAutoTemplate = null;
        if(StringUtils.isNotBlank(request.getParameter("templateId"))){
            jsmsAutoTemplate= jsmsAutoTemplateService.getByTemplateId(Integer.parseInt(request.getParameter("templateId")));
        }
        if(jsmsAutoTemplate!=null){
            mv.addObject("templateId", jsmsAutoTemplate.getTemplateId());
            mv.addObject("clientId", jsmsAutoTemplate.getClientId());
            mv.addObject("smsType", jsmsAutoTemplate.getSmsType());
            mv.addObject("sign", jsmsAutoTemplate.getSign());
            mv.addObject("content", jsmsAutoTemplate.getContent());
            mv.addObject("templateType", jsmsAutoTemplate.getTemplateType());
            mv.addObject("state", jsmsAutoTemplate.getState());
        }
        mv.setViewName("autoTemplate/add");
        return mv;
    }

    /**
     * 删除客户模板
     */
    @RequestMapping(value = "/del",method = RequestMethod.POST)
    @ResponseBody
    public R autoTemplateDel(HttpSession session, HttpServletRequest request) {
        R r = new R();
        String templateIdStr = request.getParameter("templateId");
        Integer templateId = null;
        // 模板ID
        if (StringUtils.isNotBlank(templateIdStr)) {
            templateId  = Integer.parseInt(templateIdStr);
        }

        if (templateId == null) {
            r.error("模板ID不能为空");
            return r;
        }else{
            r = jsmsAutoTemplateService.delAutoTemplate(templateId);
            return r;
        }
    }
    /**
     * 新增客户模板
     */
    @RequestMapping("/autoTemplateSave")
    @ResponseBody
    public R autoTemplateSave(HttpSession session, HttpServletRequest request,@RequestParam Map<String,String> params) {
        boolean isMod = false;
        JsmsAutoTemplate template = new JsmsAutoTemplate();
        R r = null;
        // 模板ID
        Object obj = request.getParameter("templateId");
        if (obj != null && obj != "") {
            isMod = true;
            template.setTemplateId(Integer.parseInt(String.valueOf(obj)));
        }
        // 模板类型
        obj = request.getParameter("templateType");
        if (obj != null && obj != "") {
            template.setTemplateType(Integer.parseInt(String.valueOf(obj)));
        }

        // 短信类型
        obj = request.getParameter("smsType");
        if (obj != null && obj != "") {
            template.setSmsType(Integer.parseInt(String.valueOf(obj)));
        }
        // 模板内容
        obj = request.getParameter("content");
        if (obj != null && obj != "") {
            template.setContent(String.valueOf(obj));
        }
        // 短信签名
        obj = request.getParameter("sign");
        if (obj != null && obj != "") {
            template.setSign(String.valueOf(obj));
        }
        if (StringUtils.isNotBlank(request.getParameter("state"))){
            if (request.getParameter("state").equals("1")){
                template.setRemark(" ");
                template.setState(AutoTemplateStatus.待审核.getValue());
            }else if(request.getParameter("state").equals("3")){
                template.setRemark(" ");
                template.setState(AutoTemplateStatus.审核不通过.getValue());
            }
        }else{
            template.setState(AutoTemplateStatus.待审核.getValue());
        }
        template.setSubmitType(AutoTemplateSubmitType.客户提交.getValue());
        template.setWebId(7);
        template.setClientId(String.valueOf(session.getAttribute("clientid")));
        template.setTemplateLevel(TemplateLevel.用户级别.getValue());
        if (isMod) {
            if (template == null){
                r.error("智能模板不能为空");
                return r;
            }else{
                if (template.getTemplateId() == null){
                    r.error("智能模板的模板ID不能为空");
                    return r;
                }else{
                    r = jsmsAutoTemplateService.modifyTemplate(template);
                }
            }
        } else {
            template.setUserId(String.valueOf(session.getAttribute("clientid")));
            r = jsmsAutoTemplateService.addAutoTemplate(template);
        }
        return r;
    }
    @RequestMapping("/downloadExcelTemplate")
    public void downloadExcelTemplate(HttpSession session, HttpServletRequest request,HttpServletResponse response) {
        String path = request.getServletContext().getRealPath("/template/批量添加智能模板.xls");
        FileUtils.download(path,response);
    }

    /**
     * 批量添加智能模板
     */
    @RequestMapping(path="/import",method = RequestMethod.GET )
    public ModelAndView autoTemplateImportTemp(HttpSession session, HttpServletRequest request, ModelAndView mv){
        mv.setViewName("autoTemplate/import");
        return mv;
    }
    @RequestMapping("/addAutoTemplateBatch")
    @ResponseBody
    public R importOperationExcel(HttpSession session,HttpServletRequest request,@RequestParam("excel") CommonsMultipartFile file) {
        R r = new R();
        String fileName = file.getOriginalFilename();
        LOGGER.debug("importOperationExcel[fileName={}]", fileName);
        if (StringUtils.isBlank(file.getContentType())) {
            r.setCode(500);
            r.setMsg("请先选择导入Excel");
            return r;
        }
        if (file.getSize() > 2097152L) {
            r.setCode(500);
            r.setMsg("您选择的文件大于2M,请将excel拆分后重新导入");
            return r;
        }
        LOGGER.debug("导入文件的类型 ----> {}", file.getContentType());
        String path = new StringBuilder(ConfigUtils.temp_file_dir).toString();
        FileUtils.delete(path);
        FileUtils.upload2(path, fileName, file);
        // 获得Excel文件中的数据
        LOGGER.debug("智能模板Excel 读取完成  ----------> 开始解析");
        r = jsmsUserAutoTemplateService.addAutoTemplateBatch(null,null,String.valueOf(session.getAttribute("clientid")), path,WebId.客户端.getValue());
        return r;
    }
    @RequestMapping("/exportImportResult")
    @ResponseBody
    public String exportError(HttpSession session,HttpServletResponse response){
        String msg = "";
        String filePath = ConfigUtils.temp_file_dir +"/import"+ "/批量添加智能模板结果-userid-" + session.getAttribute("clientid")+".xls";
        File file = new File(filePath);
        if(file.exists()){
            FileUtils.download(filePath,response);
            msg = "下载成功";
        }else{
            msg = "文件过期、不存在或者已经被管理员删除";
        }
        return msg;
    }
    private File multipartToFile(MultipartFile multfile) throws IOException {
        CommonsMultipartFile cf = (CommonsMultipartFile)multfile;
        //这个myfile是MultipartFile的
        DiskFileItem fi = (DiskFileItem) cf.getFileItem();
        File file = fi.getStoreLocation();
        return file;
    }

    @RequestMapping(path="/commonlist",method = RequestMethod.GET )
    public ModelAndView overAllList(HttpSession session,HttpServletRequest request, ModelAndView mv)throws Exception{
        SmsAccountModelPo accountModel = (SmsAccountModelPo) session.getAttribute(Constant.LOGIN_USER_INFO);
        mv.addObject("accountModel", accountModel);
        everyPageShouldHave(mv, request.getServerName(), "/autoTemplate/list", request);
        mv.setViewName("autoTemplate/commonlist");
        return mv;
    }
    @RequestMapping(path="/commonlist",method = RequestMethod.POST)
    @ResponseBody
    public String overAllListQuery(@RequestParam Map<String,String> params){
        int index =0;
        params.put("currentPage",params.get("page"));
        params.put("pageRowCount",params.get("rows"));
       // params.put("clientId","'*'");
        JsmsPage jsmsPage = PageConvertUtil.paramToPage(params);
        jsmsPage.setParams(params);
        jsmsPage.setOrderByClause("a.create_time DESC");
        JsmsPage queryPage = jsmsUserAutoTemplateService.findListOfAutoTemplate(jsmsPage,WebId.客户端.getValue(), AutoTemplateLevel.用户级别);
        List<JsmsAutoTemplateDTO> list = new ArrayList<>();
        for (Object temp : queryPage.getData()) {
            index =index+1;
            JsmsAutoTemplateDTO jsmsAutoTemplateDTO = new JsmsAutoTemplateDTO();
            BeanUtils.copyProperties(temp , jsmsAutoTemplateDTO);
            jsmsAutoTemplateDTO.setOrderNo(index);
            list.add(jsmsAutoTemplateDTO);
        }
        queryPage.setData(list);
        PageContainer page  = PageConvertUtil.pageToContainer(queryPage);
        return JsonUtils.toJson(page);
    }
}
