package cn.zk.controller;

import cn.zk.common.AdminException;
import cn.zk.common.Resp;
import cn.zk.common.RespCode;
import cn.zk.entity.PathDataVO;
import cn.zk.entity.PathVO;
import cn.zk.entity.ZkInfo;
import cn.zk.service.ZkInfoService;
import cn.zk.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.util.List;

/**
 * <br/>
 * Created on 2018/6/12 19:32.
 *
 * @author zhubenle
 */
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Controller
public class ZkInfoController {

    private final static String ZKINFOS_FAIL_MESSAGE = "zkInfosFailMessage";
    private final static String ZKINFO_FAIL_MESSAGE = "zkInfoFailMessage";

    private final ZkInfoService zkInfoService;

    @GetMapping(value = "/zkinfos")
    public String zkInfos(ModelMap modelMap, @ModelAttribute(ZKINFOS_FAIL_MESSAGE) String zkInfosFailMessage) {
        modelMap.addAttribute("zkinfos", zkInfoService.listAll());
        modelMap.addAttribute(ZKINFOS_FAIL_MESSAGE, zkInfosFailMessage);
        return "views/zkinfos";
    }

    @DeleteMapping(value = "/zkinfo/{alias}")
    @ResponseBody
    public Resp<String> ajaxDeleteZkInfo(@PathVariable(value = "alias") String alias) {
        Resp<String> userResp = new Resp<>();
        try {
            zkInfoService.deleteZkInfoByAlias(alias);
            userResp.success(null);
        } catch (AdminException e) {
            log.error("删除zkInfo失败: {}", e.getCodeMsg());
            userResp.fail(e);
        } catch (Exception e) {
            log.error("删除zkInfo异常", e);
            userResp.fail(RespCode.ERROR_99999, e);
        }
        return userResp;
    }

    @PostMapping(value = "/zkinfo")
    public String saveZkInfo(ZkInfo zkInfo, RedirectAttributesModelMap modelMap) {
        try {
            zkInfoService.saveZkInfo(zkInfo);
        } catch (AdminException e) {
            log.error("删除zkInfo失败: {}", e.getCodeMsg());
            modelMap.addFlashAttribute(ZKINFOS_FAIL_MESSAGE, e.getCodeMsg());
        } catch (Exception e) {
            log.error("删除zkInfo异常", e);
            modelMap.addFlashAttribute(ZKINFOS_FAIL_MESSAGE, e.getMessage());
        }
        return "redirect:zkinfos";
    }

    @GetMapping(value = "/zkinfo/{alias}")
    public String toZkInfo(@PathVariable(value = "alias") String alias, ModelMap modelMap) {
        try {
            List<ZkInfo> zkInfos = zkInfoService.listAll();
            modelMap.addAttribute("zkinfos", zkInfos);
            modelMap.addAttribute("zkinfo", zkInfos.stream()
                    .filter(zkInfo -> zkInfo.getAlias().equals(alias))
                    .findFirst()
                    .orElseThrow(() -> new AdminException(RespCode.ERROR_10004)));
        } catch (AdminException e) {
            log.error("获取alias={}失败: {}", alias, e.getCodeMsg());
            modelMap.addAttribute(ZKINFO_FAIL_MESSAGE, e.getCodeMsg());
        } catch (Exception e) {
            log.error("获取alias={}异常", alias, e);
            modelMap.addAttribute(ZKINFO_FAIL_MESSAGE, e.getMessage());
        }
        return "views/zkinfo";
    }

    @PostMapping(value = "/zkinfo/reconnect/{alias}")
    @ResponseBody
    public Resp<String> ajaxReconnectZk(@PathVariable(value = "alias") String alias) {
        Resp<String> resp = new Resp<>();
        try {
            zkInfoService.reconnectZk(alias);
        } catch (AdminException e) {
            log.error("重连alias={} zookeeper失败: {}", alias, e.getCodeMsg());
            resp.fail(e);
        } catch (Exception e) {
            log.error("重连alias={} zookeeper异常", alias, e);
            resp.fail(RespCode.ERROR_99999, e);
        }
        return resp;
    }

    /**
     * 获取zookeeper路径下的子路径列表
     */
    @GetMapping(value = "/zkinfo/path/{alias}")
    @ResponseBody
    public Resp<List<PathVO>> ajaxGetPath(@PathVariable(value = "alias") String alias,
                                          @RequestParam(value = "id", required = false, defaultValue = "") String id) {
        Resp<List<PathVO>> userResp = new Resp<>();
        try {
            userResp.success(zkInfoService.listZkChildrenPath(alias, id));
        } catch (AdminException e) {
            log.error("获取alias={}, path={}子路径失败: {}", alias, id, e.getCodeMsg());
            userResp.fail(e);
        } catch (Exception e) {
            log.error("获取alias={}, path={}子路径异常", alias, id, e);
            userResp.fail(RespCode.ERROR_99999, e);
        }
        return userResp;
    }

