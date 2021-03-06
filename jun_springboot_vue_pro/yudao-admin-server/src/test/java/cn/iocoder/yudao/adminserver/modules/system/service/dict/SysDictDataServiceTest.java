package cn.iocoder.yudao.adminserver.modules.system.service.dict;

import cn.iocoder.yudao.adminserver.BaseDbUnitTest;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.adminserver.modules.system.controller.dict.vo.data.SysDictDataCreateReqVO;
import cn.iocoder.yudao.adminserver.modules.system.controller.dict.vo.data.SysDictDataExportReqVO;
import cn.iocoder.yudao.adminserver.modules.system.controller.dict.vo.data.SysDictDataPageReqVO;
import cn.iocoder.yudao.adminserver.modules.system.controller.dict.vo.data.SysDictDataUpdateReqVO;
import cn.iocoder.yudao.adminserver.modules.system.dal.dataobject.dict.SysDictDataDO;
import cn.iocoder.yudao.adminserver.modules.system.dal.dataobject.dict.SysDictTypeDO;
import cn.iocoder.yudao.adminserver.modules.system.dal.mysql.dict.SysDictDataMapper;
import cn.iocoder.yudao.adminserver.modules.system.mq.producer.dict.SysDictDataProducer;
import cn.iocoder.yudao.adminserver.modules.system.service.dict.impl.SysDictDataServiceImpl;
import cn.iocoder.yudao.framework.common.util.collection.ArrayUtils;
import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import com.google.common.collect.ImmutableTable;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

import static cn.hutool.core.bean.BeanUtil.getFieldValue;
import static cn.iocoder.yudao.adminserver.modules.system.enums.SysErrorCodeConstants.*;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
* {@link SysDictDataServiceImpl} ??????????????????
*
* @author ????????????
*/
@Import(SysDictDataServiceImpl.class)
public class SysDictDataServiceTest extends BaseDbUnitTest {

    @Resource
    private SysDictDataServiceImpl dictDataService;

    @Resource
    private SysDictDataMapper dictDataMapper;
    @MockBean
    private SysDictTypeService dictTypeService;
    @MockBean
    private SysDictDataProducer dictDataProducer;

