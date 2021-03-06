package cn.iocoder.yudao.adminserver.modules.tool.controller.codegen;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.ZipUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.adminserver.modules.tool.controller.codegen.vo.ToolCodegenDetailRespVO;
import cn.iocoder.yudao.adminserver.modules.tool.controller.codegen.vo.ToolCodegenPreviewRespVO;
import cn.iocoder.yudao.adminserver.modules.tool.controller.codegen.vo.ToolCodegenUpdateReqVO;
import cn.iocoder.yudao.adminserver.modules.tool.controller.codegen.vo.table.ToolCodegenTablePageReqVO;
import cn.iocoder.yudao.adminserver.modules.tool.controller.codegen.vo.table.ToolCodegenTableRespVO;
import cn.iocoder.yudao.adminserver.modules.tool.controller.codegen.vo.table.ToolSchemaTableRespVO;
import cn.iocoder.yudao.adminserver.modules.tool.convert.codegen.ToolCodegenConvert;
import cn.iocoder.yudao.adminserver.modules.tool.dal.dataobject.codegen.ToolCodegenColumnDO;
import cn.iocoder.yudao.adminserver.modules.tool.dal.dataobject.codegen.ToolCodegenTableDO;
import cn.iocoder.yudao.adminserver.modules.tool.dal.dataobject.codegen.ToolSchemaTableDO;
import cn.iocoder.yudao.adminserver.modules.tool.service.codegen.ToolCodegenService;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.servlet.ServletUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Api(tags = "???????????????")
@RestController
@RequestMapping("/tool/codegen")
@Validated
public class ToolCodegenController {

    @Resource
    private ToolCodegenService codegenService;

