package com.iteaj.iboot.module.iot.collect.store;

import com.iteaj.iboot.module.iot.collect.CollectOption;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class StoreActionFactory implements InitializingBean {

    @Autowired(required = false)
    private List<StoreAction> actions;

    protected StoreActionFactory() { }

    private Map<String, StoreAction> actionMap = new ConcurrentHashMap<>(8);
    private static StoreActionFactory actionFactory = new StoreActionFactory();

    public static StoreActionFactory getInstance() {
        return actionFactory;
    }

    /**
     * 获取采集动作
     * @param actionName
     * @return
     */
    public StoreAction get(String actionName) {
        if(!StringUtils.hasText(actionName)) {
            return null;
        }

        return actionMap.get(actionName);
    }

    /**
     * 返回所有动作名称
     * @return
     */
    public Set<String> getNames() {
        return this.actionMap.keySet();
    }

    /**
     * 返回所有的采集动作
     * @return
     */
    public Collection<StoreAction> getActions() {
        return this.actionMap.values();
    }

    /**
     * 是否有采集动作
     * @param actionName
     * @return
     */
    public boolean isExists(String actionName) {
        return actionMap.containsKey(actionName);
    }

    /**
     * 移除指定动作
     * @param actionName
     * @return
     */
    public StoreAction remove(String actionName) {
        return actionMap.remove(actionName);
    }

    /**
     * 注册采集动作
     * @param actionName
     * @param storeAction
     * @return
     */
    public StoreAction register(String actionName, StoreAction storeAction) {
        return actionMap.putIfAbsent(actionName, storeAction);
    }

    /**
     * 注册采集动作
     * @param storeAction
     * @return
     */
    public StoreAction register(StoreAction storeAction) {
        return actionMap.putIfAbsent(storeAction.getName(), storeAction);
    }

    public List<CollectOption> options() {
        return this.actionMap.values().stream()
                .map(item -> new CollectOption(item.getDesc(), item.getName()))
                .collect(Collectors.toList());
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if(!CollectionUtils.isEmpty(actions)) {
            this.actions.forEach(item -> {
                if(!this.isExists(item.getName())) {
                    this.register(item);
                } else {
                    throw new BeanInitializationException("存在相同的采集动作["+item.getName()+"]");
                }
            });
        }
    }
}