    /**
     * ??????????????????????????????????????????
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testInitLocalCache() {
        // mock ??????
        SysDictDataDO dictData01 = randomDictDataDO();
        dictDataMapper.insert(dictData01);
        SysDictDataDO dictData02 = randomDictDataDO();
        dictDataMapper.insert(dictData02);

        // ??????
        dictDataService.initLocalCache();
        // ?????? labelDictDataCache ??????
        ImmutableTable<String, String, SysDictDataDO> labelDictDataCache =
                (ImmutableTable<String, String, SysDictDataDO>) getFieldValue(dictDataService, "labelDictDataCache");
        assertEquals(2, labelDictDataCache.size());
        assertPojoEquals(dictData01, labelDictDataCache.get(dictData01.getDictType(), dictData01.getLabel()));
        assertPojoEquals(dictData02, labelDictDataCache.get(dictData02.getDictType(), dictData02.getLabel()));
        // ?????? valueDictDataCache ??????
        ImmutableTable<String, String, SysDictDataDO> valueDictDataCache =
                (ImmutableTable<String, String, SysDictDataDO>) getFieldValue(dictDataService, "valueDictDataCache");
        assertEquals(2, valueDictDataCache.size());
        assertPojoEquals(dictData01, valueDictDataCache.get(dictData01.getDictType(), dictData01.getValue()));
        assertPojoEquals(dictData02, valueDictDataCache.get(dictData02.getDictType(), dictData02.getValue()));
        // ?????? maxUpdateTime ??????
        Date maxUpdateTime = (Date) getFieldValue(dictDataService, "maxUpdateTime");
        assertEquals(ObjectUtils.max(dictData01.getUpdateTime(), dictData02.getUpdateTime()), maxUpdateTime);
    }

    @Test
    public void testGetDictDataPage() {
        // mock ??????
        SysDictDataDO dbDictData = randomPojo(SysDictDataDO.class, o -> { // ???????????????
            o.setLabel("??????");
            o.setDictType("yunai");
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
        });
        dictDataMapper.insert(dbDictData);
        // ?????? label ?????????
        dictDataMapper.insert(ObjectUtils.clone(dbDictData, o -> o.setLabel("???")));
        // ?????? dictType ?????????
        dictDataMapper.insert(ObjectUtils.clone(dbDictData, o -> o.setDictType("nai")));
        // ?????? status ?????????
        dictDataMapper.insert(ObjectUtils.clone(dbDictData, o -> o.setStatus(CommonStatusEnum.DISABLE.getStatus())));
        // ????????????
        SysDictDataPageReqVO reqVO = new SysDictDataPageReqVO();
        reqVO.setLabel("???");
        reqVO.setDictType("yu");
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());

        // ??????
        PageResult<SysDictDataDO> pageResult = dictDataService.getDictDataPage(reqVO);
        // ??????
        assertEquals(1, pageResult.getTotal());
        assertEquals(1, pageResult.getList().size());
        assertPojoEquals(dbDictData, pageResult.getList().get(0));
    }

    @Test
    public void testGetDictDataList() {
        // mock ??????
        SysDictDataDO dbDictData = randomPojo(SysDictDataDO.class, o -> { // ???????????????
            o.setLabel("??????");
            o.setDictType("yunai");
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
        });
        dictDataMapper.insert(dbDictData);
        // ?????? label ?????????
        dictDataMapper.insert(ObjectUtils.clone(dbDictData, o -> o.setLabel("???")));
        // ?????? dictType ?????????
        dictDataMapper.insert(ObjectUtils.clone(dbDictData, o -> o.setDictType("nai")));
        // ?????? status ?????????
        dictDataMapper.insert(ObjectUtils.clone(dbDictData, o -> o.setStatus(CommonStatusEnum.DISABLE.getStatus())));
        // ????????????
        SysDictDataExportReqVO reqVO = new SysDictDataExportReqVO();
        reqVO.setLabel("???");
        reqVO.setDictType("yu");
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());

        // ??????
        List<SysDictDataDO> list = dictDataService.getDictDatas(reqVO);
        // ??????
        assertEquals(1, list.size());
        assertPojoEquals(dbDictData, list.get(0));
    }

    @Test
    public void testCreateDictData_success() {
        // ????????????
        SysDictDataCreateReqVO reqVO = randomPojo(SysDictDataCreateReqVO.class,
                o -> o.setStatus(randomCommonStatus()));
        // mock ??????
        when(dictTypeService.getDictType(eq(reqVO.getDictType()))).thenReturn(randomDictTypeDO(reqVO.getDictType()));

        // ??????
        Long dictDataId = dictDataService.createDictData(reqVO);
        // ??????
        assertNotNull(dictDataId);
        // ?????????????????????????????????
        SysDictDataDO dictData = dictDataMapper.selectById(dictDataId);
        assertPojoEquals(reqVO, dictData);
        // ????????????
        verify(dictDataProducer, times(1)).sendDictDataRefreshMessage();
    }

    @Test
    public void testUpdateDictData_success() {
        // mock ??????
        SysDictDataDO dbDictData = randomDictDataDO();
        dictDataMapper.insert(dbDictData);// @Sql: ?????????????????????????????????
        // ????????????
        SysDictDataUpdateReqVO reqVO = randomPojo(SysDictDataUpdateReqVO.class, o -> {
            o.setId(dbDictData.getId()); // ??????????????? ID
            o.setStatus(randomCommonStatus());
        });
        // mock ?????????????????????
        when(dictTypeService.getDictType(eq(reqVO.getDictType()))).thenReturn(randomDictTypeDO(reqVO.getDictType()));

        // ??????
        dictDataService.updateDictData(reqVO);
        // ????????????????????????
        SysDictDataDO dictData = dictDataMapper.selectById(reqVO.getId()); // ???????????????
        assertPojoEquals(reqVO, dictData);
        // ????????????
        verify(dictDataProducer, times(1)).sendDictDataRefreshMessage();
    }

    @Test
    public void testDeleteDictData_success() {
        // mock ??????
        SysDictDataDO dbDictData = randomDictDataDO();
        dictDataMapper.insert(dbDictData);// @Sql: ?????????????????????????????????
        // ????????????
        Long id = dbDictData.getId();

        // ??????
        dictDataService.deleteDictData(id);
        // ????????????????????????
        assertNull(dictDataMapper.selectById(id));
        // ????????????
        verify(dictDataProducer, times(1)).sendDictDataRefreshMessage();
    }

    @Test
    public void testCheckDictDataExists_success() {
        // mock ??????
        SysDictDataDO dbDictData = randomDictDataDO();
        dictDataMapper.insert(dbDictData);// @Sql: ?????????????????????????????????

        // ????????????
        dictDataService.checkDictDataExists(dbDictData.getId());
    }

    @Test
    public void testCheckDictDataExists_notExists() {
        assertServiceException(() -> dictDataService.checkDictDataExists(randomLongId()), DICT_DATA_NOT_EXISTS);
    }

    @Test
    public void testCheckDictTypeValid_success() {
        // mock ??????????????????????????????
        String type = randomString();
        when(dictTypeService.getDictType(eq(type))).thenReturn(randomDictTypeDO(type));

        // ??????, ??????
        dictDataService.checkDictTypeValid(type);
    }

    @Test
    public void testCheckDictTypeValid_notExists() {
        assertServiceException(() -> dictDataService.checkDictTypeValid(randomString()), DICT_TYPE_NOT_EXISTS);
    }

    @Test
    public void testCheckDictTypeValid_notEnable() {
        // mock ??????????????????????????????
        String dictType = randomString();
        when(dictTypeService.getDictType(eq(dictType))).thenReturn(
                randomPojo(SysDictTypeDO.class, o -> o.setStatus(CommonStatusEnum.DISABLE.getStatus())));

        // ??????, ???????????????
        assertServiceException(() -> dictDataService.checkDictTypeValid(dictType), DICT_TYPE_NOT_ENABLE);
    }

    @Test
    public void testCheckDictDataValueUnique_success() {
        // ???????????????
        dictDataService.checkDictDataValueUnique(randomLongId(), randomString(), randomString());
    }

    @Test
    public void testCheckDictDataValueUnique_valueDuplicateForCreate() {
        // ????????????
        String dictType = randomString();
        String value = randomString();
        // mock ??????
        dictDataMapper.insert(randomDictDataDO(o -> {
            o.setDictType(dictType);
            o.setValue(value);
        }));

        // ?????????????????????
        assertServiceException(() -> dictDataService.checkDictDataValueUnique(null, dictType, value),
                DICT_DATA_VALUE_DUPLICATE);
    }

    @Test
    public void testCheckDictDataValueUnique_valueDuplicateForUpdate() {
        // ????????????
        Long id = randomLongId();
        String dictType = randomString();
        String value = randomString();
        // mock ??????
        dictDataMapper.insert(randomDictDataDO(o -> {
            o.setDictType(dictType);
            o.setValue(value);
        }));

        // ?????????????????????
        assertServiceException(() -> dictDataService.checkDictDataValueUnique(id, dictType, value),
                DICT_DATA_VALUE_DUPLICATE);
    }

    // ========== ???????????? ==========

    @SafeVarargs
    private static SysDictDataDO randomDictDataDO(Consumer<SysDictDataDO>... consumers) {
        Consumer<SysDictDataDO> consumer = (o) -> {
            o.setStatus(randomCommonStatus()); // ?????? status ?????????
        };
        return randomPojo(SysDictDataDO.class, ArrayUtils.append(consumer, consumers));
    }

    /**
     * ?????????????????????????????????
     *
     * @param type ????????????
     * @return SysDictTypeDO ??????
     */
    private static SysDictTypeDO randomDictTypeDO(String type) {
        return randomPojo(SysDictTypeDO.class, o -> {
            o.setType(type);
            o.setStatus(CommonStatusEnum.ENABLE.getStatus()); // ?????? status ?????????
        });
    }

}
