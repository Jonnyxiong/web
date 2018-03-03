package com.ucpaas.sms.controller;


import com.alibaba.fastjson.JSON;
import com.jsmsframework.common.dto.JsmsPage;
import com.jsmsframework.common.dto.ResultVO;
import com.jsmsframework.common.entity.JsmsSystemErrorDesc;
import com.jsmsframework.common.enums.*;
import com.jsmsframework.common.service.JsmsSystemErrorDescService;
import com.jsmsframework.common.util.BeanUtil;
import com.jsmsframework.sms.send.dto.JsmsAccessSmsDTO;
import com.jsmsframework.sms.send.dto.TimerSendTaskDTO;
import com.jsmsframework.sms.send.entity.JsmsTimerSendTask;
import com.jsmsframework.sms.send.service.JsmsSendService;
import com.jsmsframework.sms.send.service.JsmsSubmitProgressService;
import com.jsmsframework.sms.send.service.JsmsTimerSendPhonesService;
import com.jsmsframework.sms.send.service.JsmsTimerSendTaskService;
import com.jsmsframework.sms.send.util.JsmsSendParam;
import com.jsmsframework.user.audit.service.JsmsUserAutoTemplateService;
import com.ucpaas.sms.common.AjaxResult;
import com.ucpaas.sms.common.entity.R;
import com.ucpaas.sms.common.util.SecurityUtils;
import com.ucpaas.sms.common.util.file.ExcelUtils;
import com.ucpaas.sms.common.util.file.FileUtils;
import com.ucpaas.sms.constant.Constant;
import com.ucpaas.sms.model.*;
import com.ucpaas.sms.model.dto.AccessTimerSmsDto;
import com.ucpaas.sms.model.dto.JsmsAutoTemplateDTO;
import com.ucpaas.sms.model.param.PageParam;
import com.ucpaas.sms.model.po.SmsAccountModelPo;
import com.ucpaas.sms.service.BussinessService;
import com.ucpaas.sms.service.SMSSendService;
import com.ucpaas.sms.util.ConfigUtils;
import com.ucpaas.sms.util.DateUtil;
import com.ucpaas.sms.util.JsonUtils;
import com.ucpaas.sms.util.PageConvertUtil;
import com.ucpaas.sms.util.web.ServletUtil;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
@RequestMapping("/bussiness")
public class BussinessController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(BussinessController.class);

    @Autowired
    private BussinessService bussinessService;

    @Autowired
    private SMSSendService smsSendService;
    @Autowired
    private JsmsUserAutoTemplateService jsmsUserAutoTemplateService;
    @Autowired
    private JsmsSystemErrorDescService jsmsSystemErrorDescService;
    @Autowired
    private JsmsTimerSendPhonesService jsmsTimerSendPhonesService;
    @Autowired
    private JsmsTimerSendTaskService jsmsTimerSendTaskService;
    @Autowired
    private JsmsSendParam jsmsSendParam;
    @Autowired
    private JsmsSubmitProgressService jsmsSubmitProgressService;
    @Autowired
    private JsmsSendService jsmsSendService;

    /**
     * 短信发送页面
     */
    @RequestMapping(value = "/send", method = RequestMethod.GET)
    public ModelAndView smsView(HttpServletRequest request, ModelAndView mv, HttpSession session) throws Exception {
        mv.addObject("accountModel", ServletUtil.getLoginUserInfo(request));
        mv.addObject("isOEM", session.getAttribute("isOEM"));
        mv.addObject("clientId", ServletUtil.getLoginUserInfo(request).getClientId());
        everyPageShouldHave(mv, request.getServerName(), "/bussiness/send", request);
        mv.setViewName("bussiness/send");
        mv.addObject("img_url", ConfigUtils.smsp_img_url);
        return mv;
    }

    //查询所有审核通过的用户级别智能模板
    @RequestMapping(path = "/autoTemplateList", method = RequestMethod.POST)
    @ResponseBody
    public String autoTemplateQuery(HttpSession session, @RequestParam Map<String, String> params) {
        SmsAccountModelPo accountModel = (SmsAccountModelPo) session.getAttribute(Constant.LOGIN_USER_INFO);
        params.put("currentPage", params.get("page"));
        params.put("pageRowCount", params.get("rows"));
        params.put("state", String.valueOf(AutoTemplateStatus.审核通过.getValue()));
        params.put("clientId", accountModel.getClientId());
        //params.put("templateLevel","1");
        JsmsPage jsmsPage = PageConvertUtil.paramToPage(params);
        jsmsPage.setParams(params);
        jsmsPage.setOrderByClause("a.create_time DESC");
        JsmsPage queryPage = jsmsUserAutoTemplateService.findListOfAutoTemplate(jsmsPage, WebId.客户端.getValue(), AutoTemplateLevel.用户级别);
        PageContainer page = PageConvertUtil.pageToContainer(queryPage);
        return JsonUtils.toJson(page);
    }

    //查询所有审核通过的通用级别智能模板
    @RequestMapping(path = "/autoTemplateCommonList", method = RequestMethod.POST)
    @ResponseBody
    public String overAllTemplateQuery(@RequestParam Map<String, String> params) {
        params.put("currentPage", params.get("page"));
        params.put("pageRowCount", params.get("rows"));
        int index = 0;
        params.put("state", String.valueOf(AutoTemplateStatus.审核通过.getValue()));
        //params.put("clientId","'*'");
        //params.put("templateLevel","0");//通用模板
        JsmsPage jsmsPage = PageConvertUtil.paramToPage(params);
        jsmsPage.setParams(params);
        jsmsPage.setOrderByClause("a.create_time DESC");
        JsmsPage queryPage = jsmsUserAutoTemplateService.findListOfAutoTemplate(jsmsPage, WebId.客户端.getValue(), AutoTemplateLevel.全局级别);
        List<JsmsAutoTemplateDTO> list = new ArrayList<>();
        for (Object temp : queryPage.getData()) {
            index = index + 1;
            JsmsAutoTemplateDTO jsmsAutoTemplateDTO = new JsmsAutoTemplateDTO();
            BeanUtils.copyProperties(temp, jsmsAutoTemplateDTO);
            jsmsAutoTemplateDTO.setOrderNo(index);
            list.add(jsmsAutoTemplateDTO);
        }
        queryPage.setData(list);
        PageContainer page = PageConvertUtil.pageToContainer(queryPage);
        return JsonUtils.toJson(page);
    }

    /**
     * 短信发送
     */
    @RequestMapping(value = "/sendSMS", method = RequestMethod.POST)
    @ResponseBody
    public R sendSms(@ModelAttribute("smsModel") AccessSmsBO accessSmsBO,
                     @RequestParam(value = "arr[]", required = false) String[] arr, HttpServletRequest request) {

        if (arr != null) {
            R r = null;
            for (String ar : arr) {
                if (ar.contains("{") || ar.contains("}") || ar.contains("#*#")) {
                    r = R.error("内容不能含有关键字符'{'、'}'或'#*#'");
                    break;
                }
            }
            if (r != null) {
                return r;
            }
            accessSmsBO.setContents(arr);
        }

        SmsAccountModelPo userInfo = ServletUtil.getLoginUserInfo(request);
        accessSmsBO.setClientid(userInfo.getClientId());
        accessSmsBO.setPassword(SecurityUtils.encryptMD5(userInfo.getPassword()));

        if (ServletUtil.isOem(userInfo)) {
            R r;
            Map<String, Object> smspResp = smsSendService.sendSmsOem(accessSmsBO);
            if ((Boolean) smspResp.get("success")) {
                r = R.ok(smspResp.get("msg").toString());
            } else {
                r = R.error(smspResp.get("msg").toString());
            }
            return r;
        } else {
            try {
                HttpProtocolType httpProtocolType = HttpProtocolType.getInstanceByValue(userInfo.getHttpProtocolType());
                return smsSendService.sendSms(accessSmsBO,httpProtocolType);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return R.error("发送短信错误");
        }
    }

    private ResultVO send(SmsAccountModelPo userInfo, JsmsAccessSmsDTO jsmsAccessSmsDTO) {
        ResultVO resultVO;
        if (ServletUtil.isOem(userInfo)) {
            resultVO = smsSendService.oemSmsSend(jsmsAccessSmsDTO, userInfo.getClientId());
            resultVO.setCode(Code.REDIRECT);
        } else {
            try {
                AccessSmsBO accessSmsBO = new AccessSmsBO();
                accessSmsBO.setClientid(userInfo.getClientId());
                accessSmsBO.setPassword(SecurityUtils.encryptMD5(userInfo.getPassword()));
                accessSmsBO.setContent(jsmsAccessSmsDTO.getContent());
                accessSmsBO.setMobile(jsmsAccessSmsDTO.getMobile());
                accessSmsBO.setSmstype(jsmsAccessSmsDTO.getSmstype());

                StringBuilder content = new StringBuilder("【");
                content.append(jsmsAccessSmsDTO.getSign())
                        .append("】")
                        .append(jsmsAccessSmsDTO.getContent());
                accessSmsBO.setContent(content.toString());
                HttpProtocolType httpProtocolType = HttpProtocolType.getInstanceByValue(userInfo.getHttpProtocolType());

                R r = smsSendService.sendSms(accessSmsBO,httpProtocolType);
                if (r.getCode() == 0) {
                    resultVO = ResultVO.successDefault(r.getMsg());
                } else {
                    resultVO = ResultVO.failure(r.getMsg());
                }
            } catch (Exception e) {
                logger.error("短信发送异常 --> {}", e);
                resultVO = ResultVO.failure("发送短信错误");
            }
        }
        return resultVO;
    }

    /**
     * 短信定时发送
     */
    @RequestMapping(value = "/sendSMSTim", method = RequestMethod.POST)
    @ResponseBody
    public ResultVO sendSMSTim(@RequestBody AccessTimerSmsDto accessTimerSmsDto, HttpSession session) {
        String clientid = session.getAttribute("clientid").toString();
        ResultVO resultVO = new ResultVO();
        accessTimerSmsDto.getJsmsAccessTimerSms().setClientid(clientid);
        resultVO = smsSendService.sendTimSmsOem(accessTimerSmsDto.getJsmsAccessTimerSms(), clientid, accessTimerSmsDto.getTaskId(), accessTimerSmsDto.getChargeNumTotal(), accessTimerSmsDto.getSubmitFlag());
        return resultVO;
    }

    /**
     * OEM短信发送
     */
    @RequestMapping(value = "/newOemSendSMS", method = RequestMethod.POST)
    @ResponseBody
    public ResultVO newOemSendSMS(@RequestBody JsmsAccessSmsDTO jsmsAccessSmsDTO, HttpServletRequest request) {
        SmsAccountModelPo userInfo = ServletUtil.getLoginUserInfo(request);
        ResultVO resultVO;
//        resultVO = smsSendService.oemSmsSend(jsmsAccessSmsDTO, userInfo.getClientId());
        resultVO = send( userInfo, jsmsAccessSmsDTO);
        return resultVO;
    }

    @RequestMapping(value = "/importMobile", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> importMobile(@RequestParam("excel") CommonsMultipartFile excel,
                                            HttpServletRequest request) {
        SmsAccountModel userInfo = ServletUtil.getLoginUserInfo(request);
        Map<String, Object> result = new HashMap<>();
        Date date1 = new Date();
        try {
            logger.debug("clientid={}正在批量导入手机号", userInfo.getClientId());
            result = smsSendService.importMobile(excel);
        } catch (Exception e) {
            logger.error("短信号码批量导入失败 ", e);
            result.put("success", false);
            result.put("msg", "短信号码批量导入失败，请联系客服");
        }
        Date date2 = new Date();
        result.put("time", date2.getTime() - date1.getTime());
        logger.debug("clientid={}批量导入手机号导入完成, 耗时={} result={}", userInfo.getClientId(),
                date2.getTime() - date1.getTime(), JsonUtils.toJson(result));
        return result;
    }

    @RequestMapping(value = "/mobile-example")
    public ResponseEntity<byte[]> download(HttpServletRequest request) {

        String path = request.getSession().getServletContext().getRealPath("/template/sms-template.xlsx");
        String fileName = "手机号导入模板.xlsx";
        return example(path, fileName);
    }

    @RequestMapping(value = "/mobile-example-csv")
    public ResponseEntity<byte[]> downloadCSV(HttpServletRequest request) {

        String path = request.getSession().getServletContext().getRealPath("/template/sms-template.csv");
        String fileName = "手机号导入模板.csv";
        return example(path, fileName);
    }

    @RequestMapping(value = "/mobile-example-txt")
    public ResponseEntity<byte[]> downloadTXT(HttpServletRequest request) {

        String path = request.getSession().getServletContext().getRealPath("/template/sms-template.txt");
        String fileName = "手机号导入模板.txt";
        return example(path, fileName);
    }

    public ResponseEntity<byte[]> example(String path, String fileName) {
        File downloadFile = new File(path);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        try {
            headers.setContentDispositionFormData("attachment", new String(fileName.getBytes("utf8"), "ISO-8859-1"));
        } catch (UnsupportedEncodingException e) {
            headers.setContentDispositionFormData("attachment", fileName);
        }
        try {
            return new ResponseEntity<byte[]>(org.apache.commons.io.FileUtils.readFileToByteArray(downloadFile), headers,
                    HttpStatus.CREATED);
        } catch (IOException e) {
            logger.debug("下载模板异常-->{}", e);
            return null;
        }
    }

    /**
     * 短信发送记录
     */
    @RequestMapping(value = "/record")
    public ModelAndView record(HttpServletRequest request, HttpSession session, ModelAndView modelAndView)
            throws Exception {
        JsmsSystemErrorDesc jsmsSystemErrorDesc = new JsmsSystemErrorDesc();
        SmsAccountModelPo accountModel = ServletUtil.getLoginUserInfo(request);
        modelAndView.addObject("accountModel", accountModel);
        everyPageShouldHave(modelAndView, request.getServerName(), "/bussiness/record", request);
        modelAndView.setViewName("bussiness/record");

        Map<String, String> params = ServletUtil.getFormData(request);
        params.put("clientid", accountModel.getClientId());
        String start_time = params.get("start_time");
        String end_time = params.get("end_time");
        String mobile = params.get("mobile");
        String content = params.get("content");
        String state = params.get("state");
        // 设置默认时间
        if (start_time == null || end_time == null) {
            DateTime dt = DateTime.now();
            start_time = dt.withMillisOfDay(0).toString("yyyy-MM-dd HH:mm:ss");
            end_time = dt.toString("yyyy-MM-dd HH:mm:ss");
            params.put("start_time", start_time);
            params.put("end_time", end_time);
        }
        request.setAttribute("start_time", start_time);
        request.setAttribute("end_time", end_time);
        request.setAttribute("mobile", mobile);
        request.setAttribute("content", content);
        request.setAttribute("state", state);
        // 查询短信记录
        Map<String, Object> p = new HashMap<String, Object>();
        p.putAll(params);

        boolean isOem = ServletUtil.isOem(accountModel);
        if (isOem) {
            modelAndView.addObject("minDate", DateUtil.dateToStr(DateUtil.getDateFromToday(-45), "yyyy-MM-dd"));
        } else {
            DateTime dt = DateTime.now();
            request.setAttribute("min_time", dt.minusMonths(3).toString("yyyy-MM-dd"));
            request.setAttribute("max_time", dt.toString("yyyy-MM-dd"));
            buildSMSType(p);
        }
        p.put("isOem", ServletUtil.isOem(accountModel));

        PageContainer page = bussinessService.querySmsSendRecord(p);
        if (page.getList().size() > 0) {
            for (int i = 0; i < page.getList().size(); i++) {
                if (String.valueOf(page.getList().get(i).get("state")).equals("0")) {
                    page.getList().get(i).put("status", "发送中");
                    page.getList().get(i).put("errorcode_name", "-");
                } else if (String.valueOf(page.getList().get(i).get("state")).equals("1")) {
                    page.getList().get(i).put("status", "未知");
                    page.getList().get(i).put("errorcode_name", "-");
                } else if (String.valueOf(page.getList().get(i).get("state")).equals("3")) {
                    page.getList().get(i).put("status", "发送成功");
                    page.getList().get(i).put("errorcode_name", "-");
                } else if (String.valueOf(page.getList().get(i).get("state")).equals("4")) {
                    page.getList().get(i).put("status", "发送失败");
                    page.getList().get(i).put("errorcode_name", "其它错误" + page.getList().get(i).get("submit"));
                } else if (String.valueOf(page.getList().get(i).get("state")).equals("6")) {
                    page.getList().get(i).put("status", "发送失败");
                    page.getList().get(i).put("errorcode_name", "其它错误" + page.getList().get(i).get("report"));
                } else {
                    page.getList().get(i).put("status", "拦截");
                    String errorcode = page.getList().get(i).get("errorcode").toString();
                    if (com.ucpaas.sms.common.util.StringUtils.isNotBlank(errorcode)) {
                        jsmsSystemErrorDesc = jsmsSystemErrorDescService.getBySyscode(errorcode.substring(0, 7));
                        if (jsmsSystemErrorDesc != null) {
                            (page.getList().get(i)).put("errorcode_name", errorcode.substring(0, 7) + "-" + jsmsSystemErrorDesc.getClientSideNote());
                        } else {
                            (page.getList().get(i)).put("errorcode_name", errorcode.substring(0, 7) + "-" + "原因不明");
                        }
                    } else {
                        (page.getList().get(i)).put("errorcode_name", "");
                    }
                }
            }
        }
        // 指定返回视图和模型
        modelAndView.addObject("page", page);

        return modelAndView;
    }

    // 短信导出实时检查数据
    @RequestMapping(value = "/checkSmsRecordNum")
    @ResponseBody
    public AjaxResult checkSmsRecordNum(HttpServletRequest request) throws Exception {
        AjaxResult ajaxResult = new AjaxResult();
        SmsAccountModelPo accountModel = ServletUtil.getLoginUserInfo(request);
        Map<String, String> params = ServletUtil.getFormData(request);
        params.put("clientid", ServletUtil.getLoginUserInfo(request).getClientId());
        String start_time = params.get("start_time");
        String end_time = params.get("end_time");

        // 设置默认时间
        if (start_time == null || end_time == null) {
            DateTime dt = DateTime.now();
            start_time = dt.withMillisOfDay(0).toString("yyyy-MM-dd HH:mm");
            end_time = dt.toString("yyyy-MM-dd HH:mm");
            params.put("start_time", start_time);
            params.put("end_time", end_time);
        }

        // 查询短信记录数量
        Map<String, Object> p = new HashMap<String, Object>();
        p.putAll(params);

        boolean isOem = ServletUtil.isOem(accountModel);
        if (!isOem) {
            buildSMSType(p);
        }

        List<Map<String, Object>> resultList = bussinessService.querySmsSendRecordCount(p);

        int totalCount = 0;
        if (resultList != null && resultList.size() > 0) {
            totalCount = Integer.parseInt(resultList.get(0).get("totalCount").toString());
        }

        if (totalCount == 0) {
            ajaxResult.setIsSuccess(false);
        } else {
            ajaxResult.setIsSuccess(true);
        }

        return ajaxResult;
    }

    @RequestMapping(value = "/exportRecord")
    @ResponseBody
    public Map exportRecord(HttpServletRequest request, HttpServletResponse response, HttpSession session)
            throws Exception {
        JsmsSystemErrorDesc jsmsSystemErrorDesc = new JsmsSystemErrorDesc();
        List<Map<String, Object>> list = new ArrayList<>();
        List<Map<String, Object>> newList = new ArrayList<>();
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("success", false);
        result.put("msg", "生成报表失败");
        try {
            SmsAccountModelPo accountModel = (SmsAccountModelPo) session.getAttribute(Constant.LOGIN_USER_INFO);
            if (accountModel == null) {
                result.put("success", false);
                result.put("msg", "非法请求");
                return result;
            }
            String clientId = accountModel.getClientId();
            Map<String, String> params = ServletUtil.getFormData(request);
            params.put("clientid", clientId);
            String start_time = params.get("start_time");
            String end_time = params.get("end_time");
            StringBuffer fileName = new StringBuffer();
            fileName.append("短信记录-");

            boolean isOem = ServletUtil.isOem(accountModel);

            // 设置默认时间
            if (start_time == null || end_time == null) {
                DateTime dt = DateTime.now();
                start_time = dt.minusMinutes(3).toString("yyyy-MM-dd HH:mm");
                if (isOem) {
                    start_time = dt.withMillisOfDay(0).toString("yyyy-MM-dd HH:mm");
                }
                end_time = dt.toString("yyyy-MM-dd HH:mm");
                params.put("start_time", start_time);
                params.put("end_time", end_time);
            }
            Date endTime = DateUtil.stringToDate(end_time, "yyyy-MM-dd HH:mm");
            fileName.append(DateUtil.dateToStr(endTime, "yyyyMMddHHmm"));
            fileName.append(".xls");

            String text = params.get("text");
            String filePath = ConfigUtils.temp_file_dir + "/" + clientId + "/" + fileName.toString();

            Excel excel = new Excel();
            excel.setFilePath(filePath);
            excel.setTitle("短信记录");
            if (text != null) {
                excel.addRemark("查询条件：" + text);
            }
            excel.addHeader(20, "手机号", "phone");
            if (!isOem) {
                excel.addHeader(20, "短信类型", "smstype_name");
            }
            excel.addHeader(20, "发送内容", "content");
            excel.addHeader(20, "发送状态", "status");
            excel.addHeader(20, "状态码", "errorcode_name");
            if (!isOem) {
                excel.addHeader(20, "提交时间", "subTime");
            }
            excel.addHeader(20, "发送时间", "sendTime");
            if (isOem) {
                excel.addHeader(20, "计费条数", "charge_num");
            }

            Map<String, Object> p = new HashMap<String, Object>();
            p.putAll(params);

            if (!isOem) {
                buildSMSType(p);
            }
            p.put("isOem", ServletUtil.isOem(accountModel));
            list = bussinessService.querySmsRecord4Excel(p);
            if (list.size() > 0) {
                for (int i = 0; i < list.size(); i++) {
                    if (String.valueOf(list.get(i).get("state")).equals("0")) {
                        list.get(i).put("status", "发送中");
                        list.get(i).put("errorcode_name", "-");
                    } else if (String.valueOf(list.get(i).get("state")).equals("1")) {
                        list.get(i).put("status", "未知");
                        list.get(i).put("errorcode_name", "-");
                    } else if (String.valueOf(list.get(i).get("state")).equals("3")) {
                        list.get(i).put("status", "发送成功");
                        list.get(i).put("errorcode_name", "-");
                    } else if (String.valueOf(list.get(i).get("state")).equals("4")) {
                        list.get(i).put("status", "发送失败");
                        list.get(i).put("errorcode_name", "其它错误" + list.get(i).get("submit"));
                    } else if (String.valueOf(list.get(i).get("state")).equals("6")) {
                        list.get(i).put("status", "发送失败");
                        list.get(i).put("errorcode_name", "其它错误" + list.get(i).get("report"));
                    } else {
                        list.get(i).put("status", "拦截");
                        String errorcode = list.get(i).get("errorcode").toString();
                        if (com.ucpaas.sms.common.util.StringUtils.isNotBlank(errorcode)) {
                            jsmsSystemErrorDesc = jsmsSystemErrorDescService.getBySyscode(errorcode.substring(0, 7));
                            if (jsmsSystemErrorDesc != null) {
                                (list.get(i)).put("errorcode_name", errorcode.substring(0, 7) + "-" + jsmsSystemErrorDesc.getClientSideNote());
                            } else {
                                (list.get(i)).put("errorcode_name", errorcode.substring(0, 7) + "-" + "原因不明");
                            }
                        } else {
                            (list.get(i)).put("errorcode_name", "");
                        }
                    }
                }
            }
            excel.setDataList(list);
            if (ExcelUtils.exportExcel(excel)) {
                result.put("success", true);
                result.put("msg", "报表生成成功");
                result.put("fileName", fileName.toString());
                return result;
            }
        } catch (Exception e) {
            logger.error("生成报表失败", e);
        }
        return result;

    }

    private void buildSMSType(Map<String, Object> p) {
        List<Integer> smsTypes = new ArrayList<>();
        smsTypes.add(SMSType.通知短信.getValue());
        smsTypes.add(SMSType.验证码短信.getValue());
        smsTypes.add(SMSType.营销短信.getValue());
        p.put("smsTypes", smsTypes);
    }

    @RequestMapping(value = "/downloadExcel")
    @ResponseBody
    public void downloadExcel(HttpSession session, HttpServletResponse response, String fileName) throws Exception {
        SmsAccountModelPo accountModel = (SmsAccountModelPo) session.getAttribute(Constant.LOGIN_USER_INFO);
        if (accountModel == null) {
            logger.info("非法用户请求导出报表，请求参数fileName{}", fileName);
            return;
        }
        String filePath = ConfigUtils.temp_file_dir + "/" + accountModel.getClientId() + "/" + fileName;
        logger.debug("下载报表fileName={}", filePath);
        FileUtils.download(response, filePath);
        FileUtils.delete(filePath);
    }

    @RequestMapping(value = "/price")
    public ModelAndView price(String countryInfo, PageParam pageParam, ModelAndView mv, HttpServletRequest request,
                              HttpSession session) throws Exception {
        SmsAccountModelPo accountModel = ServletUtil.getLoginUserInfo(request);
        mv.addObject("accountModel", accountModel);
        everyPageShouldHave(mv, request.getServerName(), "/bussiness/price", request);
        PageBean pageBean = bussinessService.queryInterShortMessage(pageParam, countryInfo);
        mv.addObject("countryInfo", countryInfo);
        mv.addObject("pageBean", pageBean);
        mv.setViewName("bussiness/price");

        return mv;
    }

    /**
     * 短信模版页面
     */
    @RequestMapping(value = "/template", method = RequestMethod.GET)
    public ModelAndView template(HttpServletRequest request, ModelAndView mv) {
        SmsAccountModelPo accountModel = ServletUtil.getLoginUserInfo(request);
        mv.addObject("accountModel", accountModel);
        try {
            everyPageShouldHave(mv, request.getServerName(), "/bussiness/template", request);
        } catch (Exception e) {
            logger.error("系统错误  ---> {}", e);
        }
        mv.setViewName("bussiness/template");
        return mv;
    }

    //普通
    @RequestMapping(value = "/statistics")
    public ModelAndView statistics(HttpServletRequest request, ModelAndView mv, HttpSession session) {

        SmsAccountModelPo accountModel = ServletUtil.getLoginUserInfo(request);
        mv.addObject("accountModel", accountModel);
        try {
            everyPageShouldHave(mv, request.getServerName(), "/bussiness/statistics", request);
        } catch (Exception e) {
            logger.error("系统错误  ---> {}", e);
        }

        Map<String, Object> formData = new HashMap<>();
        getStatisticFormData(request, formData);
        formData.put("paytype", accountModel.getPaytype());
        formData.put("clientId", accountModel.getClientId());
        formData.put("isOem", ServletUtil.isOem(accountModel));
        formData.put("isZk", ServletUtil.isZk(accountModel));
        formData.put("isBr", ServletUtil.isBr(accountModel));
        PageContainer statPage = new PageContainer();
        Map subtotal = new HashMap<>();
        try {
            statPage = bussinessService.queryStatistic(formData);
            subtotal = bussinessService.queryStatisticTotal(formData);
        } catch (Exception e) {
            logger.error("查询数据统计错误", e);
        }

        mv.addObject("subtotal", subtotal);
        mv.addObject("statPage", statPage);
        mv.addObject("start_time", formData.get("start_time"));
        mv.addObject("end_time", formData.get("end_time"));
        mv.addObject("minDate", DateUtil.dateToStr(DateUtil.getDateFromToday(-90), "yyyy-MM-dd"));
        mv.addObject("maxDate", DateUtil.dateToStr(DateUtil.getDateFromToday(-1), "yyyy-MM-dd"));
        mv.addObject("period", formData.get("period"));
        mv.addObject("smstype", formData.get("smstype"));
        mv.addObject("producttype", formData.get("producttype"));

        // 返回视图
        mv.setViewName("bussiness/statistics");
        return mv;
    }

    //国际
    @RequestMapping(value = "/statistics/isms")
    public ModelAndView isms(HttpServletRequest request, ModelAndView mv, HttpSession session) {
        SmsAccountModelPo accountModel = ServletUtil.getLoginUserInfo(request);
        mv.addObject("accountModel", accountModel);
        try {
            everyPageShouldHave(mv, request.getServerName(), "/bussiness/statistics", request);
        } catch (Exception e) {
            logger.error("系统错误  ---> {}", e);
        }

        Map<String, Object> formData = new HashMap<>();
        if (formData.get("product_type") == null) {
            formData.put("producttype", 2);
        }
        getStatisticFormData(request, formData);
        formData.put("paytype", accountModel.getPaytype());
        formData.put("clientId", accountModel.getClientId());
        formData.put("isOem", ServletUtil.isOem(accountModel));
        formData.put("isZk", ServletUtil.isZk(accountModel));
        formData.put("isBr", ServletUtil.isBr(accountModel));
        PageContainer statPage = new PageContainer();
        Map subtotal = new HashMap<>();
        try {
            statPage = bussinessService.queryStatistic(formData);
            subtotal = bussinessService.queryStatisticTotal(formData);
        } catch (Exception e) {
            logger.error("查询数据统计错误", e);
        }

        mv.addObject("subtotal", subtotal);
        mv.addObject("statPage", statPage);
        mv.addObject("start_time", formData.get("start_time"));
        mv.addObject("end_time", formData.get("end_time"));
        mv.addObject("minDate", DateUtil.dateToStr(DateUtil.getDateFromToday(-90), "yyyy-MM-dd"));
        mv.addObject("maxDate", DateUtil.dateToStr(DateUtil.getDateFromToday(-1), "yyyy-MM-dd"));
        mv.addObject("period", formData.get("period"));
        mv.addObject("smstype", formData.get("smstype"));
        mv.addObject("producttype", formData.get("producttype"));

        // 返回视图
        mv.setViewName("bussiness/isms");
        return mv;
    }

    @RequestMapping(value = "/exportStatistics")
    @ResponseBody
    public Map exportStatistics(HttpServletRequest request, ModelAndView mv, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("msg", "生成报表失败");

        SmsAccountModelPo accountModel = ServletUtil.getLoginUserInfo(request);
        mv.addObject("accountModel", accountModel);
        try {
            everyPageShouldHave(mv, request.getServerName(), "/bussiness/statistics", request);
        } catch (Exception e) {
            logger.error("系统错误  ---> {}", e);
        }

        Map<String, Object> formData = new HashMap<>();
        getStatisticFormData(request, formData);
        formData.put("paytype", accountModel.getPaytype());
        formData.put("clientId", accountModel.getClientId());
        formData.put("isOem", ServletUtil.isOem(accountModel));
        try {
            formData.put("limit", "LIMIT 0 , 60000");
            List<Map<String, Object>> data = bussinessService.exportStatistics(formData);
            if (data == null || data.size() < 1) {
                result.put("msg", "根据条件查询到的记录数为0，导出Excel文件失败");
                return result;
            }
            this.changeChinese(data);

            StringBuffer fileName = new StringBuffer();
            String product_type = (String) (formData.get("producttype") == null ? "" : formData.get("producttype"));
            logger.info("页面中传的产品类型值为product_type={}", product_type);
            if ("2".equals(product_type)) {
                fileName.append("国际短信统计");
            } else {
                fileName.append("普通短信统计");
            }
            Excel excel = new Excel();
            fileName.append("_").append(formData.get("startDate")).append("-")
                    .append(DateUtil.getYeasterDay((Integer) formData.get("endDate"))).append("");
            excel.setTitle(fileName.toString());
            fileName.append(".xls");
            String filePath = ConfigUtils.temp_file_dir + "/" + accountModel.getClientId() + "/" + fileName.toString();
            excel.setFilePath(filePath);
            excel.addHeader(10, "序号", "rownum");
            excel.addHeader(20, "日期", "date");
            excel.addHeader(20, "短信类型", "smstype");
            excel.addHeader(20, "提交条数(条)", "num_all");
            excel.addHeader(20, "计费条数（条）", "chargetotal");
            excel.addHeader(20, "成功条数（条）", "num_sucs");
            excel.addHeader(20, "未知条数(条)", "num_known");
            excel.addHeader(20, "失败条数(条)", "num_fail");
            excel.addHeader(20, "待发送条数(条)", "notsend");
            excel.addHeader(20, "拦截条数(条)", "interceptTotal");
            excel.setShowRownum(false);
            int i = 1;
            for (Map da : data) {
                da.put("rownum", i++);
            }
            int totalCount = data.size();
            Map subtotal = bussinessService.queryStatisticTotal(formData);
            subtotal.put("date", "共计");
            subtotal.put("num_all", subtotal.get("num_all_total"));
            subtotal.put("chargetotal", subtotal.get("chargeTotal_total"));
            subtotal.put("num_sucs", subtotal.get("num_sucs_total"));
            subtotal.put("num_known", subtotal.get("num_known_total"));
            subtotal.put("num_fail", subtotal.get("num_fail_total"));
            subtotal.put("notsend", subtotal.get("num_pending_total"));
            subtotal.put("interceptTotal", subtotal.get("num_intercept_total"));

            data.add(subtotal);
            excel.setDataList(data);
            if (ExcelUtils.exportExcel(excel, true)) {
                result.put("success", true);
                result.put("msg", "报表生成成功");
                result.put("fileName", fileName.toString());
                return result;
            }
        } catch (Exception e) {
            logger.error("行业短信统计报表生成失败", e);
        }
        return result;
    }

    private void getStatisticFormData(HttpServletRequest request, Map<String, Object> formData) {
        Calendar yeasterCal = Calendar.getInstance();
        yeasterCal.setTime(new Date());
        yeasterCal.add(Calendar.DATE, -1);
        String yesterday_time = DateUtil.dateToStr(yeasterCal.getTime(), "yyyy-MM-dd");
        String start_time = yesterday_time;
        String end_time = yesterday_time;
        // 日发送数据分页查询
        String value;
        for (Map.Entry<String, String[]> map : request.getParameterMap().entrySet()) {
            value = StringUtils.join(map.getValue(), ",");
            if (StringUtils.isNotBlank(value)) {
                formData.put(map.getKey(), value.trim());
            }
        }

        start_time = (String) (formData.get("start_time") == null ? yesterday_time : formData.get("start_time"));
        end_time = (String) (formData.get("end_time") == null ? yesterday_time : formData.get("end_time"));
        int period = formData.get("period") == null ? 0 : Integer.valueOf((String) formData.get("period"));
        if (period == 0) { // 昨天
            int yesterday = DateUtil.getDateFromTodayInInt(-1);
            formData.put("startDate", yesterday);
            formData.put("endDate", Integer.valueOf(yesterday_time.replace("-", "")));
        }
        if (period == 1) { // 30天
            formData.put("startDate", DateUtil.getDateFromTodayInInt(-30));
            formData.put("endDate", Integer.valueOf(yesterday_time.replace("-", "")));
        }
        if (period == 2) {
            formData.put("startDate", Integer.valueOf(start_time.replace("-", "")));
            formData.put("endDate", Integer.valueOf(end_time.replace("-", "")));
        }

        formData.put("start_time", start_time);
        formData.put("end_time", end_time);
    }

    /**
     * 转换短信类型和产品类型
     *
     * @param data
     * @return
     */
    private List<Map<String, Object>> changeChinese(List<Map<String, Object>> data) {
        for (Map<String, Object> datum : data) {
            // 短信类型转中文
            if ("0".equals(datum.get("smstype"))) {
                datum.put("smstype", "通知");
            } else if ("4".equals(datum.get("smstype"))) {
                datum.put("smstype", "验证码");
            } else if ("5".equals(datum.get("smstype"))) {
                datum.put("smstype", "营销");
            } else if ("6".equals(datum.get("smstype"))) {
                datum.put("smstype", "告警");
            } else if ("7".equals(datum.get("smstype"))) {
                datum.put("smstype", "USSD");
            } else if ("8".equals(datum.get("smstype"))) {
                datum.put("smstype", "闪信");
            }
            // 产品类型转中文
            if ("0".equals(datum.get("product_type"))) {
                datum.put("product_type", "行业");
            } else if ("1".equals(datum.get("product_type"))) {
                datum.put("product_type", "营销");
            } else if ("2".equals(datum.get("product_type"))) {
                datum.put("product_type", "国际");
            } else if ("7".equals(datum.get("product_type"))) {
                datum.put("product_type", "USSD");
            } else if ("8".equals(datum.get("product_type"))) {
                datum.put("product_type", "闪信");
            } else if ("9".equals(datum.get("product_type"))) {
                datum.put("product_type", "挂机短信");
            }
        }
        return data;
    }

    /**
     * 定时短信入口/路由
     *
     * @param mv
     * @return
     */
    @RequestMapping(path = "/smsTimerSend", method = RequestMethod.GET)
    public ModelAndView smsTimerSend(ModelAndView mv, HttpSession session) throws Exception {
        SmsAccountModelPo accountModel = (SmsAccountModelPo) session.getAttribute(Constant.LOGIN_USER_INFO);
        mv.addObject("accountModel", accountModel);
        everyPageShouldHave(mv, request.getServerName(), "/bussiness/send", request);
        mv.setViewName("bussiness/timerSend");
        return mv;
    }

    /**
     * 定时短信入口/路由
     *
     * @param mv
     * @return
     */
    @RequestMapping(path = "/smsTimerTask", method = RequestMethod.GET)
    public ModelAndView smsTimerTask(ModelAndView mv, HttpSession session) throws Exception {
        SmsAccountModelPo accountModel = (SmsAccountModelPo) session.getAttribute(Constant.LOGIN_USER_INFO);
        mv.addObject("accountModel", accountModel);
        everyPageShouldHave(mv, request.getServerName(), "/bussiness/send", request);
        mv.setViewName("bussiness/timerTask");
        return mv;
    }

    /**
     * 获取定时短信内容
     *
     * @param params
     * @return
     */
    @RequestMapping(path = "/smsTimerSend", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "定时短信查询", notes = "定时短信查询", tags = "短信服务", response = JsmsPage.class)
    @ApiImplicitParams({@ApiImplicitParam(name = "page", value = "当前页码", dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "rows", value = "每页行数", dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "status", value = "任务状态", dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "selectTimeFlag", value = "时间选择", dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "timeStart", value = "开始时间", dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "timeEnd", value = "结束时间", dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "taskId", value = "任务ID", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "smstype", value = "短信类型", dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "sign", value = "短信签名", dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "content", value = "短信内容", dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "params", value = "参数", dataType = "int", paramType = "query")})
    public String smsTimerSendQuery(@RequestParam Map<String, String> params, HttpSession session) {
        SmsAccountModelPo accountModel = (SmsAccountModelPo) session.getAttribute(Constant.LOGIN_USER_INFO);
        /*params.put("submittype", String.valueOf(TaskSubmitTypeEnum.子账户.getValue()));*/
        params.put("clientId", accountModel.getClientId());
        if (com.jsmsframework.common.util.StringUtils.isBlank(params.get("smstype"))) {
            params.remove("smstype");
        }
        if (com.jsmsframework.common.util.StringUtils.isBlank(params.get("status"))) {
            params.remove("status");
        }
        JsmsPage jsmsPage = PageConvertUtil.paramToPage(params);
        jsmsPage.setParams(params);
        jsmsPage.setOrderByClause(" submit_time DESC ");
        try {
            jsmsPage = bussinessService.smsTimerSendQuery(jsmsPage, WebId.客户端, accountModel.getClientId());
        } catch (Exception e) {
            logger.error("查询定时短信异常 ----> ", e);
        }
        PageContainer page = PageConvertUtil.pageToContainer(jsmsPage);
        return JsonUtils.toJson(page);
    }


    @RequestMapping(path = "/getAllPhone/list", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "查看号码", notes = "查看号码", tags = "定时短信", response = JsmsPage.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "taskId", value = "任务ID", dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "submitTime", value = "提交时间", dataType = "string", paramType = "query")
    })
    public ResultVO getAllPhone(String taskId, String submitTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<String> phoneList = new ArrayList<>();
        ResultVO resultVO = new ResultVO();
        try {
            phoneList = jsmsTimerSendPhonesService.getAllPhoneOfList(sdf.parse(submitTime), taskId);
        } catch (Exception e) {
            logger.error("查询号码异常 ----> ", e);
        }
        resultVO.setCode(Code.SUCCESS);
        resultVO.setData(org.apache.commons.lang3.StringUtils.join(phoneList, ","));
        return resultVO;
    }

    @RequestMapping(path = "/checkSubmitTime", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "校验时间", notes = "定时短信", tags = "定时短信")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "submitTime", value = "提交时间", dataType = "string", paramType = "query")
    })
    public ResultVO checkSubmitTime(String submitTime) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date submitTimes = null;
        try {
            submitTimes = df.parse(submitTime);
        } catch (ParseException e) {
            logger.debug("日期格式化异常", e);
        }
        java.util.Date date = new Date();
        long l = submitTimes.getTime() - date.getTime();
        long day = l / (24 * 60 * 60 * 1000);
        long hour = (l / (60 * 60 * 1000) - day * 24);
        long min = ((l / (60 * 1000)) - day * 24 * 60 - hour * 60);
        long s = (l / 1000 - day * 24 * 60 * 60 - hour * 60 * 60 - min * 60);
        if (l <= jsmsSendParam.getSendCFG(JsmsSendParam.TimeSendCFG.取消发送最小间隔) * 60 * 1000) {
            return ResultVO.failure(Code.REDIRECT, "本短信即将发送，无法取消！");
        } else {
            return ResultVO.successDefault(Code.SUCCESS, "本短信将在" + day + "天" + hour + "小时" + min + "分" + s + "秒后发送,确认取消此定时短信？");
        }
    }

    @RequestMapping(path = "/undoSend", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "取消发送", notes = "定时短信", tags = "定时短信", response = JsmsPage.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "taskId", value = "任务ID", dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "submitTime", value = "提交时间", dataType = "string", paramType = "query")
    })
    public ResultVO updateStatus(String taskId, String submitTime, HttpSession session) {
        ResultVO resultVO = new ResultVO();
        resultVO = checkSubmitTime(submitTime);
        if (resultVO.getCode().equals(Code.SUCCESS.getValue())) {
            DateTime date = new DateTime();
            String clientid = String.valueOf(session.getAttribute("clientid"));
            return jsmsTimerSendTaskService.cancelSendTask(taskId, date, clientid);
        } else {
            return ResultVO.failure(Code.REDIRECT, "本短信即将发送，无法取消！");
        }
    }

    @RequestMapping(path = "/edit", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "编辑", notes = "定时短信", tags = "定时短信", response = JsmsPage.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "taskId", value = "任务ID", dataType = "int", paramType = "query")
    })
    public ResultVO edit(String taskId) throws ParseException {
        JsmsTimerSendTask jsmsTimerSendTask = jsmsTimerSendTaskService.getByTaskId(taskId);
        TimerSendTaskDTO timerSendTaskDTO = new TimerSendTaskDTO();
        String phoneStr = "";
        if (jsmsTimerSendTask != null) {
            BeanUtil.copyProperties(jsmsTimerSendTask, timerSendTaskDTO);
            phoneStr = jsmsTimerSendPhonesService.getAllPhone(jsmsTimerSendTask.getSubmitTime(), taskId);
            timerSendTaskDTO.setPhoneStr(phoneStr);
            return ResultVO.successDefault(Code.SUCCESS, timerSendTaskDTO);
        } else {
            return ResultVO.failure(Code.OPT_ERR, "编辑异常！");
        }
    }

    /**
     * 短信任务页面
     */
    @RequestMapping(value = "/sendTask", method = RequestMethod.GET)
    public ModelAndView sendTaskView(HttpServletRequest request, ModelAndView mv) {
        mv.addObject("accountModel", ServletUtil.getLoginUserInfo(request));
        try {
            everyPageShouldHave(mv, request.getServerName(), "/bussiness/send", request);
        } catch (Exception e) {
            logger.error("系统错误  ---> {}", e);
        }
        mv.setViewName("bussiness/sendTask");
        return mv;
    }

    @RequestMapping(path = "/sendTask", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "发送任务查询", notes = "发送任务查询", tags = "短信服务", response = JsmsPage.class)
    @ApiImplicitParams({@ApiImplicitParam(name = "page", value = "当前页码", dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "rows", value = "每页行数", dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "creater", value = "创建者", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "createTimeStart", value = "开始时间", dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "createTimeEnd", value = "结束时间", dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "id", value = "任务ID", dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "smstype", value = "短信类型", dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "sign", value = "短信签名", dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "content", value = "短信内容", dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "params", value = "参数", dataType = "int", paramType = "query")})
    public String sendTaskQuery(@RequestParam Map<String, String> params, HttpSession session, JsmsPage jsmsPage) {
        SmsAccountModelPo accountModel = (SmsAccountModelPo) session.getAttribute(Constant.LOGIN_USER_INFO);
        params.put("clientId", accountModel.getClientId());

        jsmsPage.setParams(params);
        jsmsPage.setOrderByClause(" create_time DESC,id DESC ");
        try {
            jsmsPage = jsmsSubmitProgressService.queryPageList(jsmsPage, WebId.客户端, accountModel.getClientId());
        } catch (Exception e) {
            logger.error("查询发送任务异常 ----> ", e);
        }
        PageContainer page = PageConvertUtil.pageToContainer(jsmsPage);
        return JSON.toJSONString(page);
    }

    @PostMapping("/parsefile")
    @ApiOperation(value = "手机号文件导入", notes = "手机号文件导入", tags = "短信服务", response = ResultVO.class, consumes = "application/octet-stream")
    @ResponseBody
    public ResultVO parsefile(String filePath, HttpServletRequest request) {
        if (StringUtils.isBlank(filePath)) {
            return ResultVO.failure(Code.OPT_ERR, "文件路径不存在");
        }
        ResultVO result;
        String sysPath;
        if (ConfigUtils.current_tomcat_data_dir.endsWith("/")) {
            sysPath = ConfigUtils.current_tomcat_data_dir.substring(0, ConfigUtils.current_tomcat_data_dir.lastIndexOf("/"));
        } else if (ConfigUtils.current_tomcat_data_dir.endsWith("\\")) {
            sysPath = ConfigUtils.current_tomcat_data_dir.substring(0, ConfigUtils.current_tomcat_data_dir.lastIndexOf("\\"));
        } else {
            sysPath = ConfigUtils.current_tomcat_data_dir;
        }
        try {
            result = jsmsSendService.parseMobileFile(ConfigUtils.file_download_url, filePath, ConfigUtils.temp_file_dir, sysPath);

        } catch (Exception e) {
            logger.debug("导入文件异常 ---> {}", e);
            result = ResultVO.failure("请求超时,请稍后再试...");
        }
        return result;
    }
}
