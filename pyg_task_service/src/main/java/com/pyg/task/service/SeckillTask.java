package com.pyg.task.service;

import com.pyg.mapper.TbSeckillGoodsMapper;
import com.pyg.pojo.TbSeckillGoods;
import com.pyg.pojo.TbSeckillGoodsExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * @author huyy
 * @Title: SeckillTask
 * @ProjectName pyg_parent
 * @Description: 定时任务
 * @date 2018/12/179:01
 */

@Component
public class SeckillTask {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private TbSeckillGoodsMapper seckillGoodsMapper;

    /**
     * 刷新秒杀商品
     */
    @Scheduled(cron = "0 * * * * ?")
    public void refreshSeckillGoods(){

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            //每分钟执行一次，将数据库中新增的秒杀商品数据放入缓存
            System.out.println("执行了调度任务："+sdf.format(new Date()));

            //1. 获取缓存中的秒杀商品id列表，排除这些id，不查询
            Set seckillIds = redisTemplate.boundHashOps("seckillGodds").keys();

            //2. 组装查询条件： 排除缓存中有的id,状态审核，库存大于0，正在秒杀中

            TbSeckillGoodsExample example = new TbSeckillGoodsExample();
            TbSeckillGoodsExample.Criteria criteria = example.createCriteria();
            criteria.andStatusEqualTo("1");
            criteria.andStockCountGreaterThan(0);
            criteria.andStartTimeLessThan(new Date());
            criteria.andEndTimeGreaterThan(new Date());

            //排除缓存中已经有的商品
            if (seckillIds!=null&&seckillIds.size()>0){
                criteria.andIdNotIn(new ArrayList<>(seckillIds));
            }

            List<TbSeckillGoods> seckillGoodsList = seckillGoodsMapper.selectByExample(example);

            //3. 放入redis缓存
            //装入缓存
            for (TbSeckillGoods seckillGoods : seckillGoodsList) {
                redisTemplate.boundHashOps("seckillGoods").put(seckillGoods.getId(),seckillGoods);
            }
            System.out.println("已将"+seckillGoodsList.size()+"条商品放入缓存中");
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 移除秒杀商品
     */
    @Scheduled(cron = "* 5 * * * ?")
    public void removeSeckillGoods(){
        //结束时间小于等于当前时间，就移除
        System.out.println("移除秒杀商品正在执行");
        //扫描缓存中秒杀商品列表，发现过期的移除
        List<TbSeckillGoods> seckillGoods = redisTemplate.boundHashOps("seckillGoods").values();

        for (TbSeckillGoods seckillGood : seckillGoods) {
            //查询商品的结束时间与当前时间对比，如果小于就删除并同步到数据库
            if (seckillGood.getEndTime().getTime()<new Date().getTime()){
                seckillGoodsMapper.updateByPrimaryKey(seckillGood);
                redisTemplate.boundHashOps("seckillGoods").delete(seckillGood);
                System.out.println("过期商品已删除");
            }
        }
        System.out.println("移除秒杀任务结束");
    }

}