    /**
     * 删除zookeeper指定路径
     */
    @DeleteMapping(value = "/path/{alias}")
    @ResponseBody
    public Resp<String> ajaxDeletePath(@PathVariable(value = "alias") String alias,
                                       @RequestParam(value = "dataVersion") Integer dataVersion,
                                       @RequestParam(value = "pathId") String pathId) {
        Resp<String> userResp = new Resp<>();
        try {
            zkInfoService.deletePath(alias, pathId, dataVersion);
            userResp.success(null);
        } catch (AdminException e) {
            log.error("删除alias={}, path={}节点失败: {}", alias, pathId, e.getCodeMsg());
            userResp.fail(e);
        } catch (Exception e) {
            log.error("删除alias={}, path={}节点异常", alias, pathId, e);
            userResp.fail(RespCode.ERROR_99999, e);
        }
        return userResp;
    }

    /**
     * 添加zookeeper路径
     */
    @PostMapping(value = "/path/add/{alias}")
    @ResponseBody
    public Resp<String> ajaxCreatePath(@PathVariable(value = "alias") String alias,
                                       @RequestParam(value = "pathId") String pathId,
                                       @RequestParam(value = "data", required = false) String data,
                                       @RequestParam(value = "createMode", required = false, defaultValue = "0") Integer createMode) {
        Resp<String> userResp = new Resp<>();
        try {
            userResp.success(zkInfoService.createPath(alias, pathId, data, createMode));
        } catch (AdminException e) {
            log.error("创建alias={}, path={}节点失败: {}", alias, pathId, e.getCodeMsg());
            userResp.fail(e);
        } catch (Exception e) {
            log.error("创建alias={}, path={}节点异常", alias, pathId, e);
            userResp.fail(RespCode.ERROR_99999, e);
        }
        return userResp;
    }

    /**
     * 编辑zookeeper路径(有子节点的路径只能修改值，没子节点的路径都可以修改)
     */
    @PostMapping(value = "/path/edit/{alias}")
    @ResponseBody
    public Resp<String> ajaxEditPath(@PathVariable(value = "alias") String alias,
                                     @RequestParam(value = "newPathId") String newPathId,
                                     @RequestParam(value = "oldPathId") String oldPathId,
                                     @RequestParam(value = "dataVersion") Integer dataVersion,
                                     @RequestParam(value = "data", required = false) String data,
                                     @RequestParam(value = "createMode") Integer createMode) {
        Resp<String> userResp = new Resp<>();
        try {
            userResp.success(zkInfoService.updatePath(alias, newPathId, oldPathId, data, dataVersion, createMode));
        } catch (AdminException e) {
            log.error("修改alias={}, path={}节点失败: {}", alias, oldPathId, e.getCodeMsg());
            userResp.fail(e);
        } catch (Exception e) {
            log.error("修改alias={}, path={}节点异常", alias, oldPathId, e);
            userResp.fail(RespCode.ERROR_99999, e);
        }
        return userResp;
    }

    /**
     * 获取zookeeper路径节点的数据和状态信息
     */
    @GetMapping(value = "/path/data/{alias}")
    @ResponseBody
    public Resp<PathDataVO> ajaxGetPathData(@PathVariable(value = "alias") String alias,
                                            @RequestParam(value = "pathId", required = false, defaultValue = "") String pathId) {
        Resp<PathDataVO> userResp = new Resp<>();
        try {
            userResp.success(StringUtils.isEmpty(pathId) ? null : zkInfoService.getPathData(alias, pathId));
        } catch (AdminException e) {
            log.error("获取alias={}, path={}数据失败: {}", alias, pathId, e.getCodeMsg());
            userResp.fail(e);
        } catch (Exception e) {
            log.error("获取alias={}, path={}数据异常", alias, pathId, e);
            userResp.fail(RespCode.ERROR_99999, e);
        }
        return userResp;
    }

    /**
     * 复制一个节点所有信息粘贴到另一个节点下
     */
    @GetMapping(value = "/path/{alias}/copy/paste")
    @ResponseBody
    public Resp<PathDataVO> copyPastePath(@PathVariable(value = "alias") String alias,
                                          @RequestParam(value = "copy") String copy,
                                          @RequestParam(value = "paste") String paste,
                                          @RequestParam(value = "nPaste", required = false) String nPaste) {
        Resp<PathDataVO> userResp = new Resp<>();
        try {
            zkInfoService.copyPastePath(alias, copy, paste, nPaste);
            userResp.success();
        } catch (AdminException e) {
            log.error("复制alias={}, path={}到path={}下失败: {}", alias, copy, paste, e.getCodeMsg());
            userResp.fail(e);
        } catch (Exception e) {
            log.error("复制alias={}, path={}到path={}下异常", alias, copy, paste, e);
            userResp.fail(RespCode.ERROR_99999, e);
        }
        return userResp;
    }
}