    @GetMapping("/db/table/list")
    @ApiOperation(value = "???????????????????????????????????????", notes = "???????????????????????? Codegen ??????")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "tableName", value = "?????????????????????", required = true, example = "yudao", dataTypeClass = String.class),
            @ApiImplicitParam(name = "tableComment", value = "?????????????????????", required = true, example = "??????", dataTypeClass = String.class)
    })
    @PreAuthorize("@ss.hasPermission('tool:codegen:query')")
    public CommonResult<List<ToolSchemaTableRespVO>> getSchemaTableList(
            @RequestParam(value = "tableName", required = false) String tableName,
            @RequestParam(value = "tableComment", required = false) String tableComment) {
        // ???????????????????????????????????????
        List<ToolSchemaTableDO> schemaTables = codegenService.getSchemaTableList(tableName, tableComment);
        // ????????? Codegen ?????????????????????
        Set<String> existsTables = CollectionUtils.convertSet(codegenService.getCodeGenTableList(), ToolCodegenTableDO::getTableName);
        schemaTables.removeIf(table -> existsTables.contains(table.getTableName()));
        return success(ToolCodegenConvert.INSTANCE.convertList04(schemaTables));
    }

    @GetMapping("/table/page")
    @ApiOperation("?????????????????????")
    @PreAuthorize("@ss.hasPermission('tool:codegen:query')")
    public CommonResult<PageResult<ToolCodegenTableRespVO>> getCodeGenTablePage(@Valid ToolCodegenTablePageReqVO pageReqVO) {
        PageResult<ToolCodegenTableDO> pageResult = codegenService.getCodegenTablePage(pageReqVO);
        return success(ToolCodegenConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/detail")
    @ApiOperation("???????????????????????????")
    @ApiImplicitParam(name = "tableId", value = "?????????", required = true, example = "1024", dataTypeClass = Long.class)
    @PreAuthorize("@ss.hasPermission('tool:codegen:query')")
    public CommonResult<ToolCodegenDetailRespVO> getCodegenDetail(@RequestParam("tableId") Long tableId) {
        ToolCodegenTableDO table = codegenService.getCodegenTablePage(tableId);
        List<ToolCodegenColumnDO> columns = codegenService.getCodegenColumnListByTableId(tableId);
        // ????????????
        return success(ToolCodegenConvert.INSTANCE.convert(table, columns));
    }

    @ApiOperation("????????????????????????????????????????????????????????????????????????")
    @ApiImplicitParam(name = "tableNames", value = "????????????", required = true, example = "sys_user", dataTypeClass = List.class)
    @PostMapping("/create-list-from-db")
    @PreAuthorize("@ss.hasPermission('tool:codegen:create')")
    public CommonResult<List<Long>> createCodegenListFromDB(@RequestParam("tableNames") List<String> tableNames) {
        return success(codegenService.createCodegenListFromDB(tableNames));
    }

    @ApiOperation("?????? SQL ?????????????????????????????????????????????????????????")
    @ApiImplicitParam(name = "sql", value = "SQL ????????????", required = true, example = "sql", dataTypeClass = String.class)
    @PostMapping("/create-list-from-sql")
    @PreAuthorize("@ss.hasPermission('tool:codegen:create')")
    public CommonResult<Long> createCodegenListFromSQL(@RequestParam("sql") String sql) {
        return success(codegenService.createCodegenListFromSQL(sql));
    }

    @ApiOperation("????????????????????????????????????")
    @PutMapping("/update")
    @PreAuthorize("@ss.hasPermission('tool:codegen:update')")
    public CommonResult<Boolean> updateCodegen(@Valid @RequestBody ToolCodegenUpdateReqVO updateReqVO) {
        codegenService.updateCodegen(updateReqVO);
        return success(true);
    }

    @ApiOperation("??????????????????????????????????????????????????????????????????")
    @PutMapping("/sync-from-db")
    @ApiImplicitParam(name = "tableId", value = "?????????", required = true, example = "1024", dataTypeClass = Long.class)
    @PreAuthorize("@ss.hasPermission('tool:codegen:update')")
    public CommonResult<Boolean> syncCodegenFromDB(@RequestParam("tableId") Long tableId) {
        codegenService.syncCodegenFromDB(tableId);
        return success(true);
    }

    @ApiOperation("?????? SQL ???????????????????????????????????????????????????")
    @PutMapping("/sync-from-sql")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "tableId", value = "?????????", required = true, example = "1024", dataTypeClass = Long.class),
            @ApiImplicitParam(name = "sql", value = "SQL ????????????", required = true, example = "sql", dataTypeClass = String.class)
    })
    @PreAuthorize("@ss.hasPermission('tool:codegen:update')")
    public CommonResult<Boolean> syncCodegenFromSQL(@RequestParam("tableId") Long tableId,
                                                    @RequestParam("sql") String sql) {
        codegenService.syncCodegenFromSQL(tableId, sql);
        return success(true);
    }

    @ApiOperation("????????????????????????????????????")
    @DeleteMapping("/delete")
    @ApiImplicitParam(name = "tableId", value = "?????????", required = true, example = "1024", dataTypeClass = Long.class)
    @PreAuthorize("@ss.hasPermission('tool:codegen:delete')")
    public CommonResult<Boolean> deleteCodegen(@RequestParam("tableId") Long tableId) {
        codegenService.deleteCodegen(tableId);
        return success(true);
    }

    @ApiOperation("??????????????????")
    @GetMapping("/preview")
    @ApiImplicitParam(name = "tableId", value = "?????????", required = true, example = "1024", dataTypeClass = Long.class)
    @PreAuthorize("@ss.hasPermission('tool:codegen:preview')")
    public CommonResult<List<ToolCodegenPreviewRespVO>> previewCodegen(@RequestParam("tableId") Long tableId) {
        Map<String, String> codes = codegenService.generationCodes(tableId);
        return success(ToolCodegenConvert.INSTANCE.convert(codes));
    }

    @ApiOperation("??????????????????")
    @GetMapping("/download")
    @ApiImplicitParam(name = "tableId", value = "?????????", required = true, example = "1024", dataTypeClass = Long.class)
    @PreAuthorize("@ss.hasPermission('tool:codegen:download')")
    public void downloadCodegen(@RequestParam("tableId") Long tableId,
                                HttpServletResponse response) throws IOException {
        // ????????????
        Map<String, String> codes = codegenService.generationCodes(tableId);
        // ?????? zip ???
        String[] paths = codes.keySet().toArray(new String[0]);
        ByteArrayInputStream[] ins = codes.values().stream().map(IoUtil::toUtf8Stream).toArray(ByteArrayInputStream[]::new);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ZipUtil.zip(outputStream, paths, ins);
        // ??????
        ServletUtils.writeAttachment(response, "codegen.zip", outputStream.toByteArray());
    }

}
